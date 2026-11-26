package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank String password) {
}