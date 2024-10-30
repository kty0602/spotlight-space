package com.spotlightspace.core.admin.repository;

import com.spotlightspace.core.attachment.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Attachment, Long>, AdminQueryRepository {

}
