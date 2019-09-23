package com.guavus.aiq.dashboard.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public final class Validator {
    public static final String VALID_CHARACTERS_PATTERN = "[^/\\\\<>;:|{}\\[\\]!$#\\?%\\*'\"&~\\^`]*";

    private Validator() {
    }

    public static void assertNotNull(Object object, String parameter) {
        if (object == null) {
            throw new NullPointerException(parameter + " must not be null");
        }
    }

    public static void assertNotBlank(String string, String parameter) {
        if (StringUtils.isBlank(string)) {
            throw new IllegalArgumentException(parameter + " must not be blank");
        }
    }

    public static void assertIsNull(Object object, String parameter) {
        if (object != null) {
            throw new IllegalArgumentException(parameter + " must be null, but was: " + object);
        }
    }

    public static void assertTrue(Supplier<Boolean> condition, Supplier<String> message) {
        if (!(Boolean)condition.get()) {
            throw new IllegalArgumentException((String)message.get());
        }
    }

    public static void assertTrue(Boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertCollectionNotEmpty(Collection<?> list, String parameter) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(parameter);
        }
    }

    public static void assertMapNotEmpty(Map<?, ?> map, String parameter) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException(parameter);
        }
    }

    public static void assertValidCharacters(String string, String parameter) {
        if (string != null && !string.matches("[^/\\\\<>;:|{}\\[\\]!$#\\?%\\*'\"&~\\^`]*")) {
            throw new IllegalArgumentException(parameter + " must not contain invalid characters");
        }
    }
}
