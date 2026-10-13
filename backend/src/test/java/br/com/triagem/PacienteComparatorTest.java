package br.com.triagem;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacienteComparatorTest {

    @Test
    void mesmoHorarioDesempataPelaOrdemDeChegada() {
        LocalDateTime t = LocalDateTime.of(2026, 10, 13, 10, 0);
        Paciente a = new Paciente(1, "A", 30, Prioridade.AMARELO, t, 0);
        Paciente b = new Paciente(2, "B", 30, Prioridade.AMARELO, t, 1);
        assertTrue(new PacienteComparator().compare(a, b) < 0);
        assertTrue(new PacienteComparator().compare(b, a) > 0);
    }

    @Test
    void codigoFormatadoTemTresDigitos() {
        Paciente p = new Paciente(7, "A", 30, Prioridade.AZUL, LocalDateTime.now(), 0);
        assertEquals("P007", p.getCodigoFormatado());
        assertEquals(StatusPaciente.AGUARDANDO, p.getStatus());
        assertEquals("Aguardando", p.getStatus().getDescricao());
        assertNull(p.getHorarioAtendimento());
    }
}
