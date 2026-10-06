package com.freemaf.agent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
public final class RuleProvider {
    private RuleProvider() {}
    public static String readRuleFile(Path path) {
        try { return Files.readString(path, StandardCharsets.UTF_8); }
        catch (IOException e) {
            AppLogger.warn("RuleProvider read failed for " + path + ": " + e.getMessage());
            return "";
        }
    }
}
