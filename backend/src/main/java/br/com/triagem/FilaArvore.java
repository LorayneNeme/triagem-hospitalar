package br.com.triagem;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Árvore rubro-negra (TreeSet). Todas as operações em O(log n), inclusive remover(p),
 * o que deixa a alteração de prioridade mais barata que no heap.
 * Depende de o PacienteComparator nunca devolver 0 para pacientes diferentes
 * (garantido pela ordem de chegada); senão o TreeSet descartaria um deles.
 */
public class FilaArvore implements FilaAtendimento {
    private final TreeSet<Paciente> fila = new TreeSet<>(new PacienteComparator());

    @Override
    public void inserir(Paciente paciente) {
        fila.add(paciente);
    }

    @Override
    public Paciente consultarPrimeiro() {
        return fila.isEmpty() ? null : fila.first();
    }

    @Override
    public Paciente removerPrimeiro() {
        return fila.pollFirst();
    }

    @Override
    public boolean remover(Paciente paciente) {
        return fila.remove(paciente);
    }

    @Override
    public int tamanho() {
        return fila.size();
    }

    /** A árvore já é percorrida em ordem. O(n). */
    @Override
    public List<Paciente> listarEmOrdem() {
        return new ArrayList<>(fila);
    }

    @Override
    public String nome() {
        return "Árvore (TreeSet)";
    }
}
