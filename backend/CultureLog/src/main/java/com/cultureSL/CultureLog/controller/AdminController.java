package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.*;
import com.cultureSL.CultureLog.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controlador REST para operaciones de administración.
 * Todos los endpoints requieren rol ADMIN (protegido en SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private static final Set<String> VALID_USER_SORT_FIELDS = Set.of(
            "id", "username", "email", "createdAt"
    );

    private static final Set<String> VALID_POST_SORT_FIELDS = Set.of(
            "id", "createdAt", "likeCount", "commentCount"
    );

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        String safeSortBy = VALID_USER_SORT_FIELDS.contains(sortBy) ? sortBy : "id";
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(safeSortBy).descending()
                : Sort.by(safeSortBy).ascending();
        return ResponseEntity.ok(adminService.listUsers(
                PageRequest.of(page, Math.min(size, 100), sort), userId));
    }

    @GetMapping("/users/autocomplete")
    public ResponseEntity<List<UserAutocompleteResponse>> autocompleteUsers(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(adminService.autocompleteUsers(q));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserDetail(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(adminService.getUserDetail(id, adminId));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<AdminPostResponse>> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long linkedItemId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String safeSortBy = VALID_POST_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(safeSortBy).descending()
                : Sort.by(safeSortBy).ascending();
        return ResponseEntity.ok(adminService.listPosts(
                PageRequest.of(page, Math.min(size, 100), sort), authorId, linkedItemId));
    }

    @GetMapping("/items/linked-autocomplete")
    public ResponseEntity<List<ItemAutocompleteResponse>> autocompleteLinkedItems(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(adminService.autocompleteLinkedItems(q));
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<List<AdminCommentResponse>> getPostComments(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getPostComments(id));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        adminService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        adminService.deleteMediaItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
