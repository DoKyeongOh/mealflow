package com.odk.pjt.mealflow.security;

import com.odk.pjt.mealflow.user.AccountProvisioningService;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class AccountLinkageOidcUserService extends OidcUserService {

    private final AccountProvisioningService accountProvisioningService;

    public AccountLinkageOidcUserService(AccountProvisioningService accountProvisioningService) {
        this.accountProvisioningService = accountProvisioningService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Long userId = accountProvisioningService.findOrCreateForGoogle(oidcUser);
        return new MealflowOidcUser(oidcUser, userId);
    }
}
