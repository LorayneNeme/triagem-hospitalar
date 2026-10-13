package br.com.triagem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TriagemService {
    private final FilaAtendimento fila;

    private final Map<Integer, Paciente> pacientesPorCodigo = new HashMap<>();

    private final List<Paciente> atendidos = new ArrayList<>();

    private final Supplier<LocalDateTime> relogio;

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

    public Paciente cadastrarPaciente(int codigo, String nome, int idade, int nivelPrioridade) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("O código deve ser maior que zero.");
        }

        if (pacientesPorCodigo.containsKey(codigo)) {
            throw new IllegalArgumentException("Já existe paciente com o código " + codigo + ".");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do paciente é obrigatório.");
        }

        if (idade < 0 || idade > 130) {
            throw new IllegalArgumentException("A idade deve estar entre 0 e 130.");
        }

        Prioridade prioridade = Prioridade.porNivel(nivelPrioridade);

        Paciente paciente = new Paciente(
                codigo,
                nome.trim(),
                idade,
                prioridade,
                relogio.get(),
                proximaOrdem++
        );

        fila.inserir(paciente);
        pacientesPorCodigo.put(codigo, paciente);
        return paciente;
    }

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

    /** Encontra pacientes aguardando e também os que já foram chamados. */
    public Paciente buscarPaciente(int codigo) {
        return pacientesPorCodigo.get(codigo);
    }

    /**
     * Posição na fila começando em 1, ou -1 se o paciente não existe ou já foi chamado.
     * Custo de listarEmOrdem() + O(n): O(n log n) no heap, O(n) na árvore e na lista.
     */
    public int posicaoNaFila(int codigo) {
        Paciente paciente = pacientesPorCodigo.get(codigo);

        if (paciente == null || paciente.getStatus() != StatusPaciente.AGUARDANDO) {
            return -1;
        }

        return listarFilaOrdenada().indexOf(paciente) + 1;
    }

    public boolean alterarPrioridade(int codigo, int novaPrioridade) {
        Paciente paciente = pacientesPorCodigo.get(codigo);

        if (paciente == null || paciente.getStatus() != StatusPaciente.AGUARDANDO) {
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

        for (Paciente paciente : pacientesPorCodigo.values()) {
            porCor.merge(paciente.getPrioridade(), 1, Integer::sum);
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
                fila.tamanho(),
                atendidos.size(),
                porCor,
                media,
                maior
        );
    }
}
