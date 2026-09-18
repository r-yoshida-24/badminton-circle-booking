package com.badminton.security;

public interface LineIdTokenVerifier {

    LineIdTokenClaims verify(String idToken);
}
