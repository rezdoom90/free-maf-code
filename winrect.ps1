Add-Type @'
using System;
using System.Runtime.InteropServices;
public class W {
    [DllImport("user32.dll")] public static extern IntPtr GetForegroundWindow();
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr h, out RECT r);
    [DllImport("user32.dll", CharSet=CharSet.Unicode)] public static extern int GetWindowTextW(IntPtr h, System.Text.StringBuilder s, int n);
    [StructLayout(LayoutKind.Sequential)] public struct RECT { public int Left, Top, Right, Bottom; }
}
'@
Write-Host 'Focus a window. Ctrl+C to exit.'
while ($true) {
    $h = [W]::GetForegroundWindow()
    $sb = New-Object System.Text.StringBuilder 512
    [W]::GetWindowTextW($h, $sb, 512) | Out-Null
    $r = New-Object W+RECT
    [W]::GetWindowRect($h, [ref]$r) | Out-Null
    $w = $r.Right - $r.Left
    $hh = $r.Bottom - $r.Top
    Write-Host ("x={0,5} y={1,5} w={2,5} h={3,5}  title={4}" -f $r.Left, $r.Top, $w, $hh, $sb.ToString())
    Start-Sleep -Milliseconds 200
}