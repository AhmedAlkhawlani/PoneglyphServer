////package com.nova.poneglyph.service;
////
////import com.nova.poneglyph.model.UserToken;
////import com.nova.poneglyph.repository.UserTokenRepository;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////
////import java.util.Date;
////
////@Service
////public class UserTokenService {
////
////    @Autowired
////    private UserTokenRepository userTokenRepository;
////
////    public void saveOrUpdateToken(String userId, String token, Date expiration) {
////        UserToken userToken = userTokenRepository.findByUserId(userId)
////                .orElse(new UserToken());
////        userToken.setUserId(userId);
////        userToken.setToken(token);
////        userToken.setExpiration(expiration);
////        userToken.setValid(true);
////        userTokenRepository.save(userToken);
////    }
////
////    public boolean isTokenValid(String token) {
////        return userTokenRepository.findByToken(token)
////                .filter(UserToken::isValid)
////                .isPresent();
////    }
////
////    public String getCurrentTokenByUserId(String userId) {
////        return userTokenRepository.findByUserId(userId)
////                .map(UserToken::getToken)
////                .orElse(null);
////    }
////    public void invalidateToken(String token) {
////        userTokenRepository.findByToken(token).ifPresent(t -> {
////            t.setValid(false);
////            userTokenRepository.save(t);
////        });
////    }
////
////}
////
//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.model.UserToken;
//import com.nova.poneglyph.repository.UserTokenRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//
//@Service
//public class UserTokenService {
//
//    @Autowired
//    private UserTokenRepository userTokenRepository;
//
////    @Transactional
////    public void saveOrUpdateToken(String userId, String token, Date expiration) {
////        Optional<UserToken> existingToken = userTokenRepository.findByUserId(userId);
////
////        UserToken userToken = existingToken.orElse(new UserToken());
////        userToken.setUserId(userId);
////        userToken.setToken(token);
////        userToken.setExpiration(expiration);
////        userToken.setValid(true);
////
////        userTokenRepository.save(userToken);
////    }
////
////    public boolean isTokenValid(String token) {
////        return userTokenRepository.findByToken(token)
////                .map(t -> t.isValid() && !isTokenExpired(t))
////                .orElse(false);
////    }
//    public void saveOrUpdateToken(String userId, String token, Date expiration) {
//        UserToken userToken = userTokenRepository.findByUserId(userId)
//                .orElse(new UserToken());
//
//        userToken.setUserId(userId);
//        userToken.setToken(token);
//        userToken.setExpiration(expiration);
//        userTokenRepository.save(userToken);
//    }
//
//    public boolean isTokenValid(String token) {
//        return userTokenRepository.findByToken(token)
//                .map(t -> !t.getExpiration().before(new Date()))
//                .orElse(false);
//    }
//    private boolean isTokenExpired(UserToken token) {
//        return token.getExpiration().before(new Date());
//    }
//
//    public String getCurrentTokenByUserId(String userId) {
//        return userTokenRepository.findByUserId(userId)
//                .filter(UserToken::isValid)
//                .map(UserToken::getToken)
//                .orElse(null);
//    }
//
//    @Transactional
//    public void invalidateToken(String token) {
//        userTokenRepository.findByToken(token).ifPresent(t -> {
//            t.setValid(false);
//            userTokenRepository.save(t);
//        });
//    }
//
//    @Transactional
//    public void invalidateAllTokensForUser(String userId) {
//        userTokenRepository.findByUserId(userId).ifPresent(t -> {
//            t.setValid(false);
//            userTokenRepository.save(t);
//        });
//    }
//}
