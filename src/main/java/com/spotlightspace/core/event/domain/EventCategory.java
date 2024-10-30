package com.spotlightspace.core.event.domain;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EventCategory {

    MUSIC("MUSIC"),
    CONCERT("CONCERT"),
    ART("ART"),
    MOVIE("MOVIE"),
    COMMUNITY("COMMUNITY"),
    WORKSHOP("WORKSHOP");



    private final String categoryRole;

    public static EventCategory of(String category) {
        return Arrays.stream(EventCategory.values())
                .filter(c -> c.name().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.WRONG_CATEGORY_NAME));
    }
}
