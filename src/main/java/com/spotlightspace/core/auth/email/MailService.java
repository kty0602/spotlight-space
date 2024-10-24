package com.spotlightspace.core.auth.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String senderEmail;
    private static int number;

    public static void createNumber() {
        number = (int) (Math.random() * (90000)) + 100000;
    }

    public MimeMessage CreateMail(String mail) {
        //메일의 앞뒤 공백을 제거해줌
        mail = mail.trim();
        // 랜덤으로 인증번호를 생성해줌
        createNumber();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            // 이메일 형식을 체크함
            InternetAddress emailAddr = new InternetAddress(mail);
            emailAddr.validate();

            //발신자 이메일 주소를 설정함 (보내는사람 - 혜미.)
            message.setFrom(senderEmail);
            //수신자 이메일을 설정함 (받는사람)
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            //메일 본문입니다
            message.setSubject("이메일 인증");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
            body += "<h1>" + number + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body, "UTF-8", "html");
        }
        catch (AddressException exception) {
            System.out.println("잘못된 이메일 형식: " + mail);
            exception.printStackTrace();
        }
        catch (MessagingException exception) {
            exception.printStackTrace();
        }
        return message;
    }

    // 이메일을 전송합니다
    public int sendMail(String mail) {
        MimeMessage message = CreateMail(mail);
        javaMailSender.send(message);

        return number;
    }
}
