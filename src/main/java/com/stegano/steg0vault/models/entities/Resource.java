package com.stegano.steg0vault.models.entities;

import com.stegano.steg0vault.models.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name="vault_resources")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Audited
@EntityListeners(AuditingEntityListener.class)
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resources_seq")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    @Column
    private String description;

    @ManyToOne
    private Collection collection;

    @ManyToOne
    private AlgorithmEntity algorithm;

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

    public String getImageName() {
        return String.join("_", name.split(" ")) + "." + imageType.toString().toLowerCase();
    }
}
