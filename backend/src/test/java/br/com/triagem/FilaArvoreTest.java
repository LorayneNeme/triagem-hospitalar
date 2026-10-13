package br.com.triagem;

class FilaArvoreTest extends FilaAtendimentoContratoTest {

    @Override
    protected FilaAtendimento criarFila() {
        return new FilaArvore();
    }
}
