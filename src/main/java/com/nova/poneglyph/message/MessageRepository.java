//package com.nova.poneglyph.message;
//
//import jakarta.transaction.Transactional;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//@Repository
//public interface MessageRepository extends JpaRepository<Message, String> {
//
//    @Query(name = MessageConstants.FIND_MESSAGES_BY_CHAT_ID)
//    List<Message> findMessagesByChatId(@Param("chatId") String chatId);
//
//    @Query(name = MessageConstants.SET_MESSAGES_TO_SEEN_BY_CHAT)
//    @Modifying
//    void setMessagesToSeenByChatId(@Param("chatId") String chatId, @Param("newState") MessageState state);
//
//    List<Message> findAllByChat_Id(String chatId);
//
////
////    @Modifying
////    @Transactional
////    @Query("""
////    UPDATE Message m
////    SET m.state = :state
////    WHERE m.chat.id = :chatId
////      AND m.receiverId = :receiverId
////      AND m.state <> :state
////""")
////    int setMessagesToSeenByChatIdAndReceiverId(
////            @Param("chatId") String chatId,
////            @Param("receiverId") String receiverId,
////            @Param("state") MessageState state
////    );
//@Modifying
//@Query("UPDATE Message m SET m.state = :state " +
//        "WHERE m.chat.id = :chatId AND m.receiverId = :receiverId AND m.state != :state")
//void setMessagesToSeenByChatIdAndReceiverId(@Param("chatId") String chatId,
//                                            @Param("receiverId") String receiverId,
//                                            @Param("state") MessageState state);
//
//    List<Message> findMessagesByChatIdAndReceiverIdAndStateNot(String chat_id, String receiverId, MessageState state);
//}
