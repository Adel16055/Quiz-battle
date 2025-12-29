package ru.itis.quizbattle.server;

import ru.itis.quizbattle.common.Protocol;
import ru.itis.quizbattle.common.Question;

public class GameState {
    private int player1Hp = Protocol.INITIAL_HP;
    private int player2Hp = Protocol.INITIAL_HP;
    private Question currentQuestion;
    private int currentPlayer = 1; // Кто должен отвечать сейчас
    private QuestionManager questionManager;

    public GameState(QuestionManager qm) {
        this.questionManager = qm;
        currentQuestion = qm.getRandomQuestion();
    }

    public String processAnswer(int playerId, String answer) {
        if (playerId != currentPlayer) {
            return "Не ваш ход!";
        }

        if (currentQuestion.isCorrect(answer)) {
            // Правильный ответ - наносим урон противнику
            if (playerId == 1) {
                player2Hp -= Protocol.DAMAGE_PER_QUESTION;
            } else {
                player1Hp -= Protocol.DAMAGE_PER_QUESTION;
            }

            // Меняем игрока
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            currentQuestion = questionManager.getRandomQuestion();

            return "Правильно! Нанесено " + Protocol.DAMAGE_PER_QUESTION + " урона";
        } else {
            // Неправильный ответ - ход переходит другому
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            currentQuestion = questionManager.getRandomQuestion();
            return "Неправильно! Ход переходит сопернику";
        }
    }

    public boolean isGameOver() {
        return player1Hp <= 0 || player2Hp <= 0;
    }

    public int getWinner() {
        if (player1Hp <= 0) return 2;
        if (player2Hp <= 0) return 1;
        return 0;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public int getPlayer1Hp() {
        return Math.max(0, player1Hp);
    }

    public int getPlayer2Hp() {
        return Math.max(0, player2Hp);
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }
}