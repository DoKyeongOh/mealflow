package com.odk.pjt.mealflow.user;

import com.odk.pjt.mealflow.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}
