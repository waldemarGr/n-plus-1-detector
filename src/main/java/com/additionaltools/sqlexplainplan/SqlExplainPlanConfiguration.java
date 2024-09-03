package com.additionaltools.sqlexplainplan;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.additionaltools.common.AnnotationScannerService;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SqlExplainPlanConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SqlExplainPlanConfiguration.class);


    @Bean
    public String basePath(AnnotationScannerService annotationScannerService) {
        return annotationScannerService.getPackageNameForAnnotatedClass(EnableQueryPlanAnalysis.class);
    }

    @Bean
    @ConditionalOnMissingBean(AnnotationScannerService.class)
    public AnnotationScannerService annotationScannerService() {
        return new AnnotationScannerService();
    }


    @Bean
    public SQLAppender sqlAppender(String basePath, Explainer explainer) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        SQLAppender sqlAppender = new SQLAppender(basePath, explainer);
        sqlAppender.setContext(context);
        sqlAppender.start();

        Logger hibernateSqlLogger = context.getLogger("org.hibernate.SQL");
        Logger hibernateBindLogger = context.getLogger("org.hibernate.orm.jdbc.bind");

        hibernateSqlLogger.addAppender(sqlAppender);
        hibernateBindLogger.addAppender(sqlAppender);

        return sqlAppender;
    }

    @Bean
    @ConditionalOnExpression("#{ '${spring.datasource.driverClassName}'.contains('mysql') }")
    public Explainer mySqlExplainer(JdbcTemplate jdbcTemplate) {
        return new MySqlExplainer(jdbcTemplate);
    }

    @Bean
    @ConditionalOnExpression("#{ '${spring.datasource.driverClassName}'.contains('oracle') }")
    public Explainer oracleExplainer(JdbcTemplate jdbcTemplate) {
        return new OracleExplainer(jdbcTemplate);
    }

    @PostConstruct
    public void init() {
        configureLoggingLevels();
    }

    private void configureLoggingLevels() {
        updateLoggingLevel("org.hibernate.SQL", "DEBUG");
        updateLoggingLevel("org.hibernate.orm.jdbc.bind", "TRACE");
    }

    private void updateLoggingLevel(String loggerName, String desiredLevel) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(loggerName);
        Level currentLevel = logger.getLevel();
        Level newLevel = parseLogLevel(desiredLevel);

        if (currentLevel == null || !currentLevel.equals(newLevel)) {
            logger.setLevel(newLevel);
            log.info("Updated logging level for {} from {} to {}", loggerName, currentLevel, newLevel);
        } else {
            log.info("Logging level for {} is already set to {}", loggerName, newLevel);
        }
    }

    private Level parseLogLevel(String level) {
        return switch (level.toUpperCase()) {
            case "DEBUG" -> Level.DEBUG;
            case "TRACE" -> Level.TRACE;
            case "INFO" -> Level.INFO;
            case "WARN" -> Level.WARN;
            case "ERROR" -> Level.ERROR;
            default -> throw new IllegalArgumentException("Unsupported logging level: " + level);
        };
    }

}