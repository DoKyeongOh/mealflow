package com.odk.pjt.mealflow.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAuthProviderAndAuthSubject(String authProvider, String authSubject);
}
