package br.com.triagem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TriagemService triagem = new TriagemService();
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) {
        int opcao;

        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> listarFilaTriagem();
                    case 3 -> classificar();
                    case 4 -> consultarProximo();
                    case 5 -> chamarProximo();
                    case 6 -> buscar();
                    case 7 -> alterarPrioridade();
                    case 8 -> listarFila();
                    case 9 -> mostrarEstatisticas();
                    case 0 -> System.out.println("Sistema encerrado.");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erro: " + e.getMessage());
            }

            System.out.println();
        } while (opcao != 0);
    }

    private static void exibirMenu() {
        System.out.println("======================================");
        System.out.println("     SISTEMA DE TRIAGEM HOSPITALAR");
        System.out.println("======================================");
        System.out.println("--- Recepção e triagem ---");
        System.out.println("1 - Cadastrar paciente");
        System.out.println("2 - Consultar fila de triagem");
        System.out.println("3 - Classificar paciente (triagem)");
        System.out.println("--- Atendimento ---");
        System.out.println("4 - Consultar próximo paciente");
        System.out.println("5 - Chamar próximo paciente");
        System.out.println("6 - Buscar paciente por código");
        System.out.println("7 - Alterar prioridade");
        System.out.println("8 - Consultar fila de atendimento");
        System.out.println("9 - Estatísticas");
        System.out.println("0 - Sair");
    }

    private static void cadastrar() {
        String nome = lerTexto("Nome: ");
        int idade = lerInteiro("Idade: ");
        String telefone = lerTexto("Telefone (opcional): ");
        String endereco = lerTexto("Endereço (opcional): ");
        String queixa = lerTexto("Queixa principal (opcional): ");

        Paciente paciente = triagem.cadastrarPaciente(nome, idade, telefone, endereco, queixa);
        System.out.println("Paciente cadastrado: " + paciente.getCodigoFormatado() + " - " + paciente.getNome());
        System.out.println("Status: " + paciente.getStatus().getDescricao()
                + " (posição " + triagem.posicaoNaFilaTriagem(paciente.getCodigo()) + " na triagem)");
    }

    private static void listarFilaTriagem() {
        List<Paciente> pacientes = triagem.listarFilaTriagem();

        if (pacientes.isEmpty()) {
            System.out.println("Ninguém aguardando triagem.");
            return;
        }

        System.out.println("--- FILA DE TRIAGEM (ordem de chegada) ---");
        int posicao = 1;
        for (Paciente paciente : pacientes) {
            System.out.println(posicao++ + ". " + paciente.getCodigoFormatado() + " - " + paciente.getNome()
                    + " | chegada " + hora(paciente.getHorarioChegada())
                    + (paciente.getQueixa().isEmpty() ? "" : " | " + paciente.getQueixa()));
        }
    }

    private static void classificar() {
        Paciente proximo = triagem.consultarProximoTriagem();

        if (proximo == null) {
            System.out.println("Ninguém aguardando triagem.");
            return;
        }

        System.out.println("Próximo da triagem: " + proximo.getCodigoFormatado() + " - " + proximo.getNome());
        String entrada = lerTexto("Código do paciente (Enter para o próximo): ");
        int codigo = entrada.isEmpty() ? proximo.getCodigo() : Paciente.lerCodigo(entrada);

        mostrarPrioridades();
        int prioridade = lerInteiro("Prioridade (1 a 5): ");
        String especialidade = lerTexto("Especialidade (opcional): ");
        String sala = lerTexto("Sala (opcional): ");
        String observacao = lerTexto("Observação (opcional): ");

        Paciente paciente = triagem.classificar(codigo, prioridade, especialidade, sala, observacao);
        System.out.println(paciente.getCodigoFormatado() + " classificado como "
                + paciente.getPrioridade().getDescricao() + ". Posição na fila de atendimento: "
                + triagem.posicaoNaFila(codigo));
    }

    private static void consultarProximo() {
        Paciente paciente = triagem.consultarProximo();
        System.out.println(paciente == null
                ? "Não há pacientes aguardando atendimento."
                : "Próximo paciente: " + paciente);
    }

    private static void chamarProximo() {
        Paciente paciente = triagem.chamarProximo();

        if (paciente == null) {
            System.out.println("Não há pacientes aguardando atendimento.");
            return;
        }

        System.out.println("Próximo paciente:");
        System.out.println(paciente.getCodigoFormatado() + " - " + paciente.getNome());
        System.out.println("Prioridade: " + paciente.getPrioridade().getDescricao());
        System.out.println("Status: " + StatusPaciente.AGUARDANDO_ATENDIMENTO.getDescricao()
                + " → " + paciente.getStatus().getDescricao());
        mostrarHorarios(paciente);
    }

    private static void buscar() {
        int codigo = lerCodigo("Código do paciente (ex.: P025): ");
        Paciente paciente = triagem.buscarPaciente(codigo);

        if (paciente == null) {
            System.out.println("Paciente não encontrado.");
            return;
        }

        System.out.println("Paciente: " + paciente.getCodigoFormatado() + " - " + paciente.getNome());
        System.out.println("Status: " + paciente.getStatus().getDescricao());

        switch (paciente.getStatus()) {
            case AGUARDANDO_TRIAGEM -> {
                int posicao = triagem.posicaoNaFilaTriagem(codigo);
                System.out.println(posicao > 0 ? "Posição na fila de triagem: " + posicao : "Em triagem agora.");
                System.out.println("Chegada: " + hora(paciente.getHorarioChegada()));
            }
            case AGUARDANDO_ATENDIMENTO -> {
                System.out.println("Prioridade: " + paciente.getPrioridade().getDescricao());
                System.out.println("Posição na fila: " + triagem.posicaoNaFila(codigo));
                System.out.println("Chegada: " + hora(paciente.getHorarioChegada()));
            }
            case EM_ATENDIMENTO -> {
                System.out.println("Prioridade: " + paciente.getPrioridade().getDescricao());
                mostrarHorarios(paciente);
            }
        }
    }

    private static void alterarPrioridade() {
        int codigo = lerCodigo("Código do paciente (ex.: P025): ");
        Paciente paciente = triagem.buscarPaciente(codigo);

        if (paciente == null || paciente.getStatus() != StatusPaciente.AGUARDANDO_ATENDIMENTO) {
            System.out.println(paciente == null
                    ? "Paciente não encontrado."
                    : "Só dá para alterar quem está na fila de atendimento (status: "
                    + paciente.getStatus().getDescricao() + ").");
            return;
        }

        Prioridade anterior = paciente.getPrioridade();
        System.out.println("Paciente: " + paciente.getCodigoFormatado() + " - " + paciente.getNome());
        System.out.println("Prioridade atual: " + anterior.getNivel() + " - " + anterior.getDescricao());
        mostrarPrioridades();
        int nova = lerInteiro("Nova prioridade (1 a 5): ");

        triagem.alterarPrioridade(codigo, nova);
        System.out.println("Prioridade alterada: " + anterior.getDescricao() + " → "
                + paciente.getPrioridade().getDescricao() + ". Nova posição: " + triagem.posicaoNaFila(codigo));
    }

    private static void listarFila() {
        List<Paciente> pacientes = triagem.listarFilaOrdenada();

        if (pacientes.isEmpty()) {
            System.out.println("Fila de atendimento vazia.");
            return;
        }

        System.out.println("--- FILA DE ATENDIMENTO ---");
        int posicao = 1;
        for (Paciente paciente : pacientes) {
            System.out.println(posicao++ + ". " + paciente.getCodigoFormatado() + " "
                    + paciente.getPrioridade().getDescricao() + " - " + paciente.getNome());
        }
    }

    private static void mostrarEstatisticas() {
        EstatisticasFila estatisticas = triagem.gerarEstatisticas();

        System.out.println("--- ESTATÍSTICAS ---");
        System.out.println("Total de pacientes: " + estatisticas.total());
        System.out.println("Aguardando triagem: " + estatisticas.aguardandoTriagem());
        System.out.println("Aguardando atendimento: " + estatisticas.aguardandoAtendimento());
        System.out.println("Atendidos: " + estatisticas.atendidos());
        System.out.println("Classificados por cor:");

        for (Map.Entry<Prioridade, Integer> cor : estatisticas.porCor().entrySet()) {
            System.out.println("  " + cor.getKey().getDescricao() + ": " + cor.getValue());
        }

        System.out.println("Tempo médio de espera: " + formatarMinutos(estatisticas.tempoMedioEspera()));
        System.out.println("Maior tempo de espera: " + formatarMinutos(estatisticas.maiorTempoEspera()));
    }

    /** Spec 4.8: chegada, início do atendimento e tempo de espera. */
    private static void mostrarHorarios(Paciente paciente) {
        System.out.println("Chegada: " + hora(paciente.getHorarioChegada()));
        System.out.println("Atendimento: " + hora(paciente.getHorarioAtendimento()));
        System.out.println("Tempo de espera: " + formatarMinutos(paciente.getTempoEspera()));
    }

    private static String hora(LocalDateTime horario) {
        return horario.format(HORA);
    }

    private static String formatarMinutos(Duration duracao) {
        return duracao.toMinutes() + " minutos";
    }

    private static void mostrarPrioridades() {
        System.out.println("1 - Vermelho (maior prioridade)");
        System.out.println("2 - Laranja");
        System.out.println("3 - Amarelo");
        System.out.println("4 - Verde");
        System.out.println("5 - Azul (menor prioridade)");
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    /** Aceita "P025", "p25" ou "25". */
    private static int lerCodigo(String mensagem) {
        while (true) {
            try {
                return Paciente.lerCodigo(lerTexto(mensagem));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static int lerInteiro(String mensagem) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(mensagem));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }
}
