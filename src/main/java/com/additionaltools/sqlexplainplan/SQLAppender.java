package com.additionaltools.sqlexplainplan;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Custom appender for capturing SQL queries and their execution plans.
 * <p>
 * This appender listens to Hibernate's SQL logging and captures executed SQL queries along with their
 * bound parameters. It then generates an execution plan using the provided {@link Explainer} and logs the results.
 * </p>
 */
public class SQLAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SQLAppender.class);
    private final String basePath;
    private final Explainer explainer;

    public SQLAppender(String basePath, Explainer explainer) {
        this.explainer = explainer;
        this.basePath = basePath;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        SqlDefinitionHolder sqlDefinitionHolder = SqlDefinitionHolder.getInstance();
        if (eventObject.getLoggerName().equals("org.hibernate.SQL")) {
            String sql = eventObject.getFormattedMessage();
            SqlDefinition sqlDefinition = new SqlDefinition(sql, new LinkedList<>(), getCallerMethod(basePath), new ArrayList<>());
            sqlDefinitionHolder.addSqlDef(sqlDefinition);
        }
        if (eventObject.getLoggerName().equals("org.hibernate.orm.jdbc.bind")) {
            String argument = eventObject.getFormattedMessage();
            SqlDefinition last = sqlDefinitionHolder.getLast();
            last.arguments().add(argument);
        }

        if (sqlDefinitionHolder.getLast().isCompleted()) {
            List<Map<String, Object>> oracleExecutionPlans = explainer.explainQuery(sqlDefinitionHolder.getLast().getSqlWithArguments());
            sqlDefinitionHolder.getLast().explanations().addAll(oracleExecutionPlans);
            logger.info(sqlDefinitionHolder.getLast().toString());
            printTable(oracleExecutionPlans);
        }
    }

    private String getCallerMethod(String basePath) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Arrays.stream(stackTrace)
                .map(stackTraceElement -> stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber())
                .filter(methodName -> methodName.contains(basePath))
                .findFirst().orElse("Unknown method");
    }

    private void printTable(List<Map<String, Object>> tables) {
        int keyWidth = 20;  // Szerokość kolumny dla klucza
        StringBuilder logBuilder = new StringBuilder();

        for (int i = 0; i < tables.size(); i++) {
            Map<String, Object> map = tables.get(i);
            logBuilder.append("Lvl ").append(i + 1).append("\n");
            String separator = "+".repeat(keyWidth + 4) + "+";
            logBuilder.append(separator).append("\n");
            String header = String.format("| %-" + keyWidth + "s | %s |", "Key", "Value");
            logBuilder.append(header).append("\n");
            logBuilder.append(separator).append("\n");

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = truncate(entry.getKey(), keyWidth);
                String value = entry.getValue() != null ? entry.getValue().toString() : "null";
                String row = String.format("| %-" + keyWidth + "s | %s |", key, value);
                logBuilder.append(row).append("\n");
            }
            logBuilder.append(separator).append("\n\n");
        }

        logger.info("\n{}", logBuilder);
    }

    private String truncate(String value, int length) {
        if (value.length() <= length) {
            return value;
        }
        return value.substring(0, length - 1) + "…";
    }
}

