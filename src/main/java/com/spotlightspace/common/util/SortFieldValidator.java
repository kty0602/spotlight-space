package com.spotlightspace.common.util;

import java.util.List;

public class SortFieldValidator {

    private static final List<String> VALID_SORT_FIELDS = List.of(
            "title",
            "content",
            "contents",
            "location",
            "startAt",
            "endAt",
            "maxPeople",
            "price",
            "category",
            "recruitmentStartAt",
            "recruitmentFinishAt",
            "email",
            "nickname",
            "phoneNumber",
            "role",
            "id",
            "createdAt",
            "updatedAt",
            "discountAmount"
    );

    public static void validateSortField(String sortField) {
        if (!VALID_SORT_FIELDS.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }
    }
}
