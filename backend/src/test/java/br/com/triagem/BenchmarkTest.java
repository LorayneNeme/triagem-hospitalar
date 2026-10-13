package br.com.triagem;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchmarkTest {

    @Test
    void medirEsvaziaAFilaEDevolveTemposValidos() {
        FilaAtendimento fila = new FilaHeap();
        Benchmark.Resultado r = Benchmark.medir(fila, Cenario.EQUILIBRADO, 500, 42L);
        assertEquals(0, fila.tamanho());
        assertEquals(500, r.n());
        assertEquals(Cenario.EQUILIBRADO, r.cenario());
        assertEquals("Heap (PriorityQueue)", r.estrutura());
        assertTrue(r.insercaoMs() >= 0 && r.remocaoMs() >= 0 && r.alteracaoMs() >= 0 && r.consultaNs() >= 0);
        assertTrue(r.pacientesPorSegundo() > 0);
    }

    @Test
    void medianaDeQuantidadeImparEPar() {
        assertEquals(3.0, Benchmark.mediana(new double[]{5, 1, 3}));
        assertEquals(2.5, Benchmark.mediana(new double[]{4, 1, 3, 2}));
        assertEquals(7.0, Benchmark.mediana(new double[]{7}));
    }

    @Test
    void medirMedianaRepeteComFilasNovasEUsaAMediana() {
        Benchmark.Resultado r = Benchmark.medirMediana(FilaArvore::new, Cenario.MAIORIA_AZUL, 300, 42L, 3);
        assertEquals(300, r.n());
        assertEquals(Cenario.MAIORIA_AZUL, r.cenario());
        assertEquals("Árvore (TreeSet)", r.estrutura());
        assertTrue(r.insercaoMs() >= 0 && r.remocaoMs() >= 0 && r.alteracaoMs() >= 0);
        assertTrue(r.pacientesPorSegundo() > 0);
    }

    @Test
    void cenarioMaioriaAzulFicaPertoDe70PorCento() {
        Random r = new Random(42);
        long azuis = IntStream.range(0, 10_000)
                .filter(i -> Cenario.MAIORIA_AZUL.sortear(r) == Prioridade.AZUL)
                .count();
        assertTrue(azuis > 6_500 && azuis < 7_500, "azuis = " + azuis);
    }

    @Test
    void cenarioMaioriaVermelhoFicaPertoDe70PorCento() {
        Random r = new Random(42);
        long vermelhos = IntStream.range(0, 10_000)
                .filter(i -> Cenario.MAIORIA_VERMELHO.sortear(r) == Prioridade.VERMELHO)
                .count();
        assertTrue(vermelhos > 6_500 && vermelhos < 7_500, "vermelhos = " + vermelhos);
    }

    @Test
    void cenarioEquilibradoUsaAsCincoCores() {
        Random r = new Random(42);
        int[] contagem = new int[6];
        for (int i = 0; i < 10_000; i++) {
            contagem[Cenario.EQUILIBRADO.sortear(r).getNivel()]++;
        }
        for (int nivel = 1; nivel <= 5; nivel++) {
            assertTrue(contagem[nivel] > 1_700 && contagem[nivel] < 2_300, "nível " + nivel + " = " + contagem[nivel]);
        }
    }

    @Test
    void mesmaPrioridadeSempreAmarelo() {
        Random r = new Random(1);
        for (int i = 0; i < 100; i++) {
            assertEquals(Prioridade.AMARELO, Cenario.MESMA_PRIORIDADE.sortear(r));
        }
    }
}
