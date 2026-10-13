package br.com.triagem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Mede as três estruturas de FilaAtendimento com 100 a 1.000.000 de pacientes
 * em quatro cenários e grava o resultado em resultados/benchmark.csv.
 *
 * Executar: java -cp target/classes br.com.triagem.Benchmark
 */
public class Benchmark {

    public record Resultado(
            String estrutura,
            Cenario cenario,
            int n,
            double insercaoMs,
            double consultaNs,
            double alteracaoMs,
            double remocaoMs,
            double totalMs,
            double pacientesPorSegundo) {
    }

    private static final int[] TAMANHOS = {100, 1_000, 10_000, 100_000, 1_000_000};
    private static final int LIMITE_LISTA_ORDENADA = 100_000;
    private static final int CONSULTAS = 1_000;
    private static final int MAX_ALTERACOES = 1_000;
    private static final long SEMENTE = 42L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 13, 8, 0);
    private static final Path ARQUIVO_CSV = Path.of("resultados", "benchmark.csv");

    /** Impede a JIT de descartar as consultas por não usarem o resultado. */
    private static volatile int sumidouro;

    public static Resultado medir(FilaAtendimento fila, Cenario cenario, int n, long semente) {
        Random random = new Random(semente);

        // 1. Pacientes gerados antes de cronometrar.
        Paciente[] pacientes = new Paciente[n];
        for (int i = 0; i < n; i++) {
            pacientes[i] = new Paciente(i + 1, "Paciente " + (i + 1), 30,
                    cenario.sortear(random), INICIO.plusSeconds(i), i);
        }

        // 2. Inserção.
        long inicio = System.nanoTime();
        for (Paciente paciente : pacientes) {
            fila.inserir(paciente);
        }
        long insercao = System.nanoTime() - inicio;

        // 3. Consulta do próximo.
        inicio = System.nanoTime();
        for (int i = 0; i < CONSULTAS; i++) {
            sumidouro += fila.consultarPrimeiro().getCodigo();
        }
        double consultaNs = (System.nanoTime() - inicio) / (double) CONSULTAS;

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

        double insercaoMs = insercao / 1e6;
        double alteracaoMs = alteracao / 1e6;
        double remocaoMs = remocao / 1e6;
        double totalMs = insercaoMs + alteracaoMs + remocaoMs;
        double pacientesPorSegundo = n / (totalMs / 1000.0);

        return new Resultado(fila.nome(), cenario, n, insercaoMs, consultaNs,
                alteracaoMs, remocaoMs, totalMs, pacientesPorSegundo);
    }

    public static void main(String[] args) throws IOException {
        List<Supplier<FilaAtendimento>> estruturas = List.of(FilaHeap::new, FilaArvore::new, FilaListaOrdenada::new);

        // Aquecimento da JIT; resultado descartado.
        for (Supplier<FilaAtendimento> estrutura : estruturas) {
            medir(estrutura.get(), Cenario.EQUILIBRADO, 10_000, 1L);
        }

        List<String> linhasCsv = new ArrayList<>();
        linhasCsv.add("estrutura,cenario,n,insercao_ms,consulta_ns,alteracao_ms,remocao_ms,total_ms,pacientes_por_segundo");

        System.out.printf("%-28s %-17s %10s %12s %12s %13s %12s %12s %16s%n",
                "Estrutura", "Cenário", "Pacientes", "Inserção ms", "Consulta ns",
                "Alteração ms", "Remoção ms", "Total ms", "Pacientes/s");

        for (int n : TAMANHOS) {
            for (Cenario cenario : Cenario.values()) {
                for (Supplier<FilaAtendimento> estrutura : estruturas) {
                    FilaAtendimento fila = estrutura.get();

                    if (fila instanceof FilaListaOrdenada && n > LIMITE_LISTA_ORDENADA) {
                        System.out.printf("%-28s %-17s %10d   pulado (O(n²))%n", fila.nome(), cenario, n);
                        continue;
                    }

                    Resultado r = medir(fila, cenario, n, SEMENTE);

                    System.out.printf(Locale.ROOT, "%-28s %-17s %10d %12.2f %12.1f %13.2f %12.2f %12.2f %16.0f%n",
                            r.estrutura(), r.cenario(), r.n(), r.insercaoMs(), r.consultaNs(),
                            r.alteracaoMs(), r.remocaoMs(), r.totalMs(), r.pacientesPorSegundo());

                    linhasCsv.add(String.format(Locale.ROOT, "%s,%s,%d,%.4f,%.2f,%.4f,%.4f,%.4f,%.1f",
                            r.estrutura(), r.cenario(), r.n(), r.insercaoMs(), r.consultaNs(),
                            r.alteracaoMs(), r.remocaoMs(), r.totalMs(), r.pacientesPorSegundo()));
                }
            }
        }

        Files.createDirectories(ARQUIVO_CSV.getParent());
        Files.write(ARQUIVO_CSV, linhasCsv, StandardCharsets.UTF_8);
        System.out.println();
        System.out.println("Resultados gravados em " + ARQUIVO_CSV.toAbsolutePath());
    }
}
