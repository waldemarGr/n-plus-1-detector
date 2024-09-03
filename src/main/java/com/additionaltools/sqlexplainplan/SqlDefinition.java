package com.additionaltools.sqlexplainplan;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record SqlDefinition(String sql, Queue<String> arguments, String methodExecution,
                            List<Map<String, Object>> explanations) {
    public String getSqlWithArguments() {
        Pattern pattern = Pattern.compile("\\?");
        Matcher matcher = pattern.matcher(sql);

        StringBuilder sqlWithArgs = new StringBuilder();

        Iterator<String> argIterator = arguments.iterator();

        while (matcher.find()) {
            String arg = argIterator.hasNext() ? argIterator.next() : "?";

            String formattedArg = formatArgument(arg);

            matcher.appendReplacement(sqlWithArgs, formattedArg);
        }
        matcher.appendTail(sqlWithArgs);

        return sqlWithArgs.toString();
    }

    public long countQueryPlaceholders() {
        return sql.chars().filter(value -> value == '?').count();
    }

    private String formatArgument(String arg) {
        // Wzór regex do wyodrębnienia wartości z nawiasów kwadratowych
        Pattern pattern = Pattern.compile("\\[([^]]*)]");
        Matcher matcher = pattern.matcher(arg);

        if (matcher.find()) {
            String value = matcher.group(1);

            // Sprawdzanie typu argumentu
            if (arg.contains("VARCHAR")) {
                // Tekstowy argument
                return "'" + value.replace("'", "''") + "'";
            } else if (arg.contains("INTEGER") || arg.contains("BIGINT")) {
                // Liczbowy argument
                return value;
            }
        }
        return "?";
    }

    public boolean isCompleted() {
        return countQueryPlaceholders() == arguments().size();
    }

    @Override
    public String toString() {
        return "SqlDefinition{" +
               " methodExecution='" + methodExecution + '\'' +
               "sql='" + sql + '\'' +
               ", arguments=" + arguments +
               ", explanations=" + explanations +
               '}';
    }
}
