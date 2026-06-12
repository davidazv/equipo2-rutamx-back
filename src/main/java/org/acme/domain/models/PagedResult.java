package org.acme.domain.models;

import java.util.List;

public class PagedResult<T> {
    private final List<T> items;
    private final long totalItems;
    private final int page;
    private final int size;

    public PagedResult(List<T> items, long totalItems, int page, int size) {
        this.items = items;
        this.totalItems = totalItems;
        this.page = page;
        this.size = size;
    }

    public List<T> getItems() { return items; }
    public long getTotalItems() { return totalItems; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalPages() {
        if (size <= 0) return 0;
        return (int) Math.ceil((double) totalItems / size);
    }
    public boolean isHasNext() { return page + 1 < getTotalPages(); }
    public boolean isHasPrevious() { return page > 0; }
}
