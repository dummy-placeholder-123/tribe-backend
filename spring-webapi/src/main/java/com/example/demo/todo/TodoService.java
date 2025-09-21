package com.example.demo.todo;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * In-memory service layer that fakes a persistence boundary.
 *
 * <p>Keeping a service class, even when the logic seems trivial, pays off quickly:
 * - it centralizes data handling so controllers stay lean;
 * - it allows you to swap implementations (e.g., JPA, MongoDB) without touching HTTP code;
 * - it offers a natural location for validation and business rules.
 */
@Service
public class TodoService {

    /** Backing store containing all to-do items created during this runtime. */
    private final List<TodoItem> items = new ArrayList<>();

    /**
     * Create a new to-do item with a generated identifier.
     *
     * @param description human-friendly explanation of the work to perform
     * @return fully populated {@link TodoItem} instance so the controller can echo it to the client
     */
    public TodoItem create(String description) {
        TodoItem item = new TodoItem(UUID.randomUUID(), description, Instant.now());
        // Store the freshly minted item so future API calls can see it.
        items.add(item);
        return item;
    }

    /**
     * Retrieve an immutable snapshot of all to-do items currently tracked by the service.
     *
     * @return defensive copy of the item list to avoid callers fiddling with our internal state
     */
    public List<TodoItem> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
