
package com.freemaf.agent;

import java.io.IOException;

import java.io.Reader;

import java.io.Writer;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.LinkedHashMap;

import java.util.Map;

import java.util.Properties;

public final class Config {

    private static final Path FILE = Path.of("agent", "config.properties");

    static final Map<String, String> DEFAULTS = new LinkedHashMap<>();

    static {

        DEFAULTS.put("window.title", "DeepSeek");

        DEFAULTS.put("window.work.x", "50");

        DEFAULTS.put("window.work.y", "50");

        DEFAULTS.put("window.work.width", "673");

        DEFAULTS.put("window.work.height", "473");

        DEFAULTS.put("chrome.exe.path", "");

        DEFAULTS.put("chrome.deepseek.url", "https://chat.deepseek.com/");

        DEFAULTS.put("chrome.signin.keywords", "sign_in,Sign in,Login,Вход");

        DEFAULTS.put("calibration.done", "false");

        DEFAULTS.put("calibration.prompt",

                "Reply with exactly one fenced code block and no text outside it. The block must contain a single line: OK");

        DEFAULTS.put("marker.regenerate.x", "0");

        DEFAULTS.put("marker.regenerate.y", "0");

        DEFAULTS.put("marker.regenerate.color", "000000");

        DEFAULTS.put("marker.regenerate.dx", "73");

        DEFAULTS.put("marker.regenerate.dy", "258");

        DEFAULTS.put("marker.regenerate.dx_offset", "103");

        DEFAULTS.put("marker.copy_last.x", "0");

        DEFAULTS.put("marker.copy_last.y", "0");

        DEFAULTS.put("marker.copy_last.color", "000000");

        DEFAULTS.put("marker.copy_last.dx", "482");

        DEFAULTS.put("marker.copy_last.dy", "198");

        DEFAULTS.put("marker.input_field.x", "94");

        DEFAULTS.put("marker.input_field.y", "393");

        DEFAULTS.put("marker.input_field.color", "333335");

        DEFAULTS.put("marker.input_field.dx", "44");

        DEFAULTS.put("marker.input_field.dy", "343");

        DEFAULTS.put("marker.deepthink.x", "119");

        DEFAULTS.put("marker.deepthink.y", "453");

        DEFAULTS.put("marker.deepthink.color", "6196F8");

        DEFAULTS.put("marker.deepthink.on_color", "6196F8");

        DEFAULTS.put("marker.deepthink.dx", "69");

        DEFAULTS.put("marker.deepthink.dy", "403");

        DEFAULTS.put("marker.search.x", "236");

        DEFAULTS.put("marker.search.y", "453");

        DEFAULTS.put("marker.search.color", "5B82E6");

        DEFAULTS.put("marker.search.on_color", "5B82E6");

        DEFAULTS.put("marker.search.dx", "186");

        DEFAULTS.put("marker.search.dy", "403");

        DEFAULTS.put("timeout.generation_ms", "180000");

        DEFAULTS.put("timeout.script_ms", "120000");

        DEFAULTS.put("timeout.scroll_ms", "30000");

        DEFAULTS.put("timeout.new_window_ms", "45000");

        DEFAULTS.put("marker.retry.x", "140");

        DEFAULTS.put("marker.retry.y", "240");

        DEFAULTS.put("marker.retry.color", "F5AC31");

        DEFAULTS.put("timeout.retry_base_ms", "300000");

        DEFAULTS.put("timeout.retry_base_jitter_ms", "100000");

        DEFAULTS.put("timeout.retry_increment_base_ms", "300000");

        DEFAULTS.put("timeout.retry_increment_step_ms", "100000");

        DEFAULTS.put("marker.retry.max_attempts", "10");

        DEFAULTS.put("marker.send.dx", "610");

        DEFAULTS.put("marker.send.dy", "402");

        DEFAULTS.put("marker.send.x", "0");

        DEFAULTS.put("marker.send.y", "0");

        DEFAULTS.put("marker.send.active_color", "000000");

        DEFAULTS.put("marker.send.disabled_color", "000000");

        DEFAULTS.put("attachments.max_files", "20");

        DEFAULTS.put("attachments.max_size_mb", "50");

        DEFAULTS.put("timeout.attach_upload_ms", "120000");

        DEFAULTS.put("timeout.attach_poll_ms", "500");

        DEFAULTS.put("min_interval_ms", "1000");

        DEFAULTS.put("max_retries", "3");

        DEFAULTS.put("max_copy_retries", "3");

        DEFAULTS.put("debug.show_in_chat", "true");

        DEFAULTS.put("speed.mode", "INSTANT");

        DEFAULTS.put("ps.output.limit_chars", "60000");
        DEFAULTS.put("watchdog.script.timeout.seconds", "1800");
        DEFAULTS.put("watchdog.script.check.interval.ms", "1000");
        DEFAULTS.put("watchdog.script.warn.threshold.seconds", "1200");
        DEFAULTS.put("session.lastManagedHwnd", "0");
        DEFAULTS.put("session.lastManagedTitle", "");
        DEFAULTS.put("session.lastPhase", "EXECUTOR");
        DEFAULTS.put("session.lastReviewerHwnd", "0");
        DEFAULTS.put("session.lastReviewerTitle", "");

    }

