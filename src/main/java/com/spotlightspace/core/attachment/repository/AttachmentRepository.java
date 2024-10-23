package com.spotlightspace.core.attachment.repository;

import com.spotlightspace.core.attachment.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long>, AttachmentQueryRepository {

}
