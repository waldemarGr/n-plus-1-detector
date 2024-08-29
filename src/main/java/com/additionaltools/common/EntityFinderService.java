package com.additionaltools.common;

import jakarta.persistence.Entity;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * A simple service to help you find all the JPA entities in a given package.
 *
 * <p>This service scans a specified package and returns all the classes
 * that are marked with the {@link Entity} annotation. It's pretty handy when you need
 * to gather all your entities in one place.</p>
 *
 * <p>How to use it:</p>
 * <pre>{@code
 * EntityFinderService entityFinderService = new EntityFinderService();
 * Set<Class<?>> entities = entityFinderService.findEntitiesInPackage("com.example.package");
 * }</pre>
 *
 * <p>Just pass in the package name you want to scan, and boom—you get a set of all the entities
 * in that package. Don't forget to have the Reflections library in your classpath, though.</p>
 *
 * @see Entity
 * @see Reflections
 */
@Service
public class EntityFinderService {

    public Set<Class<?>> findEntitiesInPackage(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getTypesAnnotatedWith(Entity.class);
    }
}

