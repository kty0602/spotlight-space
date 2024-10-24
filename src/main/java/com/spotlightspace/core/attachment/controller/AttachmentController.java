package com.spotlightspace.core.attachment.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.core.attachment.dto.AttachmentRequestDto;
import com.spotlightspace.core.attachment.dto.GetAttachmentResponseDto;
import com.spotlightspace.core.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * 생성 후 첨부파일을 나중에 생성할 때 사용하는 로직
     * @param authUser
     * @param tableId
     * @param requestDto
     * @param files
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
     * @param tableId
     * @param attachmentId
     * @param requestDto
     * @return
     */
    @DeleteMapping("/{tableId}/attachment/{attachmentId}")
    public ResponseEntity<String> deleteAttachment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("tableId") Long tableId,
            @PathVariable("attachmentId") Long attachmentId,
            @RequestBody AttachmentRequestDto requestDto
    ) {

        TableRole tableRole = requestDto.getTableRole();
        attachmentService.deleteAttachment(attachmentId, tableId, tableRole, authUser);
        return new ResponseEntity<>("성공적으로 삭제되었습니다.", HttpStatus.OK);
    }
}
