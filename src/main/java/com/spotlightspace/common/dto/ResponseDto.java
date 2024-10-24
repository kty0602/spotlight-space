package com.spotlightspace.common.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ResponseDto<T> {
    private int status;
    private T body;
    private String message;
}
