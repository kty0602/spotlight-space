package com.spotlightspace.core.auth.email;

import com.spotlightspace.core.auth.email.dto.MatchMailRequestDto;
import com.spotlightspace.core.auth.email.dto.SendMailRequestDto;
import jakarta.mail.MessagingException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
@Slf4j
public class MailController {

    private final MailService mailService;

    /**
     * 이메일 인증을 진행합니다. redis를 사용하여 5분 ttl을 두었습니다.
     *
     * @param mailRequest 메일을 입력합니다.
     * @return 보낼 메일에 진짜로 회원가입 문자가 날라가요! 조심해주세요~
     */
    @PostMapping("/mailSend")
    public ResponseEntity<HashMap<String, Object>> mailSend(@RequestBody SendMailRequestDto mailRequest)
            throws MessagingException {
        //유저 체크 로직
        mailService.emailUserCehck(mailRequest.getMail());

        // 비동기 작업 시작
        mailService.sendMail(mailRequest.getMail());

        HashMap<String, Object> immediateResponse = new HashMap<>();
        immediateResponse.put("success", Boolean.TRUE);
        immediateResponse.put("message", "이메일을 확인해 주세요.");

        return ResponseEntity.ok(immediateResponse);
    }


    /**
     * 메일에 보내준 번호를 체크합니다, 레디스에 저장된 값을 꺼내옵니다, redis에 저장될 키는
     *
     * @param machMailRequestDto 이메일과, 인증번호를 입력해줍니다
     * @return 결과값은 true false로 반환해줍니다.
     */
    @GetMapping("/mailCheck")
    public ResponseEntity<Boolean> mailCheck(@RequestBody MatchMailRequestDto machMailRequestDto) {

        boolean isMatch = mailService.mailCheck(machMailRequestDto);

        return ResponseEntity.ok(isMatch);
    }
}
