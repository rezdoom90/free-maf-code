
package com.freemaf.agent;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTextArea;

import java.awt.BorderLayout;

import java.awt.Font;

public final class ConsolePanel extends JPanel {

    private final JTextArea textArea = new JTextArea();

    private int fontSize = 12;

    public ConsolePanel() {

        setLayout(new BorderLayout());

        textArea.setEditable(false);

        textArea.setLineWrap(true);

        textArea.setWrapStyleWord(true);

        applyFont();

        JScrollPane sp = new JScrollPane(textArea);

        sp.setBackground(DarkTheme.BG);

        sp.getViewport().setBackground(DarkTheme.FIELD);

        add(sp, BorderLayout.CENTER);

    }

    public void append(String line) {

        textArea.append(line + System.lineSeparator());

        textArea.setCaretPosition(textArea.getDocument().getLength());

    }

    public void setFontSize(int size) {

        this.fontSize = size;

        applyFont();

    }

    private void applyFont() {

        textArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize));

        textArea.setBackground(DarkTheme.FIELD);

        textArea.setForeground(DarkTheme.FG);

        textArea.setCaretColor(DarkTheme.FG);

    }

}
