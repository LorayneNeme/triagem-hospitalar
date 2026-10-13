package br.com.triagem;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regras que toda implementação de FilaAtendimento precisa cumprir. */
abstract class FilaAtendimentoContratoTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 13, 8, 0);

    protected abstract FilaAtendimento criarFila();

    private static Paciente paciente(int codigo, Prioridade prioridade) {
        return new Paciente(codigo, "Paciente " + codigo, 30, prioridade, INICIO.plusSeconds(codigo), codigo);
    }

    @Test
    void vaziaDevolveNull() {
        FilaAtendimento f = criarFila();
        assertNull(f.consultarPrimeiro());
        assertNull(f.removerPrimeiro());
        assertEquals(0, f.tamanho());
    }

    @Test
    void removeEmOrdemDePrioridadeEChegada() {
        FilaAtendimento f = criarFila();
        f.inserir(paciente(1, Prioridade.VERDE));
        f.inserir(paciente(2, Prioridade.AZUL));
        f.inserir(paciente(3, Prioridade.VERMELHO));
        f.inserir(paciente(4, Prioridade.AMARELO));
        f.inserir(paciente(5, Prioridade.LARANJA));

        List<Integer> ordem = new ArrayList<>();
        while (f.tamanho() > 0) {
            ordem.add(f.removerPrimeiro().getCodigo());
        }
        assertEquals(List.of(3, 5, 4, 1, 2), ordem);
    }

    @Test
    void listarEmOrdemNaoAlteraAFila() {
        FilaAtendimento f = criarFila();
        f.inserir(paciente(1, Prioridade.VERDE));
        f.inserir(paciente(2, Prioridade.VERMELHO));
        Paciente primeiro = f.consultarPrimeiro();

        List<Paciente> lista = f.listarEmOrdem();

        assertEquals(List.of(2, 1), lista.stream().map(Paciente::getCodigo).toList());
        assertEquals(2, f.tamanho());
        assertSame(primeiro, f.consultarPrimeiro());
    }

    @Test
    void removerEReinserirComNovaPrioridadeReposiciona() {
        FilaAtendimento f = criarFila();
        Paciente p1 = paciente(1, Prioridade.AMARELO);
        Paciente p2 = paciente(2, Prioridade.AMARELO);
        f.inserir(p1);
        f.inserir(p2);

        assertTrue(f.remover(p2));
        p2.setPrioridade(Prioridade.LARANJA);
        f.inserir(p2);

        assertSame(p2, f.consultarPrimeiro());
        assertTrue(f.remover(p2)); // a estrutura continua íntegra depois da reinserção
        assertEquals(1, f.tamanho());
    }

    @Test
    void milPacientesAleatoriosSaemOrdenados() {
        FilaAtendimento f = criarFila();
        Random random = new Random(42);
        Prioridade[] cores = Prioridade.values();
        for (int i = 1; i <= 1000; i++) {
            f.inserir(paciente(i, cores[random.nextInt(cores.length)]));
        }

        PacienteComparator comparador = new PacienteComparator();
        Paciente anterior = f.removerPrimeiro();
        while (f.tamanho() > 0) {
            Paciente atual = f.removerPrimeiro();
            assertTrue(comparador.compare(anterior, atual) < 0);
            anterior = atual;
        }
    }
}
