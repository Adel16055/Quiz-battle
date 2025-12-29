package ru.itis.quizbattle.client;

import ru.itis.quizbattle.common.Message;
import ru.itis.quizbattle.common.Protocol;
import java.io.*;
import java.net.Socket;

public class Client extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientGUI gui;
    private int playerId;

    public Client(String serverAddress, ClientGUI gui) {
        this.gui = gui;
        try {
            socket = new Socket(serverAddress, Protocol.PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            gui.addMessage("Подключено к серверу " + serverAddress);
        } catch (IOException e) {
            gui.addMessage("Ошибка подключения: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                Message msg = Message.fromString(serverMessage);
                processMessage(msg);
            }
        } catch (IOException e) {
            gui.addMessage("Соединение разорвано");
        }
    }

    private void processMessage(Message msg) {
        switch (msg.getType()) {
            case JOIN:
                playerId = Integer.parseInt(msg.getData()[0]);
                gui.setPlayerId(playerId);
                gui.addMessage("Вы игрок " + playerId);
                break;

            case QUESTION:
                String question = msg.getData()[0];
                gui.setQuestion(question);
                break;

            case STATE:
                int player1Hp = Integer.parseInt(msg.getData()[0]);
                int player2Hp = Integer.parseInt(msg.getData()[1]);
                gui.updateGameState(player1Hp, player2Hp);
                break;

            case CHAT:
                String sender = msg.getData()[0];
                String text = msg.getData()[1];
                String prefix = sender.equals("0") ? "Система" : "Игрок " + sender;
                gui.addMessage(prefix + ": " + text);
                break;

            case WIN:
                int winner = Integer.parseInt(msg.getData()[0]);
                gui.showWinner(winner);
                break;

            case ERROR:
                gui.addMessage("Ошибка: " + msg.getData()[0]);
                break;
        }
    }

    public void sendAnswer(String answer) {
        Message msg = new Message(Message.Type.ANSWER, String.valueOf(playerId), answer);
        out.println(msg.toString());
    }
}