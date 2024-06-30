package com.stegano.steg0vault.repositories;

import com.stegano.steg0vault.models.DTOs.Action;
import com.stegano.steg0vault.models.DTOs.CollectionAction;
import com.stegano.steg0vault.models.entities.Collection;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.entities.User;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class AuditRepository {
    private final AuditReader auditReader;

    public AuditRepository(EntityManagerFactory factory) {
        this.auditReader = AuditReaderFactory.get(factory.createEntityManager());
    }

    public List<Action> getUserLogs(Long id) {

        List<Number> revisionNumbers = auditReader.getRevisions(User.class, id);
        List<Date> dates = revisionNumbers.stream().map(
                auditReader::getRevisionDate
        ).toList();

        AuditQuery query = auditReader
                .createQuery()
                .forRevisionsOfEntity(User.class, true, true)
                .add(AuditEntity.property("id").eq(id));

        List<Action> logs = new ArrayList<>();
        List<User> users = query.getResultList();

        User user = users.get(0);

        logs.add(
                Action.builder()
                        .name("CREATE")
                        .info("Created at " + dates.get(0).toString() + " Email: " + user.getEmail() + " First name: " + user.getFirstName() + " Last name: " + user.getLastName())
                        .build()
        );

        for(int i = 1; i < users.size(); i++) {
            if(!users.get(i).getLastName().equals(users.get(i - 1).getLastName()) ||
               !users.get(i).getFirstName().equals(users.get(i - 1).getFirstName()))
                logs.add(
                        Action.builder()
                                .name("UPDATE")
                                .info("Name changed at " + dates.get(i).toString() + " :: " +
                                        users.get(i - 1).getLastName() + " " + users.get(i - 1).getFirstName() +
                                        " --> " + users.get(i).getLastName() + " " + users.get(i).getFirstName()
                                )
                                .build()
                );
            else if(!users.get(i).getPassword().equals(users.get(i - 1).getPassword()))
                logs.add(
                        Action.builder()
                                .name("UPDATE")
                                .info("Password changed at " + dates.get(i).toString())
                                .build()
                );
        }

        if(users.get(users.size() - 1).getDeletedAt() != null)
            logs.add(
                    Action.builder()
                            .name("DELETE")
                            .info("Deleted at " + dates.get(dates.size() - 1).toString())
                            .build()
            );
        return logs;
    }

    public List<CollectionAction> getCollectionLogs(User user) {

        List<CollectionAction> collectionActions = new ArrayList<>();
        int index = 0;
        for(Collection collection : user.getCollections()) {

            List<Action> logs = new ArrayList<>();
            index++;

            List<Number> revisionNumbers = auditReader.getRevisions(Collection.class, collection.getId());
            List<Date> dates = revisionNumbers.stream().map(
                    auditReader::getRevisionDate
            ).toList();

            AuditQuery query = auditReader
                    .createQuery()
                    .forRevisionsOfEntity(Collection.class, true, true)
                    .add(AuditEntity.property("id").eq(collection.getId()));
            List<Collection> collections = query.getResultList();

            logs.add(
                    Action.builder()
                            .name("CREATE")
                            .info("Created at " + dates.get(0).toString() + " Name: " + collections.get(0).getName() + " Description: " + collections.get(0).getCollectionDescription())
                            .build()
            );

            for(int i = 1; i < collections.size(); i++) {
                if(!collections.get(i).getName().equals(collections.get(i - 1).getName()) ||
                        !collections.get(i).getCollectionDescription().equals(collections.get(i - 1).getCollectionDescription()))
                    logs.add(
                            Action.builder()
                                    .name("UPDATE")
                                    .info("Changed at " + dates.get(i).toString() + " Name: " + collections.get(i - 1).getName() + " Description: " + collections.get(i - 1).getCollectionDescription() + " --> Name: " + collections.get(i).getName() + " Description: " + collections.get(i).getCollectionDescription())
                                    .build()
                    );
            }

            if(collections.get(collections.size() - 1).getDeletedAt() != null)
                logs.add(
                        Action.builder()
                                .name("DELETE")
                                .info("Deleted at " + dates.get(dates.size() - 1).toString())
                                .build()
                );

            collectionActions.add(
                    CollectionAction.builder()
                            .name("Collection no. " + index)
                            .actionList(logs)
                            .build()
            );
        }
        return collectionActions;
    }

    public List<CollectionAction> getResourceLogs(User user) {
        List<CollectionAction> collectionActions = new ArrayList<>();
        int index = 0;
        for(Collection collection : user.getCollections()) {
            for(Resource resource : collection.getResources()) {
                index++;

                List<Number> revisionNumbers = auditReader.getRevisions(Resource.class, resource.getId());
                List<Date> dates = revisionNumbers.stream().map(
                        auditReader::getRevisionDate
                ).toList();

                AuditQuery query = auditReader
                        .createQuery()
                        .forRevisionsOfEntity(Resource.class, true, true)
                        .add(AuditEntity.property("id").eq(resource.getId()));
                List<Resource> resources = query.getResultList();
                List<Action> logs = new ArrayList<>();

                logs.add(
                    Action.builder()
                            .name("CREATE")
                            .info("Created at " + dates.get(0).toString() + " Name: " + resources.get(0).getName() + " Description: " + resources.get(0).getDescription())
                            .build()
                );


                for(int i = 1; i < resources.size(); i++) {
                    if(!resources.get(i).getName().equals(resources.get(i - 1).getName()) ||
                            !resources.get(i).getDescription().equals(resources.get(i - 1).getDescription()))
                        logs.add(
                                Action.builder()
                                        .name("UPDATE")
                                        .info("Changed at " + dates.get(i).toString() + " Name: " + resources.get(i - 1).getName()
                                                + " Description: " + resources.get(i - 1).getDescription() + " --> Name: "
                                                + resources.get(i).getName() + " Description: " + resources.get(i).getDescription()
                                        )
                                        .build()
                        );
                }

                if(resources.get(resources.size() - 1).getDeletedAt() != null)
                    logs.add(
                            Action.builder()
                                    .name("DELETE")
                                    .info("Deleted at " + dates.get(dates.size() - 1).toString())
                                    .build()
                    );

                collectionActions.add(
                        CollectionAction.builder()
                                .name("Resource no. " + index)
                                .actionList(logs)
                                .build()
                );
            }
        }
        return collectionActions;
    }
}
