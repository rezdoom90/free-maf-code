
package com.freemaf.agent;

public final class ScriptLogger {

    private ScriptLogger() {

    }

    public static void save(String script) {

        HistoryService.write("scripts", script);

    }

}
