package ru.itis.quizbattle.server;

import ru.itis.quizbattle.common.Question;
import java.io.*;
import java.util.*;

public class QuestionManager {
    private List<Question> questions = new ArrayList<>();
    private Random random = new Random();

    public QuestionManager(String filename) {
        loadQuestions(filename);
    }

    private void loadQuestions(String filename) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    questions.add(new Question(parts[0], parts[1]));
                }
            }
            System.out.println("Загружено " + questions.size() + " вопросов");
        } catch (IOException e) {
            System.err.println("Ошибка загрузки вопросов: " + e.getMessage());
            // Создаём тестовые вопросы
            questions.add(new Question("Сколько будет 2+2?", "4"));
            questions.add(new Question("Столица России?", "Москва"));
        }
    }

    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(random.nextInt(questions.size()));
    }
}