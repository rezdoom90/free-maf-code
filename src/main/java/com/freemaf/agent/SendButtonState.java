
package com.freemaf.agent;

public final class SendButtonState {

    private SendButtonState() {}

    public static boolean isEnabled(String inputText, int attachedFileCount) {

        boolean hasText = inputText != null && !inputText.trim().isEmpty();

        return hasText || attachedFileCount > 0;

    }

}
