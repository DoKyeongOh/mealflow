package com.odk.pjt.mealflow.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_accounts_provider_subject", columnNames = {"auth_provider", "auth_subject"}),
            @UniqueConstraint(name = "uk_accounts_user_id", columnNames = {"user_id"})
        })
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "auth_provider", nullable = false, length = 64)
    private String authProvider;

    @Column(name = "auth_subject", nullable = false, length = 1024)
    private String authSubject;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
