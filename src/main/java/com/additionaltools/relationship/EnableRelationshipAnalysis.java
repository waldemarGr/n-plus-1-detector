package com.additionaltools.relationship;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to enable HashCodeAnalysis configuration.
 * This annotation will import the necessary configuration class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RelationshipAnalysisConfiguration.class)
public @interface EnableRelationshipAnalysis {
}