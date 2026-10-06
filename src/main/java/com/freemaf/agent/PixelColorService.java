
package com.freemaf.agent;

import com.freemaf.agent.winapi.WinApiService;

import com.sun.jna.Pointer;

public final class PixelColorService {

    public int readPixel(int x, int y) {

        Pointer hdc = WinApiService.USER32.GetDC(null);

        if (hdc == null) {

            throw new IllegalStateException("GetDC returned null");

        }

        try {

            int colorRef = WinApiService.GDI32.GetPixel(hdc, x, y);

            if (colorRef == 0xFFFFFFFF) {

                throw new IllegalStateException("GetPixel returned CLR_INVALID for " + x + "," + y);

            }

            int r = colorRef & 0xFF;

            int g = (colorRef >> 8) & 0xFF;

            int b = (colorRef >> 16) & 0xFF;

            return (r << 16) | (g << 8) | b;

        } finally {

            WinApiService.USER32.ReleaseDC(null, hdc);

        }

    }

}
