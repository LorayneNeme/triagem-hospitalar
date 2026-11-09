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

    /** Aceita o nível ("1") ou o nome da cor ("Vermelho", sem diferenciar maiúsculas), como o site envia. */
    public static Prioridade ler(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Informe a prioridade (1 a 5 ou o nome da cor).");
        }

        String valor = texto.trim();

        for (Prioridade prioridade : values()) {
            if (prioridade.descricao.equalsIgnoreCase(valor)) return prioridade;
        }

        try {
            return porNivel(Integer.parseInt(valor));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Prioridade inválida: " + valor + ".");
        }
    }
}