    private final Properties props = new Properties();

    public Config() {

        for (Map.Entry<String, String> e : DEFAULTS.entrySet()) {

            props.setProperty(e.getKey(), e.getValue());

        }

        if (Files.exists(FILE)) {

            try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {

                props.load(reader);

                AppLogger.info("Config loaded from " + FILE.toAbsolutePath());

                migrate();

            } catch (IOException e) {

                AppLogger.warn("Config load failed: " + e.getMessage());

            }

        } else {

            AppLogger.info("Config not found, creating default at " + FILE.toAbsolutePath());

            try { save(); } catch (IOException e) {

                AppLogger.warn("Failed to create default config: " + e.getMessage());

            }

        }

    }

    private void migrate() {

        java.util.List<String> obsolete = new java.util.ArrayList<>();

        for (Object rawKey : props.keySet()) {

            String key = String.valueOf(rawKey);

            if (!DEFAULTS.containsKey(key)) obsolete.add(key);

        }

        if (obsolete.isEmpty()) return;

        for (String key : obsolete) props.remove(key);

        AppLogger.info("Config: removed obsolete keys (" + obsolete.size() + "): " + obsolete);

        try {

            save();

        } catch (IOException e) {

            AppLogger.warn("Config: failed to persist migration: " + e.getMessage());

        }

    }

    public String getWindowTitle() { return get("window.title", "DeepSeek"); }

    public int getInt(String key, int defaultValue) {

        String value = props.getProperty(key);

        if (value == null || value.isBlank()) return defaultValue;

        try { return Integer.parseInt(value.trim()); }

        catch (NumberFormatException e) {

            AppLogger.warn("Invalid int config " + key + ": " + value);

            return defaultValue;

        }

    }

    public long getLong(String key, long defaultValue) {

        String value = props.getProperty(key);

        if (value == null || value.isBlank()) return defaultValue;

        try { return Long.parseLong(value.trim()); }

        catch (NumberFormatException e) {

            AppLogger.warn("Invalid long config " + key + ": " + value);

            return defaultValue;

        }

    }

    public boolean getBoolean(String key, boolean defaultValue) {

        String value = props.getProperty(key);

        if (value == null || value.isBlank()) return defaultValue;

        return Boolean.parseBoolean(value.trim());

    }

    public String get(String key, String defaultValue) { return props.getProperty(key, defaultValue); }

    public void set(String key, String value) { props.setProperty(key, value); }

    public boolean isCalibrated() { return getBoolean("calibration.done", false); }

    public void setCalibrated(boolean value) { props.setProperty("calibration.done", String.valueOf(value)); }

    public void saveMarker(String markerKey, int x, int y, int rgb) {

        props.setProperty("marker." + markerKey + ".x", String.valueOf(x));

        props.setProperty("marker." + markerKey + ".y", String.valueOf(y));

        props.setProperty("marker." + markerKey + ".color", ColorUtils.toHex(rgb));

    }

    public void save() throws IOException {

        Path parent = FILE.getParent();

        if (parent != null) Files.createDirectories(parent);

        try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {

            props.store(writer, "Free MAF Code config");

        }

    }


    public long getScriptTimeoutSeconds() {
        return getLong("watchdog.script.timeout.seconds", 1800L);
    }

    public long getScriptCheckIntervalMs() {
        return getLong("watchdog.script.check.interval.ms", 1000L);
    }

        public long getScriptWarnThresholdSeconds() {
        return getLong("watchdog.script.warn.threshold.seconds", 1200L);
    }

    public long getLastManagedHwnd() {
        return getLong("session.lastManagedHwnd", 0L);
    }

    public String getLastManagedTitle() {
        return get("session.lastManagedTitle", "");
    }

    public String getLastPhase() {
        return get("session.lastPhase", "EXECUTOR");
    }

    public long getLastReviewerHwnd() {
        return getLong("session.lastReviewerHwnd", 0L);
    }

    public String getLastReviewerTitle() {
        return get("session.lastReviewerTitle", "");
    }

    public void setLastManagedHwnd(long hwnd) {
        props.setProperty("session.lastManagedHwnd", String.valueOf(hwnd));
        persist();
    }

    public void setLastManagedTitle(String title) {
        props.setProperty("session.lastManagedTitle", title == null ? "" : title);
        persist();
    }

    public void setLastPhase(String phase) {
        props.setProperty("session.lastPhase", phase == null ? "EXECUTOR" : phase);
        persist();
    }

    public void setLastReviewerHwnd(long hwnd) {
        props.setProperty("session.lastReviewerHwnd", String.valueOf(hwnd));
        persist();
    }

    public void setLastReviewerTitle(String title) {
        props.setProperty("session.lastReviewerTitle", title == null ? "" : title);
        persist();
    }

    public void persist() {
        try {
            save();
        } catch (IOException e) {
            AppLogger.warn("Config persist failed: " + e.getMessage());
        }
    }
}

