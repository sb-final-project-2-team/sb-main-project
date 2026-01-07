package com.codeit.closet.common.mail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender javaMailSender;

  public void sendResetPasswordMail(String to, String tempPassword) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("[Closet] 비밀번호 초기화 안내");
    message.setText(
        "안녕하세요.\n\n" +
            "요청하신 임시 비밀번호를 안내드립니다.\n\n" +
            "────────────────────────────\n" +
            "임시 비밀번호:  " + tempPassword + "\n" +
            "유효 시간: 3분\n" +
            "────────────────────────────\n\n" +
            "해당 비밀번호로 로그인하신 후,\n" +
            "반드시 새로운 비밀번호로 변경해 주세요.\n\n" +
            "보안을 위해 본 비밀번호는 짧은 시간 동안만 사용 가능합니다.\n" +
            "본인이 요청하지 않은 경우, 이 메일을 무시해 주세요.\n\n" +
            "감사합니다.\n" +
            "Closet 팀 드림"
    );

    javaMailSender.send(message);
  }
}
