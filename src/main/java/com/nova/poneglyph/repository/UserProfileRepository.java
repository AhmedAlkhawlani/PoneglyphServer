package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.user.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
