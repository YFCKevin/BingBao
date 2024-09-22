package com.yfckevin.bingBao.controller;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.LoginDTO;
import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.entity.Member;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.repository.MemberRepository;
import com.yfckevin.bingBao.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.swing.text.html.Option;
import java.text.SimpleDateFormat;
import java.util.Optional;

@Controller
public class LoginController {
    Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;
    private final MemberService memberService;

    public LoginController(@Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties, MemberService memberService) {
        this.sdf = sdf;
        this.configProperties = configProperties;
        this.memberService = memberService;
    }

    @PostMapping("/loginCheck")
    public ResponseEntity<?> loginCheck (@RequestBody LoginDTO loginDTO, HttpSession session){

        logger.info("[loginCheck]");
        ResultStatus resultStatus = new ResultStatus();
        System.out.println(loginDTO.getAccount() +"/"+ loginDTO.getPassword());

        Optional<Member> memberOpt = memberService.findByAccountAndPassword(loginDTO.getAccount(), loginDTO.getPassword());
        if (memberOpt.isEmpty()) {
            resultStatus.setCode("C004");
            resultStatus.setMessage("查無會員");
        } else {
            final Member member = memberOpt.get();
            MemberDTO dto = new MemberDTO();
            dto.setName(member.getName());
            dto.setAccount(member.getAccount());
            session.setAttribute("member", dto);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(configProperties.getGlobalDomain() + "dashboard.html");
        }
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout (HttpSession session){
        logger.info("[logout]");
        ResultStatus resultStatus = new ResultStatus();

        session.removeAttribute("member");
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }
}
