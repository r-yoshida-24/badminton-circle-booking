package com.badminton.security;

import com.badminton.config.LineProperties;
import com.badminton.exception.LineAuthenticationException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NimbusLineIdTokenVerifierTest {

    private RSAPrivateKey privateKey;
    private LineIdTokenVerifier verifier;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        LineProperties lineProperties = new LineProperties();
        lineProperties.setChannelId("test-channel-id");
        lineProperties.setIssuer("https://access.line.me");

        verifier = new NimbusLineIdTokenVerifier(NimbusJwtDecoder.withPublicKey(publicKey).build(), lineProperties);
    }

    @Test
    void verifyReturnsClaimsWhenTokenIsValid() throws Exception {
        String token = token("U123", Instant.now().plusSeconds(300), List.of("test-channel-id"), privateKey);

        LineIdTokenClaims claims = verifier.verify(token);

        assertThat(claims.subject()).isEqualTo("U123");
        assertThat(claims.displayName()).isEqualTo("テスト太郎");
    }

    @Test
    void verifyFailsWhenSignatureIsInvalid() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair otherKeyPair = keyPairGenerator.generateKeyPair();
        String token = token("U123", Instant.now().plusSeconds(300), List.of("test-channel-id"), (RSAPrivateKey) otherKeyPair.getPrivate());

        assertThatThrownBy(() -> verifier.verify(token))
                .isInstanceOf(LineAuthenticationException.class)
                .hasMessageContaining("検証に失敗");
    }

    @Test
    void verifyFailsWhenTokenExpired() throws Exception {
        String token = token("U123", Instant.now().minusSeconds(60), List.of("test-channel-id"), privateKey);

        assertThatThrownBy(() -> verifier.verify(token))
                .isInstanceOf(LineAuthenticationException.class)
                .hasMessageContaining("検証に失敗");
    }

    @Test
    void verifyFailsWhenAudienceMismatched() throws Exception {
        String token = token("U123", Instant.now().plusSeconds(300), List.of("different-channel"), privateKey);

        assertThatThrownBy(() -> verifier.verify(token))
                .isInstanceOf(LineAuthenticationException.class)
                .hasMessageContaining("検証に失敗");
    }

    private String token(String subject, Instant expiresAt, List<String> audience, RSAPrivateKey signingKey) throws JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("https://access.line.me")
                .audience(audience)
                .expirationTime(Date.from(expiresAt))
                .issueTime(Date.from(Instant.now()))
                .claim("name", "テスト太郎")
                .build();
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
                claimsSet
        );
        signedJWT.sign(new RSASSASigner(signingKey));
        return signedJWT.serialize();
    }
}
