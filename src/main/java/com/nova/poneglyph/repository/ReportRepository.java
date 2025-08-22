package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.moderation.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}
