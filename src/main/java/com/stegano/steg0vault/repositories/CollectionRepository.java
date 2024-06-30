package com.stegano.steg0vault.repositories;

import com.stegano.steg0vault.models.entities.Collection;
import org.springframework.data.history.Revisions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long>, RevisionRepository<Collection, Long, Long> {
    Collection getCollectionByNameAndUserIdAndDeletedAtIsNull(String collectionName, Long userId);
    ArrayList<Collection> getCollectionsByUserId(Long userId);
    @Override
    Revisions<Long, Collection> findRevisions(Long id);
}
