package com.stegano.steg0vault.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="vault_algorithms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@Audited
@EntityListeners(AuditingEntityListener.class)
public class AlgorithmEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "algorithm_seq")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

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
            mappedBy = "algorithm"
    )
    private final List<Resource> resources = new ArrayList<>();
}
