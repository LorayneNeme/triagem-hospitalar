package br.com.triagem;

class FilaHeapTest extends FilaAtendimentoContratoTest {

    @Override
    protected FilaAtendimento criarFila() {
        return new FilaHeap();
    }
}
