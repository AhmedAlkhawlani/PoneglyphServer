package com.nova.poneglyph.user.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nova.poneglyph.user.dto.UserDetailsDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateRequest {
    @NotBlank(message = "Id is required")
    private String id;
    private String username;
    private String password;
    private UserDetailsDto userDetails;
}
