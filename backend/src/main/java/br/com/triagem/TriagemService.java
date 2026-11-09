package br.com.triagem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Duas filas, como no site:
 * 1. fila de triagem — FIFO (ArrayDeque), pacientes sem cor na ordem em que chegaram;
 * 2. fila de atendimento — FilaAtendimento, ordenada por cor e depois por ordem de chegada.
 */
public class TriagemService {
    private final FilaAtendimento fila;

    private final Deque<Paciente> filaTriagem = new ArrayDeque<>();

    /** Todos os pacientes, em ordem de cadastro. Busca por código em O(1). */
    private final Map<Integer, Paciente> pacientesPorCodigo = new LinkedHashMap<>();

    private final List<Paciente> atendidos = new ArrayList<>();

    private final Supplier<LocalDateTime> relogio;

    private int proximoCodigo = 1;

    private long proximaOrdem = 0;

    public TriagemService() {
        this(LocalDateTime::now);
    }

    public TriagemService(Supplier<LocalDateTime> relogio) {
        this(new FilaHeap(), relogio);
    }

    public TriagemService(FilaAtendimento fila, Supplier<LocalDateTime> relogio) {
        this.fila = fila;
        this.relogio = relogio;
    }

    // ---------- Recepção ----------

    public Paciente cadastrarPaciente(String nome, int idade) {
        return cadastrarPaciente(nome, idade, "", "", "");
    }

