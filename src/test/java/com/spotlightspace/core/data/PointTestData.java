package com.spotlightspace.core.data;

import com.spotlightspace.core.point.domain.Point;
import com.spotlightspace.core.point.dto.request.CreatePointRequestDto;
import com.spotlightspace.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.test.util.ReflectionTestUtils;

import static com.spotlightspace.core.data.UserTestData.*;

@RequiredArgsConstructor
public class PointTestData {

    public static CreatePointRequestDto createDefaultPointRequestDto() {
        return CreatePointRequestDto.from(
                10000
        );
    }

    public static Point testPoint() {
        CreatePointRequestDto pointRequestDto = createDefaultPointRequestDto();
        User user = testUser();
        ReflectionTestUtils.setField(user, "id", 1L);
        Point point = Point.of(pointRequestDto.getPrice(), user);
        ReflectionTestUtils.setField(point, "id", 1L);
        return point;
    }
}
