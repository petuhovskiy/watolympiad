package com.petukhovsky.wat.server;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Arthur on 26.09.2014.
 */
public class Task {
    private String location;
    private ArrayList<TestGroup> tests = new ArrayList<TestGroup>();
    private String name = "Untilted";
    private String timeLimit = "2000ms";
    private String memoryLimit = "64M";
    private String checker = null;

    @SuppressWarnings("unchecked")
    public Task(String path) {
        location = path;
        File config = new File(path + "/config.yml");
        try {
            FileInputStream fis = new FileInputStream(config);
            Yaml yaml = new Yaml();
            HashMap<String, Object> params = (HashMap<String, Object>) yaml.load(fis);
            try {
                ArrayList<HashMap<String, Object>> a = (ArrayList<HashMap<String, Object>>) params.get("tests");
                for (HashMap<String, Object> i : a)
                    try {
                        tests.add(new TestGroup((Integer) i.get("count"), (Integer) i.get("points"), (Integer) i.get("type"), (String) i.get("input-files"), (String) i.get("output-files"), (String) i.get("location"), (Integer) i.get("first_test")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                name = (String) params.get("name");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                timeLimit = (String) params.get("time-limit");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                memoryLimit = (String) params.get("memory-limit");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                checker = (String) params.get("checker");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.e("Task config not found: " + location);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("Error while reading config: " + location);
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void checkSolve(Source source) {
        Log.d("checkSolve");
        int score = 0;
        int globalTest = 0;
        boolean pretestPassed = true;
        String msg = "<style>table {background-color: gray;}td{background-color: white; align: center;}th{background-color: white;}</style><table align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"2\"><tr><th>Номер теста</th><th>Время</th><th>Память</th><th>Вердикт</th></tr>";
        for (int i = 0; i < tests.get(0).getCount(); i++) {
            globalTest++;
            RunResult r =  checkTest(globalTest, tests.get(0), i);
            if (r.getVerdict() != 0) {
                pretestPassed = false;
            } else {
                score += tests.get(0).getPoints();
            }
            msg += r.getInfo();
            Gui.getGui().setTestText(msg + "</table>");
        }
        source.setMsg(msg + "</table>");
        if (!pretestPassed) {
            source.setStatus(1);
            source.setScore(score);
            return;
        }
        for (int i = 1; i < tests.size(); i++)
            for (int j = 0; j < tests.get(i).getCount(); j++) {
                globalTest++;
                RunResult r = checkTest(globalTest, tests.get(i), j);
                if (r.getVerdict() == 0) {
                    score += tests.get(i).getPoints();
                }
                msg += r.getInfo();
                Gui.getGui().setTestText(msg + "</table>");
            }
        source.setStatus(2);
        source.setScore(score);
    }

    private RunResult checkTest(int test, TestGroup testGroup, int testInGroup) {
        Checker.clearButSolve();
        try {
            Files.copy(new File(location + "/" + testGroup.getLocation() + "/" + String.format(testGroup.getInFormat(), testGroup.getFirstTest() + testInGroup)).toPath(), new File(Checker.CHECK_DIR + "input.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            RunResult r = new RunResult(0, "", "");
            r.setInfo("<tr><td>" + String.valueOf(test) + "</td><td></td><td></td><td><font color=\"red\">Внутренняя ошибка</font></td></tr>");
            r.setVerdict(-1);
            return r;
        }
        RunResult r = Checker.runexe(timeLimit, memoryLimit);
        String info = "<tr><td>" + String.valueOf(test) + "</td><td>" + r.getTime() + "</td><td>" + r.getMemory() + "</td><td>";
        if (r.getExitcode() != 0) {
            info += "<font color=\"red\">";
            switch (r.getExitcode()) {
                case -1:
                    info += "Внутренняя ошибка";
                    break;
                case 1:
                    info += "Превышен лимит бездействия";
                    break;
                case 2:
                    info += "Превышен лимит памяти";
                    break;
                case 3:
                    info += "Превышен лимит времени";
                    break;
                case 4:
                    info += "Ошибка во времени выполнения";
                    break;
                case 5:
                    info += "Ошибка безопасности";
                    break;
            }
            info += "</font></td></tr>";
            r.setInfo(info);
            r.setVerdict(r.getExitcode());
            return r;
        }
        boolean accepted;
        if (checker == null) {
            accepted = Checker.tokenCheck(location + "/" + testGroup.getLocation() + "/" + String.format(testGroup.getInFormat(), testGroup.getFirstTest() + testInGroup), location + "/" + testGroup.getLocation() + "/" + String.format(testGroup.getOutFormat(), testGroup.getFirstTest() + testInGroup), Checker.CHECK_DIR + "output.txt");
        } else {
            accepted = Checker.customCheck(location + "/" + checker, location + "/" + testGroup.getLocation() + "/" + String.format(testGroup.getInFormat(), testGroup.getFirstTest() + testInGroup), location + "/" + testGroup.getLocation() + "/" + String.format(testGroup.getOutFormat(), testGroup.getFirstTest() + testInGroup), Checker.CHECK_DIR + "output.txt");
        }
        if (accepted) {
            info += "<font color=\"green\">Принято";
            r.setVerdict(0);
        } else {
            info += "<font color=\"red\">Неправильный ответ";
            r.setVerdict(4);
        }
        info += "</font></td></tr>";
        r.setInfo(info);
        return r;
    }

    class TestGroup {
        private int count;
        private int points;
        private int type;
        private String inFormat;
        private String outFormat;
        private String location;
        private int firstTest;

        TestGroup(int count, int ball, int type, String inFormat, String outFormat, String location, int firstTest) {
            this.count = count;
            this.points = ball;
            this.type = type;
            this.inFormat = inFormat;
            this.outFormat = outFormat;
            this.location = location;
            this.firstTest = firstTest;
        }

        public int getPoints() {
            return points;
        }

        public int getCount() {
            return count;
        }

        public int getType() {
            return type;
        }

        public String getInFormat() {
            return inFormat;
        }

        public String getOutFormat() {
            return outFormat;
        }

        public String getLocation() {
            return location;
        }

        public int getFirstTest() {
            return firstTest;
        }
    }
}
