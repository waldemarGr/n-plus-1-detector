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
        return jdbcTemplate.queryForList("EXPLAIN PLAN  %s".formatted(query));
    }
}
