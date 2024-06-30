package com.stegano.steg0vault.repositories;

import com.stegano.steg0vault.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends RevisionRepository<User, Long, Long>, JpaRepository<User, Long> {
    User getUserByEmail(String email);
    User getUserById(Long email);
    User getUserByResetPasswordTokenAndDeletedAtIsNull(String resetPasswordToken);
    User getUserByEmailAndDeletedAtIsNull(String email);

}
