package br.com.triagem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertTrue(r.insercaoMs() >= 0 && r.remocaoMs() >= 0 && r.alteracaoUsPorOp() >= 0 && r.consultaNs() >= 0);
        assertTrue(r.pacientesPorSegundo() > 0);
    }

    @Test
    void pacientesPorSegundoUsaSoInsercaoERemocao() {
        // Pendência 5: as alterações (quantidade fixa) ficam fora do ciclo e têm coluna própria.
        Benchmark.Resultado r = Benchmark.medir(new FilaArvore(), Cenario.EQUILIBRADO, 2_000, 42L);
        assertEquals(r.insercaoMs() + r.remocaoMs(), r.cicloMs(), 1e-9);
        assertEquals(2_000 / (r.cicloMs() / 1000.0), r.pacientesPorSegundo(), 1e-6);
        assertEquals(1_000, r.alteracoes());
        assertEquals(100, Benchmark.medir(new FilaHeap(), Cenario.EQUILIBRADO, 100, 42L).alteracoes());
        assertTrue(r.alteracaoUsPorOp() > 0);
    }

    @Test
    void chegadaAleatoriaEmbaralhaAOrdemDeInsercao() {
        // Pendência 2: cada paciente mantém a sua ordem de chegada, mas entra na fila fora de ordem.
        Paciente[] pacientes = Benchmark.gerarPacientes(Cenario.CHEGADA_ALEATORIA, 1_000, new Random(42));
        long[] ordens = Arrays.stream(pacientes).mapToLong(Paciente::getOrdemChegada).toArray();
        assertFalse(IntStream.range(1, ordens.length).allMatch(i -> ordens[i - 1] < ordens[i]));
        long[] ordenadas = ordens.clone();
        Arrays.sort(ordenadas);
        assertArrayEquals(LongStream.range(0, 1_000).toArray(), ordenadas); // permutação de 0..n-1

        Paciente[] emOrdem = Benchmark.gerarPacientes(Cenario.EQUILIBRADO, 1_000, new Random(42));
        assertTrue(IntStream.range(1, 1_000)
                .allMatch(i -> emOrdem[i - 1].getOrdemChegada() < emOrdem[i].getOrdemChegada()));
    }

    @Test
    void medirChegadaAleatoriaComCadaEstrutura() {
        for (FilaAtendimento fila : new FilaAtendimento[]{new FilaHeap(), new FilaArvore(), new FilaListaOrdenada()}) {
            Benchmark.Resultado r = Benchmark.medir(fila, Cenario.CHEGADA_ALEATORIA, 500, 42L);
            assertEquals(0, fila.tamanho());
            assertEquals(Cenario.CHEGADA_ALEATORIA, r.cenario());
        }
    }

    @Test
    void csvAbreNoExcelEmPortugues(@TempDir Path pasta) throws Exception {
        // Pendência 6: BOM UTF-8, ';' como separador e vírgula decimal.
        Benchmark.Resultado r = new Benchmark.Resultado("Árvore (TreeSet)", Cenario.MAIORIA_AZUL, 1000,
                1.5, 12.25, 2.25, 3.75, 266666.7, 1000, 0.5);
        Path arquivo = pasta.resolve("sub").resolve("b.csv");

        Benchmark.gravarCsv(arquivo, List.of(r));

        byte[] bytes = Files.readAllBytes(arquivo);
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, Arrays.copyOf(bytes, 3));
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        assertEquals("﻿estrutura;cenario;n;insercao_ms;consulta_ns;remocao_ms;ciclo_ms;pacientes_por_segundo;"
                + "alteracoes;alteracao_us_por_op", linhas.get(0));
        assertEquals("Árvore (TreeSet);MAIORIA_AZUL;1000;1,5000;12,25;2,2500;3,7500;266666,7;1000;0,5000",
                linhas.get(1));
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
        assertTrue(r.insercaoMs() >= 0 && r.remocaoMs() >= 0 && r.alteracaoUsPorOp() >= 0);
        assertEquals(r.insercaoMs() + r.remocaoMs(), r.cicloMs(), 1e-9);
        assertEquals(300, r.alteracoes());
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
    void chegadaAleatoriaSorteiaAsCincoCoresComoOEquilibrado() {
        Random a = new Random(7);
        Random b = new Random(7);
        for (int i = 0; i < 100; i++) {
            assertEquals(Cenario.EQUILIBRADO.sortear(a), Cenario.CHEGADA_ALEATORIA.sortear(b));
        }
        assertTrue(Cenario.CHEGADA_ALEATORIA.embaralhaChegada());
        assertFalse(Cenario.EQUILIBRADO.embaralhaChegada());
    }

    @Test
    void mesmaPrioridadeSempreAmarelo() {
        Random r = new Random(1);
        for (int i = 0; i < 100; i++) {
            assertEquals(Prioridade.AMARELO, Cenario.MESMA_PRIORIDADE.sortear(r));
        }
    }
}
