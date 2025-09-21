package com.example.demo.todo;

import java.time.Instant;
import java.util.UUID;

/**
 * Simple domain object representing a "to-do" item.
 *
 * <p>A real application would persist these records in a database. For this sample we retain items in memory
 * while the application process is running. The UUID identifier mirrors how you might expose stable IDs in a production API.
 */
public class TodoItem {

    /** Unique identifier used by clients to reference a particular to-do item. */
    private final UUID id;

    /** Human-readable description of the work to accomplish. */
    private final String description;

    /** Timestamp noting when the item was created. Useful for sorting and audit trails. */
    private final Instant createdAt;

    public TodoItem(UUID id, String description, Instant createdAt) {
        this.id = id;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
