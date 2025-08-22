package com.nova.poneglyph.dto;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivacySettings {
    private Boolean lastSeenVisible;
    private Boolean profilePhotoVisible;
    private Boolean statusVisible;
}
