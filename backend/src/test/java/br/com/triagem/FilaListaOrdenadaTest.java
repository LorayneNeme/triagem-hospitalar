package br.com.triagem;

class FilaListaOrdenadaTest extends FilaAtendimentoContratoTest {

    @Override
    protected FilaAtendimento criarFila() {
        return new FilaListaOrdenada();
    }
}
