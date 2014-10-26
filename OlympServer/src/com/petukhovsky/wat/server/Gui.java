package com.petukhovsky.wat.server;

import javafx.util.Pair;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Arthur on 10/9/2014.
 */
public class Gui extends JFrame {

    private static Gui gui;
    private final JTabbedPane tabbedPane;
    private JEditorPane testPane;
    private JEditorPane msgPane;
    private DefaultListModel<String> msgModel;
    private JList<String> msgList;
    private ArrayList<Pair<Integer, String>> messages = new ArrayList<Pair<Integer, String>>();
    private HashMap<Olympiad, JEditorPane> results = new HashMap<Olympiad, JEditorPane>();
    private JComboBox<String> olympChooser;

    public Gui() {
        setLookAndFeel();
        gui = this;
        setResizable(false);
        setTitle("Server");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        tabbedPane = new JTabbedPane(){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 600);
            }
        };
        JPanel testing = new JPanel();
        testing.setLayout(null);
        testPane = new JEditorPane();
        testPane.setContentType("text/html");
        testPane.setEditable(false);
        JScrollPane jsp = new JScrollPane(testPane);
        jsp.setBounds(5, 5, 790, 550);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        testing.add(jsp);
        tabbedPane.addTab("Testing", testing);
        final JPanel messagesPane = new JPanel();
        messagesPane.setLayout(null);
        msgPane = new JEditorPane();
        msgPane.setEditable(true);
        jsp = new JScrollPane(msgPane);
        jsp.setBounds(5, 5, 790, 250);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagesPane.add(jsp);
        msgList = new JList<String>();
        msgList.setLayoutOrientation(JList.VERTICAL);
        msgModel = new DefaultListModel<String>();
        msgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        msgList.setModel(msgModel);
        final JButton answer = new JButton("Ответить");
        msgList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                answer.setEnabled(true);
                int i = msgList.getSelectedIndex();
                msgPane.setText(messages.get(i).getValue());
            }
        });
        jsp = new JScrollPane(msgList);
        jsp.setBounds(5, 285, 790, 278);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagesPane.add(jsp);
        answer.setBounds(5, 257, 100, 25);
        answer.setEnabled(false);
        answer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = msgList.getSelectedIndex();
                WatOlympiad.answerQuestion(messages.get(i).getKey(), msgPane.getText());
            }
        });
        messagesPane.add(answer);
        JButton answerToAll = new JButton("Отправить всем");
        answerToAll.setBounds(110, 257, 150, 25);
        olympChooser = new JComboBox<String>();
        olympChooser.setBounds(263, 257, 200, 25);
        answerToAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = msgList.getSelectedIndex();
                int olymp = olympChooser.getSelectedIndex();
                Olympiad o = WatOlympiad.getOlympiad(olymp);
                WatOlympiad.messageReceived(-1, olympChooser.getSelectedIndex(), Olympiad.formatTime(o.getTimeFromStart()) + " \n" + msgPane.getText());
            }
        });
        messagesPane.add(olympChooser);
        messagesPane.add(answerToAll);
        tabbedPane.addTab("Messages", messagesPane);
        add(tabbedPane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateOlympiadTitles() {
        String[] s = WatOlympiad.getOlympiadsTitles();
        for (int i = 0; i < s.length; i++) olympChooser.addItem(s[i]);
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Gui getGui() {
        return gui;
    }

    public void setTestText(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                testPane.setText(s);
            }
        });
    }

    public void updateMessage(int id, final String msg) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getKey() == id) {
                messages.set(i, new Pair<Integer, String>(id, msg));
                msgModel.setElementAt(msg, i);
                return;
            }
        }
        msgModel.addElement(msg);
        messages.add(new Pair<Integer, String>(id, msg));
    }

    public void setResults(final Olympiad olympiad, final String s) {
        if (results.containsKey(olympiad)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    results.get(olympiad).setText(s);
                }
            });
        } else {
            JPanel panel = new JPanel();
            panel.setLayout(null);
            JEditorPane resultsPane = new JEditorPane();
            results.put(olympiad, resultsPane);
            resultsPane.setContentType("text/html");
            resultsPane.setEditable(false);
            resultsPane.setText(s);
            JScrollPane jsp = new JScrollPane(resultsPane);
            jsp.setBounds(2, 54, 796, 506);
            jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            panel.add(jsp);
            final JEditorPane infoPane = new JEditorPane();
            infoPane.setEditable(false);
            jsp = new JScrollPane(infoPane);
            jsp.setBounds(2, 2, 550, 50);
            jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            panel.add(jsp);
            JButton start = new JButton("start");
            start.setBounds(554, 2, 60, 24);
            JButton stop = new JButton("stop");
            stop.setBounds(554, 28, 60, 24);
            final JTextField jtf = new JTextField();
            jtf.setBounds(616, 2, 160, 24);
            panel.add(jtf);
            start.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long time;
                    try {
                        time = Integer.parseInt(jtf.getText().trim());
                    } catch (Exception e1) {
                        return;
                    }
                    olympiad.startAfter(time);
                }
            });
            stop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    olympiad.stop();
                }
            });
            panel.add(start);
            panel.add(stop);
            tabbedPane.addTab(olympiad.getName(), panel);
            Timer timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    infoPane.setText(olympiad.getState());
                }
            });
            timer.start();
        }
    }
}
