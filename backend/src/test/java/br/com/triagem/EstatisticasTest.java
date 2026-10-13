package br.com.triagem;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EstatisticasTest {

    @Test
    void semPacientesTudoZero() {
        TriagemService s = new TriagemService();
        assertNull(s.consultarProximo());
        assertNull(s.chamarProximo());

        EstatisticasFila e = s.gerarEstatisticas();
        assertEquals(0, e.total());
        assertEquals(0, e.aguardando());
        assertEquals(0, e.atendidos());
        assertEquals(Duration.ZERO, e.tempoMedioEspera());
        assertEquals(Duration.ZERO, e.maiorTempoEspera());
        assertEquals(5, e.porCor().size());
        assertEquals(0, e.porCor().get(Prioridade.VERMELHO));
    }

    @Test
    void calculaMediaEMaiorEspera() {
        RelogioFalso r = new RelogioFalso(LocalDateTime.of(2026, 10, 13, 13, 20));
        TriagemService s = new TriagemService(r);
        s.cadastrarPaciente(1, "A", 30, 1);
        s.cadastrarPaciente(2, "B", 30, 3);
        s.cadastrarPaciente(3, "C", 30, 3);
        r.avancar(Duration.ofMinutes(45));
        s.chamarProximo(); // A esperou 45 min
        r.avancar(Duration.ofMinutes(15));
        s.chamarProximo(); // B esperou 60 min

        EstatisticasFila e = s.gerarEstatisticas();
        assertEquals(3, e.total());
        assertEquals(1, e.aguardando());
        assertEquals(2, e.atendidos());
        assertEquals(1, e.porCor().get(Prioridade.VERMELHO));
        assertEquals(2, e.porCor().get(Prioridade.AMARELO));
        assertEquals(0, e.porCor().get(Prioridade.AZUL));
        assertEquals(Duration.ofMinutes(52).plusSeconds(30), e.tempoMedioEspera());
        assertEquals(Duration.ofMinutes(60), e.maiorTempoEspera());
    }
}
