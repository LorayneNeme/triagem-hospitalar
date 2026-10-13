package br.com.triagem;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private static List<Integer> codigos(List<Paciente> pacientes) {
        return pacientes.stream().map(Paciente::getCodigo).toList();
    }
}
