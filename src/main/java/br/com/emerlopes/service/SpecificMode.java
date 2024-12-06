package br.com.emerlopes.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class SpecificMode {
    private Scanner scanner = new Scanner(System.in);
    private String appName;
    private String sigla;
    private String serviceName;
    private String route;  // Adicionar a rota como uma variável de classe
    private int warningErrors = 5;
    private int criticalErrors = 10;
    private int warningRequests = 5;
    private int criticalRequests = 10;
    private int warningLatency = 5;
    private int criticalLatency = 10;

    public void handleSpecificMode() {
        while (true) {
            System.out.println("Menu de criação de monitores padrão:");
            System.out.println("1. Criar monitores padrão");
            System.out.println("2. Sair");

            System.out.print("Digite sua opção: ");
            String input = scanner.nextLine().trim();
            if ("1".equals(input)) {
                collectApplicationDetails();
                collectRouteDetails();
                createStandardMonitors();
            } else if ("2".equals(input)) {
                System.out.println("Saindo do programa...");
                break;
            } else {
                System.out.println("Opção inválida, por favor tente novamente.");
            }
        }
    }

    private void collectApplicationDetails() {
        System.out.println("Informe a sigla da aplicação:");
        sigla = scanner.nextLine();

        System.out.println("Informe o nome da aplicação:");
        appName = scanner.nextLine();

        System.out.println("Informe o nome do serviço no Datadog:");
        serviceName = scanner.nextLine();
    }

    private void collectRouteDetails() {
        System.out.println("Informe a rota para a qual os monitores serão criados:");
        route = scanner.nextLine();
    }

    private void createStandardMonitors() {
        System.out.println("Criando monitores padrão para erros, latência e requisições para a rota: " + route);
        try {
            Path directory = Paths.get("monitors");
            Files.createDirectories(directory);

            generateStandardMonitor(directory, "erros", warningErrors, criticalErrors);
            generateStandardMonitor(directory, "requisicoes", warningRequests, criticalRequests);
            generateStandardMonitor(directory, "latencia", warningLatency, criticalLatency);

            System.out.println("Monitores padrão criados com sucesso.");
        } catch (IOException e) {
            System.err.println("Erro ao criar monitores: " + e.getMessage());
        }
    }

    private void generateStandardMonitor(Path directory, String monitorType, int warningThreshold, int criticalThreshold) throws IOException {
        String formattedRoute = formatRoute(route);  // Garantir que a rota esteja formatada corretamente
        String alertName = String.format("[%s-%s] - Monitor padrão de %s para a rota %s", sigla, appName, monitorType, formattedRoute);
        Map<String, Object> monitor = new LinkedHashMap<>();
        monitor.put("name", alertName);
        monitor.put("type", "query alert");
        monitor.put("message", String.format("Alerta padrão de %s configurado para a rota %s.", monitorType, formattedRoute));
        monitor.put("query", buildQuery(monitorType));
        monitor.put("monitor_thresholds", Map.of("warning", warningThreshold, "critical", criticalThreshold));
        monitor.put("include_tags", true);
        monitor.put("tags", new String[]{"monitor_type:" + monitorType, "service:" + serviceName, "route:" + formattedRoute});

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(monitor);
        Path filePath = directory.resolve(serviceName + "_" + formattedRoute + "_" + monitorType + "_monitor.json");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, json);
    }

    private String buildQuery(String monitorType) {
        return String.format("last_5m):sum:%s.count{service:%s, route:%s} > 100", monitorType, serviceName, route);
    }

    private String formatRoute(
            final String route
    ) {
        String formatted = route.replace("{", "_").replace("}", "_").replace("/", "_");
        formatted = formatted.replaceAll("_+", "_").replaceAll("^_", "");
        return formatted;
    }

    private int promptForInt(String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida, por favor insira um número inteiro.");
            }
        }
    }

}

