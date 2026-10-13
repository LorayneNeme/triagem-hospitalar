package br.com.triagem;

import java.time.Duration;
import java.util.Map;

/**
 * Retrato da fila num instante.
 *
 * @param porCor           pacientes de cada cor (aguardando + atendidos), com as 5 cores sempre presentes
 * @param tempoMedioEspera média dos atendidos; Duration.ZERO se ninguém foi atendido
 * @param maiorTempoEspera maior espera entre os atendidos; Duration.ZERO se ninguém foi atendido
 */
public record EstatisticasFila(
        int total,
        int aguardando,
        int atendidos,
        Map<Prioridade, Integer> porCor,
        Duration tempoMedioEspera,
        Duration maiorTempoEspera) {
}
