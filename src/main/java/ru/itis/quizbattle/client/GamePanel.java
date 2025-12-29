package ru.itis.quizbattle.client;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private int player1Hp = 100;
    private int player2Hp = 100;
    private String currentQuestion = "";
    private int myPlayerId;

    public GamePanel(int playerId) {
        this.myPlayerId = playerId;
        setPreferredSize(new Dimension(800, 400));
        setBackground(new Color(240, 240, 240));
    }

    public void updateState(int player1Hp, int player2Hp) {
        this.player1Hp = player1Hp;
        this.player2Hp = player2Hp;
        repaint();
    }

    public void setQuestion(String question) {
        this.currentQuestion = question;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисуем поле боя
        drawBattleField(g2d);

        // Рисуем HP бары
        drawHpBars(g2d);

        // Рисуем текущий вопрос
        drawQuestion(g2d);

        // Рисуем номер игрока
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Вы игрок " + myPlayerId, 650, 30);
    }

    private void drawBattleField(Graphics2D g) {
        // Игрок 1 (слева)
        g.setColor(Color.RED);
        g.fillRect(50, 150, 100, 150);
        g.setColor(Color.BLACK);
        g.drawString("Игрок 1", 70, 140);

        // Игрок 2 (справа)
        g.setColor(Color.BLUE);
        g.fillRect(650, 150, 100, 150);
        g.setColor(Color.BLACK);
        g.drawString("Игрок 2", 670, 140);

        // Линия посередине
        g.setColor(Color.GRAY);
        g.drawLine(400, 50, 400, 350);
    }

    private void drawHpBars(Graphics2D g) {
        // HP игрока 1
        g.setColor(Color.GREEN);
        g.fillRect(50, 320, player1Hp * 2, 20);
        g.setColor(Color.BLACK);
        g.drawRect(50, 320, 200, 20);
        g.drawString("HP: " + player1Hp, 110, 335);

        // HP игрока 2
        g.setColor(Color.GREEN);
        g.fillRect(550, 320, player2Hp * 2, 20);
        g.setColor(Color.BLACK);
        g.drawRect(550, 320, 200, 20);
        g.drawString("HP: " + player2Hp, 610, 335);
    }

    private void drawQuestion(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));

        // Разбиваем вопрос на строки если он длинный
        String[] words = currentQuestion.split(" ");
        StringBuilder line = new StringBuilder();
        int y = 50;

        for (String word : words) {
            if (line.length() + word.length() + 1 > 50) {
                g.drawString(line.toString(), 100, y);
                y += 20;
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }
        if (line.length() > 0) {
            g.drawString(line.toString(), 100, y);
        }
    }
}