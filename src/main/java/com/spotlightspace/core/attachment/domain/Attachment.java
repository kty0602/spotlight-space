package com.spotlightspace.core.attachment.domain;

import com.spotlightspace.common.entity.TableRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableRole tableRole;

    @Column(nullable = false)
    private Long targetId;

    private Attachment(String url, TableRole tableRole, long targetId) {
        this.url = url;
        this.tableRole = tableRole;
        this.targetId = targetId;
    }

    public static Attachment create(String url, TableRole tableRole, long targetId) {
        return new Attachment(url, tableRole, targetId);
    }

}
