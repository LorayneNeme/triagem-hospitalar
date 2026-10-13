package br.com.triagem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Supplier;

public class TriagemService {
    private final PriorityQueue<Paciente> fila =
            new PriorityQueue<>(new PacienteComparator());

    private final Map<Integer, Paciente> pacientesPorCodigo = new HashMap<>();

    private final Supplier<LocalDateTime> relogio;

    private long proximaOrdem = 0;

    public TriagemService() {
        this(LocalDateTime::now);
    }

    public TriagemService(Supplier<LocalDateTime> relogio) {
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

        fila.add(paciente);
        pacientesPorCodigo.put(codigo, paciente);
        return paciente;
    }

    public Paciente consultarProximo() {
        return fila.peek();
    }

    public Paciente chamarProximo() {
        Paciente paciente = fila.poll();

        if (paciente != null) {
            pacientesPorCodigo.remove(paciente.getCodigo());
        }

        return paciente;
    }

    public Paciente buscarPaciente(int codigo) {
        return pacientesPorCodigo.get(codigo);
    }

    public boolean alterarPrioridade(int codigo, int novaPrioridade) {
        Paciente paciente = pacientesPorCodigo.get(codigo);

        if (paciente == null) {
            return false;
        }

        fila.remove(paciente);
        paciente.setPrioridade(Prioridade.porNivel(novaPrioridade));
        fila.add(paciente);

        return true;
    }

    public List<Paciente> listarFilaOrdenada() {
        PriorityQueue<Paciente> copia =
                new PriorityQueue<>(new PacienteComparator());
        copia.addAll(fila);

        List<Paciente> ordenados = new ArrayList<>();

        while (!copia.isEmpty()) {
            ordenados.add(copia.poll());
        }

        return ordenados;
    }

    public int quantidadeAguardando() {
        return fila.size();
    }
}
