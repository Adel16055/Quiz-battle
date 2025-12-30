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
        loadQuestionsFromFile();
        currentQuestion = getRandomQuestion();
        System.out.println("Создано состояние игры с вопросом: " +
                (currentQuestion != null ? currentQuestion.getText() : "null"));
    }

    private void loadQuestionsFromFile() {
        File file = new File("questions.txt");

        if (!file.exists()) {
            System.err.println("❌ Файл questions.txt не найден");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Пропускаем пустые строки и комментарии
                }

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

            System.out.println("✅ Загружено " + count + " вопросов из файла questions.txt");

        } catch (IOException e) {
            System.err.println("❌ Ошибка чтения файла questions.txt: " + e.getMessage());
        }
    }

    private Question getRandomQuestion() {
        if (questions.isEmpty()) {
            System.err.println("❌ Список вопросов пуст!");
            return null;
        }

        // Если все вопросы уже использованы, начинаем заново
        if (usedQuestions.size() >= questions.size()) {
            usedQuestions.clear();
            System.out.println("🔄 Все вопросы использованы, начинаем заново");
        }

        Question question;
        do {
            question = questions.get(random.nextInt(questions.size()));
        } while (usedQuestions.contains(question.getText()) && usedQuestions.size() < questions.size());

        usedQuestions.add(question.getText());
        System.out.println("🎲 Выбран вопрос: " + question.getText());
        return question;
    }

    public String processAnswer(int playerId, String answer) {
        if (playerId != currentPlayer) {
            return "Не ваш ход! Сейчас отвечает игрок " + currentPlayer;
        }

        if (currentQuestion == null) {
            System.err.println("❌ Нет текущего вопроса!");
            return "Ошибка: нет вопроса";
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