package com.petukhovsky.wat.server;

import javax.swing.*;
import java.awt.*;
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
        setTitle("Server 228");
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
            panel.setLayout(new BorderLayout());
            JEditorPane resultsPane = new JEditorPane();
            results.put(olympiad, resultsPane);
            resultsPane.setContentType("text/html");
            resultsPane.setEditable(false);
            resultsPane.setText(s);
            JScrollPane jsp = new JScrollPane(resultsPane);
            jsp.setBounds(5, 5, 790, 550);
            jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            panel.add(jsp);
            tabbedPane.addTab(olympiad.getName(), panel);
        }
    }
}
