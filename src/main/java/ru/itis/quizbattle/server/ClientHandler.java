package ru.itis.quizbattle.server;
import ru.itis.quizbattle.common.Message;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private GameState gameState;
    private PrintWriter out;
    private BufferedReader in;
    private int playerId;
    private Server server;

    public ClientHandler(Socket socket, GameState gameState, int playerId, Server server) {
        this.socket = socket;
        this.gameState = gameState;
        this.playerId = playerId;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Отправляем приветственное сообщение
            out.println(new Message(Message.Type.JOIN, String.valueOf(playerId)));

            // Отправляем первый вопрос
            sendQuestion();

            // Основной цикл обработки сообщений
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                Message msg = Message.fromString(inputLine);
                System.out.println("От игрока " + playerId + ": " + msg);

                if (msg.getType() == Message.Type.ANSWER) {
                    String answer = msg.getData()[1];
                    String result = gameState.processAnswer(playerId, answer);

                    // Отправляем обновлённое состояние всем игрокам
                    server.broadcastState();

                    // Отправляем результат ответа
                    out.println(new Message(Message.Type.CHAT, "0", result));

                    // Если игра окончена
                    if (gameState.isGameOver()) {
                        server.broadcastWinner();
                        break;
                    }

                    // Если это был правильный ответ, отправляем новый вопрос
                    if (result.contains("Правильно")) {
                        server.broadcastQuestion();
                    }
                }
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Ошибка клиента " + playerId + ": " + e.getMessage());
        }
    }

    private void sendQuestion() {
        if (gameState.getCurrentPlayer() == playerId) {
            String question = gameState.getCurrentQuestion().getText();
            out.println(new Message(Message.Type.QUESTION, question));
        }
    }

    public void sendMessage(Message msg) {
        out.println(msg.toString());
    }
}