package com.badminton.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "line")
public class LineProperties {

    private String liffId;
    private String channelId;
    private String channelSecret;
    private String issuer = "https://access.line.me";
    private String jwkSetUri = "https://api.line.me/oauth2/v2.1/certs";
    private boolean friendshipCheckEnabled = true;

    public String getLiffId() {
        return liffId;
    }

    public void setLiffId(String liffId) {
        this.liffId = liffId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelSecret() {
        return channelSecret;
    }

    public void setChannelSecret(String channelSecret) {
        this.channelSecret = channelSecret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public boolean isFriendshipCheckEnabled() {
        return friendshipCheckEnabled;
    }

    public void setFriendshipCheckEnabled(boolean friendshipCheckEnabled) {
        this.friendshipCheckEnabled = friendshipCheckEnabled;
    }
}
