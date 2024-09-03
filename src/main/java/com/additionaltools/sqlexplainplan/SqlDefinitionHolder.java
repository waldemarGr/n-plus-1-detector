package com.additionaltools.sqlexplainplan;

import java.util.LinkedList;
import java.util.List;

public class SqlDefinitionHolder {

    private static final SqlDefinitionHolder instance = new SqlDefinitionHolder();
    private final LinkedList<SqlDefinition> sqlDefinitions = new LinkedList<>();

    private SqlDefinitionHolder() {
    }

    public static SqlDefinitionHolder getInstance() {
        return instance;
    }


    public List<SqlDefinition> getSqlDefinitions() {
        return sqlDefinitions;
    }

    public void addSqlDef(SqlDefinition sqlDefinition) {
        this.sqlDefinitions.add(sqlDefinition);
    }

    public SqlDefinition getLast() {
        return this.sqlDefinitions.getLast();
    }
}