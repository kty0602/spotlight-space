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
    USER("USER"),
    EVENT("EVENT"),
    REVIEW("REVIEW");

    private final String tableRole;

    public static TableRole of(String table) {
        return Arrays.stream(TableRole.values())
                .filter(c -> c.name().equalsIgnoreCase(table))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.TABLE_NOT_FOUND));
    }
}
