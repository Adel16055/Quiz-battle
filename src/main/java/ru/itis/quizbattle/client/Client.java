package ru.itis.quizbattle.client;

import ru.itis.quizbattle.common.Message;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Сетевой клиент для подключения к серверу
 */
public class Client extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientGUI gui;
    private int playerId;

    // Карта обработчиков сообщений
    private final Map<Message.Type, Consumer<Message>> handlers = new HashMap<>();

    public Client(String serverAddress, ClientGUI gui) {
        this.gui = gui;
        initHandlers();
        connectToServer(serverAddress);
    }

    private void connectToServer(String serverAddress) {
        try {
            socket = new Socket(serverAddress, Message.PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            gui.setConnected(true);
            gui.addMessage("Подключено к серверу " + serverAddress);
        } catch (IOException e) {
            gui.setConnected(false);
            gui.addMessage("Ошибка подключения: " + e.getMessage());
        }
    }

    private void initHandlers() {
        handlers.put(Message.Type.JOIN, this::handleJoin);
        handlers.put(Message.Type.QUESTION, this::handleQuestion);
        handlers.put(Message.Type.STATE, this::handleState);
        handlers.put(Message.Type.CHAT, this::handleChat);
        handlers.put(Message.Type.WIN, this::handleWin);
        handlers.put(Message.Type.ERROR, this::handleError);
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Получено от сервера: " + serverMessage); // Отладка
                Message msg = Message.fromString(serverMessage);
                processMessage(msg);
            }
        } catch (IOException e) {
            gui.setConnected(false);
            gui.addMessage("Соединение разорвано");
        }
    }

    private void processMessage(Message msg) {
        Consumer<Message> handler = handlers.get(msg.getType());
        if (handler != null) {
            handler.accept(msg);
        } else {
            gui.addMessage("Неизвестный тип сообщения: " + msg.getType());
        }
    }

    private void handleJoin(Message msg) {
        playerId = Integer.parseInt(msg.getData()[0]);
        gui.setPlayerId(playerId);
        gui.addMessage("Вы игрок " + playerId);
        // Обновляем заголовок окна
        gui.setTitle("Quiz Battle - Игрок " + playerId);
    }

    private void handleQuestion(Message msg) {
        String question = msg.getData()[0];
        System.out.println("Получен вопрос от сервера: " + question); // Отладка
        gui.setQuestion(question);
    }

    private void handleState(Message msg) {
        int player1Hp = Integer.parseInt(msg.getData()[0]);
        int player2Hp = Integer.parseInt(msg.getData()[1]);

        gui.updateGameState(player1Hp, player2Hp);

        // Проверяем, нужно ли запустить анимацию атаки
        if (gui.getPlayer1Hp() != player1Hp || gui.getPlayer2Hp() != player2Hp) {
            // Определяем, кто атаковал (у кого уменьшилось HP)
            if (gui.getPlayer1Hp() > player1Hp) {
                gui.triggerAttackAnimation(2); // Игрок 2 атаковал
            } else if (gui.getPlayer2Hp() > player2Hp) {
                gui.triggerAttackAnimation(1); // Игрок 1 атаковал
            }
        }
    }

    private void handleChat(Message msg) {
        String sender = msg.getData()[0];
        String text = msg.getData()[1];
        String prefix = sender.equals("0") ? "Система" : "Игрок " + sender;
        gui.addMessage(prefix + ": " + text);
    }

    private void handleWin(Message msg) {
        int winner = Integer.parseInt(msg.getData()[0]);
        gui.showWinner(winner);
    }

    private void handleError(Message msg) {
        gui.addMessage("Ошибка: " + msg.getData()[0]);
    }

    public void sendAnswer(String answer) {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            Message msg = new Message(Message.Type.ANSWER, String.valueOf(playerId), answer);
            out.println(msg.toString());
        } else {
            gui.addMessage("Нет подключения к серверу");
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Игнорируем ошибку закрытия
        }
        gui.setConnected(false);
    }
}