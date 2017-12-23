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
    public static final String FIELD_DELETED = "deleted";
    public static final String FIELD_SELECTED = "selected";

    private Matchmaking matchmaking;
    private int partySize;
    private int defaultRange;
    private int rangeIncrease;
    private long matchingTime;
    private long lastUpdateTime;
    private Connection connection;

    public Connection getConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return this.connection;
        } else {
            return getDBConnection();
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public void initQuery(int partySize, int defaultRange, int rangeIncrease, long matchingTime) {
        this.partySize = partySize;
        this.defaultRange = defaultRange;
        this.rangeIncrease = rangeIncrease;
        this.matchingTime = matchingTime;
    }

    @Override
    public void initQuery(Matchmaking matchmaking) {
        this.matchmaking = matchmaking;
        this.partySize = matchmaking.getPartySize();
        this.defaultRange = matchmaking.getDefaultRange();
        this.rangeIncrease = matchmaking.getRangeIncrease();
        this.matchingTime = matchmaking.getMatchingTime();
        try {
            Class.forName("org.h2.Driver");
            try (Connection connection = getDBConnection()) {
                initTables(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addRequest(Player player) {
        boolean result = false;

        try (Connection connection = getDBConnection()) {
            result = !isPlayerInQuery(connection, player);
            if (result) {
                insertPlayer(connection, player, this.defaultRange);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void increaseRange() {
        System.out.println("Query: increase power range");
        try (Connection connection = getDBConnection()) {
            updateRange(connection, this.rangeIncrease);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Player[]> getParties() {
        List<Player[]> result = null;
        try (Connection connection = getDBConnection()) {
            result = getPartiesList(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


    @Override
    public Player[] getOutsiders() {
        List<Player> result = null;
        try (Connection connection = getDBConnection()) {
            makrOutsiders(connection);
            result = fetchOutsiders(connection);
            deleteOutsiders(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toArray(new Player[result.size()]);
    }

    @Override
    public List<Player> getAllPlayers() {
        List<Player> result = null;
        try (Connection connection = getDBConnection()) {
            result = getPlayersFromDB(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private List<Player[]> getPartiesList(Connection connection) throws SQLException {
        List<Player[]> result = new ArrayList<>();
        List<Player> players = getPlayersFromDB(connection);
        for (int i = 0; i < players.size() - 1; i++) {
            Player p = players.get(i);
            if (getParticipantsCount(connection, p.id) >= this.partySize) {
                markSelectedPlayers(connection, p.id);
                List<Player> partyForId = getPartyForId(connection, p.id, this.partySize);
                Player[] party = partyForId.toArray(new Player[partyForId.size()]);
                result.add(party);
            }
        }

        return result;
    }

    private Connection getDBConnection(String dbName) throws SQLException {
        return DriverManager.getConnection("jdbc:h2:tcp://localhost/" + dbName);
    }

    private Connection getDBConnection() throws SQLException {
        return getDBConnection("~/matchmaking");
    }

    private void initTables(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "( " +
                " " + FIELD_ID + " INT NOT NULL, " +
                " " + FIELD_POWER + " INT NOT NULL, " +
                " " + FIELD_RANGE + " INT NOT NULL, " +
                " " + FIELD_TIME + "    TIMESTAMP NOT NULL, " +
                " " + FIELD_DELETED + " BOOLEAN NOT NULL DEFAULT FALSE," +
                " " + FIELD_SELECTED + " BOOLEAN NOT NULL DEFAULT FALSE," +
                " PRIMARY KEY (" + FIELD_ID + ") " +
                " )";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    private boolean isPlayerInQuery(Connection connection, Player player) throws SQLException {
        boolean result = false;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + FIELD_ID + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, player.id);
        ResultSet rs = stmt.executeQuery();
        rs.last();
        result = (rs.getRow() > 0);

        return result;
    }

    private int insertPlayer(Connection connection, Player player, int defaultRange) throws SQLException {
        Timestamp beginTime = new Timestamp(lastUpdateTime);
        String sql = "INSERT INTO " + TABLE_NAME +
                " VALUES " +
                " (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setLong(1, player.id);
        stmt.setInt(2, player.power);
        stmt.setInt(3, defaultRange);
        stmt.setTimestamp(4, beginTime);
        stmt.setBoolean(5, false);
        stmt.setBoolean(6, false);
        return stmt.executeUpdate();
    }

    private int updateRange(Connection connection, int rangeIncrease) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " AS Q SET " +
                FIELD_RANGE + " = (SELECT MIN(" + FIELD_RANGE +
                ") FROM " + TABLE_NAME + " AS IQ " +
                " WHERE IQ." + FIELD_ID + " = Q." + FIELD_ID + ") + ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, rangeIncrease);
        return stmt.executeUpdate();
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
        markSelectedPlayers(connection, id);
        List<Player> result = getSelectedPlayers(connection);
        deleteSelectedPlayers(connection);

        return result;
    }

    private int markSelectedPlayers(Connection connection, int id) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " AS Q SET " + FIELD_SELECTED + " = TRUE " +
                " WHERE Q." + FIELD_POWER + " >= " +
                "(SELECT MIN(" + FIELD_POWER + " - " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ? ORDER BY " + FIELD_TIME + ") " +
                "AND Q." + FIELD_POWER + " <= " +
                "(SELECT MIN(" + FIELD_POWER + " + " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ? ORDER BY " + FIELD_TIME + ") " +
                " ORDER BY " + FIELD_TIME + " LIMIT ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.setInt(2, id);
        stmt.setInt(3, partySize);

        return stmt.executeUpdate();
    }

    private List<Player> getSelectedPlayers(Connection connection) throws SQLException {
        List<Player> result = new ArrayList<>();
        String sql = "";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, true);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            result.add(fetchPlayer(rs));
        }

        return result;
    }

    private int deleteSelectedPlayers(Connection connection) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + FIELD_SELECTED + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, true);

        return stmt.executeUpdate();
    }

    private int getParticipantsCount(Connection connection, int id) throws SQLException {
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
        return rs.getRow();
    }

    private int makrOutsiders(Connection connection) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET " + FIELD_DELETED + " = TRUE " +
                " WHERE DATEDIFF('MILLISECOND', " + FIELD_TIME + ", CURRENT_TIMESTAMP()) > ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setLong(1, matchingTime);
        return stmt.executeUpdate();
    }

    private List<Player> fetchOutsiders(Connection connection) throws SQLException {
        List<Player> result = new ArrayList<>();
        String sql = "SELECT " + FIELD_ID + ", " + FIELD_POWER + " FROM " + TABLE_NAME +
                " WHERE " + FIELD_DELETED + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, true);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            result.add(fetchPlayer(rs));
        }

        return result;
    }

    private boolean deleteOutsiders(Connection connection) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + FIELD_DELETED + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, true);
        return stmt.execute();
    }

}
