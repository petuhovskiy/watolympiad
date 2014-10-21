package com.petukhovsky.wat.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * Created by Arthur on 10/9/2014.
 */
public class Gui extends JFrame {

    private static Gui gui;
    private final JTabbedPane tabbedPane;
    private JEditorPane testPane;
    private HashMap<Olympiad, JEditorPane> results = new HashMap<Olympiad, JEditorPane>();

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
        add(tabbedPane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
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
