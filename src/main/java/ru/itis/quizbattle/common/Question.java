package ru.itis.quizbattle.common;

public class Question {
    private String text;
    private String correctAnswer;

    public Question(String text, String correctAnswer) {
        this.text = text;
        this.correctAnswer = correctAnswer.trim().toLowerCase();
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect(String answer) {
        return answer.trim().toLowerCase().equals(correctAnswer);
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}