    /** Cadastra sem cor e coloca no fim da fila de triagem. O código (P001, P002...) é gerado aqui. */
    public Paciente cadastrarPaciente(String nome, int idade, String telefone, String endereco, String queixa) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do paciente é obrigatório.");
        }

        if (idade < 0 || idade > 130) {
            throw new IllegalArgumentException("A idade deve estar entre 0 e 130.");
        }

        Paciente paciente = new Paciente(
                proximoCodigo++,
                nome.trim(),
                idade,
                telefone,
                endereco,
                queixa,
                relogio.get(),
                proximaOrdem++
        );

        filaTriagem.addLast(paciente);
        pacientesPorCodigo.put(paciente.getCodigo(), paciente);
        return paciente;
    }

    // ---------- Fila de triagem ----------

    public List<Paciente> listarFilaTriagem() {
        return new ArrayList<>(filaTriagem);
    }

    public Paciente consultarProximoTriagem() {
        return filaTriagem.peekFirst();
    }

    /**
     * Tira o primeiro da fila de triagem para ser classificado; null se a fila estiver vazia.
     * O status continua "Aguardando triagem" até classificar(), como no site.
     */
    public Paciente chamarParaTriagem() {
        return filaTriagem.pollFirst();
    }

    /** Posição na fila de triagem começando em 1, ou -1 se não estiver nela. O(n). */
    public int posicaoNaFilaTriagem(int codigo) {
        int posicao = 1;

        for (Paciente paciente : filaTriagem) {
            if (paciente.getCodigo() == codigo) return posicao;
            posicao++;
        }

        return -1;
    }

    public Paciente classificar(int codigo, int nivelPrioridade) {
        return classificar(codigo, nivelPrioridade, "", "", "");
    }

    /**
     * Define a cor e passa o paciente para a fila de atendimento.
     * Funciona para quem foi chamado com chamarParaTriagem() e também para quem ainda está na fila
     * de triagem (o site permite iniciar a triagem de qualquer paciente da lista).
     *
     * @throws IllegalArgumentException código inexistente ou prioridade fora de 1–5
     * @throws IllegalStateException    paciente já classificado
     */
    public Paciente classificar(int codigo, int nivelPrioridade, String especialidade, String sala,
                                String observacao) {
        Paciente paciente = pacientesPorCodigo.get(codigo);

        if (paciente == null) {
            throw new IllegalArgumentException("Paciente " + Paciente.formatarCodigo(codigo) + " não encontrado.");
        }

        // Valida antes de mexer nas filas.
        Prioridade prioridade = Prioridade.porNivel(nivelPrioridade);

        paciente.classificar(prioridade, especialidade, sala, observacao, relogio.get());
        filaTriagem.remove(paciente); // O(n); não faz nada se já tinha sido chamado
        fila.inserir(paciente);
        return paciente;
    }

    // ---------- Fila de atendimento ----------

    public Paciente consultarProximo() {
        return fila.consultarPrimeiro();
    }

    public Paciente chamarProximo() {
        Paciente paciente = fila.removerPrimeiro();

        if (paciente != null) {
            // Continua no HashMap: a busca e as estatísticas precisam de quem já foi atendido.
            paciente.iniciarAtendimento(relogio.get());
            atendidos.add(paciente);
        }

        return paciente;
    }

    /** Encontra pacientes em qualquer etapa, inclusive os já chamados. */
    public Paciente buscarPaciente(int codigo) {
        return pacientesPorCodigo.get(codigo);
    }

    /**
     * Posição na fila de atendimento começando em 1, ou -1 se o paciente não está nela.
     * Custo de listarEmOrdem() + O(n): O(n log n) no heap, O(n) na árvore e na lista.
     */
    public int posicaoNaFila(int codigo) {
        Paciente paciente = pacientesPorCodigo.get(codigo);

        if (paciente == null || paciente.getStatus() != StatusPaciente.AGUARDANDO_ATENDIMENTO) {
            return -1;
        }

        return listarFilaOrdenada().indexOf(paciente) + 1;
    }

    /** Só para quem está na fila de atendimento; devolve false para os demais. */
    public boolean alterarPrioridade(int codigo, int novaPrioridade) {
        Paciente paciente = pacientesPorCodigo.get(codigo);

        if (paciente == null || paciente.getStatus() != StatusPaciente.AGUARDANDO_ATENDIMENTO) {
            return false;
        }

        // Valida antes de mexer na fila, para um nível inválido não deixar o paciente fora dela.
        Prioridade prioridade = Prioridade.porNivel(novaPrioridade);

        // Remover ANTES de mudar a prioridade: com o paciente ainda dentro do TreeSet
        // ou da lista ordenada, a mudança deixaria a estrutura fora de ordem.
        fila.remover(paciente);
        paciente.setPrioridade(prioridade);
        fila.inserir(paciente);

        return true;
    }

    public List<Paciente> listarTodos() {
        return new ArrayList<>(pacientesPorCodigo.values());
    }

    public List<Paciente> listarAtendidos() {
        return new ArrayList<>(atendidos);
    }

    public List<Paciente> listarFilaOrdenada() {
        return fila.listarEmOrdem();
    }

    public int quantidadeAguardando() {
        return fila.tamanho();
    }

    public EstatisticasFila gerarEstatisticas() {
        Map<Prioridade, Integer> porCor = new EnumMap<>(Prioridade.class);

        for (Prioridade prioridade : Prioridade.values()) {
            porCor.put(prioridade, 0);
        }

        int aguardandoTriagem = 0;

        for (Paciente paciente : pacientesPorCodigo.values()) {
            if (paciente.getPrioridade() == null) {
                aguardandoTriagem++;
            } else {
                porCor.merge(paciente.getPrioridade(), 1, Integer::sum);
            }
        }

        Duration soma = Duration.ZERO;
        Duration maior = Duration.ZERO;

        for (Paciente paciente : atendidos) {
            Duration espera = paciente.getTempoEspera();
            soma = soma.plus(espera);

            if (espera.compareTo(maior) > 0) {
                maior = espera;
            }
        }

        Duration media = atendidos.isEmpty() ? Duration.ZERO : soma.dividedBy(atendidos.size());

        return new EstatisticasFila(
                pacientesPorCodigo.size(),
                aguardandoTriagem,
                fila.tamanho(),
                atendidos.size(),
                porCor,
                media,
                maior
        );
    }
}
