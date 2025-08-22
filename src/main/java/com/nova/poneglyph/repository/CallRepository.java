package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.message.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CallRepository extends JpaRepository<Call, UUID> {
}
