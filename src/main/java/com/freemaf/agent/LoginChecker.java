
package com.freemaf.agent;

public final class LoginChecker {

    private LoginChecker() {}

    public static boolean isSignInPage(WindowInfo window, Config config) {

        if (window == null) {

            return false;

        }

        String keywords = config.get("chrome.signin.keywords", "sign_in,Sign in,Login,Вход");

        String title = window.title().toLowerCase();

        for (String kw : keywords.split(",")) {

            String k = kw.trim().toLowerCase();

            if (!k.isEmpty() && title.contains(k)) {

                AppLogger.info("Sign-in detected in title: " + window.title());

                return true;

            }

        }

        return false;

    }

}
