package com.odk.pjt.mealflow.account;

import java.util.Optional;
import com.odk.pjt.mealflow.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAuthProviderAndAuthSubject(String authProvider, String authSubject);
}
