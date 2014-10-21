package com.petukhovsky.wat.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by Arthur on 23.09.2014.
 */
public class WatAuth extends JPanel implements ActionListener {

    private static WatAuth authPanel;
    private JTextField loginField;
    private JPasswordField passField;
    private JButton authButton;
    private JButton regButton;

    public WatAuth() {
        setLayout(null);
        authPanel = this;
        setBackground(Color.WHITE);
        loginField = new JTextField();
        passField = new JPasswordField();
        loginField.setToolTipText("Login");
        loginField.setEditable(true);
        loginField.setActionCommand("login");
        loginField.addActionListener(this);
        passField.setToolTipText("Password");
        passField.setEditable(true);
        passField.setActionCommand("login");
        passField.addActionListener(this);
        authButton = new JButton("Войти");
        authButton.setActionCommand("login");
        authButton.addActionListener(this);
        regButton = new JButton("Зарегистрироваться");
        regButton.setActionCommand("register");
        regButton.addActionListener(this);
        loginField.setBounds(240, 267, 150, 26);
        passField.setBounds(240, 297, 150, 26);
        authButton.setBounds(410, 267, 150, 26);
        regButton.setBounds(410, 297, 150, 26);
        add(loginField);
        add(passField);
        add(authButton);
        add(regButton);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("login")) {
            lock();
            WatNetwork.auth(loginField.getText(), String.copyValueOf(passField.getPassword()));
        } else if (e.getActionCommand().equals("register")) {
            lock();
            WatNetwork.register(loginField.getText(), String.copyValueOf(passField.getPassword()));
        }
    }

    public static WatAuth getWatAuth() {
        return authPanel;
    }

    private void lock() {
        authButton.setEnabled(false);
        regButton.setEnabled(false);
    }

    public void unlock() {
        authButton.setEnabled(true);
        regButton.setEnabled(true);
    }
}
