package org.acme.infrastructure.repository;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.function.Function;

/**
 * Shared helper for bulk-inserting domain items as JPA entities, flushing and
 * clearing the persistence context every {@code batchSize} rows to keep memory
 * bounded during large GTFS imports.
 */
final class BatchPersister {

    private BatchPersister() {}

    /**
     * Persists every item, mapping it to an entity via {@code toEntity}, and
     * flushes + clears the {@link EntityManager} after each batch of
     * {@code batchSize} persisted rows. Returns the number of items persisted.
     *
     * <p>The {@code toEntity} function is responsible for any per-item setup
     * (FK references, timestamps, etc.) and must return the entity to persist.
     */
    static <T, E> int persistAll(EntityManager entityManager, List<T> items,
                                 int batchSize, Function<T, E> toEntity) {
        int count = 0;
        for (T item : items) {
            E entity = toEntity.apply(item);
            entityManager.persist(entity);
            if (++count % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return count;
    }
}
