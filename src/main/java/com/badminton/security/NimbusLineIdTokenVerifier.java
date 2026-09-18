package com.badminton.security;

import com.badminton.config.LineProperties;
import com.badminton.exception.LineAuthenticationException;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NimbusLineIdTokenVerifier implements LineIdTokenVerifier {

    private final JwtDecoder jwtDecoder;

    public NimbusLineIdTokenVerifier(JwtDecoder jwtDecoder, LineProperties lineProperties) {
        this.jwtDecoder = jwtDecoder;
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(lineProperties.getIssuer());
        OAuth2TokenValidator<Jwt> withAudience = jwt -> jwt.getAudience().stream().anyMatch(lineProperties.getChannelId()::equals)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "LINE IDトークンのaudienceが一致しません。", null)
        );
        if (jwtDecoder instanceof NimbusJwtDecoder nimbusJwtDecoder) {
            nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        }
    }

    @Override
    public LineIdTokenClaims verify(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new LineAuthenticationException("LINE IDトークンが送信されていません。");
        }
        try {
            Jwt jwt = jwtDecoder.decode(idToken);
            return new LineIdTokenClaims(jwt.getSubject(), jwt.getClaimAsString("name"));
        } catch (JwtException ex) {
            throw new LineAuthenticationException("LINE IDトークンの検証に失敗しました。", ex);
        }
    }
}
