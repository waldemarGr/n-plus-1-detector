package com.additionaltools.sqlexplainplan;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to enable SqlExplainPlanConfiguration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SqlExplainPlanConfiguration.class)
public @interface EnableQueryPlanAnalysis {
}