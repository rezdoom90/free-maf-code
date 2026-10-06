
package com.freemaf.agent;

import java.io.File;

import java.util.List;

public record Prompt(String text, List<File> files, boolean userInitiated) {

    public Prompt {

        if (text == null) text = "";

        if (files == null) files = List.of();

        files = List.copyOf(files);

    }

    public static Prompt of(String text) { return new Prompt(text, List.of(), false); }

    public static Prompt of(String text, List<File> files) { return new Prompt(text, files, false); }

    public static Prompt user(String text, List<File> files) { return new Prompt(text, files, true); }

    public boolean hasFiles() { return !files.isEmpty(); }

}
