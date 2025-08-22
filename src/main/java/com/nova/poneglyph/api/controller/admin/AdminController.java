package com.nova.poneglyph.api.controller.admin;



import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<User>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.listUsers(pageable));
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable UUID userId,
            @RequestParam String reason) {
        adminService.banUser(userId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<Void> unbanUser(
            @PathVariable UUID userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok().build();
    }
}
