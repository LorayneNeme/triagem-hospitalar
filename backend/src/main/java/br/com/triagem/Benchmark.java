package br.com.triagem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Mede as três estruturas de FilaAtendimento com 100 a 1.000.000 de pacientes
 * nos cinco cenários e grava o resultado em resultados/benchmark.csv.
 * Cada combinação é medida 5 vezes e o CSV guarda a mediana.
 *
 * Executar: java -Xms2g -Xmx2g -cp target/classes br.com.triagem.Benchmark
 */
public class Benchmark {

    /**
     * @param insercaoMs          inserir os n pacientes
     * @param consultaNs          uma consulta do próximo (média de 1.000)
     * @param remocaoMs           chamar os n pacientes até esvaziar a fila
     * @param cicloMs             inserção + remoção: o caminho completo de n pacientes pela fila
     * @param pacientesPorSegundo n / cicloMs — quantos pacientes a estrutura consegue receber e chamar por segundo
     * @param alteracoes          quantas alterações de prioridade foram medidas: min(1.000, n)
     * @param alteracaoUsPorOp    custo médio de UMA alteração (remover, mudar a cor, reinserir), em microssegundos
     */
    public record Resultado(
            String estrutura,
            Cenario cenario,
            int n,
            double insercaoMs,
            double consultaNs,
            double remocaoMs,
            double cicloMs,
            double pacientesPorSegundo,
            int alteracoes,
            double alteracaoUsPorOp) {
    }

