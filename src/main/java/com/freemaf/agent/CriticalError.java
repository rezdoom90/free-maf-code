package com.freemaf.agent;
public class CriticalError extends RuntimeException {
    public CriticalError(String message) { super(message); }
    public CriticalError(String message, Throwable cause) { super(message, cause); }
}
