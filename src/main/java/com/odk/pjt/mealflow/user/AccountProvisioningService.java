package com.odk.pjt.mealflow.user;

import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AccountProvisioningService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AccountProvisioningService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Resolves or creates {@link User} + {@link Account} for Google OIDC ({@code sub} is {@link Account#getAuthSubject()}).
     */
    @Transactional
    public Long findOrCreateForGoogle(OidcUser oidcUser) {
        String provider = OAuthProviderType.GOOGLE.persistedId();
        String subject = oidcUser.getSubject();
        Optional<Account> existing = accountRepository.findByAuthProviderAndAuthSubject(provider, subject);
        if (existing.isPresent()) {
            User user = existing.get().getUser();
            syncDisplayNameIfMissing(user, oidcUser);
            return user.getId();
        }
        return createUserAndAccount(oidcUser, provider, subject);
    }

    private void syncDisplayNameIfMissing(User user, OidcUser oidcUser) {
        String name = oidcUser.getFullName();
        if (!StringUtils.hasText(name)) {
            return;
        }
        if (!StringUtils.hasText(user.getDisplayName())) {
            user.setDisplayName(name);
        }
    }

    private Long createUserAndAccount(OidcUser oidcUser, String provider, String subject) {
        try {
            User user = new User();
            user.setCreatedAt(Instant.now());
            user.setDisplayName(oidcUser.getFullName());
            userRepository.save(user);

            Account account = new Account();
            account.setUser(user);
            account.setAuthProvider(provider);
            account.setAuthSubject(subject);
            account.setCreatedAt(Instant.now());
            accountRepository.save(account);

            return user.getId();
        } catch (DataIntegrityViolationException ex) {
            return accountRepository
                    .findByAuthProviderAndAuthSubject(provider, subject)
                    .map(a -> a.getUser().getId())
                    .orElseThrow(() -> ex);
        }
    }
}
