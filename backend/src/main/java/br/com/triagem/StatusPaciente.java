package br.com.triagem;

/** Os mesmos três status do site (js/script.js). */
public enum StatusPaciente {
    AGUARDANDO_TRIAGEM("Aguardando triagem"),
    AGUARDANDO_ATENDIMENTO("Aguardando atendimento"),
    EM_ATENDIMENTO("Em atendimento");

    private final String descricao;

    StatusPaciente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
