
package com.freemaf.agent;

public record AgentResponse(Type type, String role, String content) {

    public enum Type { USER_CHAT, PS_SCRIPT }

    public static AgentResponse userChat(String role, String content) {

        return new AgentResponse(Type.USER_CHAT, role, content);

    }

    public static AgentResponse script(String role, String content) {

        return new AgentResponse(Type.PS_SCRIPT, role, content);

    }

}
