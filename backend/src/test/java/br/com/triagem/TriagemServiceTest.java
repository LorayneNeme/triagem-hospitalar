package br.com.triagem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriagemServiceTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 13, 10, 0);

    /** Recepção + triagem de uma vez, para os testes que só olham a fila de atendimento. */
    private static Paciente novo(TriagemService s, String nome, int nivel) {
        Paciente p = s.cadastrarPaciente(nome, 30);
        s.classificar(p.getCodigo(), nivel);
        return p;
    }

    // ---------- Recepção e fila de triagem ----------

    @Test
    void cadastroGeraCodigoSequencialEEntraNaFilaDeTriagem() {
        TriagemService s = new TriagemService(new RelogioFalso(INICIO));
        Paciente ana = s.cadastrarPaciente("Ana", 30, "11 99999-0000", "Rua A, 1", "Febre");
        Paciente bia = s.cadastrarPaciente("Bia", 25);

        assertEquals("P001", ana.getCodigoFormatado());
        assertEquals("P002", bia.getCodigoFormatado());
        assertEquals(StatusPaciente.AGUARDANDO_TRIAGEM, ana.getStatus());
        assertNull(ana.getPrioridade());
        assertEquals("11 99999-0000", ana.getTelefone());
        assertEquals("Rua A, 1", ana.getEndereco());
        assertEquals("Febre", ana.getQueixa());
        assertEquals("", bia.getTelefone());
        assertEquals(INICIO, ana.getHorarioChegada());

        assertEquals(List.of(ana, bia), s.listarFilaTriagem());
        assertEquals(0, s.quantidadeAguardando());
        assertNull(s.consultarProximo());
    }

    @Test
    void chamarParaTriagemSegueOrdemDeChegada() {
        TriagemService s = new TriagemService();
        Paciente a = s.cadastrarPaciente("A", 30);
        Paciente b = s.cadastrarPaciente("B", 30);

        assertSame(a, s.consultarProximoTriagem());
        assertSame(a, s.chamarParaTriagem());
        assertEquals(List.of(b), s.listarFilaTriagem());
        // Chamado, mas ainda sem cor: continua "Aguardando triagem" até ser classificado.
        assertEquals(StatusPaciente.AGUARDANDO_TRIAGEM, a.getStatus());
        assertSame(b, s.chamarParaTriagem());
        assertNull(s.chamarParaTriagem());
    }

    @Test
    void classificarMoveParaAFilaDeAtendimento() {
        RelogioFalso relogio = new RelogioFalso(INICIO);
        TriagemService s = new TriagemService(relogio);
        Paciente p = s.cadastrarPaciente("Ana", 30);
        s.chamarParaTriagem();
        relogio.avancar(Duration.ofMinutes(10));

        s.classificar(p.getCodigo(), 2, "Clínica geral", "Sala 3", "Pressão alta");

        assertEquals(Prioridade.LARANJA, p.getPrioridade());
        assertEquals("Clínica geral", p.getEspecialidade());
        assertEquals("Sala 3", p.getSala());
        assertEquals("Pressão alta", p.getObservacao());
        assertEquals(INICIO.plusMinutes(10), p.getHorarioTriagem());
        assertEquals(StatusPaciente.AGUARDANDO_ATENDIMENTO, p.getStatus());
        assertSame(p, s.consultarProximo());
        assertEquals(1, s.posicaoNaFila(p.getCodigo()));
    }

    @Test
    void classificaQualquerPacienteDaFilaDeTriagemSemChamarAntes() {
        TriagemService s = new TriagemService();
        Paciente a = s.cadastrarPaciente("A", 30);
        Paciente b = s.cadastrarPaciente("B", 30);
        Paciente c = s.cadastrarPaciente("C", 30);

        s.classificar(b.getCodigo(), 1);

        assertEquals(List.of(a, c), s.listarFilaTriagem());
        assertEquals(2, s.posicaoNaFilaTriagem(c.getCodigo()));
        assertEquals(-1, s.posicaoNaFilaTriagem(b.getCodigo()));
    }

    @Test
    void desempateUsaAChegadaNaRecepcaoENaoAHoraDaTriagem() {
        // Pendência 11 / spec 4.3: A chegou antes de B; B foi triado antes; ambos Amarelo → A primeiro.
        RelogioFalso relogio = new RelogioFalso(INICIO);
        TriagemService s = new TriagemService(relogio);
        Paciente a = s.cadastrarPaciente("A", 30);
        relogio.avancar(Duration.ofMinutes(1));
        Paciente b = s.cadastrarPaciente("B", 30);

        s.classificar(b.getCodigo(), 3);
        relogio.avancar(Duration.ofMinutes(5));
        s.classificar(a.getCodigo(), 3);

        assertEquals(List.of(a, b), s.listarFilaOrdenada());
    }

    @Test
    void classificacaoInvalidaNaoMexeNasFilas() {
        TriagemService s = new TriagemService();
        Paciente p = s.cadastrarPaciente("Ana", 30);

        assertThrows(IllegalArgumentException.class, () -> s.classificar(p.getCodigo(), 9));
        assertThrows(IllegalArgumentException.class, () -> s.classificar(99, 1));
        assertEquals(List.of(p), s.listarFilaTriagem());
        assertEquals(0, s.quantidadeAguardando());

        s.classificar(p.getCodigo(), 1);
        assertThrows(IllegalStateException.class, () -> s.classificar(p.getCodigo(), 2)); // já classificado
        assertEquals(Prioridade.VERMELHO, p.getPrioridade());
        assertEquals(1, s.quantidadeAguardando());
    }

    @Test
    void pacienteEmTriagemNaoEntraNasOperacoesDaFilaDeAtendimento() {
        TriagemService s = new TriagemService();
        Paciente p = s.cadastrarPaciente("Ana", 30);
        assertFalse(s.alterarPrioridade(p.getCodigo(), 1));
        assertEquals(-1, s.posicaoNaFila(p.getCodigo()));
        assertNull(s.chamarProximo());
        assertThrows(IllegalStateException.class, p::getTempoEspera);
    }

    @Test
    void cadastroInvalidoNaoInsere() {
        TriagemService s = new TriagemService();
        s.cadastrarPaciente("Ana", 30);
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente("  ", 30));   // nome em branco
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente(null, 30));   // nome nulo
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente("Bia", -1));  // idade negativa
        assertThrows(IllegalArgumentException.class, () -> s.cadastrarPaciente("Caio", 131)); // idade > 130
        assertEquals(1, s.listarFilaTriagem().size());
        assertEquals("P002", s.cadastrarPaciente("Duda", 0).getCodigoFormatado()); // erro não gasta código
    }

    @Test
    void listarTodosEmOrdemDeCadastro() {
        TriagemService s = new TriagemService();
        Paciente a = novo(s, "A", 5);
        Paciente b = s.cadastrarPaciente("B", 30);
        Paciente c = novo(s, "C", 1);
        s.chamarProximo();
        assertEquals(List.of(a, b, c), s.listarTodos());
    }

    // ---------- Fila de atendimento (regras da Entrega 3) ----------

    @Test
    void cenarioDaEntrega3() {
        TriagemService s = new TriagemService();
        Paciente carlos = novo(s, "Carlos", 3);
        novo(s, "Ana", 1);
        Paciente joao = novo(s, "João", 3);

        assertEquals("Ana", s.consultarProximo().getNome());
        assertEquals(3, s.quantidadeAguardando());
        assertEquals("Ana", s.chamarProximo().getNome());
        assertEquals(2, s.quantidadeAguardando());
        assertEquals(List.of(carlos, joao), s.listarFilaOrdenada());

        assertTrue(s.alterarPrioridade(joao.getCodigo(), 2));
        assertEquals("João", s.listarFilaOrdenada().get(0).getNome());
        assertFalse(s.alterarPrioridade(99, 1));
    }

    static Stream<FilaAtendimento> filas() {
        return Stream.of(new FilaHeap(), new FilaArvore(), new FilaListaOrdenada());
    }

    @ParameterizedTest
    @MethodSource("filas")
    void cenarioDaEntrega3ComCadaEstrutura(FilaAtendimento fila) {
        TriagemService s = new TriagemService(fila, LocalDateTime::now);
        Paciente carlos = novo(s, "Carlos", 3);
        novo(s, "Ana", 1);
        Paciente joao = novo(s, "João", 3);

        assertEquals("Ana", s.chamarProximo().getNome());
        assertEquals(List.of(carlos, joao), s.listarFilaOrdenada());
        assertTrue(s.alterarPrioridade(joao.getCodigo(), 2));
        assertEquals(List.of(joao, carlos), s.listarFilaOrdenada());
    }

    @Test
    void mesmoInstanteMantemOrdemDeCadastro() {
        TriagemService s = new TriagemService(new RelogioFalso(INICIO));
        Paciente p1 = novo(s, "P1", 3);
        Paciente p2 = novo(s, "P2", 3);
        Paciente p3 = novo(s, "P3", 3);
        assertEquals(List.of(p1, p2, p3), s.listarFilaOrdenada());
    }

    @Test
    void ordemDoExemploDaSecao43() {
        // chegada: P001 Verde, P002 Azul, P003 Vermelho, P004 Amarelo, P005 Laranja
        TriagemService s = new TriagemService();
        int[] prioridades = {4, 5, 1, 3, 2};
        for (int i = 0; i < 5; i++) {
            novo(s, "Paciente " + (i + 1), prioridades[i]);
        }
        assertEquals(List.of(3, 5, 4, 1, 2), codigos(s.listarFilaOrdenada()));
    }

    @Test
    void chamarRegistraAtendimentoETempoDeEspera() {
        // spec 4.8: chegada 13:20, atendimento 14:05 → 45 minutos (conta desde a chegada, não desde a triagem)
        RelogioFalso relogio = new RelogioFalso(LocalDateTime.of(2026, 10, 13, 13, 20));
        TriagemService s = new TriagemService(relogio);
        Paciente cadastrado = s.cadastrarPaciente("Paciente 001", 45);
        relogio.avancar(Duration.ofMinutes(15));
        s.classificar(cadastrado.getCodigo(), 1);
        relogio.avancar(Duration.ofMinutes(30));

        Paciente p = s.chamarProximo();
        assertSame(cadastrado, p);
        assertEquals(StatusPaciente.EM_ATENDIMENTO, p.getStatus());
        assertEquals(LocalDateTime.of(2026, 10, 13, 14, 5), p.getHorarioAtendimento());
        assertEquals(Duration.ofMinutes(45), p.getTempoEspera());
        assertSame(p, s.buscarPaciente(1));
        assertEquals(0, s.quantidadeAguardando());
        assertEquals(List.of(p), s.listarAtendidos());
    }

    @Test
    void tempoDeEsperaDeQuemAindaAguardaLancaExcecao() {
        TriagemService s = new TriagemService();
        Paciente p = novo(s, "Ana", 1);
        assertThrows(IllegalStateException.class, p::getTempoEspera);
    }

    @Test
    void posicaoNaFilaDoExemploDaSecao47() {
        // fila esperada (spec 4.6): Vermelho, Laranja, Amarelo (chegou 1º), Amarelo (chegou 2º), Verde, Azul
        TriagemService s = new TriagemService();
        Paciente amarelo1 = novo(s, "Amarelo 1", 3);
        Paciente amarelo2 = novo(s, "Amarelo 2", 3);
        Paciente vermelho = novo(s, "Vermelho", 1);
        novo(s, "Laranja", 2);
        novo(s, "Verde", 4);
        novo(s, "Azul", 5);
        assertEquals(3, s.posicaoNaFila(amarelo1.getCodigo()));
        assertEquals(4, s.posicaoNaFila(amarelo2.getCodigo()));
        assertEquals(-1, s.posicaoNaFila(999));
        s.chamarProximo();
        assertEquals(-1, s.posicaoNaFila(vermelho.getCodigo()));
        assertEquals(2, s.posicaoNaFila(amarelo1.getCodigo()));
    }

    @Test
    void naoAlteraPrioridadeDeQuemJaFoiChamado() {
        TriagemService s = new TriagemService();
        Paciente p = novo(s, "Ana", 1);
        s.chamarProximo();
        assertFalse(s.alterarPrioridade(p.getCodigo(), 5));
        assertEquals(0, s.quantidadeAguardando());
        assertNull(s.chamarProximo());
    }

    @Test
    void alterarParaAMesmaPrioridadeMantemPosicao() {
        TriagemService s = new TriagemService(new RelogioFalso(INICIO));
        Paciente a = novo(s, "A", 3);
        novo(s, "B", 3);
        assertTrue(s.alterarPrioridade(a.getCodigo(), 3));
        assertEquals(1, s.posicaoNaFila(a.getCodigo()));
    }

    @Test
    void alterarParaPrioridadeInvalidaNaoTiraDaFila() {
        TriagemService s = new TriagemService();
        Paciente p = novo(s, "Ana", 3);
        assertThrows(IllegalArgumentException.class, () -> s.alterarPrioridade(p.getCodigo(), 9));
        assertEquals(1, s.quantidadeAguardando());
        assertEquals(1, s.posicaoNaFila(p.getCodigo()));
    }

    private static List<Integer> codigos(List<Paciente> pacientes) {
        return pacientes.stream().map(Paciente::getCodigo).toList();
    }
}
