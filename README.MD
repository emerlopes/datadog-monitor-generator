# br.com.emerlopes.DatadogMonitorGeneratorApp

## Visão Geral

O `br.com.emerlopes.DatadogMonitorGeneratorApp` é uma ferramenta de linha de comando desenvolvida para gerar configurações de monitores
de alerta para o Datadog. Oferece personalização de alertas com base em rotas específicas, métricas e limiares de
alerta.

## Pré-requisitos

- Java JDK 8 ou superior.
- Bibliotecas Jackson para serialização JSON.
- Biblioteca Picocli para análise de argumentos de linha de comando.

## Configuração

Inclua as dependências do Jackson e Picocli no seu projeto usando Maven ou Gradle. Assegure-se de que todas as
dependências estão configuradas no classpath do seu projeto.

## Opções de Linha de Comando

O programa aceita várias opções de linha de comando para personalizar os alertas gerados:

### Opções Obrigatórias

- `--mode`: Define o modo de geração do alerta. Obrigatório e deve ser `specific`.
- `--route`: Rota completa, incluindo o método HTTP, para o qual o monitor será criado.
- `--app`: Nome da aplicação associada ao monitor.
- `--sigla`: Sigla da aplicação, usada no prefixo do nome do monitor.

### Opções Opcionais

- `--service`: Nome do serviço no Datadog, usado para categorizar o monitor.
- `--warning-{type}`, `--critical-{type}`: Limiares de aviso e críticos para métricas de erros, requisições e latência.
- `--threshold-{type}`: Valor de comparação para a métrica específica.
- `--last-time`: Intervalo de tempo para a métrica, padrão é `last_5m`.

### Valores Default

Se os limiares de alerta ou os valores de `threshold` não forem especificados, o sistema usará os seguintes padrões:

- `warning`: 5
- `critical`: 10
- `threshold`: 10 para erros e requisições, 200 para latência.

## Formatação da Rota

As rotas devem ser formatadas para incluir o método HTTP seguido por um underscore (`_`), com quaisquer caracteres
especiais como `{` e `}` substituídos por `_`. Além disso, remova barras extras (`/`) e underscores duplicados para
garantir uma formatação limpa e válida. Por exemplo:

- Rota original: `POST /api/boletos/{boletoId}/download`
- Formato correto: `post_/api/boletos/_boletoId_/download`

## Exemplos de Uso

### Monitoramento Básico

```bash
java -cp target/datadog-monitor-generator-1.0-SNAPSHOT.jar br.com.emerlopes.DatadogMonitorGeneratorApp \
  --mode specific \
  --route "POST_/api/boletos" \
  --app "FinanceApp" \
  --sigla "FIN"
```

### Monitoramento com Limiares Personalizados

Monitora requisições com limiares personalizados de aviso e crítico.

```bash
java -cp target/datadog-monitor-generator-1.0-SNAPSHOT.jar br.com.emerlopes.DatadogMonitorGeneratorApp \
  --mode specific \
  --route "GET_/api/users" \
  --app "UserService" \
  --sigla "USR" \
  --warning-requests 20 --critical-requests 50 \
  --threshold-requests 100
```

### Monitoramento de Latência com Intervalo de Tempo Específico

Monitora a latência de uma rota específica ao longo das últimas 2 horas.

```bash
java -cp target/datadog-monitor-generator-1.0-SNAPSHOT.jar br.com.emerlopes.DatadogMonitorGeneratorApp \
  --mode specific \
  --route "POST_/api/payments" \
  --app "PaymentService" \
  --sigla "PAY" \
  --last-time "last_2h" \
  --threshold-latency 300 \
  --warning-latency 150 --critical-latency 250
```

### Monitoramento sem Limiares Especificados

Utiliza valores padrão para limiares em um monitoramento de erros.

```bash
java -cp target/datadog-monitor-generator-1.0-SNAPSHOT.jar br.com.emerlopes.DatadogMonitorGeneratorApp \
  --mode specific \
  --route "DELETE_/api/accounts" \
  --app "AccountService" \
  --sigla "ACC"
```