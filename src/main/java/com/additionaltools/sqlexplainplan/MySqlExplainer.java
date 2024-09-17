package com.additionaltools.sqlexplainplan;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class MySqlExplainer implements Explainer {
    private final JdbcTemplate jdbcTemplate;

    public MySqlExplainer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> explainQuery(String query) {
        return jdbcTemplate.queryForList("EXPLAIN %s".formatted(query));
    }

    public String getPrintableTable(List<Map<String, Object>> tables) {
        int keyWidth = 20;
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
        return logBuilder.toString();
    }

    private String truncate(String value, int length) {
        if (value.length() <= length) {
            return value;
        }
        return value.substring(0, length - 1) + "…";
    }
}
