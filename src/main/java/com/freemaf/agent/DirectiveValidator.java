package com.freemaf.agent;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class DirectiveValidator {

    private static final List<String> PROTECTED_PATHS = List.of(
            "rules/code_reviewer.md",
            "rules/executor.md",
            "rules/judge.md",
            "agent/rules/code_reviewer.md",
            "agent/rules/executor.md",
            "agent/rules/judge.md",
            "agent_instructions.md",
            "agent/agent_instructions.md");

    private static final List<String> WRITE_COMMANDS = List.of(
            "set-content",
            "add-content",
            "out-file",
            "new-item",
            "remove-item",
            "move-item",
            "copy-item",
            "rename-item",
            "[system.io.file]::write",
            "[system.io.file]::append",
            "[system.io.file]::create",
            "[system.io.file]::delete",
            "[system.io.file]::copy",
            "[system.io.file]::move");

    private static final Pattern REDIRECT = Pattern.compile("(?<![<>=!-])>{1,2}(?!=)");

    private DirectiveValidator() {
    }

    public static ValidationResult validate(String scriptContent) {
        if (scriptContent == null || scriptContent.isEmpty()) {
            return new ValidationResult(true, "");
        }
        String[] lines = scriptContent.split("\\r?\\n", -1);
        for (String line : lines) {
            String normalized = normalize(line);
            if (!hasWriteIntent(normalized)) {
                continue;
            }
            for (String protectedPath : PROTECTED_PATHS) {
                if (normalized.contains(protectedPath)) {
                    String reason = "Script blocked: write to protected directive file '"
                            + protectedPath
                            + "' is forbidden. Among directive files only agent/project/MEMORY.md may be modified.";
                    return new ValidationResult(false, reason);
                }
            }
        }
        return new ValidationResult(true, "");
    }

    private static boolean hasWriteIntent(String normalizedLine) {
        for (String command : WRITE_COMMANDS) {
            if (normalizedLine.contains(command)) {
                return true;
            }
        }
        return REDIRECT.matcher(normalizedLine).find();
    }

    static String normalize(String value) {
        String replaced = value.replace('\\', '/');
        if (replaced.startsWith("./")) {
            replaced = replaced.substring(2);
        }
        return replaced.toLowerCase(Locale.ROOT);
    }

    public record ValidationResult(boolean allowed, String reason) {
    }
}
