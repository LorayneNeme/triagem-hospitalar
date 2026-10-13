package br.com.triagem;

public enum StatusPaciente {
    AGUARDANDO("Aguardando"),
    EM_ATENDIMENTO("Em atendimento");

    private final String descricao;

    StatusPaciente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
