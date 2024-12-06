package br.com.emerlopes;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@CommandLine.Command(
        name = "monitor-generator",
        mixinStandardHelpOptions = true,
        description = "Gera alertas para rotas em formato JSON"
)
public class DatadogMonitorGeneratorApp implements Runnable {

    @CommandLine.Option(
            names = {"--mode"},
            description = "Modo de geração do alerta: 'gateway' ou 'specific'.",
            required = true
    )
    private String mode;

    @CommandLine.Option(
            names = {"--route"},
            description = "Valor da rota específica (obrigatório para modo 'specific').",
            required = true
    )
    private String route;

    @CommandLine.Option(
            names = {"--app"},
            description = "Nome da aplicação (usado no modo 'specific').",
            required = true
    )
    private String appName;

    @CommandLine.Option(
            names = {"--sigla"},
            description = "Sigla da aplicação (usado no modo 'specific').",
            required = true
    )
    private String sigla;

    @CommandLine.Option(
            names = {"--service"},
            description = "Nome do serviço no Datadog (usado no modo 'specific')."
    )
    private String serviceName = "default-service";

    @CommandLine.Option(
            names = {"--warning-errors", "--we"},
            description = "Limiar de alerta de aviso para erros."
    )
    private int warningErrors = 5;

    @CommandLine.Option(
            names = {"--critical-errors", "--ce"},
            description = "Limiar de alerta crítico para erros."
    )
    private int criticalErrors = 10;

    @CommandLine.Option(
            names = {"--warning-requests", "--wr"},
            description = "Limiar de alerta de aviso para requisições."
    )
    private int warningRequests = 5;

    @CommandLine.Option(
            names = {"--critical-requests", "--cr"},
            description = "Limiar de alerta crítico para requisições."
    )
    private int criticalRequests = 10;

    @CommandLine.Option(
            names = {"--warning-latency", "--wl"},
            description = "Limiar de alerta de aviso para latência."
    )
    private int warningLatency = 5;

    @CommandLine.Option(
            names = {"--critical-latency", "--cl"},
            description = "Limiar de alerta crítico para latência."
    )
    private int criticalLatency = 10;

    @CommandLine.Option(
            names = {"--last-time"},
            description = "Intervalo de tempo para a métrica (e.g., 'last_5m').",
            defaultValue = "last_5m"
    )
    private String lastTime;

    @CommandLine.Option(
            names = {"--threshold-errors", "--te"},
            description = "Limite de comparação para erros."
    )
    private int thresholdErrors = 10;

    @CommandLine.Option(
            names = {"--threshold-requests", "--tr"},
            description = "Limite de comparação para requisições."
    )
    private int thresholdRequests = 10;

    @CommandLine.Option(
            names = {"--threshold-latency", "--tl"},
            description = "Limite de comparação para latência."
    )
    private int thresholdLatency = 200;

    public static void main(String[] args) {
        new CommandLine(new DatadogMonitorGeneratorApp()).execute(args);
    }

    @Override
    public void run() {
        if ("specific".equals(mode)) {
            try {
                String formattedRoute = formatRoute(route);
                generateDatadogAlertJson(formattedRoute.toLowerCase());
            } catch (IOException e) {
                System.err.println("Erro ao gerar os arquivos JSON: " + e.getMessage());
            }
        } else {
            System.out.println("Modo não é 'specific'. Nenhum alerta será gerado.");
        }
    }

    private String formatRoute(
            final String route
    ) {
        String formatted = route.replace("{", "_").replace("}", "_").replace("/", "_");
        formatted = formatted.replaceAll("_+", "_").replaceAll("^_", "");
        return formatted;
    }

    private void generateDatadogAlertJson(final String formattedRoute) throws IOException {
        Path directory = Paths.get("monitors");
        Files.createDirectories(directory); // Ensure directory exists

        generateMonitor("erros", formattedRoute, "sum(" + lastTime + "):sum:errors.count{resource_name:" + formattedRoute + "} by {resource}.as_count() >= " + thresholdErrors, warningErrors, criticalErrors, directory);
        generateMonitor("requisicoes", formattedRoute, "sum(" + lastTime + "):sum:requests.count{resource_name:" + formattedRoute + "} by {resource}.as_count() >= " + thresholdRequests, warningRequests, criticalRequests, directory);
        generateMonitor("latencia", formattedRoute, "avg(" + lastTime + "):avg:latency{resource_name:" + formattedRoute + "} by {resource} > " + thresholdLatency, warningLatency, criticalLatency, directory);
    }

    private void generateMonitor(String type, String formattedRoute, String query, int warningThreshold, int criticalThreshold, Path directory) throws IOException {
        String alertName = String.format("[%s-%s-%s] - Monitor de %s para a rota %s", sigla, appName.toUpperCase(), type.toUpperCase(), type, formattedRoute);
        Map<String, Object> monitor = new LinkedHashMap<>();
        monitor.put("name", alertName);
        monitor.put("type", "query alert");
        monitor.put("message", String.format("Alta taxa de %s detectada para %s. Notificar: @channel", type, formattedRoute));
        monitor.put("query", query);
        monitor.put("monitor_thresholds", Map.of("warning", warningThreshold, "critical", criticalThreshold));
        monitor.put("include_tags", true);
        monitor.put("tags", new String[]{"monitor_type:" + type, "service:" + serviceName});

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(monitor);
        Path filePath = directory.resolve(formattedRoute + "_" + type + "_monitor.json");
        Files.writeString(filePath, json);
        System.out.println("Monitor " + type + " gerado em JSON: " + json);
    }
}
