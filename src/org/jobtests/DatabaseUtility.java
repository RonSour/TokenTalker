package org.jobtests;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DatabaseUtility {
    private static DatabaseUtility instance;
    private ComboPooledDataSource cpds;

    private DatabaseUtility() {
        cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl("jdbc:mysql://localhost/TokenTalker?serverTimezone=UTC");
        cpds.setUser("root");
        cpds.setPassword("");
        // Optional Settings
        cpds.setInitialPoolSize(5);
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setMaxStatements(100);
    }

    public static DatabaseUtility getInstance() {
        if (instance == null)
            instance = new DatabaseUtility();
        return instance;
    }

    public ComboPooledDataSource getDataSource() {
        return cpds;
    }

    public List<Message> getLastMessages(Integer count) {
        List<Message> ret = new ArrayList<>();
        Connection connection;
        try {
            connection = getDataSource().getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT ts, name, text FROM message order by ts desc limit " + count.toString());

            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                Message m = new Message(resultSet.getString("name"), resultSet.getString("text"));
                m.setTs(resultSet.getLong("ts"));
                ret.add(m);
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean addMessage(Message message) {
        Connection connection;
        boolean result = false;
        try {
            connection = getDataSource().getConnection();
            PreparedStatement pstmt = connection.prepareStatement("insert message (ts, name, text) values (?, ?, ?)");
            pstmt.setLong(1, message.getTs());
            pstmt.setString(2, message.getName());
            pstmt.setString(3, message.getText());
            if (pstmt.executeUpdate() > 0)
                result = true;
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean checkUser(String name, String hash) {
        Connection connection;
        boolean res = false;
        try {
            connection = getDataSource().getConnection();
            //in sql requests may add check for user state and privileges
            PreparedStatement pstmt = connection.prepareStatement("select count(*) from user where name = ? and hash = ?");
            pstmt.setString(1, name);
            pstmt.setString(2, hash);

            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt(1) > 0) {
                    res = true;
                    break;
                }
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
