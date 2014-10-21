package com.petukhovsky.wat.gui;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Arthur on 23.09.2014.
 */
public class WatConsole extends JPanel implements ActionListener {

    private static WatConsole watConsole;
    private JTextPane dialogPane;
    private HTMLDocument dialogDoc;
    private HTMLEditorKit dialogHTML;
    private JTextField msgPane;
    private boolean isLocked;

    public WatConsole() {
        setLayout(null);
        watConsole = this;
        setBackground(Color.WHITE);
        JButton exit = new JButton("exit");
        exit.setBounds(7, 7, 70, 13);
        exit.setActionCommand("exit");
        exit.addActionListener(this);
        dialogPane = new JTextPane();
        dialogPane.setContentType("text/html");
        dialogPane.setEditable(false);
        ToolTipManager.sharedInstance().registerComponent(dialogPane);
        dialogDoc = (HTMLDocument) dialogPane.getDocument();
        dialogHTML = (HTMLEditorKit) dialogPane.getEditorKit();
        final JScrollPane scrollPane = new JScrollPane(dialogPane);
        scrollPane.setBounds(10, 35, 780, 530);
        add(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        msgPane = new JTextField();
        msgPane.setBounds(10, 570, 780, 25);
        msgPane.setActionCommand("send");
        msgPane.addActionListener(this);
        add(msgPane);
        add(exit);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isLocked) return;
        if (e.getActionCommand().equals("send")) {
            if (msgPane.getText().isEmpty()) return;
            WatNetwork.dialogMsg(msgPane.getText());
        } else if (e.getActionCommand().equals("exit")) {
            lock();
            WatNetwork.goToMenu();
        }
    }

    public static WatConsole getWatConsole() {
        return watConsole;
    }

    private void lock() {
        isLocked = true;
    }

    public void unlock() {
        isLocked = false;
    }

    public void clear() {
        dialogPane.setText("");
        clearMsgPane();
    }

    public void clearMsgPane() {
        msgPane.setText("");
    }

    public void addMessage(String s) {
        try {
            dialogHTML.insertHTML(dialogDoc, dialogDoc.getLength(), s, 0, 0, null);
            dialogPane.setCaretPosition(dialogDoc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
