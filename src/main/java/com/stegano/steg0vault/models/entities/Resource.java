package com.stegano.steg0vault.models.entities;

import com.stegano.steg0vault.models.enums.AlgorithmType;
import com.stegano.steg0vault.models.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="vault_resources")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resources_seq")
    private Long id;

    @Column
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    @Column
    private String description;

    @Column
    private boolean isSaved;

    @Column
    @Enumerated(EnumType.STRING)
    private AlgorithmType algorithmType;

    @ManyToOne
    private Collection collection;

    public String getImageName() {
        return String.join("_", name.split(" ")) + "." + imageType.toString().toLowerCase();
    }
}
