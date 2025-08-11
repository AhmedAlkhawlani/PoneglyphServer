//package com.nova.poneglyph.message;
//
//import org.springframework.stereotype.Service;
//
//@Service
//public class MessageMapper {
////    public MessageResponse toMessageResponse(Message message) {
////        if (message.getCreatedDate() == null) {
////            System.out.println("CreatedDate is null for message ID: " + message.getId());
////        }
////
////        return MessageResponse.builder()
////                .id(message.getId())
////                .content(message.getContent())
////                .senderId(message.getSenderId())
////                .receiverId(message.getReceiverId())
////                .type(message.getType())
////                .state(message.getState())
////                .createdAt(message.getCreatedDate()) // قد تكون null
////                .media(FileUtils.readFileFromLocation(message.getMediaFilePath()))
////                .build();
////    }
////public MessageResponse toMessageResponse(Message message) {
////    byte[] mediaData = null;
////    if (message.getMediaFilePath() != null && !message.getMediaFilePath().isEmpty()) {
////        mediaData = FileUtils.readFileFromLocation(message.getMediaFilePath());
////    }
////
////    return MessageResponse.builder()
////            .id(message.getId())
////            .content(message.getContent())
////            .senderId(message.getSenderId())
////            .receiverId(message.getReceiverId())
////            .type(message.getType())
////            .state(message.getState())
////            .createdAt(message.getCreatedDate())
////            .media(mediaData)
////            .build();
////}
//
//    public MessageResponse toMessageResponse(Message message) {
////        String base64Media = null;
////        if (message.getMediaFilePath() != null) {
////            byte[] mediaBytes = FileUtils.readFileFromLocation(message.getMediaFilePath());
////            if(mediaBytes != null){
////                base64Media = Base64.getEncoder().encodeToString(mediaBytes);
////            }
////        }
//        String mediaUrl;
//        if (message.getMediaFilePath() != null&&!message.getMediaFilePath().isEmpty()) {
//            mediaUrl ="http://192.168.1.102:8080/media/" + extractUserAndFile(message.getMediaFilePath());
//        }else {
//            mediaUrl =null;
//        }
////        String mediaUrl = message.getMediaFilePath() != null
////                ? "http://10.0.2.2:8080/media/" + extractUserAndFile(message.getMediaFilePath())
////                : null;
////        String mediaUrl = null;
////        if (message.getMediaFilePath() != null) {
////            mediaUrl = "http://localhost:8080/media/" + Paths.get(message.getMediaFilePath()).getFileName().toString();
////        }
//
//        return MessageResponse.builder()
//                .id(message.getId())
//                .content(message.getContent())
//                .senderId(message.getSenderId())
//                .receiverId(message.getReceiverId())
//                .type(message.getType())
//                .state(message.getState())
//                .createdAt(message.getCreatedDate())
//                .media(mediaUrl)
////                .media(base64Media)
//                .build();
//    }
//    private String extractUserAndFile(String path) {
//        // input: ./uploads/users/14b59572-7115-4c68-8c92-38a73e3c2d02/1752886282803.mp4
//        return path.replace("./uploads/users/", "");
//    }
//}
