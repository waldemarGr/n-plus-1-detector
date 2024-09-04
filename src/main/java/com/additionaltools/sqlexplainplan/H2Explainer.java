package com.additionaltools.sqlexplainplan;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class H2Explainer implements Explainer {
    private final JdbcTemplate jdbcTemplate;

    public H2Explainer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> explainQuery(String query) {
        return (List<Map<String, Object>>) Map.of();
    }

    @Override
    public void printTable(List<Map<String, Object>> tables) {

    }
}
