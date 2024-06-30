package com.stegano.steg0vault.repositories;

import com.stegano.steg0vault.models.entities.AlgorithmEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlgorithmRepository extends RevisionRepository<AlgorithmEntity, Long, Long>, JpaRepository<AlgorithmEntity, Long> {
    AlgorithmEntity findAlgorithmEntityByName(String name);
}