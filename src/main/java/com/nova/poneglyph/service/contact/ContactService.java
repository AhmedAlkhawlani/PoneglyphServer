//package com.nova.poneglyph.service.contact;
//
//
//
//import com.nova.poneglyph.domain.enums.SyncStatus;
//import com.nova.poneglyph.domain.user.Contact;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.dto.contactDto.ContactDto;
//import com.nova.poneglyph.dto.contactDto.ContactSyncRequest;
//import com.nova.poneglyph.exception.ContactException;
//
//import com.nova.poneglyph.repository.ContactRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.service.presence.PresenceService;
//import com.nova.poneglyph.util.PhoneUtil;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//
//import static com.nova.poneglyph.util.PhoneUtil.MIN_PHONE_LENGTH;
//
//
//@Service
//@RequiredArgsConstructor
//public class ContactService {
//
//    private final Logger log = LoggerFactory.getLogger(ContactService.class);
//    private final ContactRepository contactRepository;
//    private final UserRepository userRepository;
//    private final PhoneUtil phoneUtil;
//    private final PresenceService presenceService;
//
//
//
////@Transactional
////public void syncContacts(UUID userId, ContactSyncRequest syncDto) {
////    User user = userRepository.findById(userId)
////            .orElseThrow(() -> new ContactException("User not found"));
////
////    List<Contact> newContacts = new ArrayList<>();
////
////    for (ContactDto contactDto : syncDto.getContacts()) {
////        String normalized = PhoneUtil.normalizePhone(contactDto.getPhone());
////
////        // تخطي الأرقام القصيرة جداً
////        if (normalized.length() < MIN_PHONE_LENGTH) {
////            continue;
////        }
////
////        Contact contact = contactRepository.findByUserAndNormalizedPhone(user, normalized)
////                .orElse(new Contact());
////
////        contact.setUser(user);
////        contact.setContactPhone(contactDto.getPhone());
////        contact.setContactName(contactDto.getName());
////        contact.setNormalizedPhone(normalized);
////        contact.setLastSeen(presenceService.getLastActiveForPhone(normalized));
////        contact.setRegistered(userRepository.existsByNormalizedPhone(normalized));
////        contact.setBlocked(false);
////        contact.setSyncStatus(SyncStatus.SYNCED);
////
////        newContacts.add(contact);
////    }
////
////    // Delete old contacts not in new list
////    List<String> phoneList = newContacts.stream()
////            .map(Contact::getNormalizedPhone)
////            .toList();
////
////    contactRepository.deleteByUserAndNotInList(user, phoneList);
////
////    // Save new contacts
////    contactRepository.saveAll(newContacts);
////}
//
//    // تحويل رموز الدولة الرقمية الشائعة إلى ISO (محدود وموسع حسب حاجتك)
//    private String numericCountryToIso(String numeric) {
//        if (numeric == null) return "YE";
//        return switch (numeric) {
//            case "966" -> "SA";
//            case "967" -> "YE";
//            case "971" -> "AE";
//            case "973" -> "BH";
//            case "968" -> "OM";
//            case "974" -> "QA";
//            case "965" -> "KW";
//            default -> "YE"; // fallback
//        };
//    }
//    @Transactional
//    public void syncContacts(UUID userId, ContactSyncRequest syncDto) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ContactException("User not found"));
//
//        String defaultNumeric = user.getCountryCode() != null ? user.getCountryCode() : "967";
//        String defaultRegion = numericCountryToIso(defaultNumeric); // نفس الخريطة لديك
//
//        // نستخدم خريطة لمنع الازدواج بنفس normalizedPhone ونحافظ على آخر إدخال
//        Map<String, Contact> unique = new LinkedHashMap<>();
//
//        for (ContactDto contactDto : syncDto.getContacts()) {
//            String raw = contactDto.getPhone();
//            String normalized = PhoneUtil.normalizeWithFallbacks(raw, defaultRegion, defaultNumeric);
//
//            if (normalized == null || normalized.length() < MIN_PHONE_LENGTH) {
//                // سجل للتحليل لاحقاً (لا تحذف السجلات القديمة هنا)
//                log.debug("Skipping contact (cannot normalize): user={} rawPhone={}", userId, raw);
//                continue;
//            }
//
//            // حاول الحصول على صيغة العرض E.164 إن أمكن
//            String e164 = PhoneUtil.normalizeToE164(raw, defaultRegion);
//
//            // إذا كان موجودًا مسبقاً في الخريطة استبدل/حدّث البيانات (نأخذ آخر إسم مثلاً)
//            Contact contact = unique.getOrDefault(normalized, new Contact());
//            contact.setUser(user);
//            contact.setContactPhone(e164 != null ? e164 : raw); // أفضل حفظ E.164 إن أمكن
//            contact.setContactName(contactDto.getName());
//            contact.setNormalizedPhone(normalized);
//            contact.setLastSeen(presenceService.getLastActiveForPhone(normalized));
//            contact.setRegistered(userRepository.existsByNormalizedPhone(normalized));
//            contact.setBlocked(false);
//            contact.setSyncStatus(SyncStatus.SYNCED);
//
//            unique.put(normalized, contact);
//        }
//
//        List<String> phoneList = new ArrayList<>(unique.keySet());
//
//        // حذف السجلات القديمة التي لم تعد موجودة
//        contactRepository.deleteByUserAndNotInList(user, phoneList);
//
//        // حفظ (أو تحديث) جهات الاتصال الجديدة
//        contactRepository.saveAll(unique.values().stream().toList());
//    }
//
//    @Transactional
//    public void blockContact(UUID userId, String phone) {
//        User user = userRepository.findById(userId).orElseThrow();
//        String defaultNumeric = user.getCountryCode() != null ? user.getCountryCode() : "967";
//        String defaultRegion = numericCountryToIso(defaultNumeric);
//
//        String normalized = PhoneUtil.normalizeWithFallbacks(phone, defaultRegion, defaultNumeric);
//        if (normalized == null) {
//            throw new ContactException("Cannot normalize phone for block operation");
//        }
//
//        Contact contact = contactRepository.findByUserAndNormalizedPhone(user, normalized)
//                .orElseGet(() -> {
//                    Contact c = new Contact();
//                    c.setUser(user);
//                    c.setContactPhone(PhoneUtil.normalizeToE164(phone, defaultRegion)); // optional
//                    c.setNormalizedPhone(normalized);
//                    return c;
//                });
//
//        contact.setBlocked(true);
//        contactRepository.save(contact);
//    }
//
////    @Transactional
////    public void blockContact(UUID userId, String phone) {
////        String normalized = phoneUtil.normalizePhone(phone);
////        Contact contact = contactRepository.findByUserAndNormalizedPhone(
////                userRepository.findById(userId).orElseThrow(),
////                normalized
////        ).orElseGet(() -> {
////            Contact newContact = new Contact();
////            newContact.setUser(userRepository.findById(userId).orElseThrow());
////            newContact.setContactPhone(phone);
////            newContact.setNormalizedPhone(normalized);
////            return newContact;
////        });
////
////        contact.setBlocked(true);
////        contactRepository.save(contact);
////    }
//
//    @Transactional
//    public void unblockContact(UUID userId, String phone) {
//        User user = userRepository.findById(userId).orElseThrow();
//        String defaultRegion = numericCountryToIso(user.getCountryCode());
//        String normalized = PhoneUtil.normalizeForStorage(phone, defaultRegion);
//
//        contactRepository.findByUserAndNormalizedPhone(user, normalized)
//                .ifPresent(contact -> {
//                    contact.setBlocked(false);
//                    contactRepository.save(contact);
//                });
//    }
//
////    @Transactional
////    public void unblockContact(UUID userId, String phone) {
////        String normalized = phoneUtil.normalizePhone(phone);
////        contactRepository.findByUserAndNormalizedPhone(
////                userRepository.findById(userId).orElseThrow(),
////                normalized
////        ).ifPresent(contact -> {
////            contact.setBlocked(false);
////            contactRepository.save(contact);
////        });
////    }
//
//    @Transactional(readOnly = true)
//    public List<ContactDto> getContacts(UUID userId) {
//        return contactRepository.findByUser(
//                userRepository.findById(userId).orElseThrow()
//        ).stream().map(this::convertToDto).toList();
//    }
//
//    private ContactDto convertToDto(Contact contact) {
//        return new ContactDto(
//                contact.getContactPhone(),
//                contact.getContactName(),
//                contact.isRegistered(),
//                presenceService.isUserOnlineForPhone(contact.getNormalizedPhone()),
//                contact.getLastSeen(),
//                contact.isBlocked()
//        );
//    }
//}

