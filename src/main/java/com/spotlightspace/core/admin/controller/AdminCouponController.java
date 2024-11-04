package com.spotlightspace.core.admin.controller;

import com.spotlightspace.core.admin.dto.requestdto.AdminCouponCreateRequestDto;
import com.spotlightspace.core.admin.dto.requestdto.AdminCouponUpdateRequestDto;
import com.spotlightspace.core.admin.dto.responsedto.AdminCouponResponseDto;
import com.spotlightspace.core.admin.service.AdminCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/coupons")
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    /**
     * 쿠폰 생성
     *
     * @param requestDto 쿠폰 생성 요청 DTO
     * @return 생성된 쿠폰 정보
     */
    @PostMapping
    public ResponseEntity<AdminCouponResponseDto> createCoupon(@Valid @RequestBody AdminCouponCreateRequestDto requestDto) {
        AdminCouponResponseDto responseDto = adminCouponService.createCoupon(requestDto);
        return ResponseEntity.status(201).body(responseDto);
    }

    /**
     * 쿠폰 조회
     *
     * @param page      조회할 페이지 번호 (1부터 시작)
     * @param size      페이지 당 항목 수
     * @param keyword   검색 키워드 (선택적)
     * @param sortField 결과를 정렬할 필드명
     * @param sortOrder 정렬 순서 (오름차순 또는 내림차순)
     * @return {@link AdminCouponResponseDto}의 페이지 형태로 검색 결과 반환
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AdminCouponResponseDto>> getCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        Page<AdminCouponResponseDto> coupons = adminCouponService.getAdminCoupons(page, size, keyword, sortField, sortOrder);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 쿠폰 업데이트
     *
     * @param id         쿠폰 ID
     * @param requestDto 쿠폰 업데이트 요청 DTO
     * @return No content 응답
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateCoupon(@PathVariable Long id, @Valid @RequestBody AdminCouponUpdateRequestDto requestDto) {
        adminCouponService.updateCoupon(id, requestDto);
        return ResponseEntity.noContent().build();
    }

    /**
     * 쿠폰 삭제 (소프트 삭제)
     *
     * @param id 쿠폰 ID
     * @return No content 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        adminCouponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
