package com.spotlightspace.core.pointLog.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPointLog is a Querydsl query type for PointLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPointLog extends EntityPathBase<PointLog> {

    private static final long serialVersionUID = 1943260484L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPointLog pointLog = new QPointLog("pointLog");

    public final StringPath history = createString("history");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.spotlightspace.core.point.domain.QPoint point;

    public final com.spotlightspace.core.user.domain.QUser user;

    public QPointLog(String variable) {
        this(PointLog.class, forVariable(variable), INITS);
    }

    public QPointLog(Path<? extends PointLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPointLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPointLog(PathMetadata metadata, PathInits inits) {
        this(PointLog.class, metadata, inits);
    }

    public QPointLog(Class<? extends PointLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.point = inits.isInitialized("point") ? new com.spotlightspace.core.point.domain.QPoint(forProperty("point"), inits.get("point")) : null;
        this.user = inits.isInitialized("user") ? new com.spotlightspace.core.user.domain.QUser(forProperty("user")) : null;
    }

}

