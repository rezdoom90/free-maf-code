
package com.freemaf.agent;

public final class ExecutionLogger {

    private ExecutionLogger() {

    }

    public static void save(ExecutionResult result) {

        StringBuilder sb = new StringBuilder();

        sb.append("### STDOUT").append(System.lineSeparator());

        sb.append(result.stdout()).append(System.lineSeparator());

        sb.append("### STDERR").append(System.lineSeparator());

        sb.append(result.stderr()).append(System.lineSeparator());

        sb.append("### EXIT_CODE").append(System.lineSeparator());

        sb.append(result.exitCode());

        HistoryService.write("executions", sb.toString());

    }

}
