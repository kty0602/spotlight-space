package com.spotlightspace.common.entity;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import com.spotlightspace.core.event.domain.EventCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TableRole {

    USER(TableRole.Authority.USER),
    EVENT(TableRole.Authority.EVENT),
    REVIEW(TableRole.Authority.REVIEW);

    private final String tableRole;

    public static EventCategory of(String table) {
        return Arrays.stream(EventCategory.values())
                .filter(c -> c.name().equalsIgnoreCase(table))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.TABLE_NOT_FOUND));
    }

    public static class Authority {
        public static final String USER = "USER";
        public static final String EVENT = "EVENT";
        public static final String REVIEW = "REVIEW";
    }
}
