package ru.itis.quizbattle.server;

import ru.itis.quizbattle.common.Message;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private int playerId;
    private GameState gameState;

    public ClientHandler(Socket socket, Server server, int playerId) {
        this.socket = socket;
        this.server = server;
        this.playerId = playerId;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void sendJoinMessage() {
        try {
            printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(new Message(Message.Type.JOIN, String.valueOf(playerId)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                Message msg = Message.fromString(inputLine);

                if (msg.getType() == Message.Type.ANSWER) {
                    processAnswer(msg.getData()[1]);
                }
            }

            socket.close();
            server.removeClient(this);

        } catch (IOException e) {
            server.removeClient(this);
        }
    }

    private void processAnswer(String answer) {
        if (!server.isGameStarted()) {
            sendMessage(new Message(Message.Type.CHAT, "0", "Игра еще не началась! Ожидаем второго игрока."));
            return;
        }

        if (gameState == null) {
            sendMessage(new Message(Message.Type.ERROR, "Состояние игры не инициализировано"));
            return;
        }

        String result = gameState.processAnswer(playerId, answer);

        server.broadcastChat(String.valueOf(playerId), "Игрок " + playerId + " ответил: " + answer);
        server.broadcastChat("0", result);

        server.broadcastState();

        if (gameState.isGameOver()) {
            server.broadcastWinner();
            return;
        }

        server.sendQuestionToCurrentPlayer();
        int currentPlayer = gameState.getCurrentPlayer();
        server.broadcastChat("0", "Сейчас отвечает игрок " + currentPlayer);
    }

    public void sendMessage(Message message) {
        if (printWriter != null) {
            printWriter.println(message.toString());
            printWriter.flush();
        }
    }
}