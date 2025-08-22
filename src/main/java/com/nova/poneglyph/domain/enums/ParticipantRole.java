package com.nova.poneglyph.domain.enums;

public enum ParticipantRole {

    OWNER, ADMIN, MEMBER;

    public boolean isAdmin() {
        return this == ADMIN || this == OWNER; // نعتبر OWNER عنده صلاحيات أعلى من ADMIN
    }

    public boolean isOwner() {
        return this == OWNER;
    }


}
