package br.com.triagem;

import java.time.Duration;
import java.util.Map;

/**
 * Retrato das filas num instante.
 *
 * @param total                 todos os pacientes cadastrados na recepção
 * @param aguardandoTriagem     cadastrados e ainda sem cor
 * @param aguardandoAtendimento classificados, na fila de atendimento
 * @param atendidos             já chamados pelo médico
 * @param porCor                pacientes classificados de cada cor (aguardando + atendidos), com as 5 cores sempre presentes
 * @param tempoMedioEspera      média dos atendidos, da chegada ao atendimento; Duration.ZERO se ninguém foi atendido
 * @param maiorTempoEspera      maior espera entre os atendidos; Duration.ZERO se ninguém foi atendido
 */
public record EstatisticasFila(
        int total,
        int aguardandoTriagem,
        int aguardandoAtendimento,
        int atendidos,
        Map<Prioridade, Integer> porCor,
        Duration tempoMedioEspera,
        Duration maiorTempoEspera) {
}
