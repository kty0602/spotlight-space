package com.spotlightspace.core.attachment.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.core.attachment.dto.request.AttachmentRequestDto;
import com.spotlightspace.core.attachment.dto.response.GetAttachmentResponseDto;
import com.spotlightspace.core.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * 미리 (이벤트, 유저, 리뷰) 생성 후 첨부파일을 나중에 등록할 때 로직
     * @param authUser
     * @param tableId   만들어진 (이벤트, 유저, 리뷰)의 해당 현재 id값
     * @param requestDto (tableRole : "EVENT", "USER", "REVIEW")
     * @param files      (들어갈 첨부파일)

     * @return
     * @throws IOException
     */
    @PostMapping("/{tableId}/attachment")
    public ResponseEntity<List<GetAttachmentResponseDto>> addAttachment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("tableId") Long tableId,
            @RequestPart AttachmentRequestDto requestDto,
            @RequestPart List<MultipartFile> files
    ) throws IOException {

        TableRole tableRole = requestDto.getTableRole();
        List<GetAttachmentResponseDto> responseDtos =
                attachmentService.addNewAttachmentList(files, tableId, tableRole, authUser);
        return new ResponseEntity<>(responseDtos, HttpStatus.CREATED);
    }

    /**
     * 하나의 첨부파일 따로 수정하는 로직
     * @param authUser
     * @param tableId    만들어진 (이벤트, 유저, 리뷰)의 해당 현재 id값
     * @param attachmentId   수정할 첨부파일 id값
     * @param requestDto   (tableRole : "EVENT", "USER", "REVIEW")
     * @param file        (들어갈 첨부파일)
     * @return
     * @throws IOException
     */

    @PatchMapping("/{tableId}/attachment/{attachmentId}")
    public ResponseEntity<GetAttachmentResponseDto> updateAttachment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("tableId") Long tableId,
            @PathVariable("attachmentId") Long attachmentId,
            @RequestPart AttachmentRequestDto requestDto,
            @RequestPart MultipartFile file
    ) throws IOException {

        TableRole tableRole = requestDto.getTableRole();
        GetAttachmentResponseDto responseDto =
                attachmentService.updateAttachment(attachmentId, file, tableId, tableRole, authUser);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /**
     * 첨부파일만 따로 삭제하는 로직
     * @param authUser
     * @param tableId    만들어진 (이벤트, 유저, 리뷰)의 해당 현재 id값
     * @param attachmentId    수정할 첨부파일 id값
     * @param requestDto    (tableRole : "EVENT", "USER", "REVIEW")
     * @return
     */
    @DeleteMapping("/{tableId}/attachment/{attachmentId}")
    public ResponseEntity<Map<String, String>> deleteAttachment(

            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("tableId") Long tableId,
            @PathVariable("attachmentId") Long attachmentId,
            @RequestBody AttachmentRequestDto requestDto
    ) {

        TableRole tableRole = requestDto.getTableRole();
        attachmentService.deleteAttachment(attachmentId, tableId, tableRole, authUser);

        Map<String, String> response = new HashMap<>();
        response.put("message", "성공적으로 삭제되었습니다!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
