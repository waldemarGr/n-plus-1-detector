package com.additionaltools.sqlexplainplan;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class OracleExplainer implements Explainer {
    private final JdbcTemplate jdbcTemplate;

    public OracleExplainer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> explainQuery(String query) {
        jdbcTemplate.queryForList("EXPLAIN PLAN FOR %s".formatted(query));
        return jdbcTemplate.queryForList("SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY)");
    }

    @Override
    public String getPrintableTable(List<Map<String, Object>> tables) {
        StringBuilder logMessage = new StringBuilder();
        for (Map<String, Object> table : tables) {
            for (Map.Entry<String, Object> entry : table.entrySet()) {
                logMessage.append(entry.getValue());
            }
            logMessage.append(System.lineSeparator());
        }
       return logMessage.toString();
    }
}
