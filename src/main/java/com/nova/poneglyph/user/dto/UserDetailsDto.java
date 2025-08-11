package com.nova.poneglyph.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Embeddable;
import lombok.*;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsDto {
    private String firstName;
    private String lastName;
//    private String phoneNumber;
    private String country;
    private String city;
    private String status; // حالة المستخدم الظاهرة للآخرين
    private String aboutMe;
    private String profilePicture;
}
