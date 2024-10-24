package com.spotlightspace.core.event.domain;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EventCategory {

    MUSIC(EventCategory.Authority.MUSIC),
    CONCERT(EventCategory.Authority.CONCERT),
    ART(EventCategory.Authority.ART),
    MOVIE(EventCategory.Authority.MOVIE),
    COMMUNITY(EventCategory.Authority.COMMUNITY),
    WORKSHOP(EventCategory.Authority.WORKSHOP);



    private final String categoryRole;

    public static EventCategory of(String category) {
        return Arrays.stream(EventCategory.values())
                .filter(c -> c.name().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.WRONG_CATEGORY_NAME));
    }

    public static class Authority {
        public static final String MUSIC = "MUSIC";
        public static final String CONCERT = "CONCERT";
        public static final String ART = "ART";
        public static final String MOVIE = "MOVIE";
        public static final String COMMUNITY = "COMMUNITY";
        public static final String WORKSHOP = "WORKSHOP";
    }

}
