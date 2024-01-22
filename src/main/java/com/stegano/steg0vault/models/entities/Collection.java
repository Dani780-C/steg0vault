package com.stegano.steg0vault.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="vault_collections")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collections_seq")
    private Long id;

    @Column
    private String name;

    @Column
    private String collectionDescription;

    @ManyToOne
    private User user;

    @OneToMany(
            mappedBy = "collection",
            cascade = CascadeType.ALL
    )
    private List<Resource> resources = new ArrayList<>();
}
