package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<String> followUser(
            @PathVariable Long userId, 
            @RequestParam Long targetId) {
        
        followService.followUser(userId, targetId);
        return ResponseEntity.ok("Solicitud de seguimiento enviada/aceptada");
    }

    @PostMapping("/{userId}/unfollow")
    public ResponseEntity<String> unfollowUser(
            @PathVariable Long userId, 
            @RequestParam Long targetId) {
        
        followService.unfollowUser(userId, targetId);
        return ResponseEntity.ok("Dejado de seguir correctamente");
    }
}