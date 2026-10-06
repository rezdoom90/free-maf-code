
package com.freemaf.agent;

import java.io.File;

import java.util.List;

public final class AttachmentValidator {

    public static final int MAX_FILES = 20;

    public static final long MAX_FILE_SIZE_BYTES = 50L * 1024L * 1024L;

    private AttachmentValidator() {}

    public static ValidationResult validate(List<File> files) {

        if (files == null || files.isEmpty()) return ValidationResult.ok();

        if (files.size() > MAX_FILES) {

            return ValidationResult.error("Too many files: " + files.size() + " (max " + MAX_FILES + ")");

        }

        for (File f : files) {

            if (f == null) return ValidationResult.error("Null file in list");

            if (!f.exists()) return ValidationResult.error("File does not exist: " + f.getAbsolutePath());

            if (!f.isFile()) return ValidationResult.error("Not a regular file: " + f.getAbsolutePath());

            long size = f.length();

            if (size > MAX_FILE_SIZE_BYTES) {

                return ValidationResult.error("File too large: " + f.getName() + " (" + size + " bytes, max " + MAX_FILE_SIZE_BYTES + ")");

            }

        }

        return ValidationResult.ok();

    }

    public record ValidationResult(boolean valid, String error) {

        public static ValidationResult ok() { return new ValidationResult(true, ""); }

        public static ValidationResult error(String msg) { return new ValidationResult(false, msg); }

    }

}
