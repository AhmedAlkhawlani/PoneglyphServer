package com.nova.poneglyph.service.contact;



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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.nova.poneglyph.util.PhoneUtil.MIN_PHONE_LENGTH;


@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final PhoneUtil phoneUtil;
    private final PresenceService presenceService;

//    @Transactional
//    public void syncContacts(UUID userId, ContactSyncRequest syncDto) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ContactException("User not found"));
//
//        List<Contact> newContacts = new ArrayList<>();
//
//        for (ContactDto contactDto : syncDto.getContacts()) {
//            String normalized = phoneUtil.normalizePhone(contactDto.getPhone());
//
//            Contact contact = contactRepository.findByUserAndNormalizedPhone(user, normalized)
//                    .orElse(new Contact());
//
//            contact.setUser(user);
//            contact.setContactPhone(contactDto.getPhone());
//            contact.setContactName(contactDto.getName());
//            contact.setNormalizedPhone(normalized);
//            contact.setLastSeen(presenceService.getLastActiveForPhone(normalized));
//            contact.setRegistered(userRepository.existsByNormalizedPhone(normalized));
//            contact.setBlocked(false);
//            contact.setSyncStatus(SyncStatus.SYNCED);
//
//            newContacts.add(contact);
//        }
//
//        // Delete old contacts not in new list
//
//        //في ContactService, قبل استدعاءها قم بتحويل قائمة Contact إلى قائمة الهواتف:
//        List<String> phoneList = newContacts.stream()
//                .map(Contact::getNormalizedPhone)
//                .toList();
//
//        contactRepository.deleteByUserAndNotInList(user, phoneList);
//
////        contactRepository.deleteByUserAndNotInList(user, newContacts);
//
//        // Save new contacts
//        contactRepository.saveAll(newContacts);
//    }

@Transactional
public void syncContacts(UUID userId, ContactSyncRequest syncDto) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ContactException("User not found"));

    List<Contact> newContacts = new ArrayList<>();

    for (ContactDto contactDto : syncDto.getContacts()) {
        String normalized = PhoneUtil.normalizePhone(contactDto.getPhone());

        // تخطي الأرقام القصيرة جداً
        if (normalized.length() < MIN_PHONE_LENGTH) {
            continue;
        }

        Contact contact = contactRepository.findByUserAndNormalizedPhone(user, normalized)
                .orElse(new Contact());

        contact.setUser(user);
        contact.setContactPhone(contactDto.getPhone());
        contact.setContactName(contactDto.getName());
        contact.setNormalizedPhone(normalized);
        contact.setLastSeen(presenceService.getLastActiveForPhone(normalized));
        contact.setRegistered(userRepository.existsByNormalizedPhone(normalized));
        contact.setBlocked(false);
        contact.setSyncStatus(SyncStatus.SYNCED);

        newContacts.add(contact);
    }

    // Delete old contacts not in new list
    List<String> phoneList = newContacts.stream()
            .map(Contact::getNormalizedPhone)
            .toList();

    contactRepository.deleteByUserAndNotInList(user, phoneList);

    // Save new contacts
    contactRepository.saveAll(newContacts);
}
    @Transactional
    public void blockContact(UUID userId, String phone) {
        String normalized = phoneUtil.normalizePhone(phone);
        Contact contact = contactRepository.findByUserAndNormalizedPhone(
                userRepository.findById(userId).orElseThrow(),
                normalized
        ).orElseGet(() -> {
            Contact newContact = new Contact();
            newContact.setUser(userRepository.findById(userId).orElseThrow());
            newContact.setContactPhone(phone);
            newContact.setNormalizedPhone(normalized);
            return newContact;
        });

        contact.setBlocked(true);
        contactRepository.save(contact);
    }

    @Transactional
    public void unblockContact(UUID userId, String phone) {
        String normalized = phoneUtil.normalizePhone(phone);
        contactRepository.findByUserAndNormalizedPhone(
                userRepository.findById(userId).orElseThrow(),
                normalized
        ).ifPresent(contact -> {
            contact.setBlocked(false);
            contactRepository.save(contact);
        });
    }

    @Transactional(readOnly = true)
    public List<ContactDto> getContacts(UUID userId) {
        return contactRepository.findByUser(
                userRepository.findById(userId).orElseThrow()
        ).stream().map(this::convertToDto).toList();
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
}
