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
}
