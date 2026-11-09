package br.com.triagem;

import java.util.Random;

/** Cenários do benchmark (spec, seção 6). */
public enum Cenario {
    EQUILIBRADO(null, false),
    MAIORIA_AZUL(Prioridade.AZUL, false),
    MAIORIA_VERMELHO(Prioridade.VERMELHO, false),
    MESMA_PRIORIDADE(Prioridade.AMARELO, false),
    /** Cores como no EQUILIBRADO, mas os pacientes entram na fila em ordem embaralhada. */
    CHEGADA_ALEATORIA(null, true);

    private static final double CHANCE_DA_MAIORIA = 0.70;

    private final Prioridade predominante;
    private final boolean embaralhaChegada;

    Cenario(Prioridade predominante, boolean embaralhaChegada) {
        this.predominante = predominante;
        this.embaralhaChegada = embaralhaChegada;
    }

    public boolean embaralhaChegada() {
        return embaralhaChegada;
    }

    public Prioridade sortear(Random random) {
        Prioridade[] cores = Prioridade.values();

        if (predominante == null) {
            return cores[random.nextInt(cores.length)];
        }

        if (this == MESMA_PRIORIDADE || random.nextDouble() < CHANCE_DA_MAIORIA) {
            return predominante;
        }

        // Os 30% restantes divididos igualmente entre as outras quatro cores.
        Prioridade outra;
        do {
            outra = cores[random.nextInt(cores.length)];
        } while (outra == predominante);

        return outra;
    }
}
