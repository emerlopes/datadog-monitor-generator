# Opção --last-time

## Descrição

A opção `--last-time` é utilizada no `br.com.emerlopes.DatadogMonitorGeneratorApp` para especificar o intervalo de tempo sobre o qual as
métricas serão calculadas. Esta configuração define a janela de tempo para análise de dados e para a geração de alertas.

## Formato

O valor para `--last-time` deve ser especificado como `last_Xm` para minutos, `last_Xh` para horas, ou `last_Xd` para
dias, onde `X` representa o número de minutos, horas ou dias.

## Exemplos de Uso

Abaixo estão exemplos de como usar a opção `--last-time` para configurar diferentes intervalos de tempo, adequados para
diversas necessidades de monitoramento:

### Últimos 5 minutos

```bash
--last-time "last_5m"
```

### Última hora

```bash
--last-time "last_1h"
```

### Últimas 24 horas

```bash
--last-time "last_24h"
```

### Última semana

```bash
--last-time "last_7d"
```