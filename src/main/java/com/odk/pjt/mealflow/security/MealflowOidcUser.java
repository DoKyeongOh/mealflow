package com.odk.pjt.mealflow.security;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Wraps the delegate {@link OidcUser} from Google and exposes the internal {@link com.odk.pjt.mealflow.user.User} id for
 * app authorization (M-04).
 */
public final class MealflowOidcUser implements OidcUser {

    private final OidcUser delegate;
    private final Long userId;

    public MealflowOidcUser(OidcUser delegate, Long userId) {
        this.delegate = delegate;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
