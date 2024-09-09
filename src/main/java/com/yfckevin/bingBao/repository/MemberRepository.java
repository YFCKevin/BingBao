package com.yfckevin.bingBao.repository;

import com.yfckevin.bingBao.entity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByAccountAndPassword(String account, String password);
}
