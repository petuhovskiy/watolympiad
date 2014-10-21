package com.petukhovsky.wat.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;

/**
 * Created by Arthur on 23.09.2014.
 */
public class WatGUI extends JFrame {
    private static WatGUI gui;
    private static WatAuth authPanel;
    private static WatOlympiad olympiadPanel;

    public WatGUI() {
        setLookAndFeel();
        init();
        gui = this;
        setResizable(false);
        setTitle("Olymp");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        showAuthPanel();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void init() {
        authPanel = new WatAuth();
        olympiadPanel = new WatOlympiad();
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAuthPanel() {
        gui.getContentPane().removeAll();
        gui.getContentPane().add(authPanel);
        gui.repaint();
        gui.pack();
    }

    public static WatGUI getGui() {
        return gui;
    }

    public static void showOlympiadPanel() {
        gui.getContentPane().removeAll();
        gui.getContentPane().add(olympiadPanel);
        gui.repaint();
        gui.pack();
    }
}
