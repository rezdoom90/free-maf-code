
package com.freemaf.agent;

import com.freemaf.agent.winapi.WinApiService;

import com.sun.jna.Pointer;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;

import java.util.HashSet;

import java.util.List;

import java.util.Set;

public final class ChromeLauncher {

    private static final String CHROME_CLASS_PREFIX = "Chrome_WidgetWin";

    private static final String CHROME_PROCESS_NAME = "chrome.exe";

    private static final long LAUNCH_TIMEOUT_MS = 30000L;

    private ChromeLauncher() {}

    public static boolean sameHwnd(Pointer a, Pointer b) {

        if (a == null || b == null) return false;

        return Pointer.nativeValue(a) == Pointer.nativeValue(b);

    }

    public static List<WindowInfo> findChromeWindows() {

        List<WindowInfo> result = new ArrayList<>();

        WinApiService.USER32.EnumWindows((hwnd, lParam) -> {

            if (!WinApiService.USER32.IsWindowVisible(hwnd)) return true;

            char[] cls = new char[256];

            int clsLen = WinApiService.USER32.GetClassNameW(hwnd, cls, cls.length);

            if (!new String(cls, 0, clsLen).startsWith(CHROME_CLASS_PREFIX)) return true;

            if (!isChromeProcess(hwnd)) return true;

            char[] title = new char[512];

            int titleLen = WinApiService.USER32.GetWindowTextW(hwnd, title, title.length);

            String titleStr = new String(title, 0, titleLen);

            if (titleStr.isBlank()) return true;

            WinApiService.RECT rect = new WinApiService.RECT();

            if (!WinApiService.USER32.GetWindowRect(hwnd, rect)) return true;

            result.add(WindowInfo.of(hwnd, titleStr, rect));

            return true;

        }, null);

        return result;

    }

    public static WindowInfo findDeepSeekWindow() {

        for (WindowInfo w : findChromeWindows()) {

            if (w.title().toLowerCase().contains("deepseek")) return w;

        }

        return null;

    }

    public static WindowInfo findDeepSeekWindowExcluding(List<Pointer> excludes) {

        for (WindowInfo w : findChromeWindows()) {

            if (!w.title().toLowerCase().contains("deepseek")) continue;

            boolean skip = false;

            if (excludes != null) {

                for (Pointer e : excludes) {

                    if (sameHwnd(w.hwnd(), e)) { skip = true; break; }

                }

            }

            if (!skip) return w;

        }

        return null;

    }

    public static WindowInfo launchAndWait(Config config) throws IOException, InterruptedException {

        String chromePath = resolveChromePath(config);

        String url = config.get("chrome.deepseek.url", "https://chat.deepseek.com/");

        AppLogger.info("Launching Chrome: " + chromePath + " --new-window " + url);

        new ProcessBuilder(chromePath, "--new-window", url).start();

        long deadline = System.currentTimeMillis() + LAUNCH_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {

            Thread.sleep(1000L);

            WindowInfo w = findDeepSeekWindow();

            if (w != null) return w;

        }

        throw new IOException("Chrome window with DeepSeek did not appear within 30s");

    }

    public static WindowInfo launchNewWindow(Config config, List<Pointer> excludes)

