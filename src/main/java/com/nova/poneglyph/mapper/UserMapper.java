package com.nova.poneglyph.mapper;

import com.nova.poneglyph.user.dto.AuthUserDto;
import com.nova.poneglyph.user.dto.UserDto;
import com.nova.poneglyph.model.User;
import com.nova.poneglyph.user.request.UserUpdateRequest;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {
    //    public static UserDto toDTO(User entity) {
//        if ( entity == null ) {
//            return null;
//        }
//
//        UserDto dto = new UserDto();
//
//        dto.setId( entity.getId() );
//        dto.setName( entity.getName() );
//        dto.setActive( entity.getActive() );
//        dto.setComCode( entity.getComCode() );
//        dto.setAddedBy( entity.getAddedBy() );
//        dto.setDate( entity.getDate() );
//        dto.setCreatedAt( entity.getCreatedAt() );
//        dto.setUpdatedAt( entity.getUpdatedAt() );
//        dto.setUpdatedBy( entity.getUpdatedBy() );
//        dto.setEmail( entity.getEmail() );
//        dto.setUsername( entity.getUsername() );
//        dto.setPassword( entity.getPassword() );
//        dto.setRole( entity.getRole() );
//        dto.setLanguage( entity.getLanguage() );
//        dto.setPermissionRolesId(entity.getPermissionRoles().getId());
//        dto.setPermissionRoles(PermissionRoleMapper.toDTO(entity.getPermissionRoles()));
//
//        return dto;
//    }
    public static UserDto toDTO(User entity, Boolean withDetails) {
        if ( entity == null ) {
            return null;
        }

        UserDto dto = new UserDto();


        dto.setId( entity.getId() );
        dto.setEmail( entity.getEmail() );
        dto.setUsername( entity.getUsername() );
//        dto.setRole( entity.getRole() );
        dto.setOnline(entity.isOnline());
        dto.setLastSeen( entity.getLastSeen() );

        if ( withDetails ) {
            dto.setUserDetails(UserDetailsMapper.toDTO(entity.getProfile()));

        }

        return dto;
    }

    public static AuthUserDto toDTOAuth(User entity) {
        if ( entity == null ) {
            return null;
        }

        AuthUserDto dto = new AuthUserDto();


        dto.setId( entity.getId() );
        dto.setPassword( entity.getPassword() );
        dto.setUsername( entity.getUsername() );
        dto.setRole( entity.getRole() );


        return dto;
    }



    public static List<UserDto> dtoList(List<User> entityList, Boolean withRolls) {
        if ( entityList == null ) {
            return null;
        }

        List<UserDto> dtoList = new ArrayList<UserDto>( entityList.size() );
        for ( User entity : entityList ) {
            dtoList.add( toDTO( entity , withRolls ) );
        }

        return dtoList;
    }

    public static void map(UserUpdateRequest request, User toUpdate) {
        if (request.getUsername() != null) {
            toUpdate.setPhoneNumber(request.getUsername());
        }

        if (request.getPassword() != null) {
            toUpdate.setPassword(request.getPassword()); // يفضل تشفيرها هنا إن لزم
        }

        if (request.getUserDetails() != null) {
            toUpdate.setProfile(UserDetailsMapper.toEntity(request.getUserDetails()));
        }
    }


}
