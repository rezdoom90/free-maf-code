
package com.freemaf.agent;

public final class ColorUtils {

    private ColorUtils() {

    }

    public static int parseHex(String hex) {

        if (hex == null) {

            throw new IllegalArgumentException("hex is null");

        }

        String clean = hex.startsWith("#") ? hex.substring(1) : hex;

        if (clean.length() != 6) {

            throw new IllegalArgumentException("hex must be 6 chars: " + hex);

        }

        int r = Integer.parseInt(clean.substring(0, 2), 16);

        int g = Integer.parseInt(clean.substring(2, 4), 16);

        int b = Integer.parseInt(clean.substring(4, 6), 16);

        return (r << 16) | (g << 8) | b;

    }

    public static String toHex(int rgb) {

        return String.format("%06X", rgb & 0xFFFFFF);

    }

    public static boolean matches(int actual, int expected) {

        return actual == expected;

    }

}