    private static final int[] TAMANHOS = {100, 1_000, 10_000, 100_000, 1_000_000};
    private static final int LIMITE_LISTA_ORDENADA = 100_000;
    private static final int CONSULTAS = 1_000;
    private static final int MAX_ALTERACOES = 1_000;
    private static final long SEMENTE = 42L;
    private static final int REPETICOES = 5;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 13, 8, 0);
    private static final Path ARQUIVO_CSV = Path.of("resultados", "benchmark.csv");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final String CABECALHO_CSV = "estrutura;cenario;n;insercao_ms;consulta_ns;remocao_ms;ciclo_ms;"
            + "pacientes_por_segundo;alteracoes;alteracao_us_por_op";

    /** Impede a JIT de descartar as consultas por não usarem o resultado. */
    private static volatile int sumidouro;

    /**
     * Pacientes já classificados, com ordemChegada 0..n-1. No cenário CHEGADA_ALEATORIA o vetor
     * é embaralhado: cada um mantém a sua ordem de chegada, mas eles entram na fila fora de ordem.
     */
    static Paciente[] gerarPacientes(Cenario cenario, int n, Random random) {
        Paciente[] pacientes = new Paciente[n];
        for (int i = 0; i < n; i++) {
            pacientes[i] = new Paciente(i + 1, "Paciente " + (i + 1), 30,
                    cenario.sortear(random), INICIO.plusSeconds(i), i);
        }

        if (cenario.embaralhaChegada()) {
            // Fisher–Yates com o mesmo Random, para o resultado ser reproduzível.
            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Paciente troca = pacientes[i];
                pacientes[i] = pacientes[j];
                pacientes[j] = troca;
            }
        }

        return pacientes;
    }

    public static Resultado medir(FilaAtendimento fila, Cenario cenario, int n, long semente) {
        Random random = new Random(semente);

        // 1. Pacientes gerados antes de cronometrar.
        Paciente[] pacientes = gerarPacientes(cenario, n, random);

        // 2. Inserção.
        long inicio = System.nanoTime();
        for (Paciente paciente : pacientes) {
            fila.inserir(paciente);
        }
        long insercao = System.nanoTime() - inicio;

        // 3. Consulta do próximo. Soma numa variável local e grava no campo volatile uma vez só:
        //    escrever no volatile a cada volta custaria mais que a própria consulta.
        int soma = 0;
        inicio = System.nanoTime();
        for (int i = 0; i < CONSULTAS; i++) {
            soma += fila.consultarPrimeiro().getCodigo();
        }
        double consultaNs = (System.nanoTime() - inicio) / (double) CONSULTAS;
        sumidouro = soma;

        // 4. Alteração de prioridade: remover, mudar e reinserir.
        int alteracoes = Math.min(MAX_ALTERACOES, n);
        inicio = System.nanoTime();
        for (int i = 0; i < alteracoes; i++) {
            Paciente paciente = pacientes[random.nextInt(n)];
            fila.remover(paciente);
            paciente.setPrioridade(Prioridade.porNivel(random.nextInt(5) + 1));
            fila.inserir(paciente);
        }
        long alteracao = System.nanoTime() - inicio;

        // 5. Remoção de todos, em ordem de atendimento.
        inicio = System.nanoTime();
        while (fila.tamanho() > 0) {
            fila.removerPrimeiro();
        }
        long remocao = System.nanoTime() - inicio;

        return montar(fila.nome(), cenario, n, insercao / 1e6, consultaNs, remocao / 1e6,
                alteracoes, alteracao / 1e3 / alteracoes);
    }

    private static Resultado montar(String estrutura, Cenario cenario, int n, double insercaoMs, double consultaNs,
                                    double remocaoMs, int alteracoes, double alteracaoUsPorOp) {
        double cicloMs = insercaoMs + remocaoMs;
        return new Resultado(estrutura, cenario, n, insercaoMs, consultaNs, remocaoMs, cicloMs,
                n / (cicloMs / 1000.0), alteracoes, alteracaoUsPorOp);
    }

    /**
     * Repete a medição com filas novas e devolve a mediana de cada tempo.
     * Uma medição só sofre com pausas do coletor de lixo; a mediana descarta esses picos.
     */
    public static Resultado medirMediana(Supplier<FilaAtendimento> estrutura, Cenario cenario, int n,
                                         long semente, int repeticoes) {
        double[] insercao = new double[repeticoes];
        double[] consulta = new double[repeticoes];
        double[] remocao = new double[repeticoes];
        double[] alteracao = new double[repeticoes];
        String nome = null;
        int alteracoes = 0;

        for (int i = 0; i < repeticoes; i++) {
            System.gc(); // começa cada repetição sem o lixo da anterior
            Resultado r = medir(estrutura.get(), cenario, n, semente);
            nome = r.estrutura();
            alteracoes = r.alteracoes();
            insercao[i] = r.insercaoMs();
            consulta[i] = r.consultaNs();
            remocao[i] = r.remocaoMs();
            alteracao[i] = r.alteracaoUsPorOp();
        }

        return montar(nome, cenario, n, mediana(insercao), mediana(consulta), mediana(remocao),
                alteracoes, mediana(alteracao));
    }

    static double mediana(double[] valores) {
        double[] ordenados = valores.clone();
        Arrays.sort(ordenados);
        int meio = ordenados.length / 2;

        return ordenados.length % 2 == 1
                ? ordenados[meio]
                : (ordenados[meio - 1] + ordenados[meio]) / 2;
    }

    /** CSV para o Excel em português: BOM UTF-8, ';' como separador e vírgula decimal. */
    static void gravarCsv(Path arquivo, List<Resultado> resultados) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("﻿" + CABECALHO_CSV);

        for (Resultado r : resultados) {
            linhas.add(String.format(PT_BR, "%s;%s;%d;%.4f;%.2f;%.4f;%.4f;%.1f;%d;%.4f",
                    r.estrutura(), r.cenario(), r.n(), r.insercaoMs(), r.consultaNs(), r.remocaoMs(),
                    r.cicloMs(), r.pacientesPorSegundo(), r.alteracoes(), r.alteracaoUsPorOp()));
        }

        if (arquivo.getParent() != null) {
            Files.createDirectories(arquivo.getParent());
        }
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        List<Supplier<FilaAtendimento>> estruturas = List.of(FilaHeap::new, FilaArvore::new, FilaListaOrdenada::new);

        // Aquecimento da JIT; resultado descartado.
        for (Supplier<FilaAtendimento> estrutura : estruturas) {
            medir(estrutura.get(), Cenario.EQUILIBRADO, 10_000, 1L);
        }

        List<Resultado> resultados = new ArrayList<>();

        System.out.printf("%-28s %-17s %10s %12s %12s %12s %12s %16s %14s%n",
                "Estrutura", "Cenário", "Pacientes", "Inserção ms", "Consulta ns",
                "Remoção ms", "Ciclo ms", "Pacientes/s", "Alteração µs");

        for (int n : TAMANHOS) {
            for (Cenario cenario : Cenario.values()) {
                for (Supplier<FilaAtendimento> estrutura : estruturas) {
                    FilaAtendimento fila = estrutura.get();

                    if (fila instanceof FilaListaOrdenada && n > LIMITE_LISTA_ORDENADA) {
                        System.out.printf("%-28s %-17s %10d   pulado (O(n²))%n", fila.nome(), cenario, n);
                        continue;
                    }

                    Resultado r = medirMediana(estrutura, cenario, n, SEMENTE, REPETICOES);
                    resultados.add(r);

                    System.out.printf(Locale.ROOT, "%-28s %-17s %10d %12.2f %12.1f %12.2f %12.2f %16.0f %14.3f%n",
                            r.estrutura(), r.cenario(), r.n(), r.insercaoMs(), r.consultaNs(),
                            r.remocaoMs(), r.cicloMs(), r.pacientesPorSegundo(), r.alteracaoUsPorOp());
                }
            }
        }

        gravarCsv(ARQUIVO_CSV, resultados);
        System.out.println();
        System.out.println("Resultados gravados em " + ARQUIVO_CSV.toAbsolutePath());
    }
}
