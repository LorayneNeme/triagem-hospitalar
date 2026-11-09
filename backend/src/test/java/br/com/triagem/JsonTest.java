package br.com.triagem;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonTest {

    @Test
    void escreveObjetosListasETiposSimples() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("nome", "Ana \"Bia\"\n\\");
        obj.put("idade", 30);
        obj.put("ativo", true);
        obj.put("sala", null);
        obj.put("cores", List.of("Vermelho", 1.5));
        obj.put("vazio", Map.of());

        assertEquals("{\"nome\":\"Ana \\\"Bia\\\"\\n\\\\\",\"idade\":30,\"ativo\":true,\"sala\":null,"
                + "\"cores\":[\"Vermelho\",1.5],\"vazio\":{}}", Json.escrever(obj));
    }

    @Test
    void escreveCaracteresDeControleComoUnicode() {
        assertEquals("\"a\\u0001b\\tc\"", Json.escrever("a\u0001b\tc"));
        assertEquals("\"João\"", Json.escrever("João")); // acentos ficam como estão (UTF-8)
    }

    @Test
    void leObjetoComTodosOsTipos() {
        Object lido = Json.ler(" { \"nome\" : \"Jo\\u00e3o \\\"J\\\"\", \"idade\": 30, \"peso\": -72.5e0,"
                + " \"ok\": false, \"nada\": null, \"lista\": [1, \"dois\", [ ], {}] } ");

        @SuppressWarnings("unchecked")
        Map<String, Object> obj = (Map<String, Object>) lido;
        assertEquals("João \"J\"", obj.get("nome"));
        assertEquals(30L, obj.get("idade"));
        assertEquals(-72.5, obj.get("peso"));
        assertEquals(false, obj.get("ok"));
        assertNull(obj.get("nada"));
        assertEquals(Arrays.asList(1L, "dois", new ArrayList<>(), new LinkedHashMap<>()), obj.get("lista"));
    }

    @Test
    void idaEVoltaPreservaOConteudo() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("texto", "linha 1\r\nlinha 2 \u00e9 / \t fim");
        obj.put("numeros", List.of(0L, -3L, 2.25));
        assertEquals(obj, Json.ler(Json.escrever(obj)));
    }

    @Test
    void recusaJsonMalFormado() {
        for (String ruim : new String[]{"", "{", "{\"a\":}", "{\"a\" 1}", "[1,]", "{\"a\":1,}", "\"sem fim",
                "{} lixo", "tru", "{'a':1}", "01", "\"\\x\""}) {
            assertThrows(IllegalArgumentException.class, () -> Json.ler(ruim), ruim);
        }
    }
}
