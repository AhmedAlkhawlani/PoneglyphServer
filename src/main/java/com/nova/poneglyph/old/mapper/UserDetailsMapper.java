//package com.nova.poneglyph.mapper;
//
//import com.nova.poneglyph.model.UserProfile;
//import com.nova.poneglyph.dto.UserDetailsDto;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class UserDetailsMapper {
//    public static UserDetailsDto toDTO(UserProfile entity) {
//        if ( entity == null ) {
//            return null;
//        }
//
//        UserDetailsDto dto = new UserDetailsDto();
//
//        dto.setCity(entity.getCity());
//        dto.setCountry(entity.getCountry());
//        dto.setFirstName(entity.getFirstName());
//        dto.setLastName(entity.getLastName());
//
//
//        dto.setProfilePicture(entity.getProfilePicture());
//
//        dto.setAboutMe(entity.getAboutMe());
//        dto.setStatus(entity.getStatus());
//
//
//
//
//
//
//
//        return dto;
//    }
//
//
//
//    public static List<UserDetailsDto> dtoList(List<UserProfile> entityList) {
//        if ( entityList == null ) {
//            return null;
//        }
//
//        List<UserDetailsDto> dtoList = new ArrayList<UserDetailsDto>( entityList.size() );
//        for ( UserProfile entity : entityList ) {
//            dtoList.add( toDTO( entity ) );
//        }
//
//        return dtoList;
//    }
//    public static UserProfile toEntity(UserDetailsDto dto) {
//        if (dto == null) return null;
//
//        UserProfile entity = new UserProfile();
//
//        entity.setCity(dto.getCity());
//        entity.setCountry(dto.getCountry());
//        entity.setFirstName(dto.getFirstName());
//        entity.setLastName(dto.getLastName());
//
//        entity.setProfilePicture(dto.getProfilePicture());
//        entity.setStatus(dto.getStatus());
//        entity.setAboutMe(dto.getAboutMe());
//        return entity;
//    }
////    public static void map(UserDetailsDto request, UserProfile toUpdate) {
////        if (request == null || toUpdate == null) {
////            return;
////        }
////
////        if (request.getFirstName() != null) {
////            toUpdate.setFirstName(request.getFirstName());
////        }
////
////        if (request.getLastName() != null) {
////            toUpdate.setLastName(request.getLastName());
////        }
////
////        if (request.getCity() != null) {
////            toUpdate.setCity(request.getCity());
////        }
////
////        if (request.getCountry() != null) {
////            toUpdate.setCountry(request.getCountry());
////        }
////        if (request.getStatus() != null) {
////            toUpdate.setStatus(request.getStatus());
////        }
////
//////        if (request.getAddress() != null) {
//////            toUpdate.setAddress(request.getAddress());
//////        }
//////
//////        if (request.getPhoneNumber() != null) {
//////            toUpdate.setPhoneNumber(request.getPhoneNumber());
//////        }
////
////        if (request.getProfilePicture() != null) {
////            toUpdate.setProfilePicture(request.getProfilePicture());
////        }
////
//////        if (request.getPostalCode() != null) {
//////            toUpdate.setPostalCode(request.getPostalCode());
//////        }
////
////        if (request.getAboutMe() != null) {
////            toUpdate.setAboutMe(request.getAboutMe());
////        }
////    }
//public static void map(UserDetailsDto request, UserProfile toUpdate) {
//    if (request == null || toUpdate == null) {
//        return;
//    }
//
//    if (request.getFirstName() != null) {
//        toUpdate.setFirstName(request.getFirstName());
//    }
//
//    if (request.getLastName() != null) {
//        toUpdate.setLastName(request.getLastName());
//    }
//
//    if (request.getCity() != null) {
//        toUpdate.setCity(request.getCity());
//    }
//
//    if (request.getCountry() != null) {
//        toUpdate.setCountry(request.getCountry());
//    }
//
//    if (request.getStatus() != null) {
//        toUpdate.setStatus(request.getStatus());
//    }
//
//    // ❌ احذف هذا السطر:
//    // if (request.getProfilePicture() != null) {
//    //     toUpdate.setProfilePicture(request.getProfilePicture());
//    // }
//
//    if (request.getAboutMe() != null) {
//        toUpdate.setAboutMe(request.getAboutMe());
//    }
//}
//
//    public static void mapWithoutProfilePicture(UserDetailsDto request, UserProfile toUpdate) {
//        if (request == null || toUpdate == null) return;
//
//        if (request.getFirstName() != null) {
//            toUpdate.setFirstName(request.getFirstName());
//        }
//
//        if (request.getLastName() != null) {
//            toUpdate.setLastName(request.getLastName());
//        }
//
//        if (request.getCity() != null) {
//            toUpdate.setCity(request.getCity());
//        }
//
//        if (request.getCountry() != null) {
//            toUpdate.setCountry(request.getCountry());
//        }
//
//        if (request.getStatus() != null) {
//            toUpdate.setStatus(request.getStatus());
//        }
//
//        // لا تضع `profilePicture`
//        if (request.getAboutMe() != null) {
//            toUpdate.setAboutMe(request.getAboutMe());
//        }
//    }
//
//}
