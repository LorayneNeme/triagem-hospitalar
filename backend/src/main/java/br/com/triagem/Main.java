package br.com.triagem;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TriagemService triagem = new TriagemService();

    public static void main(String[] args) {
        int opcao;

        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1 -> cadastrar();
                    case 2 -> consultarProximo();
                    case 3 -> chamarProximo();
                    case 4 -> buscar();
                    case 5 -> alterarPrioridade();
                    case 6 -> listarFila();
                    case 7 -> mostrarEstatisticas();
                    case 0 -> System.out.println("Sistema encerrado.");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }

            System.out.println();
        } while (opcao != 0);
    }

    private static void exibirMenu() {
        System.out.println("======================================");
        System.out.println("     SISTEMA DE TRIAGEM HOSPITALAR");
        System.out.println("======================================");
        System.out.println("1 - Cadastrar paciente");
        System.out.println("2 - Consultar próximo paciente");
        System.out.println("3 - Chamar próximo paciente");
        System.out.println("4 - Buscar paciente por código");
        System.out.println("5 - Alterar prioridade");
        System.out.println("6 - Consultar fila");
        System.out.println("7 - Estatísticas");
        System.out.println("0 - Sair");
    }

    private static void cadastrar() {
        int codigo = lerInteiro("Código: ");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        int idade = lerInteiro("Idade: ");
        mostrarPrioridades();
        int prioridade = lerInteiro("Prioridade (1 a 5): ");

        Paciente paciente = triagem.cadastrarPaciente(codigo, nome, idade, prioridade);
        System.out.println("Paciente cadastrado: " + paciente);
    }

    private static void consultarProximo() {
        Paciente paciente = triagem.consultarProximo();
        System.out.println(paciente == null
                ? "Não há pacientes aguardando."
                : "Próximo paciente: " + paciente);
    }

    private static void chamarProximo() {
        Paciente paciente = triagem.chamarProximo();

        if (paciente == null) {
            System.out.println("Não há pacientes aguardando.");
            return;
        }

        System.out.println("Próximo paciente:");
        System.out.println(paciente.getCodigoFormatado() + " - " + paciente.getNome());
        System.out.println("Prioridade: " + paciente.getPrioridade().getDescricao());
        System.out.println("Status: " + StatusPaciente.AGUARDANDO.getDescricao()
                + " → " + paciente.getStatus().getDescricao());
        System.out.println("Tempo de espera: " + formatarMinutos(paciente.getTempoEspera()));
    }

    private static void buscar() {
        int codigo = lerInteiro("Código do paciente: ");
        Paciente paciente = triagem.buscarPaciente(codigo);

        if (paciente == null) {
            System.out.println("Paciente não encontrado.");
            return;
        }

        System.out.println("Paciente: " + paciente.getCodigoFormatado() + " - " + paciente.getNome());
        System.out.println("Prioridade: " + paciente.getPrioridade().getDescricao());

        if (paciente.getStatus() == StatusPaciente.AGUARDANDO) {
            System.out.println("Posição na fila: " + triagem.posicaoNaFila(codigo));
        } else {
            System.out.println("Tempo de espera: " + formatarMinutos(paciente.getTempoEspera()));
        }

        System.out.println("Status: " + paciente.getStatus().getDescricao());
    }

    private static void alterarPrioridade() {
        int codigo = lerInteiro("Código do paciente: ");
        mostrarPrioridades();
        int prioridade = lerInteiro("Nova prioridade (1 a 5): ");

        boolean alterado = triagem.alterarPrioridade(codigo, prioridade);
        System.out.println(alterado
                ? "Prioridade alterada com sucesso."
                : "Paciente não encontrado ou já chamado para atendimento.");
    }

    private static void listarFila() {
        List<Paciente> pacientes = triagem.listarFilaOrdenada();

        if (pacientes.isEmpty()) {
            System.out.println("Fila vazia.");
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
        System.out.println("Aguardando: " + estatisticas.aguardando());
        System.out.println("Atendidos: " + estatisticas.atendidos());

        for (Map.Entry<Prioridade, Integer> cor : estatisticas.porCor().entrySet()) {
            System.out.println("  " + cor.getKey().getDescricao() + ": " + cor.getValue());
        }

        System.out.println("Tempo médio de espera: " + formatarMinutos(estatisticas.tempoMedioEspera()));
        System.out.println("Maior tempo de espera: " + formatarMinutos(estatisticas.maiorTempoEspera()));
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

    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine();

            try {
                return Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }
}
