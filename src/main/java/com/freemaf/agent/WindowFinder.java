
package com.freemaf.agent;

import com.freemaf.agent.winapi.WinApiService;

import com.sun.jna.Pointer;

import javax.swing.JOptionPane;

import java.util.ArrayList;

import java.util.List;

public final class WindowFinder {

    private WindowFinder() {

    }

    public static List<WindowInfo> findWindows(String searchTitle) {

        List<Pointer> handles = new ArrayList<Pointer>();

        WinApiService.USER32.EnumWindows((hwnd, lParam) -> {

            char[] buffer = new char[512];

            int length = WinApiService.USER32.GetWindowTextW(hwnd, buffer, buffer.length);

            String title = new String(buffer, 0, length);

            if (!title.isBlank() && title.toLowerCase().contains(searchTitle.toLowerCase())) {

                handles.add(hwnd);

            }

            return true;

        }, null);

        List<WindowInfo> result = new ArrayList<WindowInfo>();

        for (Pointer hwnd : handles) {

            WinApiService.RECT rect = new WinApiService.RECT();

            if (!WinApiService.USER32.GetWindowRect(hwnd, rect)) {

                continue;

            }

            char[] buffer = new char[512];

            int length = WinApiService.USER32.GetWindowTextW(hwnd, buffer, buffer.length);

            String title = new String(buffer, 0, length);

            result.add(WindowInfo.of(hwnd, title, rect));

        }

        return result;

    }

    public static WindowInfo choose(List<WindowInfo> matches) {

        if (matches.isEmpty()) {

            return null;

        }

        if (matches.size() == 1) {

            return matches.get(0);

        }

        String[] options = new String[matches.size()];

        for (int i = 0; i < matches.size(); i++) {

            WindowInfo w = matches.get(i);

            options[i] = w.title() + " [" + w.left() + "," + w.top() + " " + w.right() + "x" + w.bottom() + "]";

        }

        Object selected = JOptionPane.showInputDialog(null, "Выберите окно", "Выбор окна", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (selected == null) {

            return null;

        }

        for (int i = 0; i < options.length; i++) {

            if (options[i].equals(selected)) {

                return matches.get(i);

            }

        }

        return null;

    }

}
