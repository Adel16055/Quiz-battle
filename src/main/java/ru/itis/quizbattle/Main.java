package ru.itis.quizbattle;

import ru.itis.quizbattle.client.ClientGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}

