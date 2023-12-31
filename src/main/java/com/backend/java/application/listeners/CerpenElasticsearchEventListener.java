package com.backend.java.application.listeners;

import com.backend.java.application.event.CerpenEntityEvent;
import com.backend.java.domain.document.CerpenIndex;
import com.backend.java.domain.entities.CerpenEntity;
import com.backend.java.repository.elasticsearch.CerpenElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CerpenElasticsearchEventListener {

    @Autowired
    private final CerpenElasticsearchRepository elasticsearchRepository;

    @EventListener(condition = """
            #event.eventMethod == T(com.backend.java.application.event.EventMethod).CREATE ||
            #event.eventMethod == T(com.backend.java.application.event.EventMethod).UPDATE
            """)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewDataEvent(CerpenEntityEvent event) {
        try {
            // Get the newly created data from the event
            var newDataCerpen = event.getCerpenEntity();

            // ConvertUtils the NewData to CerpenIndex
            CerpenIndex cerpenIndex = convertToCerpenIndex(newDataCerpen);

            // Save the new data to Elasticsearch using the repository
            elasticsearchRepository.save(cerpenIndex);

            log.info("Event New Data called....");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when insert/update into elasticsearch");
        }
    }

    @EventListener(condition = "#event.eventMethod == T(com.backend.java.application.event.EventMethod).DELETE")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteDataEvent(CerpenEntityEvent event) {
        try {
            // Get the newly created data from the event
            var newDataCerpen = event.getCerpenEntity();

            // ConvertUtils the data to the CerpenIndex
            CerpenIndex cerpenIndex = convertToCerpenIndex(newDataCerpen);

            // Delete the data on Elasticsearch using the repository
            elasticsearchRepository.delete(cerpenIndex);

            log.info("Event Delete Data called....");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when delete data into elasticsearch");
        }
    }

    private CerpenIndex convertToCerpenIndex(CerpenEntity newData) {
        CerpenIndex cerpenIndex = new CerpenIndex();
        cerpenIndex.setId(newData.getId());
        cerpenIndex.setAuthorId(newData.getAuthor().getId());
        cerpenIndex.setAuthorName(newData.getAuthor().getName());
        cerpenIndex.setTitle(newData.getTitle());
        cerpenIndex.setTema(newData.getTema());
        cerpenIndex.setCerpenContains(newData.getCerpenContains());
        cerpenIndex.setIsActive(newData.getIsActive());
        cerpenIndex.setCreatedAt(newData.getCreatedAt());
        cerpenIndex.setUpdatedAt(newData.getUpdatedAt());

        return cerpenIndex;
    }
}

