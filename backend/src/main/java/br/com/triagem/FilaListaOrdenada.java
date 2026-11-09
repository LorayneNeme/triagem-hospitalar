package br.com.triagem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Lista mantida sempre em ordem (ArrayList). Serve de base de comparação.
 *
 * A lista fica em ordem DECRESCENTE: o próximo a ser atendido é o último elemento.
 * Assim consultar e chamar o próximo são O(1) (remove do fim, sem deslocar nada).
 * Achar a posição de inserção é O(log n) com busca binária, mas inserir ou remover no meio
 * desloca os elementos seguintes: O(n). Com a lista crescente era o contrário — chamar o próximo
 * (remove(0)) custava O(n) e um paciente de cor repetida entrava perto do fim, quase de graça.
 */
public class FilaListaOrdenada implements FilaAtendimento {
    private final Comparator<Paciente> decrescente = new PacienteComparator().reversed();
    private final List<Paciente> fila = new ArrayList<>();

    @Override
    public void inserir(Paciente paciente) {
        int posicao = Collections.binarySearch(fila, paciente, decrescente);

        // binarySearch devolve -(ponto de inserção) - 1 quando o elemento não está na lista.
        fila.add(posicao < 0 ? -posicao - 1 : posicao, paciente);
    }

    @Override
    public Paciente consultarPrimeiro() {
        return fila.isEmpty() ? null : fila.get(fila.size() - 1);
    }

    @Override
    public Paciente removerPrimeiro() {
        return fila.isEmpty() ? null : fila.remove(fila.size() - 1);
    }

    @Override
    public boolean remover(Paciente paciente) {
        int posicao = Collections.binarySearch(fila, paciente, decrescente);

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
        List<Paciente> emOrdem = new ArrayList<>(fila);
        Collections.reverse(emOrdem);
        return emOrdem;
    }

    @Override
    public String nome() {
        return "Lista ordenada (ArrayList)";
    }
}
