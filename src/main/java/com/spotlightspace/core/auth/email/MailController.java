package com.spotlightspace.core.auth.email;

import com.spotlightspace.core.auth.email.dto.SendMailRequestDto;
import com.spotlightspace.core.auth.email.dto.MatchMailRequestDto;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
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
public class MailController {

    private final MailService mailService;
    private final StringRedisTemplate redisTemplate;
    //redis의 ttl을 설정하는 변수입니다.
    private static final long TTL = 5 * 60;

    /**
     * 이메일 인증을 진행합니다. redis를 사용하여 5분 ttl을 두었습니다.
     *
     * @param mailRequest 메일을 입력합니다.
     * @return 보낼 메일에 진짜로 회원가입 문자가 날라가요! 조심해주세요~
     */
    @PostMapping("/mailSend")
    public ResponseEntity<HashMap<String, Object>> mailSend(@RequestBody SendMailRequestDto mailRequest) {
        HashMap<String, Object> map = new HashMap<>();
        String mail = mailRequest.getMail();

        try {
            //메일을 보내고 번호를 가져옵니다.
            int number = mailService.sendMail(mail);
            String num = String.valueOf(number);

            //redis에 저장합니다. ttl은 5분으로 설정합니다.
            redisTemplate.opsForValue().set(mail, num, TTL, TimeUnit.SECONDS);

            //결과값을 반환하기 위한 설정값입니다.
            map.put("success", Boolean.TRUE);
            map.put("number", num);
            return ResponseEntity.ok(map);
        }
        catch (Exception exception) {
            //실패됐을시 400 에러를 만들고 반환합니다.
            map.put("success", Boolean.FALSE);
            map.put("error", exception.getMessage());
            return ResponseEntity.status(400).body(map);
        }
    }

    /**
     * 메일에 보내준 번호를 체크합니다, 레디스에 저장된 값을 꺼내옵니다, redis에 저장될 키는
     *
     * @param machMailRequestDto 이메일과, 인증번호를 입력해줍니다
     * @return 결과값은 true false로 반환해줍니다.
     */
    @GetMapping("/mailCheck")
    public ResponseEntity<Boolean> mailCheck(@RequestBody MatchMailRequestDto machMailRequestDto) {
        //저장된 값을 redis에서 가져옵니다.
        String storedNumber = redisTemplate.opsForValue().get(machMailRequestDto.getEmail());

        //값이 있다면 true로 없다면 false로 반환합니다.
        if (storedNumber != null && storedNumber.equals(machMailRequestDto.getUserNumber())) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }
}
