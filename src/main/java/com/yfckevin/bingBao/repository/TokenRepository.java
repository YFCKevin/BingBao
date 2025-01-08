package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TokenRepository extends MongoRepository<Token, String> {
    Optional<Token> findByShortStr(String token);
}
