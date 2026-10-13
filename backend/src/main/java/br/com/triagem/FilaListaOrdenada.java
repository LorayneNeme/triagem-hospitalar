package br.com.triagem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lista mantida sempre em ordem (ArrayList). Serve de base de comparação:
 * achar a posição é O(log n) com busca binária, mas inserir e remover deslocam
 * os elementos seguintes, então custam O(n).
 */
public class FilaListaOrdenada implements FilaAtendimento {
    private final PacienteComparator comparador = new PacienteComparator();
    private final List<Paciente> fila = new ArrayList<>();

    @Override
    public void inserir(Paciente paciente) {
        int posicao = Collections.binarySearch(fila, paciente, comparador);

        // binarySearch devolve -(ponto de inserção) - 1 quando o elemento não está na lista.
        fila.add(posicao < 0 ? -posicao - 1 : posicao, paciente);
    }

    @Override
    public Paciente consultarPrimeiro() {
        return fila.isEmpty() ? null : fila.get(0);
    }

    @Override
    public Paciente removerPrimeiro() {
        return fila.isEmpty() ? null : fila.remove(0);
    }

    @Override
    public boolean remover(Paciente paciente) {
        int posicao = Collections.binarySearch(fila, paciente, comparador);

        if (posicao < 0 || fila.get(posicao) != paciente) {
            return false;
        }

        fila.remove(posicao);
        return true;
    }

    @Override
    public int tamanho() {
        return fila.size();
    }

    @Override
    public List<Paciente> listarEmOrdem() {
        return new ArrayList<>(fila);
    }

    @Override
    public String nome() {
        return "Lista ordenada (ArrayList)";
    }
}
