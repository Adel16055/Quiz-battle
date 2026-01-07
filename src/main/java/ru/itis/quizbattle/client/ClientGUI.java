package ru.itis.quizbattle.client;

import ru.itis.quizbattle.common.Message;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientGUI {
    public JFrame frame;
    private JTextArea chatArea;
    private JTextField answerField;
    private JButton sendButton;
    private JLabel connectionStatus;
    private Client client;

    private int playerId = 0;
    private int player1Hp = Message.INITIAL_HP;
    private int player2Hp = Message.INITIAL_HP;
    private String currentQuestion = "Ожидание вопроса...";
    private boolean gameStarted = false;
    private int currentTurnPlayer = 0;

    private boolean isAnimating = false;
    private int attackAnimationFrame = 0;
    private int attackerId = 1;

    private boolean damageAnimationActive = false;
    private int damageAnimationFrame = 0;
    private int damagedPlayerId = 0;
    private Timer damageAnimationTimer;

    private boolean highlightAnimationActive = false;
    private int highlightAnimationFrame = 0;

    public ClientGUI() {
        createGUI();
        connectToServer();
    }

    private void createGUI() {
        frame = new JFrame("Quiz Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    client.disconnect();
                }
            }
        });

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionStatus = new JLabel("Не подключено");
        connectionStatus.setForeground(Color.RED);
        statusPanel.add(connectionStatus);
        frame.add(statusPanel, BorderLayout.NORTH);

        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(900, 600);
        frame.setMinimumSize(new Dimension(800, 500));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea(8, 40);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        answerField = new JTextField();
        answerField.addActionListener(e -> sendAnswer());

        sendButton = new JButton("Ответить");
        sendButton.addActionListener(e -> sendAnswer());
        sendButton.setEnabled(false);

        inputPanel.add(new JLabel("Ваш ответ: "), BorderLayout.WEST);
        inputPanel.add(answerField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        return bottomPanel;
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
        if (this.player1Hp > player1Hp) {
            triggerDamageAnimation(1);
        }
        if (this.player2Hp > player2Hp) {
            triggerDamageAnimation(2);
        }

        this.player1Hp = player1Hp;
        this.player2Hp = player2Hp;
        gameStarted = true;
        frame.repaint();
    }

    public void setQuestion(String question) {
        this.currentQuestion = question;
        if (!question.equals("Ожидание вопроса...")) {
            addMessage("Новый вопрос: " + question);
            gameStarted = true;
            frame.repaint();
        }
    }

    public void setPlayerId(int id) {
        this.playerId = id;
        sendButton.setEnabled(true);
        addMessage("Вы игрок " + id);

        SwingUtilities.invokeLater(() -> {
            frame.setTitle("Quiz Battle - Игрок " + id);
        });

        frame.repaint();
    }



    public void triggerDamageAnimation(int playerId) {
        this.damagedPlayerId = playerId;
        this.damageAnimationActive = true;
        this.damageAnimationFrame = 0;

        if (damageAnimationTimer != null && damageAnimationTimer.isRunning()) {
            damageAnimationTimer.stop();
        }

        damageAnimationTimer = new Timer(100, e -> {
            damageAnimationFrame++;
            if (damageAnimationFrame > 10) {
                damageAnimationActive = false;
                damageAnimationTimer.stop();
            }
            frame.repaint();
        });
        damageAnimationTimer.start();
    }

    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void setConnected(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                connectionStatus.setText("Подключено");
                connectionStatus.setForeground(Color.GREEN);
            } else {
                connectionStatus.setText("Отключено");
                connectionStatus.setForeground(Color.RED);
                sendButton.setEnabled(false);
            }
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

    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(new Color(245, 245, 255));
            setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            drawBackground(g2d, width, height);
            drawBattleField(g2d, width, height);

            if (damageAnimationActive) {
                drawDamageAnimation(g2d, width, height);
            }

            drawHpBars(g2d, width, height);
            drawQuestion(g2d, width, height);

            if (isAnimating) {
                drawAttackAnimation(g2d, width, height);
            }

            drawPlayerInfo(g2d, width, height);
        }

        private void drawBackground(Graphics2D g, int width, int height) {
            GradientPaint gradient = new GradientPaint(0, 0, new Color(230, 240, 255),
                    width, height, new Color(210, 225, 255));
            g.setPaint(gradient);
            g.fillRect(0, 0, width, height);
        }

        private void drawBattleField(Graphics2D g, int width, int height) {
            int playerWidth = width / 8;
            int playerHeight = height / 3;
            int player1X = width / 10;
            int player2X = width - width / 10 - playerWidth;
            int playerY = height / 3;

            if (gameStarted && currentTurnPlayer > 0) {
                int highlightX = (currentTurnPlayer == 1) ? player1X : player2X;

                float pulseAlpha = 0.3f;
                if (highlightAnimationActive) {
                    pulseAlpha = 0.5f * (1 - highlightAnimationFrame / 15f);
                }

                g.setColor(new Color(255, 255, 100, (int)(pulseAlpha * 255)));
                g.fillRoundRect(highlightX - 10, playerY - 10,
                        playerWidth + 20, playerHeight + 20, 30, 30);

                g.setColor(new Color(255, 200, 0, 150));
                g.setStroke(new BasicStroke(3));
                g.drawRoundRect(highlightX - 10, playerY - 10,
                        playerWidth + 20, playerHeight + 20, 30, 30);
                g.setStroke(new BasicStroke(1));
            }

            GradientPaint player1Gradient = new GradientPaint(
                    player1X, playerY, new Color(220, 50, 50),
                    player1X, playerY + playerHeight, new Color(180, 30, 30)
            );
            g.setPaint(player1Gradient);
            g.fillRoundRect(player1X, playerY, playerWidth, playerHeight, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRoundRect(player1X, playerY, playerWidth, playerHeight, 20, 20);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Игрок 1", player1X + playerWidth/4, playerY - 10);

            GradientPaint player2Gradient = new GradientPaint(
                    player2X, playerY, new Color(50, 50, 220),
                    player2X, playerY + playerHeight, new Color(30, 30, 180)
            );
            g.setPaint(player2Gradient);
            g.fillRoundRect(player2X, playerY, playerWidth, playerHeight, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRoundRect(player2X, playerY, playerWidth, playerHeight, 20, 20);
            g.drawString("Игрок 2", player2X + playerWidth/4, playerY - 10);

            g.setColor(new Color(100, 100, 100, 150));
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{10, 10}, 0));
            g.drawLine(width / 2, height / 6, width / 2, height - height / 4);
            g.setStroke(new BasicStroke(1));
        }

        private void drawDamageAnimation(Graphics2D g, int width, int height) {
            int playerWidth = width / 8;
            int playerHeight = height / 3;
            int playerX = (damagedPlayerId == 1) ? width / 10 : width - width / 10 - playerWidth;
            int playerY = height / 3;

            float alpha = 0.7f * (1 - damageAnimationFrame / 10f);
            g.setColor(new Color(255, 0, 0, (int)(alpha * 255)));
            g.fillRoundRect(playerX, playerY, playerWidth, playerHeight, 20, 20);

            if (damageAnimationFrame < 3) {
                g.setColor(new Color(255, 100, 100, 100));
                g.fillRoundRect(playerX - 5, playerY - 5,
                        playerWidth + 10, playerHeight + 10, 25, 25);
            }
        }

        private void drawHpBars(Graphics2D g, int width, int height) {
            int barWidth = width / 4;
            int barHeight = 25;
            int bar1X = width / 10;
            int bar2X = width - width / 10 - barWidth;
            int barY = height - height / 4;

            g.setColor(Color.GRAY);
            g.fillRoundRect(bar1X, barY, barWidth, barHeight, 10, 10);
            g.fillRoundRect(bar2X, barY, barWidth, barHeight, 10, 10);

            Color hpColor1 = player1Hp > 50 ? Color.GREEN :
                    player1Hp > 25 ? Color.ORANGE : Color.RED;
            Color hpColor2 = player2Hp > 50 ? Color.GREEN :
                    player2Hp > 25 ? Color.ORANGE : Color.RED;

            int fillWidth1 = (int)(barWidth * player1Hp / 100.0);
            int fillWidth2 = (int)(barWidth * player2Hp / 100.0);

            GradientPaint hpGradient1 = new GradientPaint(
                    bar1X, barY, hpColor1.brighter(),
                    bar1X, barY + barHeight, hpColor1.darker()
            );
            g.setPaint(hpGradient1);
            g.fillRoundRect(bar1X, barY, fillWidth1, barHeight, 10, 10);

            GradientPaint hpGradient2 = new GradientPaint(
                    bar2X, barY, hpColor2.brighter(),
                    bar2X, barY + barHeight, hpColor2.darker()
            );
            g.setPaint(hpGradient2);
            g.fillRoundRect(bar2X, barY, fillWidth2, barHeight, 10, 10);

            g.setColor(Color.BLACK);
            g.drawRoundRect(bar1X, barY, barWidth, barHeight, 10, 10);
            g.drawRoundRect(bar2X, barY, barWidth, barHeight, 10, 10);

            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("HP: " + player1Hp, bar1X + barWidth/2 - 25, barY + 18);
            g.drawString("HP: " + player2Hp, bar2X + barWidth/2 - 25, barY + 18);
        }

        private void drawQuestion(Graphics2D g, int width, int height) {
            int questionWidth = width - 100;
            int questionHeight = 80;
            int questionX = 50;
            int questionY = 20;

            g.setColor(new Color(255, 255, 255, 200));
            g.fillRoundRect(questionX, questionY, questionWidth, questionHeight, 15, 15);
            g.setColor(new Color(100, 100, 100));
            g.drawRoundRect(questionX, questionY, questionWidth, questionHeight, 15, 15);

            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Вопрос:", questionX + 10, questionY + 20);

            if (!gameStarted || currentQuestion == null || currentQuestion.equals("Ожидание вопроса...")) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.ITALIC, 14));
                String waitingText;
                if (playerId == 0) {
                    waitingText = "Ожидание подключения...";
                } else if (!gameStarted) {
                    waitingText = "Ожидаем подключения второго игрока...";
                } else {
                    waitingText = "Ожидание вопроса...";
                }
                int textWidth = g.getFontMetrics().stringWidth(waitingText);
                g.drawString(waitingText, questionX + (questionWidth - textWidth) / 2, questionY + 50);
                return;
            }

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 14));

            String[] words = currentQuestion.split(" ");
            StringBuilder line = new StringBuilder();
            int y = questionY + 40;
            int maxLineWidth = questionWidth - 20;

            for (String word : words) {
                if (g.getFontMetrics().stringWidth(line.toString() + word + " ") > maxLineWidth) {
                    g.drawString(line.toString(), questionX + 10, y);
                    y += 20;
                    line = new StringBuilder();
                }
                line.append(word).append(" ");
            }
            if (line.length() > 0) {
                g.drawString(line.toString(), questionX + 10, y);
            }
        }



        private void drawAttackAnimation(Graphics2D g, int width, int height) {
            int playerWidth = width / 8;
            int playerHeight = height / 3;
            int player1X = width / 10 + playerWidth/2;
            int player2X = width - width / 10 - playerWidth/2;
            int playerY = height / 3 + playerHeight/2;

            int startX = (attackerId == 1) ? player1X : player2X;
            int targetX = (attackerId == 1) ? player2X : player1X;

            float progress = attackAnimationFrame / 20.0f;
            int x = (int)(startX + (targetX - startX) * progress);
            int y = playerY;

            double rotation = progress * Math.PI * 4;

            AffineTransform oldTransform = g.getTransform();
            g.translate(x, y);
            g.rotate(rotation);

            GradientPaint projectileGradient = new GradientPaint(
                    -10, -10, Color.YELLOW,
                    10, 10, Color.RED
            );
            g.setPaint(projectileGradient);
            g.fillOval(-15, -15, 30, 30);

            g.setColor(new Color(255, 255, 0, 100));
            for (int i = 1; i <= 3; i++) {
                g.fillOval(-15 - i*2, -15 - i*2, 30 + i*4, 30 + i*4);
            }

            g.setTransform(oldTransform);
        }

        private void drawPlayerInfo(Graphics2D g, int width, int height) {
            g.setFont(new Font("Arial", Font.BOLD, 16));

            if (playerId == 0) {
                g.setColor(Color.GRAY);
                g.drawString("Ожидание подключения...", 20, 30);
            } else {

                String playerText = "Вы игрок " + playerId;
                g.setColor(Color.DARK_GRAY);
                int playerWidth = g.getFontMetrics().stringWidth(playerText);
                g.drawString(playerText, width - playerWidth - 20, 30);

                if (currentTurnPlayer == playerId && gameStarted) {
                    String turnText = " Ваш ход!";
                    g.setColor(Color.GREEN);
                    int turnWidth = g.getFontMetrics().stringWidth(turnText);
                    g.drawString(turnText, width - turnWidth - 20, 30 + 25);
                }
            }

        }
    }

    public void setCurrentTurnPlayer(int currentTurnPlayer) {
        this.currentTurnPlayer = currentTurnPlayer;
        frame.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            gui.frame.setTitle("Quiz Battle Client");
        });
    }
}