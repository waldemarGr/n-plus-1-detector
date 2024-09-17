package com.additionaltools.sqlexplainplan;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.additionaltools.logging.LoggingService;
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
    private final LoggingService loggingService;

    public SQLAppender(String basePath, Explainer explainer, LoggingService loggingService) {
        this.explainer = explainer;
        this.basePath = basePath;
        this.loggingService = loggingService;
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
            SqlDefinition last = sqlDefinitionHolder.getLast();
            List<Map<String, Object>> oracleExecutionPlans = explainer.explainQuery(last.getSqlWithArguments());
            last.explanations().addAll(oracleExecutionPlans);
            String printableTable = explainer.getPrintableTable(oracleExecutionPlans);

            String logMessage = String.format(
                    """
                            EXECUTION_PLANS:Method '%s' was executed. The associated SQL query, with bound arguments, is: '%s'.\s
                            %s
                            """,
                    last.methodExecution(), last.getSqlWithArguments(), printableTable);
            logger.info(logMessage);
            loggingService.addLog(logMessage);
        }
    }

    private String getCallerMethod(String basePath) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Arrays.stream(stackTrace)
                .map(stackTraceElement -> stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber())
                .filter(methodName -> methodName.contains(basePath))
                .findFirst().orElse("Unknown method");
    }
}

