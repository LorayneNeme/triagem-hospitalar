package br.com.triagem;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void relogioQueVoltaNoTempoNaoMudaAOrdem() {
        // B foi cadastrado depois de A, mas o relógio do computador foi atrasado entre os dois cadastros.
        LocalDateTime t = LocalDateTime.of(2026, 10, 13, 10, 0);
        Paciente a = new Paciente(1, "A", 30, Prioridade.AMARELO, t, 0);
        Paciente b = new Paciente(2, "B", 30, Prioridade.AMARELO, t.minusMinutes(5), 1);
        assertTrue(new PacienteComparator().compare(a, b) < 0);
    }

    @Test
    void pacienteJaClassificadoComecaAguardandoAtendimento() {
        Paciente p = new Paciente(7, "A", 30, Prioridade.AZUL, LocalDateTime.now(), 0);
        assertEquals(StatusPaciente.AGUARDANDO_ATENDIMENTO, p.getStatus());
        assertEquals("Aguardando atendimento", p.getStatus().getDescricao());
        assertNull(p.getHorarioAtendimento());
    }

    @Test
    void codigoFormatado() {
        assertEquals("P007", Paciente.formatarCodigo(7));
        assertEquals("P025", Paciente.formatarCodigo(25));
        assertEquals("P1000", Paciente.formatarCodigo(1000));
    }

    @Test
    void lerCodigoAceitaNumeroOuCodigoExibido() {
        // Pendência 7: o menu e a API aceitam "P025", "p25" e "25".
        assertEquals(25, Paciente.lerCodigo("P025"));
        assertEquals(25, Paciente.lerCodigo("p25"));
        assertEquals(25, Paciente.lerCodigo(" 25 "));
        assertEquals(1000, Paciente.lerCodigo("P1000"));
    }

    @Test
    void lerCodigoRecusaTextoInvalido() {
        for (String invalido : new String[]{"", "  ", "P", "P0", "0", "P-3", "abc", "A123", "P12x", null}) {
            assertThrows(IllegalArgumentException.class, () -> Paciente.lerCodigo(invalido), String.valueOf(invalido));
        }
    }

    @Test
    void lerPrioridadePorNumeroOuNome() {
        assertEquals(Prioridade.VERMELHO, Prioridade.ler("1"));
        assertEquals(Prioridade.AMARELO, Prioridade.ler("Amarelo"));
        assertEquals(Prioridade.AZUL, Prioridade.ler(" azul "));
        assertThrows(IllegalArgumentException.class, () -> Prioridade.ler("Roxo"));
        assertThrows(IllegalArgumentException.class, () -> Prioridade.ler("6"));
        assertThrows(IllegalArgumentException.class, () -> Prioridade.ler(null));
    }
}
