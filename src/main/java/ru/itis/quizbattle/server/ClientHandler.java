package ru.itis.quizbattle.server;

import ru.itis.quizbattle.common.Message;

import java.io.*;
import java.net.Socket;

/**
 * Обработчик клиентского подключения
 */
public class ClientHandler extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
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

    // Метод для отправки JOIN сообщения ПЕРЕД запуском потока
    public void sendJoinMessage() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(new Message(Message.Type.JOIN, String.valueOf(playerId)));
        } catch (IOException e) {
            System.err.println("❌ Ошибка отправки JOIN сообщения игроку " + playerId + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // Инициализируем потоки ввода
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Основной цикл обработки сообщений
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                Message msg = Message.fromString(inputLine);
                System.out.println("📨 От игрока " + playerId + ": " + msg);

                if (msg.getType() == Message.Type.ANSWER) {
                    processAnswer(msg.getData()[1]);
                }
            }

            socket.close();
            server.removeClient(this);

        } catch (IOException e) {
            System.err.println("⚠️ Ошибка клиента " + playerId + ": " + e.getMessage());
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

    public void sendMessage(Message msg) {
        if (out != null) {
            out.println(msg.toString());
        }
    }
}