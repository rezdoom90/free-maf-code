
package com.freemaf.agent;

public final class LogReporter {

    private LogReporter() {}

    public static String build(ExecutionResult result) {

        StringBuilder sb = new StringBuilder();

        sb.append("### STDOUT").append(System.lineSeparator());

        sb.append(nullSafe(result.stdout())).append(System.lineSeparator());

        sb.append("### STDERR").append(System.lineSeparator());

        sb.append(nullSafe(result.stderr())).append(System.lineSeparator());

        sb.append("### EXIT_CODE").append(System.lineSeparator());

        sb.append(result.exitCode());

        return sb.toString();

    }

    private static String nullSafe(String s) { return s == null ? "" : s; }

}
