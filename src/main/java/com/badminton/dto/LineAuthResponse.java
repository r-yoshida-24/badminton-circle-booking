package com.badminton.dto;

public record LineAuthResponse(boolean success, String redirectUrl, String message) {
}
