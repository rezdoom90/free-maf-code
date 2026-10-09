package com.freemaf.agent.winapi;
import com.freemaf.agent.AppLogger;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import java.util.List;

public final class JobObjectManager {
    private static final int JOB_OBJECT_EXTENDED_LIMIT_INFORMATION_CLASS = 9;
    private static final int JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE = 0x2000;
    private static final int PROCESS_TERMINATE = 0x0001;
    private static final int PROCESS_SET_QUOTA = 0x0100;
    private static final JobKernel32 KERNEL32;
    private static volatile Pointer jobHandle;
    static {
        JobKernel32 k32 = null;
        Pointer h = null;
        try {
            k32 = Native.load("kernel32", JobKernel32.class);
            h = k32.CreateJobObjectW(null, null);
            if (h != null) {
                JOBOBJECT_EXTENDED_LIMIT_INFORMATION info = new JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
                info.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
                info.write();
                boolean ok = k32.SetInformationJobObject(h, JOB_OBJECT_EXTENDED_LIMIT_INFORMATION_CLASS, info.getPointer(), info.size());
                if (!ok) {
                    AppLogger.warn("JobObjectManager: SetInformationJobObject failed, code=" + Native.getLastError());
                    k32.CloseHandle(h);
                    h = null;
                }
            } else {
                AppLogger.warn("JobObjectManager: CreateJobObjectW failed, code=" + Native.getLastError());
            }
        } catch (Throwable t) {
            AppLogger.warn("JobObjectManager: init failed: " + t.getMessage());
            h = null;
        }
        KERNEL32 = k32;
        jobHandle = h;
    }
    private JobObjectManager() {}
    public static boolean isActive() {
        return jobHandle != null && KERNEL32 != null;
    }
    public static boolean assign(Process p) {
        if (p == null || !isActive()) return false;
        Pointer hProcess = null;
        try {
            hProcess = KERNEL32.OpenProcess(PROCESS_SET_QUOTA | PROCESS_TERMINATE, false, (int) p.pid());
            if (hProcess == null) {
                AppLogger.warn("JobObjectManager.assign: OpenProcess failed pid=" + p.pid());
                return false;
            }
            boolean ok = KERNEL32.AssignProcessToJobObject(jobHandle, hProcess);
            if (!ok) {
                AppLogger.warn("JobObjectManager.assign: AssignProcessToJobObject failed pid=" + p.pid() + " code=" + Native.getLastError());
            }
            return ok;
        } catch (Throwable t) {
            AppLogger.warn("JobObjectManager.assign: exception pid=" + p.pid() + " msg=" + t.getMessage());
            return false;
        } finally {
            if (hProcess != null) { KERNEL32.CloseHandle(hProcess); }
        }
    }
    public static void closeHandleForTest() {
        if (jobHandle != null && KERNEL32 != null) {
            KERNEL32.CloseHandle(jobHandle);
            jobHandle = null;
        }
    }
    public static void recreateForTest() {
        if (jobHandle != null || KERNEL32 == null) return;
        Pointer h = KERNEL32.CreateJobObjectW(null, null);
        if (h == null) return;
        JOBOBJECT_EXTENDED_LIMIT_INFORMATION info = new JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
        info.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
        info.write();
        if (KERNEL32.SetInformationJobObject(h, JOB_OBJECT_EXTENDED_LIMIT_INFORMATION_CLASS, info.getPointer(), info.size())) {
            jobHandle = h;
        } else {
            KERNEL32.CloseHandle(h);
        }
    }
    public interface JobKernel32 extends StdCallLibrary {
        Pointer CreateJobObjectW(Pointer lpJobAttributes, String lpName);
        boolean AssignProcessToJobObject(Pointer hJob, Pointer hProcess);
        boolean SetInformationJobObject(Pointer hJob, int infoClass, Pointer lpInfo, int cbInfo);
        Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
        boolean CloseHandle(Pointer hObject);
    }
    public static class IO_COUNTERS extends Structure {
        public long ReadOperationCount;
        public long WriteOperationCount;
        public long OtherOperationCount;
        public long ReadTransferCount;
        public long WriteTransferCount;
        public long OtherTransferCount;
        @Override protected List<String> getFieldOrder() {
            return List.of("ReadOperationCount","WriteOperationCount","OtherOperationCount","ReadTransferCount","WriteTransferCount","OtherTransferCount");
        }
    }
    public static class JOBOBJECT_BASIC_LIMIT_INFORMATION extends Structure {
        public long PerProcessUserTimeLimit;
        public long PerJobUserTimeLimit;
        public int LimitFlags;
        public long MinimumWorkingSetSize;
        public long MaximumWorkingSetSize;
        public int ActiveProcessLimit;
        public long Affinity;
        public int PriorityClass;
        public int SchedulingClass;
        @Override protected List<String> getFieldOrder() {
            return List.of("PerProcessUserTimeLimit","PerJobUserTimeLimit","LimitFlags","MinimumWorkingSetSize","MaximumWorkingSetSize","ActiveProcessLimit","Affinity","PriorityClass","SchedulingClass");
        }
    }
    public static class JOBOBJECT_EXTENDED_LIMIT_INFORMATION extends Structure {
        public JOBOBJECT_BASIC_LIMIT_INFORMATION BasicLimitInformation;
        public IO_COUNTERS IoInfo;
        public long ProcessMemoryLimit;
        public long JobMemoryLimit;
        public long PeakProcessMemoryUsed;
        public long PeakJobMemoryUsed;
        public JOBOBJECT_EXTENDED_LIMIT_INFORMATION() { super(); }
        @Override protected List<String> getFieldOrder() {
            return List.of("BasicLimitInformation","IoInfo","ProcessMemoryLimit","JobMemoryLimit","PeakProcessMemoryUsed","PeakJobMemoryUsed");
        }
    }
}
