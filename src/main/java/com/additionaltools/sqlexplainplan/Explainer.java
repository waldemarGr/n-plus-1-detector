package com.additionaltools.sqlexplainplan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface Explainer {
    Logger logger = LoggerFactory.getLogger(Explainer.class);

    List<Map<String, Object>> explainQuery(String query);

    void printTable(List<Map<String, Object>> tables);
}

