//package com.nova.poneglyph.model;//package com.nova.poneglyph.model;
////
////import com.fasterxml.jackson.annotation.JsonInclude;
////import jakarta.persistence.Embeddable;
////import lombok.*;
////
////
////
////@Embeddable
////@Builder
////@AllArgsConstructor
////@NoArgsConstructor
////@Getter
////@Setter
////@JsonInclude(JsonInclude.Include.NON_NULL)
////public class Details {
////    private String firstName;
////    private String lastName;
////    private String phoneNumber;
////    private String country;
////    private String city;
////    private String address;
////    private String postalCode;
////    private String aboutMe;
////    private String profilePicture;
////}
////package com.nova.poneglyph.user.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Embeddable
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class UserProfile {
////    @Id
////    @GeneratedValue(strategy = GenerationType.UUID)
////    private String id;
//    @Column(name = "first_name", length = 50)
//    private String firstName;
//
//    @Column(name = "last_name", length = 50)
//    private String lastName;
//
//    @Column(name = "profile_picture_url", length = 255)
//    private String profilePicture;
//
//    @Column(name = "about_me", length = 255)
//    private String aboutMe;
//
//    @Column(name = "country", length = 50)
//    private String country;
//
//    @Column(name = "city", length = 50)
//    private String city;
//
//    @Column(name = "status", length = 100)
//    private String status; // حالة المستخدم الظاهرة للآخرين
//}
