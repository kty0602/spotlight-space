package com.spotlightspace.core.attachment.dto.response;

import com.spotlightspace.core.attachment.domain.Attachment;
import lombok.Getter;

@Getter
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
