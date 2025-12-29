package ru.itis.quizbattle.server;

import ru.itis.quizbattle.common.Message;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Основной класс сервера
 */
public class Server {
    private ServerSocket serverSocket;
    private GameState gameState;
    private List<ClientHandler> clients = new ArrayList<>();
    private boolean gameStarted = false;

    public Server() {
        gameState = null;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("✅ Сервер запущен на порту " + port);
        System.out.println("⏳ Ожидание игроков...");

        // Ждём двух игроков
        while (clients.size() < Message.MAX_PLAYERS) {
            Socket clientSocket = serverSocket.accept();
            int playerId = clients.size() + 1;

            System.out.println("🎮 Игрок " + playerId + " подключился: " +
                    clientSocket.getInetAddress().getHostAddress());

            // Создаем обработчик клиента
            ClientHandler client = new ClientHandler(clientSocket, this, playerId);
            clients.add(client);

            // Отправляем сообщение о подключении ПЕРЕД запуском потока
            client.sendJoinMessage();

            client.start();
            broadcastChat("0", "Игрок " + playerId + " присоединился к игре!");

            if (clients.size() == Message.MAX_PLAYERS) {
                startGame();
            } else {
                broadcastChat("0", "Ожидаем подключения " +
                        (Message.MAX_PLAYERS - clients.size()) + " игрока(ов)...");
            }
        }
    }

    private void startGame() {
        gameStarted = true;
        gameState = new GameState();
        System.out.println("🚀 Все игроки подключены! Начинаем игру!");
        broadcastChat("0", "Все игроки подключены! Игра начинается!");

        for (ClientHandler client : clients) {
            client.setGameState(gameState);
        }

        broadcastState();

        int currentPlayer = gameState.getCurrentPlayer();
        sendQuestionToCurrentPlayer();
        broadcastChat("0", "Игрок " + currentPlayer + " отвечает первым!");
    }

    public void broadcastState() {
        if (gameState != null) {
            Message msg = new Message(Message.Type.STATE,
                    String.valueOf(gameState.getPlayer1Hp()),
                    String.valueOf(gameState.getPlayer2Hp()));
            broadcast(msg);
        }
    }

    public void sendQuestionToCurrentPlayer() {
        if (gameState != null) {
            int currentPlayer = gameState.getCurrentPlayer();
            String question = gameState.getCurrentQuestion().getText();
            clients.get(currentPlayer - 1).sendMessage(
                    new Message(Message.Type.QUESTION, question)
            );
        }
    }

    public void broadcastWinner() {
        if (gameState != null) {
            int winner = gameState.getWinner();
            Message msg = new Message(Message.Type.WIN, String.valueOf(winner));
            broadcast(msg);
            System.out.println("🏆 Игра окончена! Победил игрок " + winner);
        }
    }

    public void broadcastChat(String sender, String message) {
        Message msg = new Message(Message.Type.CHAT, sender, message);
        broadcast(msg);
    }

    private void broadcast(Message msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("⚠️ Игрок отключился. Осталось игроков: " + clients.size());

        if (clients.size() < Message.MAX_PLAYERS && gameStarted) {
            broadcastChat("0", "Игра приостановлена. Ожидаем повторного подключения игроков...");
            gameStarted = false;
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public GameState getGameState() {
        return gameState;
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start(Message.PORT);
        } catch (IOException e) {
            System.err.println("❌ Ошибка сервера: " + e.getMessage());
        }
    }
}