package br.com.triagem;

import java.util.List;
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
        System.out.println(paciente == null
                ? "Não há pacientes aguardando."
                : "Paciente chamado: " + paciente);
    }

    private static void buscar() {
        int codigo = lerInteiro("Código do paciente: ");
        Paciente paciente = triagem.buscarPaciente(codigo);
        System.out.println(paciente == null
                ? "Paciente não encontrado."
                : paciente);
    }

    private static void alterarPrioridade() {
        int codigo = lerInteiro("Código do paciente: ");
        mostrarPrioridades();
        int prioridade = lerInteiro("Nova prioridade (1 a 5): ");

        boolean alterado = triagem.alterarPrioridade(codigo, prioridade);
        System.out.println(alterado
                ? "Prioridade alterada com sucesso."
                : "Paciente não encontrado.");
    }

    private static void listarFila() {
        List<Paciente> pacientes = triagem.listarFilaOrdenada();

        if (pacientes.isEmpty()) {
            System.out.println("Fila vazia.");
            return;
        }

        System.out.println("--- FILA EM ORDEM DE ATENDIMENTO ---");
        int posicao = 1;
        for (Paciente paciente : pacientes) {
            System.out.println(posicao++ + "º - " + paciente);
        }
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
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }
}
