
package com.freemaf.agent;

import com.freemaf.agent.winapi.WinApiService;

public final class GlobalHotkeyListener implements Runnable {

    private static final int VK_CONTROL = 0x11;

    private static final int VK_SHIFT = 0x10;

    private static final int VK_S = 0x53;

    private final Runnable onTrigger;

    private volatile boolean running = true;

    public GlobalHotkeyListener(Runnable onTrigger) {

        this.onTrigger = onTrigger;

    }

    public void stop() { running = false; }

    @Override

    public void run() {

        boolean wasPressed = false;

        while (running) {

            int ctrl = WinApiService.USER32.GetAsyncKeyState(VK_CONTROL);

            int shift = WinApiService.USER32.GetAsyncKeyState(VK_SHIFT);

            int s = WinApiService.USER32.GetAsyncKeyState(VK_S);

            boolean pressed = (ctrl & 0x8000) != 0 && (shift & 0x8000) != 0 && (s & 0x8000) != 0;

            if (pressed && !wasPressed) {

                AppLogger.info("Global hotkey Ctrl+Shift+S detected");

                try { onTrigger.run(); } catch (Exception e) {

                    AppLogger.warn("hotkey handler failed: " + e.getMessage());

                }

            }

            wasPressed = pressed;

            try { Thread.sleep(60L); }

            catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

        }

    }

}
