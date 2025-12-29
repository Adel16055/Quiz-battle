package ru.itis.quizbattle.common;

/**
 * Класс сообщений протокола с константами
 */
public class Message {

    // Константы протокола
    public static final int PORT = 8080;
    public static final int MAX_PLAYERS = 2;
    public static final int INITIAL_HP = 100;
    public static final int DAMAGE_PER_QUESTION = 20;

    // Типы сообщений
    public enum Type {
        JOIN,        // JOIN:playerId
        QUESTION,    // QUESTION:text
        ANSWER,      // ANSWER:playerId:text
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