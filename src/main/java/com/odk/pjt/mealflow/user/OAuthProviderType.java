package com.odk.pjt.mealflow.user;

/**
 * Supported OAuth/OIDC providers. The {@link #persistedId()} value is stored in {@link Account#getAuthProvider()}.
 */
public enum OAuthProviderType {
    GOOGLE("google");

    private final String persistedId;

    OAuthProviderType(String persistedId) {
        this.persistedId = persistedId;
    }

    /** String written to the database; keep stable across releases. */
    public String persistedId() {
        return persistedId;
    }
}
