package com.petukhovsky.wat.server;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Arthur on 26.09.2014.
 */
public class Olympiad {

    private static final String SOURCE_FOLDER = "/sources";
    private String location;
    private String name = "Untilted";
    private ArrayList<Task> problems = new ArrayList<Task>();
    private long duration = 0;
    private HashMap<Account, Result> results = new HashMap<Account, Result>();
    private long startTime = 0;
    private boolean isRunning = false;
    private Timer timer = new Timer();
    private String id;

    public Olympiad(String path, String id) {
        location = path;
        this.id = id;
        File file = new File(path);
        String list[] = file.list();
        for (String s : list) {
            if (new File(path + "/" + s).isDirectory() && !s.equals("sources")) problems.add(new Task(path + "/" + s));
        }
        try {
            file = new File(path + "/config.yml");
            Yaml yaml = new Yaml();
            HashMap<String, Object> params = (HashMap<String, Object>) yaml.load(new FileInputStream(file));
            try {
                name = (String) params.get("name");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                duration = (Integer) params.get("duration");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                long startTime = (Long) params.get("start-time");
                startAfter(startTime - System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.e("Olympiad config not found: " + location);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("Error while reading config: " + location);
            e.printStackTrace();
        }
        readResults();
    }

    private void readResults() {
        results = SQLite.getResults(id, tasksCount());
        rescore();
    }

    public String getName() {
        return name;
    }

    public int tasksCount() {
        return problems.size();
    }

    public ArrayList<Task> getTasks() {
        return problems;
    }

    public long getDuration() {
        return duration;
    }

    public long getTimeFromStart() {
        return System.currentTimeMillis() - startTime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        timer.cancel();
        timer = new Timer();
        if (!isRunning) return;
        isRunning = false;
        startTime = 0;
        WatOlympiad.stopOlympiad(this);
    }

    public void start(long timeAfterStart) {
        if (isRunning) return;
        if (timeAfterStart >= duration) return;
        startTime = System.currentTimeMillis() - timeAfterStart;
        timer.cancel();
        timer = new Timer();
        isRunning = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                stop();
            }
        }, duration - timeAfterStart);
        WatOlympiad.startOlympiad(this);
    }

    public void startAfter(long time) {
        if (isRunning) return;
        if (time < 0) {
            if (-time < duration) start(-time);
            return;
        }
        startTime = System.currentTimeMillis() + time;
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                start(0);
            }
        }, time);
    }

    public Source writeSourceFile(WatSocket ws, int fileLength, int type, int task, Account account) {
        Log.d("write source file " + fileLength);
        Source source = addSource(type, task, account);
        File file = source.getFile();
        OutputStream writer = null;
        try {
            writer = new FileOutputStream(file);
            byte[] f = ws.readByteArray(fileLength);
            writer.write(f);
            Log.d("write successful");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert writer != null;
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        WatOlympiad.sendStateWithoutConnection(this, account, source);
        return source;
    }

    private synchronized Source addSource(int type, int task, Account account) {
        int sourceID = getSourceID();
        String sourceLocation = location + SOURCE_FOLDER + "/" + String.format("%05d.", sourceID);
        if (type == 0) sourceLocation += "pas"; else sourceLocation += "cpp";
        Source source = new Source(type, sourceLocation, this, task, account, sourceID, formatTime(System.currentTimeMillis() - startTime), 0, "");
        SQLite.writeSource(source);
        return source;
    }

    private int getSourceID() {
        return SQLite.getSourceID(id)+1;
    }

    private String formatTime(long s) {
        s /= 1000;
        return String.format("%02d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
    }

    public void sendStates(WatSocket ws, Account account) {
        ArrayList<Source> list = SQLite.getSources(account.getId(), id);
        ws.writeInt(list.size());
        for (Source s : list) WatOlympiad.sendState(ws, s);
    }

    public void checkSolve(Source source) {
        Task task = problems.get(source.getTask());
        task.checkSolve(source);
        if (source.getStatus() == 2) setResult(source.getAccount(), source.getTask(), source.getScore());
        SQLite.updateSource(source);
        WatOlympiad.updateStateWithoutConnection(source);
    }

    private void setResult(Account account, int task, int score) {
        if (!results.containsKey(account)) results.put(account, new Result(problems.size()));
        results.get(account).setScore(task, score);
        rescore();
    }

    private void rescore() {
        List<Map.Entry<Account, Result>> s = new ArrayList<Map.Entry<Account, Result>>(results.entrySet());
        Collections.sort(s, new Comparator<Map.Entry<Account, Result>>() {
            @Override
            public int compare(Map.Entry<Account, Result> o1, Map.Entry<Account, Result> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        String msg = "<style>table {background-color: gray;}td{background-color: white; align: center;}th{background-color: white;}</style><table align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"2\"><tr><th>№</th><th>Кто</th>";
        for (int i = 0; i < problems.size(); i++) msg += "<th>" + ((char)('A' + i)) + "</th>";
        msg += "<th>=</th></tr>";
        for (int i = 0; i < s.size(); i++) {
            msg += "<tr><td>" + (i + 1) + "</td><td>" + s.get(i).getKey().getColoredUsername() + "</td>";
            for (int t = 0; t < problems.size(); t++) {
                msg += "<td>" + s.get(i).getValue().getScore(t) + "</td>";
            }
            msg += "<td>" + s.get(i).getValue().getSum() + "</td></tr>";
        }
        Gui.getGui().setResults(this, msg + "</table>");
    }

    public String getId() {
        return id;
    }

    public String getState() {
        long time = System.currentTimeMillis();
        if (isRunning) {
            return "Time passed: " + formatTime(time - startTime) + "\nTime remaining: " + formatTime(duration - time + startTime);
        } else {
            if (startTime == 0) return "Olympiad isn't running";
            else return "Time before start: " + formatTime(startTime - time);
        }
    }
}

class Result implements Comparable<Result>{
    private int[] result;
    private int sum = 0;

    public Result (int tasks) {
        result = new int[tasks];
    }

    public void setScore (int task, int score) {
        sum += score - result[task];
        result[task] = score;
    }

    @Override
    public int compareTo(Result r) {
        return r.sum - this.sum;
    }

    public int getScore(int t) {
        return result[t];
    }

    public int getSum() {
        return sum;
    }
}