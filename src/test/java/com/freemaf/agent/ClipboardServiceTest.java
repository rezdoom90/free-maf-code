
package com.freemaf.agent;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.nio.ByteOrder;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClipboardServiceTest {

    @Test

    void dropFilesHeaderIsCorrect() throws IOException {

        Path p = Files.createTempFile("cst-test", ".bin");

        try {

            Files.write(p, new byte[]{1, 2, 3});

            byte[] data = ClipboardService.buildDropFilesData(List.of(p.toFile()));

            ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

            assertEquals(20, bb.getInt());

            assertEquals(0, bb.getInt());

            assertEquals(0, bb.getInt());

            assertEquals(0, bb.getInt());

            assertEquals(1, bb.getInt());

        } finally {

            Files.deleteIfExists(p);

        }

    }

    @Test

    void dropFilesContainsNulTerminatedUtf16Path() throws IOException {

        Path p = Files.createTempFile("cst-test", ".bin");

        try {

            Files.write(p, new byte[]{1});

            byte[] data = ClipboardService.buildDropFilesData(List.of(p.toFile()));

            ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

            bb.position(20);

            StringBuilder sb = new StringBuilder();

            while (true) { char c = bb.getChar(); if (c == 0) break; sb.append(c); }

            assertEquals(p.toAbsolutePath().toString(), sb.toString());

            assertEquals(0, bb.getChar());

        } finally {

            Files.deleteIfExists(p);

        }

    }

    @Test

    void dropFilesTwoPathsSeparatedByNulls() throws IOException {

        Path a = Files.createTempFile("cst-a", ".bin");

        Path b = Files.createTempFile("cst-b", ".bin");

        try {

            byte[] data = ClipboardService.buildDropFilesData(List.of(a.toFile(), b.toFile()));

            ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

            bb.position(20);

            StringBuilder s1 = new StringBuilder();

            while (true) { char c = bb.getChar(); if (c == 0) break; s1.append(c); }

            StringBuilder s2 = new StringBuilder();

            while (true) { char c = bb.getChar(); if (c == 0) break; s2.append(c); }

            char listTerm = bb.getChar();

            assertEquals(a.toAbsolutePath().toString(), s1.toString());

            assertEquals(b.toAbsolutePath().toString(), s2.toString());

            assertEquals(0, listTerm);

            assertTrue(bb.position() <= bb.capacity());

        } finally {

            Files.deleteIfExists(a);

            Files.deleteIfExists(b);

        }

    }

}
