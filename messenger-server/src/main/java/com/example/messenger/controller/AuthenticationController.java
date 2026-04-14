package com.example.messenger.controller;

import com.example.messenger.entity.RoleEntity;
import com.example.messenger.entity.UserEntity;
import com.example.messenger.enums.Role;
import com.example.messenger.model.*;
import com.example.messenger.repository.RoleRepository;
import com.example.messenger.repository.UserRepository;
import com.example.messenger.utils.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Users userDetails = (Users) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);

        List<String> roles =
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        JwtResponse jwtResponse =
                JwtResponse.builder()
                        .token(jwt)
                        .id(userDetails.getId())
                        .username(userDetails.getUsername())
                        .email(userDetails.getEmail())
                        .roles(roles)
                        .build();

        response.addCookie(new Cookie("access_token", jwt));
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        UserEntity userEntity =
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .username(signUpRequest.getUsername())
                        .email(signUpRequest.getEmail())
                        .password(encoder.encode(signUpRequest.getPassword()))
                        .build();

        Set<String> strRoles = signUpRequest.getRole();
        Set<RoleEntity> roles = new HashSet<>();

        if (strRoles == null) {
            RoleEntity userRole =
                    roleRepository
                            .findByName(Role.USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(
                    role -> {
                        switch (role) {
                            case "ADMIN":
                                RoleEntity adminRole =
                                        roleRepository
                                                .findByName(Role.ADMIN)
                                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                roles.add(adminRole);

                                break;
                            case "MODERATOR":
                                RoleEntity modRole =
                                        roleRepository
                                                .findByName(Role.MODERATOR)
                                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                roles.add(modRole);

                                break;
                            default:
                                RoleEntity userRole =
                                        roleRepository
                                                .findByName(Role.USER)
                                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                roles.add(userRole);
                        }
                    });
        }

        userEntity.setRoles(roles);
        userRepository.save(userEntity);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
