package com.stegano.steg0vault.repositories;

import com.stegano.steg0vault.models.entities.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long>, RevisionRepository<Resource, Long, Long> {
    ArrayList<Resource> getResourcesByCollectionId(Long collectionId);
    Resource getResourceByNameAndCollectionIdAndDeletedAtIsNull(String resourceName, Long collectionId);
    Resource getResourcesById(Long id);
    Long countAllByDeletedAtIsNullAndAlgorithmId(Long id);
    Long countResourcesByAlgorithmId(Long id);
}
