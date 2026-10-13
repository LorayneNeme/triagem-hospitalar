package br.com.triagem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriagemServiceTest {

    @Test
    void cenarioDaEntrega3() {
        TriagemService s = new TriagemService();
        s.cadastrarPaciente(1, "Carlos", 3);
        s.cadastrarPaciente(2, "Ana", 1);
        s.cadastrarPaciente(3, "João", 3);

        assertEquals("Ana", s.consultarProximo().getNome());
        assertEquals(3, s.quantidadeAguardando());
        assertEquals("Ana", s.chamarProximo().getNome());
        assertEquals(2, s.quantidadeAguardando());

        assertTrue(s.alterarPrioridade(3, 2));
        assertEquals("João", s.listarFilaOrdenada().get(0).getNome());
        assertFalse(s.alterarPrioridade(99, 1));
    }
}
