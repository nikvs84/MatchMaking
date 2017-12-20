package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

import java.sql.*;
import java.util.*;

public class MatchQueryImpl implements MatchQuery {

    public static final String TABLE_NAME = "query";
    public static final String FIELD_ID = "id";
    public static final String FIELD_POWER = "power";
    public static final String FIELD_RANGE = "range";
    public static final String FIELD_TIME = "time";
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
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "( " +
                " " + FIELD_ID + " INT NOT NULL, " +
                " " + FIELD_POWER + " INT NOT NULL, " +
                " " + FIELD_RANGE + " INT NOT NULL, " +
                " " + FIELD_TIME + "    TIMESTAMP NOT NULL " +
                " PRIMARY KEY (" + FIELD_ID + ") " +
                " )";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    private void insertPlayer(Connection connection, Player player, int defaultRange) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " " +
                " VALUES " +
                " (?, ?, ?, CURRENT_TIMESTAMP())";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setLong(1, player.id);
        stmt.setInt(2, player.power);
        stmt.setInt(3, defaultRange);
    }

    private void updateRange(Connection connection, int rangeIncrease) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " AS Q SET " +
                FIELD_RANGE + " = (SELECT MIN(" + FIELD_RANGE +
                ") FROM " + TABLE_NAME + " AS IQ " +
                " WHERE IQ." + FIELD_ID + " = Q." + FIELD_ID + ") + 1";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, rangeIncrease);
    }

    private Player fetchPlayer(ResultSet rs) throws SQLException {
        int id = rs.getInt(FIELD_ID);
        int power = rs.getInt(FIELD_POWER);
        Player result = new Player(id, power);
        return result;
    }

    private List<Player> getPlayersFromDB(Connection connection) throws SQLException {
        List<Player> result = new ArrayList<>();
        String sql = "SELECT " + FIELD_ID + ", " + FIELD_POWER + " FROM " + TABLE_NAME + " ORDER BY " + FIELD_TIME;
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            result.add(fetchPlayer(rs));
        }

        return result;
    }

    private List<Player> getPartyForId(Connection connection, int id, int partySize) throws SQLException {
        List<Player> result = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " AS Q WHERE " +
                "Q." + FIELD_POWER + " >= " +
                "(SELECT MIN(" + FIELD_POWER + " - " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ?) " +
                "AND Q." + FIELD_POWER + " <= " +
                "(SELECT MIN(" + FIELD_POWER + " + " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ?)";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.setInt(2, id);
        ResultSet rs = stmt.executeQuery();
        rs.last();
        long count = rs.getRow();

        if (count >= partySize) {
            sql = "SELECT " + FIELD_ID + ", " + FIELD_POWER + " FROM " + TABLE_NAME + " AS Q WHERE " +
                    "Q." + FIELD_POWER + " >= " +
                    "(SELECT MIN(" + FIELD_POWER + " - " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ? ORDER BY " + FIELD_TIME + ") " +
                    "AND Q." + FIELD_POWER + " <= " +
                    "(SELECT MIN(" + FIELD_POWER + " + " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ? ORDER BY " + FIELD_TIME + ")";

            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setInt(2, id);
            rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(fetchPlayer(rs));
                if (result.size() >= partySize) {
                    break;
                }
            }
        }

        return result;
    }

}
