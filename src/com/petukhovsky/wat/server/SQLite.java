package com.petukhovsky.wat.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Arthur on 10/6/2014.
 */
public class SQLite {

    static{
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String,Account> getAccounts() {
        HashMap<String, Account> res = new HashMap<String, Account>();
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("select * from accs");
            while (rs.next()) {
                String username = rs.getString("Username");
                res.put(username.toLowerCase(), new Account(rs.getInt("ID"), username, rs.getString("Password"), rs.getInt("Superuser"), rs.getInt("Type"), rs.getString("Color")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static void writeAccount(Account acc) {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.execute(String.format("INSERT INTO accs (ID, Username, Password, Superuser, Type, Color) VALUES (%d, '%s', '%s', %d, %d, '%s')", acc.getId(), acc.getLogin(), acc.getPass(), acc.getSuperuser(), acc.getType(), acc.getColor()));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateAccount(int id, String key, String value) {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.execute(String.format("UPDATE accs SET %s = '%s' WHERE ID = %d", key, value, id));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateAccount(int id, String key, int value) {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.execute(String.format("UPDATE accs SET %s = %d WHERE ID = %d", key, value, id));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static HashMap<Account,Result> getResults(String id, int tasksCount) {
        HashMap<Account, Result> r = new HashMap<Account, Result>();
        HashMap<Integer, Integer> m = new HashMap<Integer, Integer>();
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("SELECT id, score, account, task FROM olymp WHERE (status=2 AND olymp='" + id + "')");
            while (rs.next()) {
                int codeId = rs.getInt("id");
                int score = rs.getInt("score");
                int account = rs.getInt("account");
                int task = rs.getInt("task");
                if (!m.containsKey(account) || m.get(account) < codeId) {
                    m.put(account, codeId);
                    Account acc = Auth.getAccount(account);
                    if (!r.containsKey(acc)) r.put(acc, new Result(tasksCount));
                    r.get(acc).setScore(task, score);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    public static int getSourceID(String id) {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("SELECT MAX(id) FROM olymp");
            while (rs.next()) return rs.getInt("MAX(id)");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("getSourceID fail");
        return -1;
    }

    public static void writeSource(Source source) {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.execute(String.format("INSERT INTO olymp (id, time, olymp, status, language, msg, location, account, task, score) " +
                    "VALUES (%d, '%s', '%s', %d, %d, '%s', '%s', %d, %d, %d)", source.getId(), source.getTime(), source.getOlympiad().getId(), source.getStatus(),
                    source.getLanguage(), source.getMsg(), source.getLocation(), source.getAccount().getId(), source.getTask(), source.getScore()));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<Source> getSources(int id, String olymp) {
        ArrayList<Source> list = new ArrayList<Source>();
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet r = statement.executeQuery("SELECT id, time, task, status, language, msg, location FROM olymp WHERE (account=" + id + " AND olymp='" + olymp + "')");
            while (r.next()) {
                list.add(new Source(r.getInt("language"), r.getString("location"), null, r.getInt("task"), null, r.getInt("id"), r.getString("time"), r.getInt("status"), r.getString("msg")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void updateSource(Source source) {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.execute(String.format("UPDATE olymp SET msg='%s', status=%d, score=%d WHERE (olymp='%s' AND id=%d)", source.getMsg(), source.getStatus(), source.getScore(), source.getOlympiad().getId(), source.getId()));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void getCheckQueue() {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection("jdbc:sqlite:" + Checker.RES_DIR + "watserver.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet r = statement.executeQuery("SELECT id, time, task, status, language, msg, location, olymp, account, score FROM olymp WHERE status=0");
            while (r.next()) {
               WatOlympiad.addCheckTask(new Source(r.getInt("language"), r.getString("location"), WatOlympiad.getOlympiad(r.getString("olymp")), r.getInt("task"), Auth.getAccount(r.getInt("account")), r.getInt("id"), r.getString("time"), r.getInt("status"), r.getString("msg")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
