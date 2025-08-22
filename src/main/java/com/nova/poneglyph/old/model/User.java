////package com.nova.poneglyph.model;
////
////import com.nova.poneglyph.user.enums.Active;
////import com.nova.poneglyph.user.enums.Role;
////import jakarta.persistence.*;
////import lombok.*;
////import org.springframework.security.core.GrantedAuthority;
////import org.springframework.security.core.authority.SimpleGrantedAuthority;
////import org.springframework.security.core.userdetails.UserDetails;
////
////import java.util.Collection;
////import java.util.List;
////
////
////@Entity(name = "users")
////@Builder
////@AllArgsConstructor
////@NoArgsConstructor
////@Getter
////@Setter
////public class User extends BaseEntity implements UserDetails {
////    @Column(unique = true, nullable = false)
////    private String username;
////
////    @Column(nullable = false)
////    private String password;
////
////    @Column(unique = true, nullable = false, updatable = false)
////    private String email;
////
////    @Enumerated(EnumType.STRING)
////    private Role role;
////
////    @Enumerated(EnumType.STRING)
////    private Active active;
////
////    @Embedded
////    private Details userDetails;
////
////    @Override
////    public Collection<? extends GrantedAuthority> getAuthorities() {
////        return List.of(new SimpleGrantedAuthority(role.name()));
////    }
////
////
////    @Override
//////    public String getUsername() {
//////        return email;
//////    }
////    public String getUsername() {
////        return username;
////    }
////
////    @Override
////    public boolean isAccountNonExpired() {
////        return true;
////    }
////
////    @Override
////    public boolean isAccountNonLocked() {
////        return true;
////    }
////
////    @Override
////    public boolean isCredentialsNonExpired() {
////        return true;
////    }
////
////    @Override
////    public boolean isEnabled() {
////        return true;
////    }
////
////
////    @Override
////    public String toString() {
////        return "User{" +
////
////                ", email='" + email + '\'' +
////                ", name='" + username + '\'' +
////
////                ", password='" + password + '\'' +
////                ", role='" + role + '\'' +
////                '}';
////    }
////}
//package com.nova.poneglyph.model;
//
//
//import com.nova.poneglyph.enums.old.user.enums.Active;
//import com.nova.poneglyph.enums.old.user.enums.Role;
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.time.LocalDateTime;
//import java.util.Collection;
//import java.util.List;
//
//@Entity(name = "users")
//@Table(name = "users",
//        indexes = {
//                @Index(name = "idx_user_phone", columnList = "phoneNumber", unique = true),
//                @Index(name = "idx_user_email", columnList = "email", unique = true)
//        })
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class User extends BaseEntity implements UserDetails {
//
//    @Column(name = "phone_number", unique = true, nullable = false, length = 20)
//    private String phoneNumber;
//
//    @Column(nullable = false)
//    private String password;
//
//    @Column(unique = true, nullable = false, updatable = false)
//    private String email;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private Role role;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    private Active active = Active.ACTIVE;
//
//    /**
//     *
//     *
//     @Embedded تعني أن الخصائص داخل UserProfile سيتم تخزينها داخل جدول users نفسه، وليست في جدول منفصل.
//     وهذا يعني:
//
//     لن يتم إنشاء جدول منفصل باسم user_profile.
//     لا يمكن أن يكون UserProfile كائنًا مستقلاً يُحدث أو يُحفظ بمفرده.
//     إذا كان toUpdate.getProfile() يعيد null، فأنت بحاجة إلى توليد كائن جديد وربطه يدويًا.
//     لا يوجد Cascade أو علاقة مستقلة، لأن Embedded ليس @OneToOne.
//     * */
//    @Embedded
//    private UserProfile profile;
//
////    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
////    @JoinColumn(name = "profile_id")
////    private UserProfile profile;
//
//
//
//    // حقول الحالة الحية
//    @Column(name = "is_online")
//    private boolean online = false;
//
//    @Column(name = "last_seen")
//    private LocalDateTime lastSeen;
//
//    @Column(name = "current_session_id", length = 100)
//    private String currentWebSocketSessionId;
//
//    // فهرس للبحث السريع
//    @Column(name = "phone_number_index", length = 20)
//    private String phoneNumberIndex;
//
//    @PrePersist
//    @PreUpdate
//    private void setIndexes() {
//        this.phoneNumberIndex = this.phoneNumber;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//
//        return List.of(new SimpleGrantedAuthority(role.name()));
//    }
//
//    @Override
//    public String getUsername() {
//        return phoneNumber; // استخدام رقم الهاتف كاسم مستخدم
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return active != Active.LOCKED;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return active == Active.ACTIVE;
//    }
//}