package com.nova.poneglyph.service.contact;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.nova.poneglyph.domain.enums.SyncStatus;
import com.nova.poneglyph.domain.user.Contact;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.contactDto.ContactDto;
import com.nova.poneglyph.dto.contactDto.ContactSyncRequest;
import com.nova.poneglyph.exception.ContactException;
import com.nova.poneglyph.repository.ContactRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.service.presence.PresenceService;
import com.nova.poneglyph.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.nova.poneglyph.util.PhoneUtil.MIN_PHONE_LENGTH;

@Service
@RequiredArgsConstructor
@Log4j2
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final PresenceService presenceService;

    private static final String APP_DEFAULT_REGION = "YE";

    /**
     * يحوّل رمز الدولة الرقمي (مثلاً "967") إلى رمز الـ ISO باستخدام libphonenumber.
     * إذا فشل الإستخراج يعيد APP_DEFAULT_REGION.
     */
    private String numericCountryToIso(String numeric) {
        if (numeric == null || numeric.isBlank()) return APP_DEFAULT_REGION;
        try {
            int countryCode = Integer.parseInt(numeric);
            String region = PhoneNumberUtil.getInstance().getRegionCodeForCountryCode(countryCode);
            return region != null ? region : APP_DEFAULT_REGION;
        } catch (NumberFormatException ex) {
            log.warn("numericCountryToIso: invalid numeric country '{}', fallback to {}", numeric, APP_DEFAULT_REGION);
            return APP_DEFAULT_REGION;
        }
    }

    @Transactional
    public void syncContacts(UUID userId, ContactSyncRequest syncDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ContactException("User not found"));

        String userCountryNumeric = user.getCountryCode() != null ? user.getCountryCode() : "967";
        String defaultRegion = numericCountryToIso(userCountryNumeric);

        Map<String, Contact> unique = new LinkedHashMap<>();

        for (ContactDto contactDto : syncDto.getContacts()) {
            String raw = contactDto.getPhone();

            // نمرّر رمز الدولة الرقمي للمستخدم كـ fallback
            String normalized = PhoneUtil.normalizeWithFallbacks(raw, defaultRegion, userCountryNumeric);

            if (normalized == null || normalized.length() < MIN_PHONE_LENGTH) {
                log.debug("Skipping contact (cannot normalize): user={} rawPhone={}", userId, raw);
                continue;
            }

            String e164 = PhoneUtil.normalizeToE164(raw, defaultRegion);

            Contact contact = unique.getOrDefault(normalized, new Contact());
            contact.setUser(user);
            contact.setContactPhone(e164 != null ? e164 : raw);
            contact.setContactName(contactDto.getName());
            contact.setNormalizedPhone(normalized);
            contact.setLastSeen(presenceService.getLastActiveForPhone(normalized));
            contact.setRegistered(userRepository.existsByNormalizedPhone(normalized));
            contact.setBlocked(false);
            contact.setSyncStatus(SyncStatus.SYNCED);

            unique.put(normalized, contact);
        }

        List<String> phoneList = new ArrayList<>(unique.keySet());

        contactRepository.deleteByUserAndNotInList(user, phoneList);

        contactRepository.saveAll(unique.values().stream().toList());
    }

    @Transactional
    public void blockContact(UUID userId, String phone) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ContactException("User not found"));
        String userCountryNumeric = user.getCountryCode() != null ? user.getCountryCode() : "967";
        String defaultRegion = numericCountryToIso(userCountryNumeric);

        String normalized = PhoneUtil.normalizeWithFallbacks(phone, defaultRegion, userCountryNumeric);
        if (normalized == null) {
            throw new ContactException("Cannot normalize phone for block operation");
        }

        Contact contact = contactRepository.findByUserAndNormalizedPhone(user, normalized)
                .orElseGet(() -> {
                    Contact c = new Contact();
                    c.setUser(user);
                    c.setContactPhone(PhoneUtil.normalizeToE164(phone, defaultRegion));
                    c.setNormalizedPhone(normalized);
                    return c;
                });

        contact.setBlocked(true);
        contactRepository.save(contact);
    }

    @Transactional
    public void unblockContact(UUID userId, String phone) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ContactException("User not found"));
        String userCountryNumeric = user.getCountryCode() != null ? user.getCountryCode() : "967";
        String defaultRegion = numericCountryToIso(userCountryNumeric);

        String normalized = PhoneUtil.normalizeWithFallbacks(phone, defaultRegion, userCountryNumeric);
        if (normalized == null) {
            log.debug("Cannot normalize phone for unblock operation: user={}, phone={}", userId, phone);
            return;
        }

        contactRepository.findByUserAndNormalizedPhone(user, normalized)
                .ifPresent(contact -> {
                    contact.setBlocked(false);
                    contactRepository.save(contact);
                });
    }

    @Transactional(readOnly = true)
    public List<ContactDto> getContacts(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ContactException("User not found"));
        return contactRepository.findByUser(user).stream().map(this::convertToDto).toList();
    }

    private ContactDto convertToDto(Contact contact) {
        return new ContactDto(
                contact.getContactPhone(),
                contact.getContactName(),
                contact.isRegistered(),
                presenceService.isUserOnlineForPhone(contact.getNormalizedPhone()),
                contact.getLastSeen(),
                contact.isBlocked()
        );
    }
    @Transactional
    public List<ContactDto> sync(User currentUser, ContactSyncRequest request) {
        return request.getContacts().stream()
                .map(dto -> {
                    String normalized = PhoneUtil.normalizeWithFallbacks(
                            dto.getPhone(),
                            currentUser.getCountryCode(), // ISO
                            currentUser.getCountryCode()   // numeric
                    );
                    if (normalized == null) {
                        return null; // skip
                    }

                    // check if another user has this number
                    User found = userRepository.findByNormalizedPhone(normalized).orElse(null);

                    Contact contact = Contact.builder()
                            .user(currentUser)
                            .normalizedPhone(normalized)
                            .contactPhone(dto.getPhone())
                            .contactName(dto.getName())
                            .registered(found != null)
                            .lastSeen(found != null ? found.getLastActive() : null)
                            .blocked(false)
                            .build();

                    contactRepository.save(contact);

                    return new ContactDto(
                            dto.getPhone(),
                            dto.getName(),
                            found != null,
                            found != null && found.isOnline(),
                            found != null ? found.getLastActive() : null,
                            false
                    );
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }
}


//
//package com.nova.poneglyph.service.contact;
//
//import com.nova.poneglyph.domain.enums.SyncStatus;
//import com.nova.poneglyph.domain.user.Contact;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.dto.contactDto.ContactDto;
//import com.nova.poneglyph.dto.contactDto.ContactSyncRequest;
//import com.nova.poneglyph.exception.ContactException;
//import com.nova.poneglyph.repository.ContactRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.service.presence.PresenceService;
//import com.nova.poneglyph.util.PhoneUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//
//import static com.nova.poneglyph.util.PhoneUtil.MIN_PHONE_LENGTH;
//
//@Service
//@RequiredArgsConstructor
//@Log4j2
//public class ContactService {
//
//    private final ContactRepository contactRepository;
//    private final UserRepository userRepository;
//    private final PresenceService presenceService;
//
//    /**
//     * تحويل رمز الدولة الرقمي إلى ISO region قصير (خريطة صغيرة قابلة للتوسيع).
//     * مثال: "967" -> "YE", "966" -> "SA"
//     */
//    private String numericCountryToIso(String numeric) {
//        if (numeric == null) return "YE";
//        return switch (numeric) {
//            case "966" -> "SA";
//            case "967" -> "YE";
//            case "971" -> "AE";
//            case "973" -> "BH";
//            case "968" -> "OM";
//            case "974" -> "QA";
//            case "965" -> "KW";
//            default -> "YE";
//        };
//    }
//
//    @Transactional
//    public void syncContacts(UUID userId, ContactSyncRequest syncDto) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ContactException("User not found"));
//
//        String userCountryNumeric = user.getCountryCode() != null ? user.getCountryCode() : "967";
//        String defaultRegion = numericCountryToIso(userCountryNumeric);
//
//        // استخدام LinkedHashMap للحفاظ على ترتيب جهات الاتصال كما أرسلها المستخدم
//        Map<String, Contact> unique = new LinkedHashMap<>();
//
//        for (ContactDto contactDto : syncDto.getContacts()) {
//            String raw = contactDto.getPhone();
//
//            // نحاول التوحيد مع fallback إلى رمز الدولة للمستخدم الحالي إن لزم
//            String normalized = PhoneUtil.normalizeWithFallbacks(raw, defaultRegion, userCountryNumeric);
//
//            if (normalized == null || normalized.length() < MIN_PHONE_LENGTH) {
//                log.debug("Skipping contact (cannot normalize): user={} rawPhone={}", userId, raw);
//                continue;
//            }
//
//            // صيغة العرض E.164 إن وجدت
//            String e164 = PhoneUtil.normalizeToE164(raw, defaultRegion);
//
//            Contact contact = unique.getOrDefault(normalized, new Contact());
//            contact.setUser(user);
//            contact.setContactPhone(e164 != null ? e164 : raw);
//            contact.setContactName(contactDto.getName());
//            contact.setNormalizedPhone(normalized);
//            contact.setLastSeen(presenceService.getLastActiveForPhone(normalized));
//            contact.setRegistered(userRepository.existsByNormalizedPhone(normalized));
//            contact.setBlocked(false);
//            contact.setSyncStatus(SyncStatus.SYNCED);
//
//            unique.put(normalized, contact);
//        }
//
//        List<String> phoneList = new ArrayList<>(unique.keySet());
//
//        // حذف السجلات القديمة غير المدرجة في اللائحة الجديدة
//        contactRepository.deleteByUserAndNotInList(user, phoneList);
//
//        // حفظ/تحديث السجلات الجديدة
//        contactRepository.saveAll(unique.values().stream().toList());
//    }
//
//    @Transactional
//    public void blockContact(UUID userId, String phone) {
//        User user = userRepository.findById(userId).orElseThrow(() -> new ContactException("User not found"));
//        String userCountryNumeric = user.getCountryCode() != null ? user.getCountryCode() : "967";
//        String defaultRegion = numericCountryToIso(userCountryNumeric);
//
//        String normalized = PhoneUtil.normalizeWithFallbacks(phone, defaultRegion, userCountryNumeric);
//        if (normalized == null) {
//            throw new ContactException("Cannot normalize phone for block operation");
//        }
//
//        Contact contact = contactRepository.findByUserAndNormalizedPhone(user, normalized)
//                .orElseGet(() -> {
//                    Contact c = new Contact();
//                    c.setUser(user);
//                    c.setContactPhone(PhoneUtil.normalizeToE164(phone, defaultRegion));
//                    c.setNormalizedPhone(normalized);
//                    return c;
//                });
//
//        contact.setBlocked(true);
//        contactRepository.save(contact);
//    }
//
//    @Transactional
//    public void unblockContact(UUID userId, String phone) {
//        User user = userRepository.findById(userId).orElseThrow(() -> new ContactException("User not found"));
//        String userCountryNumeric = user.getCountryCode() != null ? user.getCountryCode() : "967";
//        String defaultRegion = numericCountryToIso(userCountryNumeric);
//
//        String normalized = PhoneUtil.normalizeWithFallbacks(phone, defaultRegion, userCountryNumeric);
//        if (normalized == null) {
//            log.debug("Cannot normalize phone for unblock operation: user={}, phone={}", userId, phone);
//            return;
//        }
//
//        contactRepository.findByUserAndNormalizedPhone(user, normalized)
//                .ifPresent(contact -> {
//                    contact.setBlocked(false);
//                    contactRepository.save(contact);
//                });
//    }
//
//    @Transactional(readOnly = true)
//    public List<ContactDto> getContacts(UUID userId) {
//        User user = userRepository.findById(userId).orElseThrow(() -> new ContactException("User not found"));
//        return contactRepository.findByUser(user).stream().map(this::convertToDto).toList();
//    }
//
//    private ContactDto convertToDto(Contact contact) {
//        return new ContactDto(
//                contact.getContactPhone(),
//                contact.getContactName(),
//                contact.isRegistered(),
//                presenceService.isUserOnlineForPhone(contact.getNormalizedPhone()),
//                contact.getLastSeen(),
//                contact.isBlocked()
//        );
//    }
//}
