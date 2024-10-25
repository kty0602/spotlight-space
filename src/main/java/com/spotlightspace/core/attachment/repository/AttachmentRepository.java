package com.spotlightspace.core.attachment.repository;

import com.spotlightspace.common.entity.TableRole;
import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.attachment.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.ATTACHMENT_NOT_FOUND;

public interface AttachmentRepository extends JpaRepository<Attachment, Long>, AttachmentQueryRepository {

    Attachment findByTableRoleAndTargetId(TableRole tableRole, Long userId);
    Optional<Attachment> findByTargetIdAndTableRole(Long targetId, TableRole tableRole);
    List<Attachment> findAllByTargetIdAndTableRole(Long targetId, TableRole tableRole);

    default Attachment findByIdOrElseThrow(long id) {
        return findById(id)
                .orElseThrow(() -> new ApplicationException(ATTACHMENT_NOT_FOUND));
    }
}
