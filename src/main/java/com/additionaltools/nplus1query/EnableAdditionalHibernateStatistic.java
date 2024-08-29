package com.additionaltools.nplus1query;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable additional Hibernate statistics features.
 *
 * <p>This annotation imports the {@link TransactionalStatisticConfiguration} class into the Spring application context,
 * which automatically configures beans for enhanced SQL statistics management and transaction monitoring.</p>
 *
 * <p>When this annotation is added to a configuration class, it triggers the registration of
 * {@link SQLStatisticsService} and {@link TransactionAspect} beans, enabling additional Hibernate
 * statistics and transaction monitoring capabilities.</p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(TransactionalStatisticConfiguration.class)
public @interface EnableAdditionalHibernateStatistic {
}
