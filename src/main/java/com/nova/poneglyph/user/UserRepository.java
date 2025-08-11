//package com.nova.poneglyph.user;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface UserRepository extends JpaRepository<User, String> {
//
//    @Query(name = UserConstants.FIND_USER_BY_EMAIL)
//    Optional<User> findByEmail(@Param("email") String userEmail);
//
//    @Query(name = UserConstants.FIND_ALL_USERS_EXCEPT_SELF)
//    List<User> findAllUsersExceptSelf(@Param("publicId") String publicId);
//
//    @Query(name = UserConstants.FIND_USER_BY_PUBLIC_ID)
//    Optional<User> findByPublicId(@Param("publicId") String senderId);
//
//        boolean existsByEmail(String email);
////    Optional<User> findByEmail(String email);
//
//    Optional<User> findByName( String name);
//
//    boolean existsByName(String attr0);
//}
