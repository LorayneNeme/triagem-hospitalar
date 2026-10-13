package br.com.triagem;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriagemServiceTest {

    @Test
    void cenarioDaEntrega3() {
        TriagemService s = new TriagemService();
        s.cadastrarPaciente(1, "Carlos", 40, 3);
        s.cadastrarPaciente(2, "Ana", 40, 1);
        s.cadastrarPaciente(3, "João", 40, 3);

        assertEquals("Ana", s.consultarProximo().getNome());
        assertEquals(3, s.quantidadeAguardando());
        assertEquals("Ana", s.chamarProximo().getNome());
        assertEquals(2, s.quantidadeAguardando());
        assertEquals(List.of(1, 3), codigos(s.listarFilaOrdenada()));

        assertTrue(s.alterarPrioridade(3, 2));
        assertEquals("João", s.listarFilaOrdenada().get(0).getNome());
        assertFalse(s.alterarPrioridade(99, 1));
    }

    @Test
    void mesmoInstanteMantemOrdemDeCadastro() {
        TriagemService s = new TriagemService(new RelogioFalso(LocalDateTime.of(2026, 10, 13, 10, 0)));
        s.cadastrarPaciente(1, "P1", 20, 3);
        s.cadastrarPaciente(2, "P2", 20, 3);
        s.cadastrarPaciente(3, "P3", 20, 3);
        assertEquals(List.of(1, 2, 3), codigos(s.listarFilaOrdenada()));
    }

    @Test
    void ordemDoExemploDaSecao43() {
        // chegada: P001 Verde, P002 Azul, P003 Vermelho, P004 Amarelo, P005 Laranja
        TriagemService s = new TriagemService();
        int[] prioridades = {4, 5, 1, 3, 2};
        for (int i = 0; i < 5; i++) {
            s.cadastrarPaciente(i + 1, "Paciente " + (i + 1), 30, prioridades[i]);
        }
        assertEquals(List.of(3, 5, 4, 1, 2), codigos(s.listarFilaOrdenada()));
    }

    @Test
    void cadastroInvalidoNaoInsere() {
        TriagemService s = new TriagemService();
        s.cadastrarPaciente(1, "Ana", 30, 1);
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(1, "Outro", 30, 1)); // código repetido
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(2, "  ", 30, 1));     // nome em branco
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(3, "Bia", -1, 1));    // idade negativa
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(4, "Caio", 131, 1));  // idade > 130
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(5, "Duda", 30, 0));   // prioridade 0
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(6, "Edu", 30, 6));    // prioridade 6
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(0, "Fê", 30, 1));     // código 0
        assertEquals(1, s.quantidadeAguardando());
    }

    @Test
    void chamarRegistraAtendimentoETempoDeEspera() {
        // spec 4.8: chegada 13:20, atendimento 14:05 → 45 minutos
        RelogioFalso relogio = new RelogioFalso(LocalDateTime.of(2026, 10, 13, 13, 20));
        TriagemService s = new TriagemService(relogio);
        s.cadastrarPaciente(3, "Paciente 003", 45, 1);
        relogio.avancar(Duration.ofMinutes(45));

        Paciente p = s.chamarProximo();
        assertEquals(StatusPaciente.EM_ATENDIMENTO, p.getStatus());
        assertEquals(LocalDateTime.of(2026, 10, 13, 14, 5), p.getHorarioAtendimento());
        assertEquals(Duration.ofMinutes(45), p.getTempoEspera());
        assertSame(p, s.buscarPaciente(3));
        assertEquals(0, s.quantidadeAguardando());
        assertEquals(List.of(p), s.listarAtendidos());
    }

    @Test
    void tempoDeEsperaDeQuemAindaAguardaLancaExcecao() {
        TriagemService s = new TriagemService();
        Paciente p = s.cadastrarPaciente(1, "Ana", 30, 1);
        assertThrows(IllegalStateException.class, p::getTempoEspera);
    }

    @Test
    void posicaoNaFilaDoExemploDaSecao47() {
        // fila esperada (spec 4.6): P031 Vermelho, P018 Laranja, P025 Amarelo, P009 Amarelo, P014 Verde, P021 Azul
        TriagemService s = new TriagemService();
        s.cadastrarPaciente(25, "P25", 30, 3);
        s.cadastrarPaciente(9, "P09", 30, 3);
        s.cadastrarPaciente(31, "P31", 30, 1);
        s.cadastrarPaciente(18, "P18", 30, 2);
        s.cadastrarPaciente(14, "P14", 30, 4);
        s.cadastrarPaciente(21, "P21", 30, 5);
        assertEquals(3, s.posicaoNaFila(25));
        assertEquals(4, s.posicaoNaFila(9));
        assertEquals(-1, s.posicaoNaFila(999));
        s.chamarProximo(); // P031
        assertEquals(-1, s.posicaoNaFila(31));
        assertEquals(2, s.posicaoNaFila(25));
    }

    @Test
    void naoAlteraPrioridadeDeQuemJaFoiChamado() {
        TriagemService s = new TriagemService();
        s.cadastrarPaciente(1, "Ana", 30, 1);
        s.chamarProximo();
        assertFalse(s.alterarPrioridade(1, 5));
        assertEquals(0, s.quantidadeAguardando());
        assertNull(s.chamarProximo());
    }

    @Test
    void alterarParaAMesmaPrioridadeMantemPosicao() {
        TriagemService s = new TriagemService(new RelogioFalso(LocalDateTime.of(2026, 10, 13, 10, 0)));
        s.cadastrarPaciente(1, "A", 30, 3);
        s.cadastrarPaciente(2, "B", 30, 3);
        assertTrue(s.alterarPrioridade(1, 3));
        assertEquals(1, s.posicaoNaFila(1));
    }

    @Test
    void alterarParaPrioridadeInvalidaNaoTiraDaFila() {
        TriagemService s = new TriagemService();
        s.cadastrarPaciente(1, "Ana", 30, 3);
        assertThrows(IllegalArgumentException.class, () -> s.alterarPrioridade(1, 9));
        assertEquals(1, s.quantidadeAguardando());
        assertEquals(1, s.posicaoNaFila(1));
    }

    @Test
    void codigoDePacienteJaAtendidoContinuaOcupado() {
        TriagemService s = new TriagemService();
        s.cadastrarPaciente(1, "Ana", 30, 1);
        s.chamarProximo();
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(1, "Bia", 30, 2));
    }

    private static List<Integer> codigos(List<Paciente> pacientes) {
        return pacientes.stream().map(Paciente::getCodigo).toList();
    }
}
