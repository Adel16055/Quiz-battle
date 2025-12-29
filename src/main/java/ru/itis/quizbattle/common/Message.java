package ru.itis.quizbattle.common;

public class Message {
    public enum Type {
        JOIN,        // JOIN:playerId
        QUESTION,    // QUESTION:text
        ANSWER,      // ANSWER:playerId:text
        DAMAGE,      // DAMAGE:playerId:value
        STATE,       // STATE:player1Hp:player2Hp
        WIN,         // WIN:playerId
        CHAT,        // CHAT:playerId:text
        ERROR        // ERROR:message
    }

    private Type type;
    private String[] data;

    public Message(Type type, String... data) {
        this.type = type;
        this.data = data;
    }

    public String toString() {
        return type.name() + ":" + String.join(":", data);
    }

    public static Message fromString(String str) {
        String[] parts = str.split(":", 2);
        Type type = Type.valueOf(parts[0]);
        String[] data = parts.length > 1 ? parts[1].split(":") : new String[0];
        return new Message(type, data);
    }

    public Type getType() {
        return type;
    }

    public String[] getData() {
        return data;
    }
}