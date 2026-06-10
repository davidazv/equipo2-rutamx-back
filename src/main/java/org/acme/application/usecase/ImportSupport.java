package org.acme.application.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Shared helpers for GTFS/CSV import use cases.
 */
final class ImportSupport {

    private ImportSupport() {}

    /**
     * Returns a new list preserving input order, keeping only the first item
     * for each distinct key produced by {@code keyFn}. Items keep their original
     * relative order; duplicates (same key as an earlier item) are dropped.
     */
    static <T> List<T> dedupBy(List<T> items, Function<T, String> keyFn) {
        List<T> deduped = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (T item : items) {
            if (seen.add(keyFn.apply(item))) {
                deduped.add(item);
            }
        }
        return deduped;
    }
}
