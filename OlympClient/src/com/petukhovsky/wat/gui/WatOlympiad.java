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
    private JEditorPane msgPane;
    private DefaultListModel<String> msgModel;
    private JList<String> msgList;
    private ArrayList<Pair<Integer, String>> messages;

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
            //ignored
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
        if (state == 1) sendButton.setEnabled(false);
    }

    public void unlock() {
        if (state == 1) sendButton.setEnabled(true);
    }

    public void showChoosePanel(String[] s) {
        setLayout(null);
        olympTitles = s;
        state = 0;
        removeAll();
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
        repaint();
    }

    public String getTime() {
        if (timeShow == 0) return formatDuration(System.currentTimeMillis() - startTime);
        else return formatDuration(duration - System.currentTimeMillis() + startTime);
    }

    public void showOlympPanel(String[] arr, long duration, final ArrayList<Pair<Integer, String>> messages) {
        setLayout(new BorderLayout());
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
        JTabbedPane jTabbedPane = new JTabbedPane();
        JPanel olymp = new JPanel();
        olymp.setLayout(null);
        olymp.setBackground(Color.WHITE);
        this.duration = duration;
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
        jsp.setBounds(5, 38, 392, 406);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        olymp.add(jsp);
        sourcePanel = new JEditorPane();
        sourcePanel.setEditable(false);
        jsp = new JScrollPane(sourcePanel);
        sourcePanel.setContentType("text/c");
        jsp.setBounds(400, 38, 395, 406);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        olymp.add(jsp);
        pretestsPanel = new JTextPane();
        pretestsPanel.setContentType("text/html");
        pretestsPanel.setEditable(false);
        jsp = new JScrollPane(pretestsPanel);
        jsp.setBounds(5, 447, 790, 120);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        olymp.add(jsp);
        olymp.add(chooseButton);
        olymp.add(languageChooser);
        olymp.add(filePathField);
        olymp.add(currentTime);
        olymp.add(taskChooser);
        olymp.add(sendButton);
        jTabbedPane.addTab("Задачи", olymp);
        final JPanel msgPanel = new JPanel();
        msgPanel.setLayout(null);
        msgPanel.setBackground(Color.WHITE);
        msgPane = new JEditorPane();
        msgPane.setEditable(false);
        jsp = new JScrollPane(msgPane);
        jsp.setBounds(5, 235, 790, 328);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        msgPanel.add(jsp);
        msgList = new JList<String>();
        msgList.setLayoutOrientation(JList.VERTICAL);
        msgModel = new DefaultListModel<String>();
        msgList.setModel(msgModel);
        this.messages = messages;
        for (int i = 0; i < messages.size(); i++) msgModel.addElement(messages.get(i).getValue());
        msgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        msgList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int i = msgList.getSelectedIndex();
                msgPane.setText(messages.get(i).getValue());
            }
        });
        jsp = new JScrollPane(msgList);
        jsp.setBounds(5, 5, 790, 200);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        msgPanel.add(jsp);
        jTabbedPane.addTab("Сообщения", msgPanel);
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(null);
        questionPanel.setBackground(Color.WHITE);
        final JEditorPane questionPane = new JEditorPane();
        questionPane.setEditable(true);
        jsp = new JScrollPane(questionPane);
        jsp.setBounds(5, 5, 790, 200);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        questionPanel.add(jsp);
        JButton answer = new JButton("Задать вопрос");
        answer.setBounds(5, 207, 150, 25);
        answer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WatNetwork.sendMessage(questionPane.getText());
                questionPane.setText("");
            }
        });
        questionPanel.add(answer);
        jTabbedPane.addTab("Задать вопрос", questionPanel);
        add(jTabbedPane);
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

    public void messageUpdate(int id, String msg) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getKey() == id) {
                messages.set(i, new Pair<Integer, String>(id, msg));
                msgModel.setElementAt(msg, i);
                if (msgList.getSelectedIndex() == i) {
                    msgPane.setText(msg);
                }
                return;
            }
        }
        messages.add(new Pair<Integer, String>(id, msg));
        msgModel.addElement(msg);
    }

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

