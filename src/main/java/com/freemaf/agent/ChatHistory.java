
package com.freemaf.agent;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.StandardOpenOption;

import java.util.ArrayList;

import java.util.List;

public final class ChatHistory {

    public enum Kind { USER, SYSTEM, AGENT, DEBUG }

    public record Entry(Kind kind, String text) {}

    private static final Path FILE = Path.of("agent", "project", "chat_history.txt");

    private static final String SEP = "### ";

    private ChatHistory() {}

    public static void append(Entry entry) {

        try {

            Files.createDirectories(FILE.getParent());

            String encoded = SEP + entry.kind().name() + System.lineSeparator()

                    + entry.text().replace("\r", "").replace("\n", "\\n") + System.lineSeparator();

            Files.writeString(FILE, encoded, StandardCharsets.UTF_8,

                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {

            AppLogger.warn("ChatHistory append failed: " + e.getMessage());

        }

    }

    public static List<Entry> loadAll() {

        List<Entry> result = new ArrayList<>();

        if (!Files.exists(FILE)) return result;

        try {

            List<String> lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);

            Kind current = null;

            StringBuilder buf = new StringBuilder();

            for (String line : lines) {

                if (line.startsWith(SEP)) {

                    if (current != null && buf.length() > 0) {

                        result.add(new Entry(current, buf.toString()));

                        buf.setLength(0);

                    }

                    try {

                        current = Kind.valueOf(line.substring(SEP.length()).strip());

                    } catch (IllegalArgumentException e) {

                        current = Kind.SYSTEM;

                    }

                } else if (current != null) {

                    buf.append(line.replace("\\n", System.lineSeparator()));

                }

            }

            if (current != null && buf.length() > 0) {

                result.add(new Entry(current, buf.toString()));

            }

        } catch (IOException e) {

            AppLogger.warn("ChatHistory load failed: " + e.getMessage());

        }

        return result;

    }

    public static void clear() {

        try {

            Files.deleteIfExists(FILE);

        } catch (IOException e) {

            AppLogger.warn("ChatHistory clear failed: " + e.getMessage());

        }

    }

}
