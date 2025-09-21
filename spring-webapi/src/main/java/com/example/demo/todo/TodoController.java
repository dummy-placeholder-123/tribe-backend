package com.example.demo.todo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing a minimal to-do API.
 *
 * <p>Metrics to note:
 * - We bias towards returning typed domain objects (e.g., {@link TodoItem}) so that Spring automatically writes JSON.
 * - ResponseEntity gives precise control over status codes and allows augmenting responses later (headers, etc.).
 */
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        // Constructor injection keeps the controller immutable and helps during testing.
        this.todoService = todoService;
    }

    /**
     * Fetch every to-do item currently known to the application.
     *
     * @return HTTP 200 envelope containing a JSON array of to-do records
     */
    @GetMapping
    public ResponseEntity<List<TodoItem>> findAll() {
        List<TodoItem> items = todoService.findAll();
        return ResponseEntity.ok(items);
    }

    /**
     * Create a new to-do item supplied by the client.
     *
     * <p>For simplicity we map the request body onto a generic {@link Map}. In a production codebase you would
     * probably bind onto a dedicated DTO record and add validation annotations.
     *
     * @param payload request body containing a "description" key with free-form text
     * @return HTTP 201 with the created {@link TodoItem} serialized as JSON
     */
    @PostMapping
    public ResponseEntity<TodoItem> create(@RequestBody Map<String, String> payload) {
        String description = payload.getOrDefault("description", "");
        if (description.isBlank()) {
            // Keeping validation logic close to the endpoint simplifies error handling for this example.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TodoItem created = todoService.create(description);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
