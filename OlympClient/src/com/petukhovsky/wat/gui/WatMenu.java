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
public class WatMenu extends JPanel implements ActionListener {

    private JButton watConsole;
    private JButton watOlympiad;
    private static WatMenu watMenu;

    public WatMenu() {
        setLayout(null);
        watMenu = this;
        setBackground(Color.WHITE);
        watConsole = new JButton("Console");
        watConsole.setBounds(350, 268, 100, 30);
        watConsole.setActionCommand("console");
        watConsole.addActionListener(this);
        watOlympiad = new JButton("Olympiad");
        watOlympiad.setBounds(350, 302, 100, 30);
        watOlympiad.setActionCommand("olympiad");
        watOlympiad.addActionListener(this);
        add(watConsole);
        add(watOlympiad);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("console")) {
            lock();
            WatNetwork.openConsole();
        } else if (e.getActionCommand().equals("olympiad")) {
            lock();
            WatNetwork.openOlympiad();
        }
    }

    public static WatMenu getWatMenu() {
        return watMenu;
    }

    private void lock() {
        watConsole.setEnabled(false);
        watOlympiad.setEnabled(false);
    }

    public void unlock() {
        watConsole.setEnabled(true);
        watOlympiad.setEnabled(false);
    }
}
