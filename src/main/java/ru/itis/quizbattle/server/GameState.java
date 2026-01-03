package ru.itis.quizbattle.server;

import ru.itis.quizbattle.common.Message;
import ru.itis.quizbattle.common.Question;
import java.io.*;
import java.util.*;

public class GameState {
    private int player1Hp = Message.INITIAL_HP;
    private int player2Hp = Message.INITIAL_HP;
    private Question currentQuestion;
    private int currentPlayer = 1;
    private List<Question> questions = new ArrayList<>();
    private Random random = new Random();
    private Set<String> usedQuestions = new HashSet<>();

    public GameState() {
        loadQuestionsFromFile();
        currentQuestion = getRandomQuestion();
    }

    private void loadQuestionsFromFile() {

        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("questions.txt")) {

            if (inputStream == null) {
                System.err.println("Файл questions.txt не найден в classpath!");
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                int count = 0;

                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
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

                System.out.println("Загружено вопросов: " + count);

            } catch (IOException e) {
                System.err.println("Ошибка чтения файла: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Ошибка открытия потока: " + e.getMessage());
        }
    }

    private Question getRandomQuestion() {
        if (questions.isEmpty()) {
            return null;
        }

        if (usedQuestions.size() == questions.size()) {
            usedQuestions.clear();
        }

        Question question = questions.get(random.nextInt(questions.size()));

        if (usedQuestions.contains(question.getText())) {
            return getRandomQuestion();
        }

        usedQuestions.add(question.getText());
        return question;
    }


    public String processAnswer(int playerId, String answer) {
        if (playerId != currentPlayer) {
            return "Не ваш ход! Сейчас отвечает игрок " + currentPlayer;
        }

        if (currentQuestion == null) {
            return "Ошибка: нет вопроса";
        }

        boolean isCorrect = currentQuestion.isCorrect(answer);

        if (isCorrect) {
            if (playerId == 1) {
                player2Hp = Math.max(0, player2Hp - Message.DAMAGE_PER_QUESTION);
            } else {
                player1Hp = Math.max(0, player1Hp - Message.DAMAGE_PER_QUESTION);
            }
        }

        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        currentQuestion = getRandomQuestion();

        if (isCorrect) {
            return "Правильно! Нанесено " + Message.DAMAGE_PER_QUESTION + " урона";
        }
        return "Неправильно! Ход переходит сопернику";


    }


    public boolean isGameOver() {
        return player1Hp <= 0 || player2Hp <= 0;
    }

    public int getWinner() {
        if (player1Hp <= 0){
            return 2;
        }
        if (player2Hp <= 0){
            return 1;
        }
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

    @Override
    public String toString() {
        return String.format("GameState[P1 HP: %d, P2 HP: %d, Current: %d, Question: %s]",
                player1Hp, player2Hp, currentPlayer,
                currentQuestion != null ? currentQuestion.getText() : "null");
    }
}