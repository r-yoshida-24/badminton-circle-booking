package com.badminton.dto;

import jakarta.validation.constraints.NotBlank;

public record LineAuthRequest(
        @NotBlank(message = "IDトークンは必須です。") String idToken,
        Boolean friendshipChecked
) {
}
