package com.additionaltools.sqlexplainplan;

import org.apache.logging.log4j.util.Strings;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class H2Explainer implements Explainer {

    public H2Explainer(JdbcTemplate jdbcTemplate) {
    }

    public List<Map<String, Object>> explainQuery(String query) {
        return (List<Map<String, Object>>) Map.of();
    }

    @Override
    public String getPrintableTable(List<Map<String, Object>> tables) {
        return Strings.EMPTY;
    }
}
