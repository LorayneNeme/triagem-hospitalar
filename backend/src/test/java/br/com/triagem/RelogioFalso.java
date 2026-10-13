package br.com.triagem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/** Relógio controlado pelos testes: só anda quando o teste manda. */
class RelogioFalso implements Supplier<LocalDateTime> {
    private LocalDateTime agora;

    RelogioFalso(LocalDateTime inicio) {
        this.agora = inicio;
    }

    void avancar(Duration duracao) {
        agora = agora.plus(duracao);
    }

    @Override
    public LocalDateTime get() {
        return agora;
    }
}
