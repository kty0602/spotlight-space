package com.spotlightspace.core.attachment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAttachment is a Querydsl query type for Attachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttachment extends EntityPathBase<Attachment> {

    private static final long serialVersionUID = 623597218L;

    public static final QAttachment attachment = new QAttachment("attachment");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.spotlightspace.common.entity.TableRole> tableRole = createEnum("tableRole", com.spotlightspace.common.entity.TableRole.class);

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final StringPath url = createString("url");

    public QAttachment(String variable) {
        super(Attachment.class, forVariable(variable));
    }

    public QAttachment(Path<? extends Attachment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAttachment(PathMetadata metadata) {
        super(Attachment.class, metadata);
    }

}

