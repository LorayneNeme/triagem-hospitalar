package br.com.triagem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Paciente {
    private final int codigo;
    private String nome;
    private final int idade;
    private Prioridade prioridade;
    private final LocalDateTime horarioChegada;
    private final long ordemChegada;
    private StatusPaciente status;
    private LocalDateTime horarioAtendimento;

    public Paciente(int codigo, String nome, int idade, Prioridade prioridade,
                    LocalDateTime horarioChegada, long ordemChegada) {
        this.codigo = codigo;
        this.nome = nome;
        this.idade = idade;
        this.prioridade = prioridade;
        this.horarioChegada = horarioChegada;
        this.ordemChegada = ordemChegada;
        this.status = StatusPaciente.AGUARDANDO;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getCodigoFormatado() {
        return String.format("P%03d", codigo);
    }

    public String getNome() {
        return nome;
    }

    public int getIdade() {
        return idade;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public LocalDateTime getHorarioChegada() {
        return horarioChegada;
    }

    public long getOrdemChegada() {
        return ordemChegada;
    }

    public StatusPaciente getStatus() {
        return status;
    }

    public LocalDateTime getHorarioAtendimento() {
        return horarioAtendimento;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    @Override
    public String toString() {
        String hora = horarioChegada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return String.format("Código: %s | Nome: %s | Idade: %d | Prioridade: %d - %s | Chegada: %s | Status: %s",
                getCodigoFormatado(), nome, idade, prioridade.getNivel(), prioridade.getDescricao(), hora,
                status.getDescricao());
    }
}
