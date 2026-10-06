
package com.freemaf.agent;

import java.io.IOException;

import java.util.LinkedHashMap;

import java.util.Map;

public final class AutoCalibrator {

    private static final String[] STATIC_MARKERS = {"input_field", "deepthink", "search"};

    private static final String[] DYNAMIC_MARKERS = {"regenerate", "copy_last"};

    private AutoCalibrator() {}

    public static CalibrationResult calibrateStatic(Config config) throws IOException {

        return calibrate(config, STATIC_MARKERS);

    }

    public static CalibrationResult calibrateDynamic(Config config) throws IOException {

        return calibrate(config, DYNAMIC_MARKERS);

    }

    private static CalibrationResult calibrate(Config config, String[] markers) throws IOException {

        int winX = config.getInt("window.work.x", 50);

        int winY = config.getInt("window.work.y", 50);

        PixelColorService pixels = new PixelColorService();

        Map<String, String> result = new LinkedHashMap<>();

        for (String marker : markers) {

            int dx = config.getInt("marker." + marker + ".dx", -1);

            int dy = config.getInt("marker." + marker + ".dy", -1);

            if (dx < 0 || dy < 0) {

                AppLogger.warn("AutoCalibrator: skipping " + marker + " (dx/dy not configured)");

                continue;

            }

            int absX = winX + dx;

            int absY = winY + dy;

            int rgb = pixels.readPixel(absX, absY);

            String hex = ColorUtils.toHex(rgb);

            config.saveMarker(marker, absX, absY, rgb);

            result.put(marker, hex);

            AppLogger.info("AutoCalibrator: " + marker + " (" + absX + "," + absY + ") = #" + hex);

        }

        config.save();

        return new CalibrationResult(result);

    }

    public static CalibrationResult calibrateSendDisabled(Config config) throws IOException {

        return calibrateSendColor(config, "disabled_color");

    }

    public static CalibrationResult calibrateSendActive(Config config) throws IOException {

        return calibrateSendColor(config, "active_color");

    }

    private static CalibrationResult calibrateSendColor(Config config, String colorKey) throws IOException {

        int winX = config.getInt("window.work.x", 50);

        int winY = config.getInt("window.work.y", 50);

        int dx = config.getInt("marker.send.dx", -1);

        int dy = config.getInt("marker.send.dy", -1);

        if (dx < 0 || dy < 0) {

            throw new IOException("marker.send.dx/dy not configured");

        }

        PixelColorService pixels = new PixelColorService();

        int absX = winX + dx;

        int absY = winY + dy;

        int rgb = pixels.readPixel(absX, absY);

        String hex = ColorUtils.toHex(rgb);

        config.set("marker.send.x", String.valueOf(absX));

        config.set("marker.send.y", String.valueOf(absY));

        config.set("marker.send." + colorKey, hex);

        config.save();

        Map<String, String> result = new LinkedHashMap<>();

        result.put("send." + colorKey, hex);

        AppLogger.info("AutoCalibrator: send." + colorKey + " (" + absX + "," + absY + ") = #" + hex);

        return new CalibrationResult(result);

    }

    public record CalibrationResult(Map<String, String> colors) {

        public String summary() {

            StringBuilder sb = new StringBuilder();

            boolean first = true;

            for (Map.Entry<String, String> e : colors.entrySet()) {

                if (!first) sb.append(System.lineSeparator());

                sb.append("  ").append(e.getKey()).append(" = #").append(e.getValue());

                first = false;

            }

            return sb.toString();

        }

    }

}
