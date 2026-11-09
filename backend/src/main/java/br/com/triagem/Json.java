package br.com.triagem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON mínimo, escrito à mão para o projeto não depender de bibliotecas.
 *
 * Escrita: Map → objeto, List → array, String, Number, Boolean e null.
 * Leitura: objeto → LinkedHashMap, array → ArrayList, inteiro → Long, decimal → Double,
 * além de String, Boolean e null. JSON mal formado gera IllegalArgumentException.
 */
public final class Json {

    private Json() {
    }

    // ---------- Escrita ----------

    public static String escrever(Object valor) {
        StringBuilder saida = new StringBuilder();
        escrever(valor, saida);
        return saida.toString();
    }

    private static void escrever(Object valor, StringBuilder saida) {
        if (valor == null) {
            saida.append("null");
        } else if (valor instanceof String texto) {
            escreverTexto(texto, saida);
        } else if (valor instanceof Number || valor instanceof Boolean) {
            saida.append(valor);
        } else if (valor instanceof Map<?, ?> mapa) {
            saida.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> item : mapa.entrySet()) {
                if (!primeiro) saida.append(',');
                primeiro = false;
                escreverTexto(String.valueOf(item.getKey()), saida);
                saida.append(':');
                escrever(item.getValue(), saida);
            }
            saida.append('}');
        } else if (valor instanceof Iterable<?> lista) {
            saida.append('[');
            boolean primeiro = true;
            for (Object item : lista) {
                if (!primeiro) saida.append(',');
                primeiro = false;
                escrever(item, saida);
            }
            saida.append(']');
        } else {
            escreverTexto(valor.toString(), saida);
        }
    }

    private static void escreverTexto(String texto, StringBuilder saida) {
        saida.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"' -> saida.append("\\\"");
                case '\\' -> saida.append("\\\\");
                case '\n' -> saida.append("\\n");
                case '\r' -> saida.append("\\r");
                case '\t' -> saida.append("\\t");
                case '\b' -> saida.append("\\b");
                case '\f' -> saida.append("\\f");
                default -> {
                    if (c < 0x20) {
                        saida.append(String.format("\\u%04x", (int) c));
                    } else {
                        saida.append(c);
                    }
                }
            }
        }
        saida.append('"');
    }

    // ---------- Leitura ----------

    public static Object ler(String json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON vazio.");
        }

        Leitor leitor = new Leitor(json);
        leitor.pularEspacos();
        Object valor = leitor.lerValor();
        leitor.pularEspacos();

        if (!leitor.fim()) {
            throw leitor.erro("conteúdo depois do fim do JSON");
        }

        return valor;
    }

    /** Analisador descendente recursivo: um método por tipo de valor. */
    private static final class Leitor {
        private final String texto;
        private int pos;

        Leitor(String texto) {
            this.texto = texto;
        }

        boolean fim() {
            return pos >= texto.length();
        }

        IllegalArgumentException erro(String motivo) {
            return new IllegalArgumentException("JSON inválido na posição " + pos + ": " + motivo + ".");
        }

        void pularEspacos() {
            while (!fim()) {
                char c = texto.charAt(pos);
                if (c != ' ' && c != '\t' && c != '\n' && c != '\r') break;
                pos++;
            }
        }

        char atual() {
            if (fim()) throw erro("fim inesperado");
            return texto.charAt(pos);
        }

        void esperar(char c) {
            if (atual() != c) throw erro("esperava '" + c + "'");
            pos++;
        }

        Object lerValor() {
            char c = atual();
            return switch (c) {
                case '{' -> lerObjeto();
                case '[' -> lerLista();
                case '"' -> lerTexto();
                case 't' -> lerPalavra("true", Boolean.TRUE);
                case 'f' -> lerPalavra("false", Boolean.FALSE);
                case 'n' -> lerPalavra("null", null);
                default -> {
                    if (c == '-' || (c >= '0' && c <= '9')) yield lerNumero();
                    throw erro("valor inesperado '" + c + "'");
                }
            };
        }

        Map<String, Object> lerObjeto() {
            Map<String, Object> obj = new LinkedHashMap<>();
            esperar('{');
            pularEspacos();

            if (atual() == '}') {
                pos++;
                return obj;
            }

            while (true) {
                pularEspacos();
                if (atual() != '"') throw erro("esperava o nome do campo entre aspas");
                String chave = lerTexto();
                pularEspacos();
                esperar(':');
                pularEspacos();
                obj.put(chave, lerValor());
                pularEspacos();

                if (atual() == ',') {
                    pos++;
                } else {
                    esperar('}');
                    return obj;
                }
            }
        }

        List<Object> lerLista() {
            List<Object> lista = new ArrayList<>();
            esperar('[');
            pularEspacos();

            if (atual() == ']') {
                pos++;
                return lista;
            }

            while (true) {
                pularEspacos();
                lista.add(lerValor());
                pularEspacos();

                if (atual() == ',') {
                    pos++;
                } else {
                    esperar(']');
                    return lista;
                }
            }
        }

        String lerTexto() {
            esperar('"');
            StringBuilder saida = new StringBuilder();

            while (true) {
                char c = atual();
                pos++;

                if (c == '"') return saida.toString();
                if (c < 0x20) throw erro("caractere de controle dentro do texto");

                if (c != '\\') {
                    saida.append(c);
                    continue;
                }

                char escape = atual();
                pos++;
                switch (escape) {
                    case '"' -> saida.append('"');
                    case '\\' -> saida.append('\\');
                    case '/' -> saida.append('/');
                    case 'b' -> saida.append('\b');
                    case 'f' -> saida.append('\f');
                    case 'n' -> saida.append('\n');
                    case 'r' -> saida.append('\r');
                    case 't' -> saida.append('\t');
                    case 'u' -> {
                        if (pos + 4 > texto.length()) throw erro("\\u incompleto");
                        try {
                            saida.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException e) {
                            throw erro("\\u com dígitos inválidos");
                        }
                        pos += 4;
                    }
                    default -> throw erro("escape inválido \\" + escape);
                }
            }
        }

        Object lerPalavra(String palavra, Object valor) {
            if (!texto.startsWith(palavra, pos)) throw erro("esperava " + palavra);
            pos += palavra.length();
            return valor;
        }

        Number lerNumero() {
            int inicio = pos;
            if (atual() == '-') pos++;

            // Parte inteira: "0" sozinho ou dígitos sem zero à esquerda.
            if (atual() == '0') {
                pos++;
            } else {
                lerDigitos();
            }

            boolean decimal = false;

            if (!fim() && texto.charAt(pos) == '.') {
                decimal = true;
                pos++;
                lerDigitos();
            }

            if (!fim() && (texto.charAt(pos) == 'e' || texto.charAt(pos) == 'E')) {
                decimal = true;
                pos++;
                if (!fim() && (texto.charAt(pos) == '+' || texto.charAt(pos) == '-')) pos++;
                lerDigitos();
            }

            String numero = texto.substring(inicio, pos);

            if (decimal) {
                return Double.parseDouble(numero);
            }

            try {
                return Long.parseLong(numero);
            } catch (NumberFormatException e) {
                throw erro("número inteiro grande demais");
            }
        }

        private void lerDigitos() {
            int inicio = pos;
            while (!fim() && texto.charAt(pos) >= '0' && texto.charAt(pos) <= '9') pos++;
            if (pos == inicio) throw erro("esperava dígito");
        }
    }
}
