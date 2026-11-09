package br.com.triagem;

import java.util.Comparator;

/**
 * Ordem de atendimento (spec 4.3): primeiro a cor, depois quem chegou antes.
 *
 * "Quem chegou antes" usa a ordemChegada (contador do cadastro na recepção), não o horário:
 * se o relógio do computador for atrasado, o horário de um cadastro novo pode ficar menor que
 * o de um antigo, mas a ordem de cadastro não muda. Também não depende da hora da triagem.
 */
public class PacienteComparator implements Comparator<Paciente> {
    @Override
    public int compare(Paciente a, Paciente b) {
        int porPrioridade = Integer.compare(
                a.getPrioridade().getNivel(),
                b.getPrioridade().getNivel()
        );

        if (porPrioridade != 0) {
            return porPrioridade;
        }

        return Long.compare(a.getOrdemChegada(), b.getOrdemChegada());
    }
}
