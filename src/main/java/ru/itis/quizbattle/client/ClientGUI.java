package ru.itis.quizbattle.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ClientGUI {
    private JFrame frame;
    private GamePanel gamePanel;
    private JTextArea chatArea;
    private JTextField answerField;
    private JButton sendButton;
    private Client client;
    private int playerId;

    public ClientGUI() {
        createGUI();
        connectToServer();
    }

    private void createGUI() {
        frame = new JFrame("Quiz Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Игровое поле
        gamePanel = new GamePanel(0); // временно 0, потом обновим
        frame.add(gamePanel, BorderLayout.CENTER);

        // Панель чата и ввода
        JPanel bottomPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea(8, 40);
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        answerField = new JTextField();
        answerField.addActionListener(e -> sendAnswer());

        sendButton = new JButton("Ответить");
        sendButton.addActionListener(e -> sendAnswer());

        inputPanel.add(new JLabel("Ваш ответ: "), BorderLayout.WEST);
        inputPanel.add(answerField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void connectToServer() {
        String serverAddress = JOptionPane.showInputDialog(
                frame,
                "Введите адрес сервера:",
                "localhost"
        );
        if (serverAddress == null || serverAddress.trim().isEmpty()) {
            serverAddress = "localhost";
        }

        client = new Client(serverAddress, this);
        client.start();
    }

    private void sendAnswer() {
        String answer = answerField.getText().trim();
        if (!answer.isEmpty() && client != null) {
            client.sendAnswer(answer);
            answerField.setText("");
        }
    }

    public void updateGameState(int player1Hp, int player2Hp) {
        gamePanel.updateState(player1Hp, player2Hp);
    }

    public void setQuestion(String question) {
        gamePanel.setQuestion(question);
        addMessage("Новый вопрос: " + question);
    }

    public void setPlayerId(int id) {
        this.playerId = id;
        // Создаем новую панель с правильным ID игрока
        GamePanel newGamePanel = new GamePanel(id);

        // Заменяем старую панель на новую
        frame.getContentPane().removeAll();
        frame.add(newGamePanel, BorderLayout.CENTER);

        // Восстанавливаем нижнюю панель с чатом
        JPanel bottomPanel = createBottomPanel();
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.revalidate();
        frame.repaint();

        this.gamePanel = newGamePanel;
        addMessage("Вы игрок " + id);
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea(8, 40);
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        answerField = new JTextField();
        answerField.addActionListener(e -> sendAnswer());

        sendButton = new JButton("Ответить");
        sendButton.addActionListener(e -> sendAnswer());

        inputPanel.add(new JLabel("Ваш ответ: "), BorderLayout.WEST);
        inputPanel.add(answerField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        return bottomPanel;
    }

    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void showWinner(int winner) {
        SwingUtilities.invokeLater(() -> {
            String message = winner == playerId ?
                    "Поздравляем! Вы победили!" :
                    "Игрок " + winner + " победил!";
            JOptionPane.showMessageDialog(frame, message, "Конец игры", JOptionPane.INFORMATION_MESSAGE);
            sendButton.setEnabled(false);
            answerField.setEnabled(false);
        });
    }

    public void setTitle(String title) {
        frame.setTitle(title);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}