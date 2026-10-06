
package com.freemaf.agent;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

public final class ScriptSaver {

    private ScriptSaver() {

    }

    public static void save(String script) throws IOException {

        String withBom = "\uFEFF" + script;

        Files.write(Path.of("script.ps1"), withBom.getBytes(StandardCharsets.UTF_8));

    }

}
