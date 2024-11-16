package com.spotlightspace.core.auth.email;

import static com.spotlightspace.common.exception.ErrorCode.INVALID_EMAIL_MATCH;
import static com.spotlightspace.common.exception.ErrorCode.SOCIAL_LOGIN_UPDATE_NOT_ALLOWED;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.auth.email.dto.MatchMailRequestDto;
import com.spotlightspace.core.user.domain.User;
import com.spotlightspace.core.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String senderEmail;

    //redis의 ttl을 설정하는 변수입니다.
    private static final long TTL = 5 * 60;

    public HashMap<String, Object> sendMailAndStoreCode(String mail) {
        HashMap<String, Object> responseMap = new HashMap<>();

        //유저의 이메일 체크 로직.
        User user = userService.findUserEmail(mail);

        //소셜로그인일시, 비밀번호 변경을위한 이메일인증 불가.
        if (user.isSocialLogin()) {
            throw new ApplicationException(SOCIAL_LOGIN_UPDATE_NOT_ALLOWED);
        }

        try {
            int number = createRandomNumber();
            MimeMessage message = createMail(mail, number);
            javaMailSender.send(message);

            // Redis에 저장
            String key = "find:password:" + mail;
            redisTemplate.opsForValue().set(key, String.valueOf(number), TTL, TimeUnit.SECONDS);

            responseMap.put("success", Boolean.TRUE);
            responseMap.put("number", number);
        }
        catch (MessagingException exception) {
            log.error("메일 전송 실패 : {}", exception.getMessage());
            responseMap.put("success", Boolean.FALSE);
            responseMap.put("error", "메일 전송에 실패했습니다.");
        }

        return responseMap;
    }

    private int createRandomNumber() {
        return (int) (Math.random() * (900000)) + 100000; // 6자리 랜덤 숫자
    }

    // 메일 메시지 생성
    private MimeMessage createMail(String mail, int number) throws MessagingException {
        mail = mail.trim();
        MimeMessage message = javaMailSender.createMimeMessage();

        // 이메일 형식 체크
        InternetAddress emailAddr = new InternetAddress(mail);
        emailAddr.validate();

        //발신자 이메일 주소를 설정함 (보내는사람 - 혜미.)
        message.setFrom(senderEmail);
        //수신자 이메일을 설정함 (받는사람)
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        //메일 본문
        message.setSubject("이메일 인증");
        String body = "<h3>요청하신 인증 번호입니다.</h3>"
                + "<h1>" + number + "</h1>"
                + "<h3>감사합니다.</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }

    public boolean mailCheck(MatchMailRequestDto machMailRequestDto) {
        //저장된 값을 redis에서 가져옵니다.
        String key = "find:password:" + machMailRequestDto.getEmail();
        String storedNumber = redisTemplate.opsForValue().get(key);

        //값이 있다면 true로 없다면 false로 반환합니다.
        boolean isMatch = storedNumber != null && storedNumber.equals(machMailRequestDto.getUserNumber());
        if (!isMatch) {
            throw new ApplicationException(INVALID_EMAIL_MATCH);
        }

        return true;
    }
}
