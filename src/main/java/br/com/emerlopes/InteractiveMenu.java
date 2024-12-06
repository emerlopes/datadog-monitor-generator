package br.com.emerlopes;

import br.com.emerlopes.service.SpecificMode;

import java.util.Scanner;

public class InteractiveMenu {
    private Scanner scanner = new Scanner(System.in);

    public void displayMenu() {
        System.out.println("\n=======================================================");
        System.out.println("                     DATADOG MONITOR");
        System.out.println("=======================================================\n");

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Modo 'gateway'");
            System.out.println("2. Modo 'specific'");
            System.out.println("3. Sair");

            System.out.print("Digite sua opção: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    handleGatewayMode();
                    break;
                case "2":
                    handleSpecificMode();
                    break;
                case "3":
                    System.out.println("Saindo...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        }
    }

    private void handleGatewayMode() {
        System.out.println("Modo 'gateway' selecionado.");
        // Implemente a lógica para o modo gateway
    }

    private void handleSpecificMode() {
        SpecificMode specificMode = new SpecificMode();
        specificMode.handleSpecificMode();
    }

    public static void main(String[] args) {
        InteractiveMenu menu = new InteractiveMenu();
        menu.displayMenu();
    }
}
