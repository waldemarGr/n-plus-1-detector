package com.additionaltools.sqlexplainplan;

import java.util.List;
import java.util.Map;

public interface Explainer {
    List<Map<String, Object>> explainQuery(String query);
}
