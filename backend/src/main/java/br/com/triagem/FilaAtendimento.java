package br.com.triagem;

import java.util.List;

/**
 * Fila de atendimento ordenada pelo PacienteComparator (prioridade, depois chegada).
 * Cada implementação usa uma estrutura de dados diferente, para comparar o desempenho.
 */
public interface FilaAtendimento {

    void inserir(Paciente paciente);

    /** Próximo a ser atendido, sem remover; null se a fila estiver vazia. */
    Paciente consultarPrimeiro();

    /** Remove e devolve o próximo a ser atendido; null se a fila estiver vazia. */
    Paciente removerPrimeiro();

    /** Remove um paciente específico. Deve ser chamado ANTES de mudar a prioridade dele. */
    boolean remover(Paciente paciente);

    int tamanho();

    /** Pacientes em ordem de atendimento, sem alterar a fila. */
    List<Paciente> listarEmOrdem();

    String nome();
}
