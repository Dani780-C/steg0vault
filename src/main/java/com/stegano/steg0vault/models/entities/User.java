package com.stegano.steg0vault.models.entities;

import com.stegano.steg0vault.models.enums.RoleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name="User")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@Audited
@EntityListeners(AuditingEntityListener.class)
@ToString
public class User implements UserDetails {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "users_seq")
    private Long id;

    @Column(nullable = false, length = 30)
    private String firstName;

    @Column(nullable = false, length = 30)
    private String lastName;

    @Column(nullable = false, length = 40)
    @Email
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(
            nullable = false,
            updatable = false
    )
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(insertable = false, nullable = true)
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Column
    private LocalDateTime deletedAt;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    private final List<com.stegano.steg0vault.models.entities.Collection> collections = new ArrayList<>();

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Column
    private String resetPasswordToken;

    @Column
    private LocalDateTime resetPasswordTokenExpiresAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleType.toString()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
