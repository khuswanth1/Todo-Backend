package com.todo.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.todo.app.entity.PushSubscription;
import com.todo.app.entity.User;
import com.todo.app.repository.PushSubscriptionRepository;
import com.todo.app.repository.UserRepository;
import com.todo.app.util.JwtUtil;

@RestController
@RequestMapping("/api/push")
@CrossOrigin(origins = "*")
public class PushController {

    private final PushSubscriptionRepository subRepo;
    private final UserRepository userRepo;

    public PushController(PushSubscriptionRepository subRepo, UserRepository userRepo) {
        this.subRepo = subRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody SubscriptionDto dto) {
        
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        
        String email = JwtUtil.validate(token.replace("Bearer ", ""));
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PushSubscription sub = new PushSubscription();
        sub.setEndpoint(dto.endpoint());
        sub.setP256dh(dto.keys().p256dh());
        sub.setAuth(dto.keys().auth());
        sub.setUser(user);
        
        // Remove existing subscription with same endpoint to avoid duplicates
        subRepo.deleteByEndpoint(dto.endpoint());
        
        subRepo.save(sub);
        
        return ResponseEntity.ok().build();
    }

    public record Keys(String p256dh, String auth) {}
    public record SubscriptionDto(String endpoint, Keys keys) {}
}
