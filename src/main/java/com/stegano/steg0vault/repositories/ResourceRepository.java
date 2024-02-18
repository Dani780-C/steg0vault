package com.stegano.steg0vault.repositories;

import com.stegano.steg0vault.models.entities.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    ArrayList<Resource> getResourcesByCollectionId(Long collectionId);
    Resource getResourceByNameAndCollectionId(String resourceName, Long collectionId);
}
