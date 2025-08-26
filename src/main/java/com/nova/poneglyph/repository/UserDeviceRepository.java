package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
    Optional<UserDevice> findByUser_IdAndDeviceId(UUID userId, String deviceId);


    Optional<UserDevice> findByUserAndDeviceId(User user, String deviceId);

    List<UserDevice> findByUser(User user);
}
