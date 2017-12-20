package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class MatchQueryImpl implements MatchQuery {

    public static final String TABLE_NAME = "query";
    public static final String FIELD_ID = "id";
    public static final String FIELD_POWER = "power";
    public static final String FIELD_RANGE = "range";
    public static final String FIELD_TIME = "time";

    private Matchmaking matchmaking;
    private int partySize;
    private int defaultRange;
    private int rangeIncrease;
    private long matchingTime;

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
        try (Connection connection = getDBConnection()) {
            updateRange(connection, this.rangeIncrease);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Player> getMatchList(Connection connection) throws SQLException {
        List<Player> result = new ArrayList<>();
        List<Player> players = getPlayersFromDB(connection);
        for (int i = 0; i < players.size() - 1; i++) {
            Player p = players.get(i);
            if (findParticipantsCount(connection, p.id) >= this.partySize) {
                result = getPartyForId(connection, p.id, this.partySize);
                break;
            }
        }

        return result;
    }

    @Override
    public Player[] getParty() {
        Player[] result = null;
        try (Connection connection = getDBConnection()) {
            result = (Player[]) getMatchList(connection).toArray();
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

        String sql = "SELECT " + FIELD_ID + ", " + FIELD_POWER + " FROM " + TABLE_NAME + " AS Q WHERE " +
                "Q." + FIELD_POWER + " >= " +
                "(SELECT MIN(" + FIELD_POWER + " - " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ? ORDER BY " + FIELD_TIME + ") " +
                "AND Q." + FIELD_POWER + " <= " +
                "(SELECT MIN(" + FIELD_POWER + " + " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ? ORDER BY " + FIELD_TIME + ")";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.setInt(2, id);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            result.add(fetchPlayer(rs));
            if (result.size() >= partySize) {
                break;
            }
        }

        return result;
    }

    private int findParticipantsCount(Connection connection, int id) throws SQLException {
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

    @Override
    public List<Player> getOutsiders() {
        List<Player> result = new ArrayList<>();
        String sql = "SELECT " + FIELD_ID + ", " + FIELD_POWER + " FROM " + TABLE_NAME +
                " WHERE DATEDIFF('MILLISECOND', CURRENT_TIMESTAMP(), " + FIELD_TIME + ") > ?";
        try (Connection connection = getDBConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, matchingTime);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(fetchPlayer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}
