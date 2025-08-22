package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.user.Contact;
import com.nova.poneglyph.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByUserAndNormalizedPhone(User user, String normalizedPhone);
    @Transactional
    @Modifying
    @Query("DELETE FROM Contact c WHERE c.user = :user AND c.normalizedPhone NOT IN :phones")
    void deleteByUserAndNotInList(@Param("user") User user, @Param("phones") List<String> phones);
    List<Contact> findByUser(User user);


}
