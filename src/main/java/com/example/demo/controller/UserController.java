package com.example.demo.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        })
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        return userService.getCurrentUser(principal.getUsername());
    }

    @PostMapping
    @Operation(summary = "Register a user")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
        })
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.addUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/me")
            .build()
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @PutMapping("/me")
    @Operation(summary = "Replace the authenticated user's public profile", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
        })
    public UserResponse updateCurrentUser(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateCurrentUser(principal.getUsername(), request);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete the authenticated user's account", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        })
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        userService.deleteCurrentUser(principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}

