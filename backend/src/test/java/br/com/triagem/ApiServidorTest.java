package br.com.triagem;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiServidorTest {

    private final RelogioFalso relogio = new RelogioFalso(LocalDateTime.of(2026, 11, 9, 13, 20));
    private final HttpClient cliente = HttpClient.newHttpClient();
    private HttpServer servidor;
    private String base;

    @BeforeEach
    void iniciar() throws Exception {
        servidor = ApiServidor.iniciar(new TriagemService(relogio), 0); // porta 0 = qualquer porta livre
        base = "http://localhost:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void parar() {
        servidor.stop(0);
    }

    private record Resposta(int status, Object corpo, HttpResponse<String> bruta) {
        @SuppressWarnings("unchecked")
        Map<String, Object> obj() {
            return (Map<String, Object>) corpo;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista() {
            return (List<Map<String, Object>>) corpo;
        }
    }

    private Resposta enviar(String metodo, String caminho, String json) throws Exception {
        HttpRequest.Builder pedido = HttpRequest.newBuilder(URI.create(base + caminho));
        pedido.method(metodo, json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json));
        if (json != null) pedido.header("Content-Type", "application/json");

        HttpResponse<String> r = cliente.send(pedido.build(), HttpResponse.BodyHandlers.ofString());
        return new Resposta(r.statusCode(), r.body().isEmpty() ? null : Json.ler(r.body()), r);
    }

    private Resposta get(String caminho) throws Exception {
        return enviar("GET", caminho, null);
    }

    @Test
    void caminhoCompletoDoPaciente() throws Exception {
        // Recepção: idade chega como texto, igual ao value de um <input> do site.
        Resposta cadastro = enviar("POST", "/api/pacientes",
                "{\"nome\":\"Ana\",\"idade\":\"30\",\"telefone\":\"11 9999\",\"endereco\":\"Rua A\",\"queixa\":\"Febre\"}");
        assertEquals(201, cadastro.status());
        assertEquals("P001", cadastro.obj().get("codigo"));
        assertEquals("Aguardando triagem", cadastro.obj().get("status"));
        assertNull(cadastro.obj().get("prioridade"));
        assertEquals("2026-11-09T13:20:00", cadastro.obj().get("horarioChegada"));
        assertEquals("/api/pacientes/P001", cadastro.bruta().headers().firstValue("Location").orElse(""));

        enviar("POST", "/api/pacientes", "{\"nome\":\"Bia\",\"idade\":40}");

        Resposta triagem = get("/api/fila-triagem");
        assertEquals(List.of("P001", "P002"), triagem.lista().stream().map(p -> p.get("codigo")).toList());
        assertEquals(1L, triagem.lista().get(0).get("posicao"));

        // Triagem: chama o primeiro e classifica com o nome da cor, como o site guarda.
        Resposta chamado = enviar("POST", "/api/fila-triagem/chamar", null);
        assertEquals("P001", chamado.obj().get("codigo"));
        relogio.avancar(Duration.ofMinutes(15));
        Resposta classificado = enviar("POST", "/api/pacientes/P001/classificacao",
                "{\"prioridade\":\"Laranja\",\"especialidade\":\"Clínica geral\",\"sala\":\"Sala 2\",\"observacao\":\"\"}");
        assertEquals(200, classificado.status());
        assertEquals("Laranja", classificado.obj().get("prioridade"));
        assertEquals(2L, classificado.obj().get("nivelPrioridade"));
        assertEquals("Aguardando atendimento", classificado.obj().get("status"));
        assertEquals("2026-11-09T13:35:00", classificado.obj().get("horarioTriagem"));
        assertEquals(1L, classificado.obj().get("posicao"));

        enviar("POST", "/api/pacientes/p2/classificacao", "{\"prioridade\":1}");
        assertEquals(List.of("P002", "P001"),
                get("/api/fila-atendimento").lista().stream().map(p -> p.get("codigo")).toList());

        // Alterar prioridade.
        Resposta alterado = enviar("PUT", "/api/pacientes/P001/prioridade", "{\"prioridade\":\"Vermelho\"}");
        assertEquals(200, alterado.status());
        assertEquals(1L, alterado.obj().get("posicao")); // empate na cor: chegou antes
        assertEquals("P001", get("/api/fila-atendimento/proximo").obj().get("codigo"));

        // Atendimento: 13:20 → 14:05 = 45 min.
        relogio.avancar(Duration.ofMinutes(30));
        Resposta atendido = enviar("POST", "/api/fila-atendimento/chamar", null);
        assertEquals("P001", atendido.obj().get("codigo"));
        assertEquals("Em atendimento", atendido.obj().get("status"));
        assertEquals("2026-11-09T14:05:00", atendido.obj().get("horarioAtendimento"));
        assertEquals(45L, atendido.obj().get("tempoEsperaMinutos"));
        assertNull(atendido.obj().get("posicao"));

        Resposta busca = get("/api/pacientes/P001");
        assertEquals("Febre", busca.obj().get("queixa"));
        assertEquals("Clínica geral", busca.obj().get("especialidade"));

        Map<String, Object> estat = get("/api/estatisticas").obj();
        assertEquals(2L, estat.get("total"));
        assertEquals(0L, estat.get("aguardandoTriagem"));
        assertEquals(1L, estat.get("aguardandoAtendimento"));
        assertEquals(1L, estat.get("atendidos"));
        assertEquals(Map.of("Vermelho", 2L, "Laranja", 0L, "Amarelo", 0L, "Verde", 0L, "Azul", 0L), estat.get("porCor"));
        assertEquals(2700L, estat.get("tempoMedioEsperaSegundos"));
        assertEquals(2700L, estat.get("maiorTempoEsperaSegundos"));

        assertEquals(2, get("/api/pacientes").lista().size());
    }

    @Test
    void filasVaziasRespondem204() throws Exception {
        assertEquals(204, enviar("POST", "/api/fila-triagem/chamar", null).status());
        assertEquals(204, enviar("POST", "/api/fila-atendimento/chamar", null).status());
        assertEquals(204, get("/api/fila-atendimento/proximo").status());
        assertEquals(List.of(), get("/api/fila-atendimento").corpo());
    }

    @Test
    void errosRespondemComCodigoEMensagem() throws Exception {
        Resposta semNome = enviar("POST", "/api/pacientes", "{\"idade\":30}");
        assertEquals(400, semNome.status());
        assertTrue(((String) semNome.obj().get("erro")).contains("nome"));

        assertEquals(400, enviar("POST", "/api/pacientes", "{\"nome\":\"Ana\",\"idade\":\"trinta\"}").status());
        assertEquals(400, enviar("POST", "/api/pacientes", "{\"nome\":\"Ana\",\"idade\":200}").status());
        assertEquals(400, enviar("POST", "/api/pacientes", "{nome}").status());
        assertEquals(400, enviar("POST", "/api/pacientes", "[1]").status());
        assertEquals(404, get("/api/pacientes/P099").status());
        assertEquals(400, get("/api/pacientes/A123").status());
        assertEquals(404, enviar("POST", "/api/pacientes/P099/classificacao", "{\"prioridade\":1}").status());

        enviar("POST", "/api/pacientes", "{\"nome\":\"Ana\",\"idade\":30}");
        assertEquals(400, enviar("POST", "/api/pacientes/P001/classificacao", "{\"prioridade\":\"Roxo\"}").status());
        assertEquals(409, enviar("PUT", "/api/pacientes/P001/prioridade", "{\"prioridade\":1}").status()); // ainda na triagem
        assertEquals(200, enviar("POST", "/api/pacientes/P001/classificacao", "{\"prioridade\":3}").status());
        assertEquals(409, enviar("POST", "/api/pacientes/P001/classificacao", "{\"prioridade\":3}").status()); // de novo

        assertEquals(404, get("/api/nada").status());
        assertEquals(405, enviar("DELETE", "/api/pacientes", null).status());
    }

    @Test
    void liberaChamadasDoSiteEmOutraOrigem() throws Exception {
        Resposta preflight = enviar("OPTIONS", "/api/pacientes", null);
        assertEquals(204, preflight.status());
        assertEquals("*", preflight.bruta().headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        assertTrue(preflight.bruta().headers().firstValue("Access-Control-Allow-Methods").orElse("").contains("PUT"));

        Resposta lista = get("/api/pacientes");
        assertEquals("*", lista.bruta().headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        assertTrue(lista.bruta().headers().firstValue("Content-Type").orElse("").contains("charset=utf-8"));
    }
}
