package com.yfckevin.bingBao.utils;

import com.yfckevin.bingBao.controller.NotifyController;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailUtils {
    static Logger logger = LoggerFactory.getLogger(MailUtils.class);
    private static JavaMailSender sender;

    @Autowired
    public void setSender(JavaMailSender sender) {
        MailUtils.sender = sender;
    }

    /**
     * 發送email
     * @param receiverMail  接收者信箱
     * @param subject    主題
     * @param content    內容
     */
    public static void sendMail(String receiverMail, String subject, String content) {

        String[] receiver = {receiverMail};

        SimpleMailMessage message = new SimpleMailMessage();
        // 接收者信箱
        message.setTo(receiver);
        // 主旨
        message.setSubject(subject);
        // 內容
        message.setText(content);

        sender.send(message);
    }


    public static void sendHtmlMail(String receiverMail, String subject, String content) {
        MimeMessage message = sender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(receiverMail);
            helper.setSubject(subject);
            helper.setText(content, true);

            sender.send(message);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
