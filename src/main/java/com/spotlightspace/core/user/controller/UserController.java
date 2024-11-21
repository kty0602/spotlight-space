package com.spotlightspace.core.user.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.core.user.dto.request.UpdateUserRequestDto;
import com.spotlightspace.core.user.dto.response.GetSettlementListResponseDto;
import com.spotlightspace.core.user.dto.response.GetSettlementResponseDto;
import com.spotlightspace.core.user.dto.response.GetCouponResponseDto;
import com.spotlightspace.core.user.dto.response.GetUserResponseDto;
import com.spotlightspace.core.user.dto.response.UpdateUserResponseDto;
import com.spotlightspace.core.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    /**
     * 회원 수정을 진행합니다
     *
     * @param userId               수정할 user의 id를 입력받고
     * @param authUser             로그인한 user의 id를 입력받습니다
     * @param updateUserRequestDto 수정할 정보인, 비밀번호, 닉네임, 생일, 전화번호를 바꿀 수 있습니다
     * @param file                 이미지는 필수가 아닙니다
     * @return 201 CREATED 반환합니다
     * @throws IOException 이미지 업로드를 위한 exception입니다.
     */
    @PatchMapping("/user/{userId}")
    public ResponseEntity<UpdateUserResponseDto> updateUser(
            @PathVariable long userId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestPart UpdateUserRequestDto updateUserRequestDto,
            @RequestPart(required = false) MultipartFile file
    ) throws IOException {
        UpdateUserResponseDto updateUserResponseDto = userService.updateUser(userId, authUser, updateUserRequestDto,
                file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(updateUserResponseDto);
    }

    /**
     * 사용자 본인을 조회하는 로직입니다
     *
     * @param userId   유저아이디를 입력받습니다
     * @param authUser 현재 로그인중인 유저를 확인합니다.
     * @return 사용자의 정보를 반환받습니다
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<GetUserResponseDto> getUser(
            @PathVariable long userId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUser(userId, authUser.getUserId()));
    }

    /**
     * 유저를 삭제합니다
     *
     * @param userId   유저아이디를 입력받습니다
     * @param authUser 현재 로그인중인 유저 정보를 받아옵니다
     * @return 회원이 삭제됩니다
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable long userId,
            @AuthenticationPrincipal AuthUser authUser,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String accessToken = invalidateTokensAndGetAccessToken(httpServletResponse, httpServletRequest);

        userService.deleteUser(userId, authUser, accessToken);
        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 삭제되었습니다!");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * 유저의 쿠폰을 조회합니다
     *
     * @param userId   유저 id를 받습니다.
     * @param authUser 현재 로그인한 유저의 id를 받습니다
     * @return
     */
    @GetMapping("/user/{userId}/coupons")
    public ResponseEntity<List<GetCouponResponseDto>> getCoupons(
            @PathVariable long userId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        List<GetCouponResponseDto> couponList = userService.getCoupons(userId, authUser.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(couponList);
    }

    /**
     * 회원의 로그아웃을 진행합니다.
     *
     * @param authUser            현재 로그인된 유저 정보를 입력합니다
     * @param httpServletRequest  액세스 토큰을 블랙리스트에 올리기 위해 httpServletRequest를 가져옵니다
     * @param httpServletResponse 리프레시 토큰을 무효화하기 위해 coockie를 조회할 httpservletResponse를 가져옵니다
     * @return
     */
    @PostMapping("/user/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal AuthUser authUser,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        String accessToken = invalidateTokensAndGetAccessToken(httpServletResponse, httpServletRequest);

        userService.logout(authUser.getUserId(), accessToken);
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃 되었습니다.!");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 회원의 전체 정산금을 조회합니다.
     *
     * @param userId   유저 아이디를 파라미터로 입력받습니다
     * @param authUser 현재 로그인중인 유저의 정보를 받습니다.
     * @return
     */
    @GetMapping("/user/{userId}/all-settlement")
    public ResponseEntity<GetSettlementResponseDto> getAllSettlement(
            @PathVariable long userId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllSettlement(userId, authUser.getUserId()));
    }

    /**
     * 각각의 이벤트에 해당하는 정산금을 조회합니다
     *
     * @param userId   유저아이디를 입력받습니다
     * @param authUser 현재 로그인중인 유저입니다
     * @return 회원의 정산금 리스트를 반환합니다.
     */
    @GetMapping("/user/{userId}/settlement")
    public ResponseEntity<List<GetSettlementListResponseDto>> getSettlementList(
            @PathVariable long userId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getSettlementList(userId, authUser.getUserId()));
    }

    private String invalidateTokensAndGetAccessToken(
            HttpServletResponse httpServletResponse,
            HttpServletRequest httpServletRequest
    ) {
        Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        httpServletResponse.addCookie(refreshTokenCookie);

        Cookie accessTokenCookie = new Cookie("AccessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        httpServletResponse.addCookie(accessTokenCookie);

        return httpServletRequest.getHeader(AUTHORIZATION);
    }

}
