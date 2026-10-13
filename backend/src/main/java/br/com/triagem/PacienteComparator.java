package br.com.triagem;

import java.util.Comparator;

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

        int porHorario = a.getHorarioChegada().compareTo(b.getHorarioChegada());

        if (porHorario != 0) {
            return porHorario;
        }

        // Dois cadastros podem ter o mesmo horário; a ordem de chegada desempata.
        return Long.compare(a.getOrdemChegada(), b.getOrdemChegada());
    }
}
