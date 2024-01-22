package com.stegano.steg0vault.repositories;

import com.stegano.steg0vault.models.entities.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Collection getCollectionByNameAndUserId(String collectionName, Long userId);
    ArrayList<Collection> getCollectionsByUserId(Long userId);
}
