package com.example.messenger.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.messenger.record.UserResponse;
import com.example.messenger.service.OnlineOfflineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final OnlineOfflineService onlineOfflineService;

    @GetMapping("/online")
    @PreAuthorize("hasAuthority('ADMIN')")
    List<UserResponse> getOnlineUsers() {
        return onlineOfflineService.getOnlineUsers();
    }

    @GetMapping("/subscriptions")
    @PreAuthorize("hasAuthority('ADMIN')")
    Map<String, Set<String>> getSubscriptions() {
        return onlineOfflineService.getUserSubscribed();
    }
}