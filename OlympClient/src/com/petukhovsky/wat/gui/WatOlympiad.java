package com.petukhovsky.wat.gui;

import jsyntaxpane.DefaultSyntaxKit;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by Arthur on 23.09.2014.
 */
public class WatOlympiad extends JPanel implements ActionListener {

    private static final String[] LANGUAGES = {"Pascal", "C++"};
    private static final JFileChooser J_FILE_CHOOSER = new JFileChooser();

    private static WatOlympiad watOlympiad;
    private long duration;
    private JButton exit;
    private boolean isLocked;
    private int state;
    private ArrayList<TaskState> taskStates = new ArrayList<TaskState>();
    private String[] taskNames;
    private String[] olympTitles;
    private JTextField filePathField;
    private long startTime;
    private JLabel currentTime;
    private Timer timer;
    private JTable infoTable;
    private JEditorPane sourcePanel;
    private JTextPane pretestsPanel;
    private JComboBox<String> taskChooser;
    private JComboBox<String> languageChooser;
    private JButton sendButton;
    private int timeShow = 0;

    public WatOlympiad() {
        DefaultSyntaxKit.initKit();
        setLayout(null);
        watOlympiad = this;
        setBackground(Color.WHITE);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("exit")) {
            lock();
            if (state == 0) {
                WatNetwork.goToMenu();
            } else {
                WatNetwork.exitOlymp();
            }
        } else if (e.getActionCommand().equals("choosefile")) {
            int ret = J_FILE_CHOOSER.showDialog(WatGUI.getGui(), "Выбрать файл");
            if (ret == JFileChooser.APPROVE_OPTION) {
                String s = J_FILE_CHOOSER.getSelectedFile().getPath();
                filePathField.setText(s);
                if (s.endsWith(".cpp")) {
                    languageChooser.setSelectedIndex(1);
                } else if (s.endsWith(".pas")) {
                    languageChooser.setSelectedIndex(0);
                }
            }
        } else if (e.getActionCommand().equals("send")) {
            lock();
            File file = new File(filePathField.getText());
            if (!file.exists()) {
                JOptionPane.showMessageDialog(WatGUI.getGui(), "Заданного файла не существует", "Olymp", JOptionPane.WARNING_MESSAGE);
                unlock();
                return;
            }
            WatNetwork.writeSource(file, taskChooser.getSelectedIndex(), languageChooser.getSelectedIndex());
        }
    }

    private void selectOlympiad(int index) {
        lock();
        WatNetwork.selectOlympiad(index);
    }

    public void lock() {
        //exit.setEnabled(false);
        if (state == 1) sendButton.setEnabled(false);
        isLocked = true;
    }

    public void unlock() {
        //exit.setEnabled(true);
        if (state == 1) sendButton.setEnabled(true);
        isLocked = false;
    }

    public void showChoosePanel(String[] s) {
        olympTitles = s;
        state = 0;
        removeAll();
        /*
        exit = new JButton("exit");
        exit.setVisible(false); //TODO
        exit.setBounds(0, 0, 100, 40);
        exit.setActionCommand("exit");
        exit.addActionListener(this);
        */
        JList<String> jList = new JList<String>(s);
        jList.setLayoutOrientation(JList.VERTICAL);
        jList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
                if (evt.getClickCount() == 2 && r != null && r.contains(evt.getPoint())) {
                    int index = list.locationToIndex(evt.getPoint());
                    selectOlympiad(index);
                }
            }
        });
        JScrollPane jsp = new JScrollPane(jList);
        jsp.setBounds(10, 50, 780, 500);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(jsp);
        //add(exit);
        repaint();
    }

    public String getTime() {
        if (timeShow == 0) return formatDuration(System.currentTimeMillis() - startTime);
        else return formatDuration(duration - System.currentTimeMillis() + startTime);
    }

    public void showOlympPanel(String[] arr, long duration) {
        stopTimer();
        state = 1;
        currentTime = new JLabel();
        currentTime.setFont(new Font("Arial", 0, 35));
        currentTime.setText("00:00:00");
        currentTime.setBounds(657, 2, 200, 40);
        currentTime.setForeground(Color.GREEN);
        timeShow = 0;
        currentTime.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (timer == null || !timer.isRunning()) return;
                timeShow = 1 - timeShow;
                if (timeShow == 0) currentTime.setForeground(Color.GREEN);
                else currentTime.setForeground(Color.RED);
                currentTime.setText(getTime());
            }
        });
        taskNames = arr;
        removeAll();
        this.duration = duration;
        /*
        exit = new JButton("Назад");
        exit.setVisible(false); //TODO
        exit.setBounds(10, 10, 80, 25);
        exit.setActionCommand("exit");
        exit.addActionListener(this);
        */
        filePathField = new JTextField();
        filePathField.setBounds(5, 10, 211, 25);
        JButton chooseButton = new JButton("Обзор");
        chooseButton.setBounds(221, 10, 75, 25);
        chooseButton.setActionCommand("choosefile");
        chooseButton.addActionListener(this);
        sendButton = new JButton("Отправить");
        sendButton.setActionCommand("send");
        sendButton.addActionListener(this);
        sendButton.setBounds(563, 10, 88, 25);
        taskChooser = new JComboBox<String>(arr);
        taskChooser.setBounds(301, 10, 180, 25);
        languageChooser = new JComboBox<String>(LANGUAGES);
        languageChooser.setBounds(486, 10, 72, 25);
        infoTable = new JTable(new WatTableModel());
        infoTable.setAutoCreateRowSorter(true);
        infoTable.getColumnModel().getColumn(2).setMinWidth(91);
        infoTable.getColumnModel().getColumn(0).setMinWidth(60);
        infoTable.setFillsViewportHeight(true);
        infoTable.setUpdateSelectionOnSort(true);
        infoTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                int sel = infoTable.getSelectedRow();
                if (sel < 0) return;
                int trueIndex = infoTable.convertRowIndexToModel(infoTable.getSelectedRow());
                updateMsg(trueIndex);
            }
        });
        infoTable.getRowSorter().toggleSortOrder(0);
        infoTable.getRowSorter().toggleSortOrder(0);
        JScrollPane jsp = new JScrollPane(infoTable);
        jsp.setBounds(5, 38, 392, 434);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(jsp);
        sourcePanel = new JEditorPane();
        sourcePanel.setEditable(false);
        jsp = new JScrollPane(sourcePanel);
        sourcePanel.setContentType("text/c");
        jsp.setBounds(400, 38, 395, 434);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(jsp);
        pretestsPanel = new JTextPane();
        pretestsPanel.setContentType("text/html");
        pretestsPanel.setEditable(false);
        jsp = new JScrollPane(pretestsPanel);
        jsp.setBounds(5, 475, 790, 120);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(jsp);
        add(chooseButton);
        add(languageChooser);
        add(filePathField);
        add(currentTime);
        add(taskChooser);
        //add(exit);
        add(sendButton);
        lock();
        repaint();
    }

    private void updateMsg(int i) {
        if (i < 0) return;
        final String msg = taskStates.get(i).getMsg();
        final String source = taskStates.get(i).getSource();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                pretestsPanel.setText(msg);
                sourcePanel.setText(source);
            }
        });
    }
    /*
    private String formatSource(String source) {
        String res = "<style>.def{color: green;} .blue{color: blue;} .red{color: red;}</style><pre>";
        //source = source.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        String[] arr = source.split("\n");
        for (int i = 0; i < arr.length - 1; i++) res += formatSourceLine(arr[i]) + "<br>";
        if (arr.length > 0) res += formatSourceLine(arr[arr.length-1]);
        res += "</pre>";
        return res;
    }

    private String formatSourceLine(String s) {
        String res = "";
        res += "<span>";
        String buf = "";
        Pattern p = Pattern.compile("[\\w]");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '#') {
                return res + buf + "</span><span class=\"def\">" + s.substring(i) + "</span>";
            }
            if (c == '"') {
                res += buf + "</span><span class=\"blue\">\"";
                buf = "";
                i++;
                while (i < s.length() && s.charAt(i) != '"') {
                    res += s.charAt(i);
                    i++;
                }
                res += "\"</span><span>";
                continue;
            }
            if (p.matcher(String.valueOf(c)).matches()) {
                buf += s.charAt(i);
            } else {
                res += buf;
                buf = "";

            }
        }
        return res + buf + "</span>";
    }
    */
    private void stopTimer() {
        if (timer != null) timer.stop();
    }

    private Object getValueAt(int rowIndex, int columnIndex) {
        return taskStates.get(rowIndex).getValueAt(columnIndex);
    }

    public void startOlymp(long timePassed) {
        startTime = System.currentTimeMillis() - timePassed;
        timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentTime.setText(getTime());
            }
        });
        timer.setCoalesce(true);
        timer.setInitialDelay(0);
        timer.start();
        unlock();
    }

    private String formatDuration(long s) {
        s /= 1000;
        return String.format("%02d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
    }

    public void stopOlymp() {
        lock();
        if (timer != null) timer.stop();
        currentTime.setText(formatDuration(duration));
    }

    public static WatOlympiad getWatOlympiad() {
        return watOlympiad;
    }

    public void addState(int id, String time, int task, int status, int language, String msg, String source) {
        taskStates.add(new TaskState(time, task, status, language, id, msg, source));
        if (infoTable != null) infoTable.tableChanged(new TableModelEvent(new WatTableModel()));
    }

    public void changeState(int id, int status, String msg) {
        for (int i = 0; i < taskStates.size(); i++) {
            if (taskStates.get(i).getId() == id) {
                taskStates.get(i).setStatus(status);
                taskStates.get(i).setMsg(msg);
                if (infoTable.getSelectedRow() != -1 && infoTable.convertRowIndexToModel(infoTable.getSelectedRow()) == i) {
                    updateMsg(i);
                }
            }
        }
        if (infoTable != null) infoTable.tableChanged(new TableModelEvent(new WatTableModel()));
    }

    public void clearStates() {
        taskStates.clear();
    }

    private class TaskState {

        private final ImageIcon[] icons = {new ImageIcon(TaskState.class.getResource("/waiting.png")), new ImageIcon(TaskState.class.getResource("/wrong.png")), new ImageIcon(TaskState.class.getResource("/accepted.png"))};

        private String time;
        private int task;
        private int status;
        private int language;
        private int id;
        private String msg;
        private String source;

        private TaskState(String time, int task, int status, int language, int id, String msg, String source) {
            this.time = time;
            this.task = task;
            this.status = status;
            this.language = language;
            this.id = id;
            this.msg = msg;
            this.source = source;
        }

        public int getLanguage() {
            return language;
        }

        public int getStatus() {
            return status;
        }

        public int getTask() {
            return task;
        }

        public String getTime() {
            return time;
        }

        public Object getValueAt(int i) {
            if (i == 0) {
                return time;
            }
            if (i == 1) {
                return taskNames[task];
            }
            if (i == 2) {
                return icons[status];
            }
            if (i == 3) {
                return LANGUAGES[language];
            }
            return null;
        }

        public int getId() {
            return id;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
    class WatTableModel extends AbstractTableModel{

        String[] columns = {"Время", "Задача", "Статус", "Язык"};
        Class[] columnClass = new Class[]{String.class, String.class, ImageIcon.class, String.class};

        @Override
        public int getRowCount() {
            return taskStates.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return WatOlympiad.getWatOlympiad().getValueAt(rowIndex, columnIndex);
        }

        @Override
        public Class getColumnClass(int c) {
            return columnClass[c];
        }

    };
}

