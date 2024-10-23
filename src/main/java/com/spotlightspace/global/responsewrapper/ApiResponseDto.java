package com.spotlightspace.global.responsewrapper;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponseDto<T> {

    private final LocalDateTime timestamp;
    private final int statusCode;
    private final String path;
    private final T data;

    public static <T> ApiResponseDto<T> of(int statusCode, String path, T data) {
        return new ApiResponseDto<>(LocalDateTime.now(), statusCode, path, data);
    }
}
