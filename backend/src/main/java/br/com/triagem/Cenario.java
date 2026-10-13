package br.com.triagem;

import java.util.Random;

/** Distribuições de prioridade usadas no benchmark (spec, seção 6). */
public enum Cenario {
    EQUILIBRADO(null),
    MAIORIA_AZUL(Prioridade.AZUL),
    MAIORIA_VERMELHO(Prioridade.VERMELHO),
    MESMA_PRIORIDADE(Prioridade.AMARELO);

    private static final double CHANCE_DA_MAIORIA = 0.70;

    private final Prioridade predominante;

    Cenario(Prioridade predominante) {
        this.predominante = predominante;
    }

    public Prioridade sortear(Random random) {
        Prioridade[] cores = Prioridade.values();

        if (this == EQUILIBRADO) {
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
