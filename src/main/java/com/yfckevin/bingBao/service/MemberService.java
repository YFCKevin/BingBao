package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Member;

import java.util.Optional;

public interface MemberService {
    Optional<Member> findByAccountAndPassword(String account, String password);
}
