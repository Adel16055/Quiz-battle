package ru.itis.quizbattle.server;

import ru.itis.quizbattle.common.Message;
import ru.itis.quizbattle.common.Protocol;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private ServerSocket serverSocket;
    private GameState gameState;
    private List<ClientHandler> clients = new ArrayList<>();
    private QuestionManager questionManager;

    public Server() {
        questionManager = new QuestionManager("questions.txt");
        gameState = new GameState(questionManager);
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущен на порту " + port);
        System.out.println("Ожидание игроков...");

        // Ждём двух игроков
        for (int i = 1; i <= Protocol.MAX_PLAYERS; i++) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Игрок " + i + " подключился");

            ClientHandler client = new ClientHandler(clientSocket, gameState, i, this);
            clients.add(client);
            client.start();

            if (i == Protocol.MAX_PLAYERS) {
                System.out.println("Все игроки подключены! Начинаем игру!");
                broadcastState();
                broadcastQuestion();
            }
        }
    }

    public void broadcastState() {
        Message msg = new Message(Message.Type.STATE,
                String.valueOf(gameState.getPlayer1Hp()),
                String.valueOf(gameState.getPlayer2Hp()));
        broadcast(msg);
    }

    public void broadcastQuestion() {
        String question = gameState.getCurrentQuestion().getText();
        Message msg = new Message(Message.Type.QUESTION, question);
        broadcast(msg);
    }

    public void broadcastWinner() {
        int winner = gameState.getWinner();
        Message msg = new Message(Message.Type.WIN, String.valueOf(winner));
        broadcast(msg);
    }

    private void broadcast(Message msg) {
        for (ClientHandler client : clients) { // Убрал com.quizbattle.server.
            client.sendMessage(msg);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start(Protocol.PORT);
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }
}