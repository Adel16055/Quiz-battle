package ru.itis.quizbattle.client;

import ru.itis.quizbattle.common.Message;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Client extends Thread {
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private ClientGUI clientGUI;
    private int playerId;

    private final Map<Message.Type, Consumer<Message>> handlers = new HashMap<>();

    public Client(String serverAddress, ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
        initHandlers();
        connectToServer(serverAddress);
    }

    private void connectToServer(String serverAddress) {
        try {
            socket = new Socket(serverAddress, Message.PORT);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientGUI.setConnected(true);
            clientGUI.addMessage("Подключено к серверу " + serverAddress);
        } catch (IOException e) {
            clientGUI.setConnected(false);
            clientGUI.addMessage("Ошибка подключения: " + e.getMessage());
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
            while ((serverMessage = bufferedReader.readLine()) != null) {
                Message msg = Message.fromString(serverMessage);
                processMessage(msg);
            }
        } catch (IOException e) {
            clientGUI.setConnected(false);
            clientGUI.addMessage("Соединение разорвано");
        }
    }

    private void processMessage(Message msg) {
        Consumer<Message> handler = handlers.get(msg.getType());
        if (handler != null) {
            handler.accept(msg);
        } else {
            clientGUI.addMessage("Неизвестный тип сообщения: " + msg.getType());
        }
    }

    private void handleJoin(Message msg) {
        playerId = Integer.parseInt(msg.getData()[0]);
        clientGUI.setPlayerId(playerId);
        clientGUI.addMessage("Вы игрок " + playerId);
        clientGUI.setTitle("Quiz Battle - Игрок " + playerId);
    }

    private void handleQuestion(Message msg) {
        String question = msg.getData()[0];
        clientGUI.setQuestion(question);
    }

    private void handleState(Message msg) {
        int player1Hp = Integer.parseInt(msg.getData()[0]);
        int player2Hp = Integer.parseInt(msg.getData()[1]);

        clientGUI.updateGameState(player1Hp, player2Hp);

        if (clientGUI.getPlayer1Hp() != player1Hp || clientGUI.getPlayer2Hp() != player2Hp) {
            if (clientGUI.getPlayer1Hp() > player1Hp) {
                clientGUI.triggerAttackAnimation(2);
            } else if (clientGUI.getPlayer2Hp() > player2Hp) {
                clientGUI.triggerAttackAnimation(1);
            }
        }
    }

    private void handleChat(Message msg) {
        String sender = msg.getData()[0];
        String text = msg.getData()[1];
        String prefix = sender.equals("0") ? "Система" : "Игрок " + sender;
        clientGUI.addMessage(prefix + ": " + text);
    }

    private void handleWin(Message msg) {
        int winner = Integer.parseInt(msg.getData()[0]);
        clientGUI.showWinner(winner);
    }

    private void handleError(Message msg) {
        clientGUI.addMessage("Ошибка: " + msg.getData()[0]);
    }

    public void sendAnswer(String answer) {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            Message msg = new Message(Message.Type.ANSWER, String.valueOf(playerId), answer);
            printWriter.println(msg.toString());
        } else {
            clientGUI.addMessage("Нет подключения к серверу");
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
        }
        clientGUI.setConnected(false);
    }
}