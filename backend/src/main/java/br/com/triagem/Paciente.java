package br.com.triagem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Paciente {
    private final int codigo;
    private String nome;
    private Prioridade prioridade;
    private final LocalDateTime horarioChegada;

    public Paciente(int codigo, String nome, Prioridade prioridade, LocalDateTime horarioChegada) {
        this.codigo = codigo;
        this.nome = nome;
        this.prioridade = prioridade;
        this.horarioChegada = horarioChegada;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public LocalDateTime getHorarioChegada() {
        return horarioChegada;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    @Override
    public String toString() {
        String hora = horarioChegada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return String.format("Código: %d | Nome: %s | Prioridade: %d - %s | Chegada: %s",
                codigo, nome, prioridade.getNivel(), prioridade.getDescricao(), hora);
    }
}
