package com.nova.poneglyph.util;//package com.nova.poneglyph.util;
//
//import com.nova.poneglyph.domain.user.Contact;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.dto.contactDto.ContactDto;
//import com.nova.poneglyph.dto.contactDto.ContactSyncRequest;
//import com.nova.poneglyph.repository.ContactRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.service.contact.ContactService;
//import com.nova.poneglyph.service.presence.PresenceService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//
//import java.time.OffsetDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
//public class ContactServiceTest {
//
//    @Mock
//    ContactRepository contactRepository;
//
//    @Mock
//    UserRepository userRepository;
//
//    @Mock
//    PresenceService presenceService;
//
//    @InjectMocks
//    ContactService contactService;
//
//    private UUID userId;
//    private User user;
//
//    @BeforeEach
//    public void setup() {
//        userId = UUID.randomUUID();
//        user = new User();
//        user.setId(userId);
//        user.setCountryCode("967"); // Yemen as example
//    }
//
//    @Test
//    public void testSyncContacts_mixedFormats_usesUserCountryAsFallback() {
//        // arrange
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(presenceService.getLastActiveForPhone(anyString())).thenReturn(null);
//        when(contactRepository.findByUserAndNormalizedPhone(any(User.class), anyString())).thenReturn(Optional.empty());
//
//        // create sync request with mixed phones
//        List<ContactDto> dtos = List.of(
//                new ContactDto("+967712345678", "A", false, false, null, false),   // E.164 Yemen
//                new ContactDto("712345678", "B", false, false, null, false),       // local without leading zero
//                new ContactDto("0712345678", "C", false, false, null, false),      // trunk prefix
//                new ContactDto("00967712345678", "D", false, false, null, false),  // 00 prefix international
//                new ContactDto("+966512345678", "E", false, false, null, false),   // other country (Saudi)
//                new ContactDto("123", "SHORT", false, false, null, false)          // too short -> should be skipped
//        );
//
//        ContactSyncRequest req = new ContactSyncRequest();
//        req.setContacts(dtos);
//
//        // act
//        contactService.syncContacts(userId, req);
//
//        // assert: capture arguments passed to deleteByUserAndNotInList and saveAll
//        ArgumentCaptor<List<String>> deleteCaptor = ArgumentCaptor.forClass(List.class);
//        verify(contactRepository, times(1)).deleteByUserAndNotInList(eq(user), deleteCaptor.capture());
//        List<String> deletedList = deleteCaptor.getValue();
//
//        // expected normalized set includes 967... and 966...
//        assertTrue(deletedList.contains("967712345678"));
//        assertTrue(deletedList.contains("966512345678"));
//
//        ArgumentCaptor<Collection> saveCaptor = ArgumentCaptor.forClass(Collection.class);
//        verify(contactRepository, times(1)).saveAll(saveCaptor.capture());
//        Collection saved = saveCaptor.getValue();
//        assertNotNull(saved);
//        boolean has967 = saved.stream().anyMatch(o -> {
//            Contact c = (Contact) o;
//            return "967712345678".equals(c.getNormalizedPhone());
//        });
//        boolean has966 = saved.stream().anyMatch(o -> {
//            Contact c = (Contact) o;
//            return "966512345678".equals(c.getNormalizedPhone());
//        });
//        assertTrue(has967, "should save contact normalized 967...");
//        assertTrue(has966, "should save contact normalized 966...");
//
//        boolean hasShort = saved.stream().anyMatch(o -> {
//            Contact c = (Contact) o;
//            return "123".equals(c.getNormalizedPhone());
//        });
//        assertFalse(hasShort, "too short numbers must be skipped");
//    }
//
//    @Test
//    public void testBlockContact_createsAndBlocksWhenMissing() {
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(contactRepository.findByUserAndNormalizedPhone(eq(user), anyString())).thenReturn(Optional.empty());
//        when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0)); // echo saved contact
//
//        // act
//        contactService.blockContact(userId, "712345678"); // local form -> should normalize as 967712345678
//
//        // assert
//        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
//        verify(contactRepository, times(1)).save(captor.capture());
//        Contact saved = captor.getValue();
//        assertNotNull(saved);
//        assertEquals("967712345678", saved.getNormalizedPhone());
//        assertTrue(saved.isBlocked());
//    }
//
//    @Test
//    public void testUnblockContact_existingContact() {
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        Contact existing = new Contact();
//        existing.setUser(user);
//        existing.setNormalizedPhone("967712345678");
//        existing.setBlocked(true);
//
//        when(contactRepository.findByUserAndNormalizedPhone(eq(user), eq("967712345678")))
//                .thenReturn(Optional.of(existing));
//        when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        // act
//        contactService.unblockContact(userId, "712345678"); // should normalize to 967712345678 and unblock
//
//        // assert
//        assertFalse(existing.isBlocked(), "contact should have been unblocked");
//        verify(contactRepository, times(1)).save(existing);
//    }
//}



import com.nova.poneglyph.domain.enums.AccountStatus;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.contactDto.ContactDto;
import com.nova.poneglyph.dto.contactDto.ContactSyncRequest;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.service.contact.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(ContactService.class)
public class ContactServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactService contactService;

    @Test
    public void testSyncContacts_withRegisteredAndUnregistered() {
        // Create current user
        User currentUser = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+967712345678")
                .countryCode("YE")
                .normalizedPhone("967712345678")
                .verified(true)
                .accountStatus(AccountStatus.ACTIVE)
                .lastActive(OffsetDateTime.now())
                .online(true)
                .build();
        userRepository.save(currentUser);

        // Create another registered user (contact)
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+966512345678")
                .countryCode("SA")
                .normalizedPhone("966512345678")
                .verified(true)
                .accountStatus(AccountStatus.ACTIVE)
                .lastActive(OffsetDateTime.now().minusMinutes(5))
                .online(false)
                .build();
        userRepository.save(otherUser);

        // Prepare request
        List<ContactDto> contacts = List.of(
                new ContactDto("+966512345678", "Ali", false, false, null, false), // موجود بالـ DB
                new ContactDto("0712345678", "Omar", false, false, null, false),   // محلي
                new ContactDto("123", "Invalid", false, false, null, false)        // قصير → skip
        );
        ContactSyncRequest req = new ContactSyncRequest();
        req.setContacts(contacts);

        // Act
        List<ContactDto> result = contactService.sync(currentUser, req);

        // Assert
        assertEquals(2, result.size()); // واحد skip
        ContactDto first = result.get(0);
        assertTrue(first.isRegistered()); // موجود
        assertEquals("Ali", first.getName());

        ContactDto second = result.get(1);
        assertFalse(second.isRegistered()); // مو موجود
        assertEquals("Omar", second.getName());
    }
}
