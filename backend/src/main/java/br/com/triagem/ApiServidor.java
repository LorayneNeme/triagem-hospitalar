package br.com.triagem;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API HTTP do TriagemService, para o site chamar com fetch().
 * Usa o servidor que já vem no JDK (com.sun.net.httpserver): nenhuma dependência.
 *
 * Executar: java -cp target/classes br.com.triagem.ApiServidor [porta]   (padrão 8080)
 * Rotas e exemplos: README do backend, seção "API HTTP".
 */
public final class ApiServidor {

    private static final DateTimeFormatter HORARIO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final TriagemService triagem;

    private ApiServidor(TriagemService triagem) {
        this.triagem = triagem;
    }

    /** Sobe o servidor e devolve a instância (para parar: stop(0)). Porta 0 = qualquer porta livre. */
    public static HttpServer iniciar(TriagemService triagem, int porta) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        ApiServidor api = new ApiServidor(triagem);
        servidor.createContext("/api", api::atender);
        servidor.start(); // executor padrão: uma thread atende um pedido por vez
        return servidor;
    }

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        iniciar(new TriagemService(), porta);
        System.out.println("API da triagem em http://localhost:" + porta + "/api (Ctrl+C para parar)");
    }

    // ---------- Resposta e erros ----------

    private record Resposta(int status, Object corpo, String location) {
        static Resposta ok(Object corpo) {
            return new Resposta(200, corpo, null);
        }

        /** 204 quando a fila está vazia. */
        static Resposta okOuVazio(Object corpo) {
            return corpo == null ? new Resposta(204, null, null) : ok(corpo);
        }
    }

    /** Erro que já sabe o seu código HTTP (404, 405...). */
    private static final class ErroHttp extends RuntimeException {
        private final int status;

        ErroHttp(int status, String mensagem) {
            super(mensagem);
            this.status = status;
        }
    }

    private void atender(HttpExchange troca) throws IOException {
        try {
            Resposta resposta;

            try {
                if ("OPTIONS".equals(troca.getRequestMethod())) {
                    resposta = new Resposta(204, null, null); // preflight do CORS
                } else {
                    synchronized (triagem) {
                        resposta = rotear(troca.getRequestMethod(), troca.getRequestURI().getPath(), troca);
                    }
                }
            } catch (ErroHttp e) {
                resposta = erro(e.status, e.getMessage());
            } catch (IllegalArgumentException e) {
                resposta = erro(400, e.getMessage());
            } catch (IllegalStateException e) {
                resposta = erro(409, e.getMessage());
            } catch (RuntimeException e) {
                resposta = erro(500, "Erro interno: " + e);
            }

            enviar(troca, resposta);
        } finally {
            troca.close();
        }
    }

    private static Resposta erro(int status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", mensagem);
        return new Resposta(status, corpo, null);
    }

    private static void enviar(HttpExchange troca, Resposta resposta) throws IOException {
        var cabecalhos = troca.getResponseHeaders();
        cabecalhos.set("Access-Control-Allow-Origin", "*");
        cabecalhos.set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        cabecalhos.set("Access-Control-Allow-Headers", "Content-Type");
        if (resposta.location() != null) cabecalhos.set("Location", resposta.location());

        if (resposta.status() == 204) {
            troca.sendResponseHeaders(204, -1);
            return;
        }

        byte[] corpo = Json.escrever(resposta.corpo()).getBytes(StandardCharsets.UTF_8);
        cabecalhos.set("Content-Type", "application/json; charset=utf-8");
        troca.sendResponseHeaders(resposta.status(), corpo.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(corpo);
        }
    }

    // ---------- Rotas ----------

    private Resposta rotear(String metodo, String caminho, HttpExchange troca) throws IOException {
        // "/api/pacientes/P001/classificacao" → ["pacientes", "P001", "classificacao"]
        String[] partes = caminho.replaceAll("/+$", "").split("/");
        if (partes.length < 3 || !partes[0].isEmpty() || !partes[1].equals("api")) {
            throw naoEncontrada();
        }
        List<String> rota = List.of(partes).subList(2, partes.length);

        switch (rota.get(0)) {
            case "pacientes" -> {
                if (rota.size() == 1) {
                    if (metodo.equals("GET")) return Resposta.ok(listar(triagem.listarTodos(), false));
                    if (metodo.equals("POST")) return cadastrar(lerCorpo(troca));
                    throw metodoNaoPermitido();
                }

                Paciente paciente = encontrar(rota.get(1));

                if (rota.size() == 2) {
                    exigir(metodo, "GET");
                    return Resposta.ok(paraJson(paciente, posicao(paciente)));
                }

                if (rota.size() == 3 && rota.get(2).equals("classificacao")) {
                    exigir(metodo, "POST");
                    return classificar(paciente, lerCorpo(troca));
                }

                if (rota.size() == 3 && rota.get(2).equals("prioridade")) {
                    exigir(metodo, "PUT");
                    return alterarPrioridade(paciente, lerCorpo(troca));
                }
            }
            case "fila-triagem" -> {
                if (rota.size() == 1) {
                    exigir(metodo, "GET");
                    return Resposta.ok(listar(triagem.listarFilaTriagem(), true));
                }

                if (rota.size() == 2 && rota.get(1).equals("chamar")) {
                    exigir(metodo, "POST");
                    Paciente chamado = triagem.chamarParaTriagem();
                    return Resposta.okOuVazio(chamado == null ? null : paraJson(chamado, null));
                }
            }
            case "fila-atendimento" -> {
                if (rota.size() == 1) {
                    exigir(metodo, "GET");
                    return Resposta.ok(listar(triagem.listarFilaOrdenada(), true));
                }

                if (rota.size() == 2 && rota.get(1).equals("proximo")) {
                    exigir(metodo, "GET");
                    Paciente proximo = triagem.consultarProximo();
                    return Resposta.okOuVazio(proximo == null ? null : paraJson(proximo, 1));
                }

                if (rota.size() == 2 && rota.get(1).equals("chamar")) {
                    exigir(metodo, "POST");
                    Paciente chamado = triagem.chamarProximo();
                    return Resposta.okOuVazio(chamado == null ? null : paraJson(chamado, null));
                }
            }
            case "estatisticas" -> {
                if (rota.size() == 1) {
                    exigir(metodo, "GET");
                    return Resposta.ok(estatisticas());
                }
            }
            default -> {
            }
        }

        throw naoEncontrada();
    }

    private static ErroHttp naoEncontrada() {
        return new ErroHttp(404, "Rota não encontrada.");
    }

    private static ErroHttp metodoNaoPermitido() {
        return new ErroHttp(405, "Método não permitido nesta rota.");
    }

    private static void exigir(String metodo, String esperado) {
        if (!metodo.equals(esperado)) throw metodoNaoPermitido();
    }

    private Paciente encontrar(String codigoTexto) {
        int codigo = Paciente.lerCodigo(codigoTexto); // "A123" → 400
        Paciente paciente = triagem.buscarPaciente(codigo);

        if (paciente == null) {
            throw new ErroHttp(404, "Paciente " + Paciente.formatarCodigo(codigo) + " não encontrado.");
        }

        return paciente;
    }

    private Resposta cadastrar(Map<String, Object> corpo) {
        Paciente paciente = triagem.cadastrarPaciente(
                texto(corpo, "nome", null),
                lerIdade(corpo.get("idade")),
                texto(corpo, "telefone", ""),
                texto(corpo, "endereco", ""),
                texto(corpo, "queixa", ""));

        return new Resposta(201, paraJson(paciente, posicao(paciente)),
                "/api/pacientes/" + paciente.getCodigoFormatado());
    }

    private Resposta classificar(Paciente paciente, Map<String, Object> corpo) {
        Prioridade prioridade = lerPrioridade(corpo.get("prioridade"));
        triagem.classificar(paciente.getCodigo(), prioridade.getNivel(),
                texto(corpo, "especialidade", ""),
                texto(corpo, "sala", ""),
                texto(corpo, "observacao", ""));

        return Resposta.ok(paraJson(paciente, posicao(paciente)));
    }

    private Resposta alterarPrioridade(Paciente paciente, Map<String, Object> corpo) {
        Prioridade prioridade = lerPrioridade(corpo.get("prioridade"));

        if (!triagem.alterarPrioridade(paciente.getCodigo(), prioridade.getNivel())) {
            throw new IllegalStateException("Só dá para alterar a prioridade de quem está na fila de atendimento ("
                    + paciente.getCodigoFormatado() + " está \"" + paciente.getStatus().getDescricao() + "\").");
        }

        return Resposta.ok(paraJson(paciente, posicao(paciente)));
    }

    private Map<String, Object> estatisticas() {
        EstatisticasFila e = triagem.gerarEstatisticas();

        Map<String, Object> porCor = new LinkedHashMap<>();
        e.porCor().forEach((cor, quantidade) -> porCor.put(cor.getDescricao(), quantidade));

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("total", e.total());
        json.put("aguardandoTriagem", e.aguardandoTriagem());
        json.put("aguardandoAtendimento", e.aguardandoAtendimento());
        json.put("atendidos", e.atendidos());
        json.put("porCor", porCor);
        json.put("tempoMedioEsperaSegundos", e.tempoMedioEspera().getSeconds());
        json.put("maiorTempoEsperaSegundos", e.maiorTempoEspera().getSeconds());
        return json;
    }

    // ---------- Leitura do corpo ----------

    private static Map<String, Object> lerCorpo(HttpExchange troca) throws IOException {
        String texto;
        try (InputStream entrada = troca.getRequestBody()) {
            texto = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }

        if (!(Json.ler(texto) instanceof Map<?, ?> mapa)) {
            throw new IllegalArgumentException("O corpo deve ser um objeto JSON, por exemplo {\"nome\": \"Ana\"}.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> obj = (Map<String, Object>) mapa;
        return obj;
    }

    /** Campo de texto opcional; números e booleanos viram texto. */
    private static String texto(Map<String, Object> corpo, String campo, String padrao) {
        Object valor = corpo.get(campo);
        return valor == null ? padrao : String.valueOf(valor);
    }

    /** Aceita 30 ou "30" (o value de um input do site é sempre texto). */
    private static int lerIdade(Object valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Informe a idade.");
        }

        try {
            long idade = valor instanceof Long numero ? numero : Long.parseLong(String.valueOf(valor).trim());
            if (idade < Integer.MIN_VALUE || idade > Integer.MAX_VALUE) throw new NumberFormatException();
            return (int) idade;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("A idade deve ser um número inteiro: " + valor + ".");
        }
    }

    /** Aceita o nome da cor ("Laranja") ou o nível (2 ou "2"). */
    private static Prioridade lerPrioridade(Object valor) {
        return Prioridade.ler(valor == null ? null : String.valueOf(valor));
    }

    // ---------- Paciente → JSON ----------

    /** Posição atual na fila em que o paciente está; null se não está em nenhuma. */
    private Integer posicao(Paciente paciente) {
        int posicao = switch (paciente.getStatus()) {
            case AGUARDANDO_TRIAGEM -> triagem.posicaoNaFilaTriagem(paciente.getCodigo());
            case AGUARDANDO_ATENDIMENTO -> triagem.posicaoNaFila(paciente.getCodigo());
            case EM_ATENDIMENTO -> -1;
        };

        return posicao > 0 ? posicao : null;
    }

    /**
     * Lista de pacientes. Numa fila, a posição é o índice + 1.
     * Na lista geral, as posições são calculadas uma vez para todos (evita O(n²)).
     */
    private List<Map<String, Object>> listar(List<Paciente> pacientes, boolean ehFila) {
        Map<Paciente, Integer> posicoes = new IdentityHashMap<>();

        if (ehFila) {
            for (int i = 0; i < pacientes.size(); i++) posicoes.put(pacientes.get(i), i + 1);
        } else {
            List<Paciente> filaTriagem = triagem.listarFilaTriagem();
            List<Paciente> filaAtendimento = triagem.listarFilaOrdenada();
            for (int i = 0; i < filaTriagem.size(); i++) posicoes.put(filaTriagem.get(i), i + 1);
            for (int i = 0; i < filaAtendimento.size(); i++) posicoes.put(filaAtendimento.get(i), i + 1);
        }

        List<Map<String, Object>> json = new ArrayList<>();
        for (Paciente paciente : pacientes) json.add(paraJson(paciente, posicoes.get(paciente)));
        return json;
    }

    private static Map<String, Object> paraJson(Paciente p, Integer posicao) {
        Prioridade prioridade = p.getPrioridade();

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("codigo", p.getCodigoFormatado());
        json.put("nome", p.getNome());
        json.put("idade", p.getIdade());
        json.put("telefone", p.getTelefone());
        json.put("endereco", p.getEndereco());
        json.put("queixa", p.getQueixa());
        json.put("prioridade", prioridade == null ? null : prioridade.getDescricao());
        json.put("nivelPrioridade", prioridade == null ? null : prioridade.getNivel());
        json.put("especialidade", p.getEspecialidade());
        json.put("sala", p.getSala());
        json.put("observacao", p.getObservacao());
        json.put("status", p.getStatus().getDescricao());
        json.put("horarioChegada", horario(p.getHorarioChegada()));
        json.put("horarioTriagem", horario(p.getHorarioTriagem()));
        json.put("horarioAtendimento", horario(p.getHorarioAtendimento()));
        json.put("tempoEsperaMinutos",
                p.getStatus() == StatusPaciente.EM_ATENDIMENTO ? p.getTempoEspera().toMinutes() : null);
        json.put("posicao", posicao);
        return json;
    }

    private static String horario(LocalDateTime horario) {
        return horario == null ? null : horario.format(HORARIO);
    }
}
