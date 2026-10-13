package br.com.triagem;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Heap binário (PriorityQueue). Estrutura padrão do sistema.
 * inserir/removerPrimeiro O(log n), consultarPrimeiro O(1), remover(p) O(n).
 */
public class FilaHeap implements FilaAtendimento {
    private final PriorityQueue<Paciente> fila = new PriorityQueue<>(new PacienteComparator());

    @Override
    public void inserir(Paciente paciente) {
        fila.add(paciente);
    }

    @Override
    public Paciente consultarPrimeiro() {
        return fila.peek();
    }

    @Override
    public Paciente removerPrimeiro() {
        return fila.poll();
    }

    @Override
    public boolean remover(Paciente paciente) {
        return fila.remove(paciente);
    }

    @Override
    public int tamanho() {
        return fila.size();
    }

    /** Percorrer a PriorityQueue não segue a ordem; por isso esvazia uma cópia com poll(). O(n log n). */
    @Override
    public List<Paciente> listarEmOrdem() {
        PriorityQueue<Paciente> copia = new PriorityQueue<>(fila);
        List<Paciente> ordenados = new ArrayList<>(copia.size());

        while (!copia.isEmpty()) {
            ordenados.add(copia.poll());
        }

        return ordenados;
    }

    @Override
    public String nome() {
        return "Heap (PriorityQueue)";
    }
}
