package com.freemaf.agent;

import com.freemaf.agent.winapi.WinApiService;

public final class SessionDetector {

    private SessionDetector() {}

    public enum Decision {
        OPEN_NEW_CHAT,
        CONTINUE_OLD_SESSION,
        RESUME_REVIEWER_SESSION,
        RECOVER_NEW_EXECUTOR
    }

    public record SessionDecision(
            Decision decision,
            WindowInfo activeWindow,
            WindowInfo executorWindow,
            WindowInfo reviewerWindow,
            String recoveryMessage) {

        public boolean isContinuation() {
            return decision == Decision.CONTINUE_OLD_SESSION;
        }
    }

    public static SessionDecision detect(String titleSubstring, Config config) {
        String lastPhase = config.getLastPhase();
        long lastManagedHwnd = config.getLastManagedHwnd();
        long lastReviewerHwnd = config.getLastReviewerHwnd();
        WindowInfo executorWindow = WindowFinder.findByHwnd(lastManagedHwnd, titleSubstring).orElse(null);
        WindowInfo reviewerWindow = WindowFinder.findByHwnd(lastReviewerHwnd, titleSubstring).orElse(null);
        boolean reviewerPhase = "JUDGE".equals(lastPhase) || "CODE_REVIEWER".equals(lastPhase);
        if (!reviewerPhase) {
            if (reviewerWindow != null) {
                WinApiService.closeWindow(reviewerWindow.hwndValue());
                reviewerWindow = null;
            }
            if (executorWindow != null) {
                return new SessionDecision(Decision.CONTINUE_OLD_SESSION, executorWindow, executorWindow, null, null);
            }
            return new SessionDecision(Decision.OPEN_NEW_CHAT, null, null, null, null);
        }
        if (executorWindow != null && reviewerWindow != null) {
            String msg = buildResumeMessage(lastPhase);
            return new SessionDecision(Decision.RESUME_REVIEWER_SESSION, reviewerWindow, executorWindow, reviewerWindow, msg);
        }
        if (reviewerWindow != null) {
            WinApiService.closeWindow(reviewerWindow.hwndValue());
            reviewerWindow = null;
        }
        return new SessionDecision(Decision.RECOVER_NEW_EXECUTOR, null, executorWindow, null, RECOVER_MESSAGE);
    }

    private static String buildResumeMessage(String phase) {
        if ("JUDGE".equals(phase)) {
            return "Предыдущая сессия была прервана во время проверки (роль: JUDGE). Окно проверяющего сохранено. Продолжи проверку с того же места либо полностью повтори последнее действие.";
        }
        return "Предыдущая сессия была прервана во время проверки (роль: CODE_REVIEWER). Окно проверяющего сохранено. Продолжи проверку с того же места либо полностью повтори последнее действие.";
    }

    private static final String RECOVER_MESSAGE =
            "Предыдущая сессия была прервана. Последний запрос проверки завершился ошибкой при выполнении, проверка не произошла. Ошибка не на твоей стороне, ничего срочно исправлять не нужно. Запроси проверку заново.";
}
