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
    private JTextField loginReg;
    private JPasswordField passReg;
    private JTextField fNameReg;
    private JTextField sNameReg;

    public WatAuth() {
        setLayout(new BorderLayout());
        authPanel = this;
        setBackground(Color.WHITE);
        JTabbedPane jTabbedPane = new JTabbedPane();
        JLabel label;
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setLayout(null);
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
        int x = 305;
        int y = 230;
        int w = 190;
        int h = 26;
        int o = 40;
        loginField.setBounds(x, y, w, h);
        label = new JLabel("Логин:");
        label.setBounds(x - 50, y, w, h);
        loginPanel.add(label);
        passField.setBounds(x, y + o, w, h);
        label = new JLabel("Пароль:");
        label.setBounds(x - 61, y + o, w, h);
        loginPanel.add(label);
        authButton.setBounds(x + 50, y + o*2, w - 50*2, h);
        loginPanel.add(loginField);
        loginPanel.add(passField);
        loginPanel.add(authButton);
        jTabbedPane.addTab("Войти", loginPanel);
        JPanel regPanel = new JPanel();
        regPanel.setBackground(Color.WHITE);
        regPanel.setLayout(null);
        loginReg = new JTextField();
        passReg = new JPasswordField();
        fNameReg = new JTextField();
        sNameReg = new JTextField();
        regButton = new JButton("Зарегистрироваться");
        loginReg.setToolTipText("Логин");
        passReg.setToolTipText("Пароль");
        fNameReg.setToolTipText("Имя на белорусском");
        sNameReg.setToolTipText("Фамилия на белорусском");
        loginReg.setActionCommand("register");
        passReg.setActionCommand("register");
        fNameReg.setActionCommand("register");
        sNameReg.setActionCommand("register");
        regButton.setActionCommand("register");
        loginReg.addActionListener(this);
        passReg.addActionListener(this);
        fNameReg.addActionListener(this);
        sNameReg.addActionListener(this);
        regButton.addActionListener(this);
        x = 305;
        y = 170;
        loginReg.setBounds(x, y, w, h);
        label = new JLabel("Логин:");
        label.setBounds(x - 50, y, w, h);
        regPanel.add(label);
        passReg.setBounds(x, y + o, w, h);
        label = new JLabel("Пароль:");
        label.setBounds(x - 61, y + o, w, h);
        regPanel.add(label);
        fNameReg.setBounds(x, y + o*2, w, h);
        label = new JLabel("Имя на белорусском:");
        label.setBounds(x - 136, y + o*2, w, h);
        regPanel.add(label);
        sNameReg.setBounds(x, y + o*3, w, h);
        label = new JLabel("Фамилия на белорусском:");
        label.setBounds(x - 164, y + o*3, w, h);
        regPanel.add(label);
        regButton.setBounds(x + 18, y + o*4, w - 2*18, h);
        regPanel.add(loginReg);
        regPanel.add(passReg);
        regPanel.add(fNameReg);
        regPanel.add(sNameReg);
        regPanel.add(regButton);
        jTabbedPane.add("Зарегистрироваться", regPanel);
        add(jTabbedPane);
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
            WatNetwork.register(loginReg.getText(), String.copyValueOf(passReg.getPassword()), fNameReg.getText(), sNameReg.getText());
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
