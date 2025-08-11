//package com.nova.poneglyph.user;
//
//import com.nova.poneglyph.chat.Chat;
//import com.nova.poneglyph.common.BaseAuditingEntity;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.hibernate.annotations.GenericGenerator;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.time.LocalDateTime;
//import java.util.Collection;
//import java.util.List;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Entity
//@Table(name = "users")
//@NamedQuery(name = UserConstants.FIND_USER_BY_EMAIL,
//            query = "SELECT u FROM User u WHERE u.email = :email"
//)
//@NamedQuery(name = UserConstants.FIND_ALL_USERS_EXCEPT_SELF,
//            query = "SELECT u FROM User u WHERE u.id != :publicId")
//@NamedQuery(name = UserConstants.FIND_USER_BY_PUBLIC_ID,
//            query = "SELECT u FROM User u WHERE u.id = :publicId")
//public class User extends BaseAuditingEntity implements UserDetails {
//
//    private static final int LAST_ACTIVATE_INTERVAL = 5;
//
//    @Id
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
//    @Column(name = "id", updatable = false, nullable = false)
//    private String id;
//
//    private String firstName;
//    private String lastName;
////    @NotBlank(message = "Password is required")
//    private String password;
////    @NotBlank(message = "Name is required")
//    private String name;
//    private String email;
//    private String role;
//    private LocalDateTime lastSeen;
//
//    @OneToMany(mappedBy = "sender")
//    private List<Chat> chatsAsSender;
//
//    @OneToMany(mappedBy = "recipient")
//    private List<Chat> chatsAsRecipient;
//
//    @Transient
//    public boolean isUserOnline() {
//        return lastSeen != null && lastSeen.isAfter(LocalDateTime.now().minusMinutes(LAST_ACTIVATE_INTERVAL));
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(() -> "ROLE_" + role.toUpperCase());
//    }
//
//    @Override
//    public String getUsername() {
//        return name;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return UserDetails.super.isAccountNonExpired();
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return UserDetails.super.isAccountNonLocked();
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return UserDetails.super.isCredentialsNonExpired();
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return UserDetails.super.isEnabled();
//    }
//}
