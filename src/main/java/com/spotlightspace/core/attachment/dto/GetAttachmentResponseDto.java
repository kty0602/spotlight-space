package com.spotlightspace.core.attachment.dto;

import com.spotlightspace.core.attachment.domain.Attachment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetAttachmentResponseDto {

    private Long id;
    private String url;

    private GetAttachmentResponseDto(Attachment attachment) {
        this.id = attachment.getId();
        this.url = attachment.getUrl();
    }

    public static GetAttachmentResponseDto from(Attachment attachment) {
        return new GetAttachmentResponseDto(attachment);
    }

}
