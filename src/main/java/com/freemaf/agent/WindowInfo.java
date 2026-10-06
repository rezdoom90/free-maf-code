
package com.freemaf.agent;

import com.freemaf.agent.winapi.WinApiService;

import com.sun.jna.Pointer;

public record WindowInfo(Pointer hwnd, String title, int left, int top, int right, int bottom) {

    
public static WindowInfo of(Pointer hwnd, String title, WinApiService.RECT r) {
        return new WindowInfo(hwnd, title, r.left, r.top, r.right, r.bottom);
    }

    public long hwndValue() {
        return hwnd == null ? 0L : Pointer.nativeValue(hwnd);
    }

}

