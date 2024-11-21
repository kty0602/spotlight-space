package com.spotlightspace.common.util;

import java.util.List;

public class SortFieldValidator {

    private static final List<String> VALID_SORT_FIELDS = List.of("id", "email", "nickname", "phoneNumber", "location", "role", "discountAmount", "expiredAt", "code");

    public static void validateSortField(String sortField) {
        if (!VALID_SORT_FIELDS.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }
    }
}
