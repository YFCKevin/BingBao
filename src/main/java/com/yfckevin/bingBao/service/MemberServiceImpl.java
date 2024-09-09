package com.yfckevin.bingBao.service;

import com.yfckevin.bingBao.entity.Member;
import com.yfckevin.bingBao.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService{
    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Optional<Member> findByAccountAndPassword(String account, String password) {
        return memberRepository.findByAccountAndPassword(account, password);
    }
}
