
package com.freemaf.agent;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import java.io.IOException;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.ArrayList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachmentValidatorTest {

    @TempDir

    Path tmp;

    private File makeFile(String name, long size) throws IOException {

        Path p = tmp.resolve(name);

        Files.write(p, new byte[]{1});

        if (size > 1L) {

            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(p.toFile(), "rw")) {

                raf.setLength(size);

            }

        }

        return p.toFile();

    }

    @Test

    void emptyListIsValid() {

        assertTrue(AttachmentValidator.validate(List.of()).valid());

        assertTrue(AttachmentValidator.validate(null).valid());

    }

    @Test

    void singleSmallFileIsValid() throws IOException {

        assertTrue(AttachmentValidator.validate(List.of(makeFile("a.txt", 10L))).valid());

    }

    @Test

    void tooManyFilesIsInvalid() throws IOException {

        List<File> files = new ArrayList<>();

        for (int i = 0; i < AttachmentValidator.MAX_FILES + 1; i++) files.add(makeFile("f" + i + ".txt", 1L));

        AttachmentValidator.ValidationResult r = AttachmentValidator.validate(files);

        assertFalse(r.valid());

        assertTrue(r.error().toLowerCase().contains("too many"));

    }

    @Test

    void oversizeFileIsInvalid() throws IOException {

        File big = makeFile("big.bin", AttachmentValidator.MAX_FILE_SIZE_BYTES + 1L);

        AttachmentValidator.ValidationResult r = AttachmentValidator.validate(List.of(big));

        assertFalse(r.valid());

        assertTrue(r.error().toLowerCase().contains("too large"));

    }

    @Test

    void missingFileIsInvalid() {

        File missing = new File(tmp.toFile(), "does-not-exist.bin");

        AttachmentValidator.ValidationResult r = AttachmentValidator.validate(List.of(missing));

        assertFalse(r.valid());

    }

}
