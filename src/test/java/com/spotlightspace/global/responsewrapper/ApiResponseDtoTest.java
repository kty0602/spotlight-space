package com.spotlightspace.global.responsewrapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseDtoTest {

    @Test
    @DisplayName("공통 응답을 생성할 수 있다.")
    void createApiResponseDto() {
        // given
        int statusCode = 200;
        String path = "/api/v1/path";
        String data = "data";

        // when
        ApiResponseDto<String> response = ApiResponseDto.of(statusCode, path, data);

        // then
        assertThat(response.getStatusCode()).isEqualTo(statusCode);
        assertThat(response.getPath()).isEqualTo(path);
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getTimestamp()).isNotNull();
    }
}
