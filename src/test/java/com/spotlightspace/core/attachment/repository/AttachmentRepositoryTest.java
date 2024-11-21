package com.spotlightspace.core.attachment.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.spotlightspace.common.exception.ErrorCode.ATTACHMENT_NOT_FOUND;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AttachmentRepositoryTest {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Test
    @DisplayName("접근하려는 첨부파일이 없음")
    void findByIdOrElseThrow() {
        // given
        long attachmentId = 1L;

        // when & then
        Assertions.assertThatThrownBy(() -> attachmentRepository.findByIdOrElseThrow(attachmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(ATTACHMENT_NOT_FOUND.getMessage());

    }
}
