package com.stegano.steg0vault.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="vault_collections")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Audited
@EntityListeners(AuditingEntityListener.class)
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collections_seq")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String collectionDescription;

    @ManyToOne
    private User user;

    @Column(
            nullable = false,
            updatable = false
    )
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(insertable = false)
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Column
    private LocalDateTime deletedAt;

    @OneToMany(
            mappedBy = "collection",
            cascade = CascadeType.ALL
    )
    private final List<Resource> resources = new ArrayList<>();
}
