package com.spotlightspace.core.attachment.controller;

import com.spotlightspace.common.annotation.AuthUser;
import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.core.attachment.dto.AddAttachmentRequestDto;
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

    @PostMapping("/{tableId}/attachment")
    public ResponseEntity<List<GetAttachmentResponseDto>> addAttachment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("tableId") Long tableId,
            @RequestPart AddAttachmentRequestDto requestDto,
            @RequestPart List<MultipartFile> files
    ) throws IOException {

        TableRole tableRole = requestDto.getTableRole();
        List<GetAttachmentResponseDto> responseDtos =
                attachmentService.addNewAttachmentList(files, tableId, tableRole, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtos);
    }
}
