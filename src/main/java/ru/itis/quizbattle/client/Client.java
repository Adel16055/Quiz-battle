package ru.itis.quizbattle.client;

import ru.itis.quizbattle.common.Message;
import java.io.*;
import java.net.Socket;

public class Client extends Thread {
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private ClientGUI clientGUI;
    private int playerId;

    public Client(String serverAddress, ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
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
        Message.Type type = msg.getType();

        switch (type) {
            case JOIN:
                handleJoin(msg);
                break;
            case QUESTION:
                handleQuestion(msg);
                break;
            case STATE:
                handleState(msg);
                break;
            case CHAT:
                handleChat(msg);
                break;
            case WIN:
                handleWin(msg);
                break;
            case ERROR:
                handleError(msg);
                break;
            default:
                clientGUI.addMessage("Неизвестный тип сообщения: " + type);
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

        if (msg.getData().length > 2) {
            int currentPlayer = Integer.parseInt(msg.getData()[2]);
            clientGUI.setCurrentTurnPlayer(currentPlayer);
        }

        clientGUI.updateGameState(player1Hp, player2Hp);

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
            e.printStackTrace();
        }
        clientGUI.setConnected(false);
    }
}