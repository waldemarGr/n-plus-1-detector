package com.additionaltools.hashcodevalidator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@jakarta.persistence.Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @OneToOne
    private EntityChild sampleOneToOne;

    @ManyToOne
    private EntityChild sampleManyToOne;

    /*list*/
    @OneToMany
    private List<EntityChild> sampleList1;

    @ManyToMany
    private List<EntityChild> sampleList;

    /* collection*/
    @OneToMany
    private Collection<EntityChild> sampleCollection1;

    @ManyToMany
    private Collection<EntityChild> sampleCollection2;

    /* set*/
    @OneToMany
    private Set<EntityChild> sampleSet1;

    @ManyToMany
    private Set<EntityChild> sampleSet2;


}
