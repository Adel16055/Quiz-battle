package ru.itis.quizbattle.server;

import ru.itis.quizbattle.common.Message;
import ru.itis.quizbattle.common.Question;

import java.io.*;
import java.util.*;

/**
 * Управление состоянием игры и вопросами
 */
public class GameState {
    private int player1Hp = Message.INITIAL_HP;
    private int player2Hp = Message.INITIAL_HP;
    private Question currentQuestion;
    private int currentPlayer = 1; // Кто должен отвечать сейчас
    private List<Question> questions = new ArrayList<>();
    private Random random = new Random();
    private Set<String> usedQuestions = new HashSet<>();

    public GameState() {
        loadQuestions();
        currentQuestion = getRandomQuestion();
    }

    private void loadQuestions() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("questions.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    String questionText = parts[0].trim();
                    String answer = parts[1].trim();
                    if (!questionText.isEmpty() && !answer.isEmpty()) {
                        questions.add(new Question(questionText, answer));
                        count++;
                    }
                }
            }
            System.out.println("✅ Загружено " + count + " вопросов из файла");

            if (questions.isEmpty()) {
                createDefaultQuestions();
                System.out.println("⚠️ Используются вопросы по умолчанию");
            }

        } catch (IOException e) {
            System.err.println("❌ Ошибка загрузки вопросов: " + e.getMessage());
            createDefaultQuestions();
            System.out.println("⚠️ Используются вопросы по умолчанию");
        }
    }

    private void createDefaultQuestions() {
        questions.add(new Question("Сколько будет 2+2?", "4"));
        questions.add(new Question("Столица России?", "Москва"));
        questions.add(new Question("Сколько дней в неделе?", "7"));
        questions.add(new Question("Какой язык мы изучаем?", "Java"));
        questions.add(new Question("Сколько цветов у радуги?", "7"));
        questions.add(new Question("Столица Франции?", "Париж"));
        questions.add(new Question("Самая большая планета Солнечной системы?", "Юпитер"));
        questions.add(new Question("Автор 'Евгения Онегина'?", "Пушкин"));
        questions.add(new Question("Сколько сторон у квадрата?", "4"));
        questions.add(new Question("Год основания Москвы?", "1147"));
    }

    private Question getRandomQuestion() {
        if (questions.isEmpty()) {
            return null;
        }

        if (usedQuestions.size() >= questions.size()) {
            usedQuestions.clear();
            System.out.println("🔄 Все вопросы использованы, начинаем заново");
        }

        Question question;
        do {
            question = questions.get(random.nextInt(questions.size()));
        } while (usedQuestions.contains(question.getText()) && usedQuestions.size() < questions.size());

        usedQuestions.add(question.getText());
        return question;
    }

    public String processAnswer(int playerId, String answer) {
        if (playerId != currentPlayer) {
            return "Не ваш ход! Сейчас отвечает игрок " + currentPlayer;
        }

        boolean isCorrect = currentQuestion.isCorrect(answer);

        if (isCorrect) {
            if (playerId == 1) {
                player2Hp = Math.max(0, player2Hp - Message.DAMAGE_PER_QUESTION);
            } else {
                player1Hp = Math.max(0, player1Hp - Message.DAMAGE_PER_QUESTION);
            }
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            currentQuestion = getRandomQuestion();
            return "Правильно! Нанесено " + Message.DAMAGE_PER_QUESTION + " урона";
        } else {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            currentQuestion = getRandomQuestion();
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
        return player1Hp;
    }

    public int getPlayer2Hp() {
        return player2Hp;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void resetGame() {
        player1Hp = Message.INITIAL_HP;
        player2Hp = Message.INITIAL_HP;
        currentPlayer = 1;
        usedQuestions.clear();
        currentQuestion = getRandomQuestion();
    }

    @Override
    public String toString() {
        return String.format("GameState[P1 HP: %d, P2 HP: %d, Current: %d, Question: %s]",
                player1Hp, player2Hp, currentPlayer,
                currentQuestion != null ? currentQuestion.getText() : "null");
    }
}