            throws IOException, InterruptedException {

        Set<Long> before = new HashSet<>();

        for (WindowInfo w : findChromeWindows()) before.add(Pointer.nativeValue(w.hwnd()));

        StringBuilder diag = new StringBuilder();

        diag.append("before=").append(before.size()).append(" windows");

        AppLogger.info("launchNewWindow: " + diag);

        String chromePath = resolveChromePath(config);

        String url = config.get("chrome.deepseek.url", "https://chat.deepseek.com/");

        AppLogger.info("launchNewWindow: exe=" + chromePath + " url=" + url);

        Process proc;

        try {

            ProcessBuilder pb = new ProcessBuilder(chromePath, "--new-window", url);

            pb.redirectErrorStream(true);

            proc = pb.start();

            AppLogger.info("launchNewWindow: chrome pid=" + proc.pid());

        } catch (IOException e) {

            throw new IOException("Failed to launch chrome.exe: " + e.getMessage() + " [" + diag + "]", e);

        }

        long timeout = config.getLong("timeout.new_window_ms", 45000L);

        long deadline = System.currentTimeMillis() + timeout;

        int polls = 0;

        List<Long> seenNew = new ArrayList<>();

        while (System.currentTimeMillis() < deadline) {

            Thread.sleep(700L);

            polls++;

            List<WindowInfo> current = findChromeWindows();

            StringBuilder snap = new StringBuilder();

            for (WindowInfo w : current) {

                long h = Pointer.nativeValue(w.hwnd());

                boolean isNew = !before.contains(h);

                snap.append(" [hwnd=").append(h)

                        .append(" new=").append(isNew)

                        .append(" title=\"").append(w.title()).append("\"]");

                if (isNew && !seenNew.contains(h)) {

                    seenNew.add(h);

                    AppLogger.info("launchNewWindow: NEW hwnd detected=" + h

                            + " title=\"" + w.title() + "\" (poll#" + polls + ")");

                }

            }

            if (polls <= 3 || polls % 10 == 0) {

                AppLogger.info("launchNewWindow: poll#" + polls + " windowCount=" + current.size() + snap);

            }

            for (WindowInfo w : current) {

                long h = Pointer.nativeValue(w.hwnd());

                if (before.contains(h)) continue;

                boolean skip = false;

                if (excludes != null) {

                    for (Pointer e : excludes) {

                        if (sameHwnd(w.hwnd(), e)) { skip = true; break; }

                    }

                }

                if (skip) continue;

                if (w.title().toLowerCase().contains("deepseek")) {

                    AppLogger.info("launchNewWindow: FOUND new DeepSeek window hwnd=" + h

                            + " after " + polls + " polls");

                    return w;

                }

            }

        }

        String msg = "Новое окно Chrome не появилось за " + timeout + " мс. "

                + "before=" + before.size() + " seenNew=" + seenNew.size();

        AppLogger.warn("launchNewWindow: " + msg);

        throw new IOException(msg);

    }

    public static void closeWindow(WindowInfo window) {

        if (window == null) return;

        Pointer hwnd = window.hwnd();

        if (hwnd == null || !WinApiService.USER32.IsWindow(hwnd)) return;

        WinApiService.USER32.PostMessageW(hwnd, WinApiService.WM_CLOSE, null, null);

        AppLogger.info("PostMessage WM_CLOSE sent to window hwnd=" + Pointer.nativeValue(hwnd));

    }

    public static String resolveChromePath(Config config) throws IOException {

        String configured = config.get("chrome.exe.path", "").trim();

        if (!configured.isEmpty() && new File(configured).isFile()) return configured;

        List<String> candidates = new ArrayList<>();

        candidates.add("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

        candidates.add("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");

        String localAppData = System.getenv("LOCALAPPDATA");

        if (localAppData != null) candidates.add(localAppData + "\\Google\\Chrome\\Application\\chrome.exe");

        for (String c : candidates) if (new File(c).isFile()) return c;

        throw new IOException("chrome.exe not found. Set chrome.exe.path in config.properties");

    }

    private static boolean isChromeProcess(Pointer hwnd) {

        int[] pid = new int[1];

        WinApiService.USER32.GetWindowThreadProcessId(hwnd, pid);

        if (pid[0] == 0) return false;

        Pointer hProcess = WinApiService.KERNEL32.OpenProcess(

                WinApiService.PROCESS_QUERY_LIMITED_INFORMATION, false, pid[0]);

        if (hProcess == null) return false;

        try {

            char[] buf = new char[1024];

            int[] size = new int[]{buf.length};

            if (!WinApiService.KERNEL32.QueryFullProcessImageNameW(hProcess, 0, buf, size)) return false;

            return new String(buf, 0, size[0]).toLowerCase().endsWith(CHROME_PROCESS_NAME);

        } finally {

            WinApiService.KERNEL32.CloseHandle(hProcess);

        }

    }

}
