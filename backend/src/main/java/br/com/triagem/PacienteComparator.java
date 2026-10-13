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

        return a.getHorarioChegada().compareTo(b.getHorarioChegada());
    }
}
