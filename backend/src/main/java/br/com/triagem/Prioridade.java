package br.com.triagem;

public enum Prioridade {
    VERMELHO(1, "Vermelho"),
    LARANJA(2, "Laranja"),
    AMARELO(3, "Amarelo"),
    VERDE(4, "Verde"),
    AZUL(5, "Azul");

    private final int nivel;
    private final String descricao;

    Prioridade(int nivel, String descricao) {
        this.nivel = nivel;
        this.descricao = descricao;
    }

    public int getNivel() {
        return nivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Prioridade porNivel(int nivel) {
        for (Prioridade prioridade : values()) {
            if (prioridade.nivel == nivel) return prioridade;
        }
        throw new IllegalArgumentException("Prioridade deve estar entre 1 e 5.");
    }
}
