package com.nova.poneglyph.service.moderation;



import com.nova.poneglyph.domain.enums.BanType;
import com.nova.poneglyph.domain.enums.ReportStatus;
import com.nova.poneglyph.domain.moderation.Report;
import com.nova.poneglyph.domain.moderation.SystemBan;
import com.nova.poneglyph.domain.moderation.UserBlock;
import com.nova.poneglyph.domain.user.User;

import com.nova.poneglyph.dto.moderationDto.ReportDto;
import com.nova.poneglyph.exception.ModerationException;

import com.nova.poneglyph.repository.ReportRepository;
import com.nova.poneglyph.repository.SystemBanRepository;

import com.nova.poneglyph.repository.UserBlockRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final UserBlockRepository userBlockRepository;
    private final SystemBanRepository systemBanRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void blockUser(UUID blockerId, UUID blockedId, String reason, boolean silent) {
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new ModerationException("Blocker not found"));

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new ModerationException("Blocked user not found"));

        UserBlock block = new UserBlock();
        block.setBlocker(blocker);
        block.setBlocked(blocked);
        block.setBlockReason(reason);
        block.setSilent(silent);
        block.setCreatedAt(OffsetDateTime.now());

        userBlockRepository.save(block);
    }

    @Transactional
    public void unblockUser(UUID blockerId, UUID blockedId) {
//        userBlockRepository.deleteByBlockerAndBlocked(
//                userRepository.findById(blockerId).orElseThrow(),
//                userRepository.findById(blockedId).orElseThrow()
//        );
    }

    @Transactional
    public void banUser(String phone, BanType banType, String reason, String details, UUID bannedBy) {
        String normalized = PhoneUtil.normalizePhone(phone);

        // Deactivate any existing bans
//        systemBanRepository.deactivateExistingBans(normalized);

        SystemBan ban = new SystemBan();
        ban.setPhoneNumber(phone);
        ban.setNormalizedPhone(normalized);
        ban.setBanType(banType);
        ban.setBanReason(reason);
        ban.setReasonDetails(details);
        ban.setBannedBy(userRepository.findById(bannedBy).orElse(null));
        ban.setCreatedAt(OffsetDateTime.now());

        if (banType == BanType.TEMPORARY) {
            ban.setExpiresAt(OffsetDateTime.now().plusDays(7));
        }

        systemBanRepository.save(ban);
    }

    @Transactional
    public Report createReport(ReportDto reportDto, UUID reporterId) {
        Report report = new Report();
        report.setReporter(userRepository.findById(reporterId).orElse(null));

        if (reportDto.getReportedUserId() != null) {
            report.setReportedUser(userRepository.findById(reportDto.getReportedUserId()).orElse(null));
        } else {
            report.setReportedPhone(reportDto.getReportedPhone());
        }

        report.setReportType(reportDto.getReportType());
        report.setReportDetails(reportDto.getReportDetails());
        report.setCreatedAt(OffsetDateTime.now());

        return reportRepository.save(report);
    }

    @Transactional
    public void resolveReport(Long reportId, String adminNotes, ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ModerationException("Report not found"));

        report.setStatus(status);
        report.setAdminNotes(adminNotes);
        report.setResolvedAt(OffsetDateTime.now());

        reportRepository.save(report);
    }
}
