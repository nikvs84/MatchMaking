package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

import java.sql.*;
import java.util.*;

public class MatchQueryImpl implements MatchQuery {

//    public static final int LOAD_FACTOR = 1;
//    private Map<Player, Integer> waitingMap;
    private int partySize;
    private int defaultRange;
    private int rangeIncrease;

    public void initQuery(int partySize, int defaultRange, int rangeIncrease) {
        this.partySize = partySize;
//        this.waitingMap = new HashMap<Player, Integer>(partySize * LOAD_FACTOR);
        this.defaultRange = defaultRange;
        this.rangeIncrease = rangeIncrease;
    }

    public Player[] addRequest(Player player) {
        Player[] result = null;
//        if (waitingMap.size() + 1 < partySize) {
//            waitingMap.put(player, defaultRange);
//            result = (Player[]) getMatchList().toArray();
//        }
        return result;
    }

    public void increasePowerRange(int rangeIncrease) {
//        for (Map.Entry<Player, Integer> entry : this.waitingMap.entrySet()) {
//            this.waitingMap.put(entry.getKey(), entry.getValue() + rangeIncrease);
//        }
    }

    private List<Player> getMatchList() {
        List<Player> result = new ArrayList<Player>();

        return result;
    }

    private Connection getDBConnection(String dbName) throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:" + dbName);
    }

    private Connection getDBConnection() throws SQLException {
        return getDBConnection("");
    }

    private void initTables(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS query( " +
                " id INT NOT NULL, " +
                " power INT NOT NULL, " +
                " range INT NOT NULL, " +
                " PRIMARY KEY (id) " +
                " )";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private void insertPlayer(Connection connection, Player player, int defaultRange) throws SQLException {
        String sql = "INSERT INTO query " +
                " VALUES " +
                " (?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setLong(1, player.id);
        stmt.setInt(2, player.power);
        stmt.setInt(3, defaultRange);
    }

    private void updateRange(Connection connection, int rangeIncrease) throws SQLException {
        String sql = "";
        PreparedStatement stmt = connection.prepareStatement(sql);

    }

}
