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
    public static final String DB_DRIVER = "org.h2.Driver";
//    Для доступа по tcp
//    public static final String JDBC_URL = "jdbc:h2:tcp://localhost/";
    public static final String JDBC_URL = "jdbc:h2:";
    public static final String DB_NAME = "~/matchmaking";
    public static final String DB_USER_NAME = "";
    public static final String DB_PASSWORD = "";

    private Matchmaking matchmaking;
    private int partySize;
    private int defaultRange;
    private int rangeIncrease;
    private long matchingTime;
    private long lastUpdateTime;

    // Getters and Setters

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    // Functional
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
            Class.forName(DB_DRIVER);
            try {
                Connection connection = getDBConnection();
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
    public boolean cancelRequest(Player player) {
        boolean result = false;
        try (Connection connection = getDBConnection()) {
            result = deletePlayer(connection, player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void increaseRange() {
        System.out.println("Query: increase power range. Add " + this.rangeIncrease + " .");
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

    /**
     * Список с массивами (отобранными игроками) для игр.
     * @param connection соединение с БД
     * @return список массивов игроков
     * @throws SQLException исключение
     */
    private List<Player[]> getPartiesList(Connection connection) throws SQLException {
        List<Player[]> result = new ArrayList<>();
        List<Player> players = getPlayersFromDB(connection);
        for (int i = 0; i < players.size() - 1; i++) {
            Player p = players.get(i);
            if (getParticipantsCount(connection, p.id) >= this.partySize) {
                List<Player> partyForId = getPartyForId(connection, p.id, this.partySize);
                Player[] party = partyForId.toArray(new Player[partyForId.size()]);
                result.add(party);
            }
        }

        return result;
    }


    private Connection getDBConnection(String dbName) throws SQLException {
        String url = JDBC_URL + dbName;
        return DriverManager.getConnection(url, DB_USER_NAME, DB_PASSWORD);
    }

    private Connection getDBConnection() throws SQLException {
        return getDBConnection(DB_NAME);
    }

    /**
     * Создает таблицу в БД для реализации очереди игроков.
     * @param connection соединение с БД
     * @throws SQLException исключение
     */
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

    /**
     * Проверка на наличия игрока <em>player</em> в очереди.
     * @param connection соединение с БД
     * @param player игрок
     * @return <em>true</em>, если игрок есть в очереди
     * @throws SQLException исключение
     */
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

    /**
     * Добавляет игрока в очередь.
     * @param connection соединение с БД\
     * @param player игрок
     * @param defaultRange интервал допустимой мощности для подбора соперников
     * @return количество добавленных игроков (должно равняться 1)
     * @throws SQLException исключение
     */
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

    /**
     * Увеличивает интервал допустимой мощности для игроков в очереди.
     * @param connection соединение с БД
     * @param rangeIncrease значение, на которое увеличивается интервал допустимой мощности
     * @return количество игроков, для которых интервал допустимой мощности был увеличен
     * @throws SQLException исключение
     */
    private int updateRange(Connection connection, int rangeIncrease) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " AS Q SET " +
                FIELD_RANGE + " = (SELECT MIN(" + FIELD_RANGE +
                ") FROM " + TABLE_NAME + " AS IQ " +
                " WHERE IQ." + FIELD_ID + " = Q." + FIELD_ID + ") + ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, rangeIncrease);
        return stmt.executeUpdate();
    }

    /**
     * Получение игрока из элемента <em>ResultSet</em>
     * @param rs элемента <em>ResultSet</em>
     * @return игрок
     * @throws SQLException исключение
     */
    private Player fetchPlayer(ResultSet rs) throws SQLException {
        int id = rs.getInt(FIELD_ID);
        int power = rs.getInt(FIELD_POWER);
        Player result = new Player(id, power);
        return result;
    }

    /**
     * Возвращает всех список игроков в очереди.
     * @param connection соединение с БД
     * @return список всех игроков в очереди
     * @throws SQLException исключение
     */
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

    /**
     * Возвращает список игроков, которые будут отобраны для игры с игроком, у которого <em>player.id = id</em>.
     * @param connection соединение с БД
     * @param id player.id
     * @param partySize количество игроков для игры
     * @return список игроков для игры
     * @throws SQLException исключение
     */
    private List<Player> getPartyForId(Connection connection, int id, int partySize) throws SQLException {
        markSelectedPlayers(connection, id);
        List<Player> result = getSelectedPlayers(connection);
        deleteSelectedPlayers(connection);

        return result;
    }

    /**
     * Отмечает в очереди игроков, которые будут отобраны для игры с игроком, у которого <em>player.id = id</em>.
     * @param connection соединение с БД
     * @param id player.id
     * @return количество отмеченных икроков
     * @throws SQLException исключение
     */
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

    /**
     * Возвращает список игроков для игры.
     * @param connection соединение с БД
     * @return список игроков для игры
     * @throws SQLException исключение
     */
    private List<Player> getSelectedPlayers(Connection connection) throws SQLException {
        List<Player> result = new ArrayList<>();
        String sql = "SELECT " + FIELD_ID + ", " + FIELD_POWER + " FROM " + TABLE_NAME +
                " WHERE " + FIELD_SELECTED + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, true);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            result.add(fetchPlayer(rs));
        }

        return result;
    }

    /**
     * Удаляет из очереди игроков, которые уже были отобраны для игры.
     * @param connection соединение с БД
     * @return количество удаленных игроков
     * @throws SQLException исключение
     */
    private int deleteSelectedPlayers(Connection connection) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + FIELD_SELECTED + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, true);

        return stmt.executeUpdate();
    }

    /**
     * Возвращает количество возможных соперников для игрока с <em>player.id = id</em>.
     * @param connection соединение с БД
     * @param id <em>id</em> игрока
     * @return количество возможных соперников
     * @throws SQLException исключение
     */
    private int getParticipantsCount(Connection connection, int id) throws SQLException {
        int result = 0;
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " AS Q WHERE " +
                "Q." + FIELD_POWER + " >= " +
                "(SELECT MIN(" + FIELD_POWER + " - " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ?) " +
                "AND Q." + FIELD_POWER + " <= " +
                "(SELECT MIN(" + FIELD_POWER + " + " + FIELD_RANGE + ") FROM " + TABLE_NAME + " AS IQ WHERE IQ." + FIELD_ID + " = ?)";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.setInt(2, id);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            result = rs.getInt(1);
        }
        return result;
    }

    /**
     * Отмечает в БД игроков с истекшим сроком ожидания.
     * @param connection соединение с БД
     * @return количество отмеченных игроков
     * @throws SQLException исключение
     */
    private int makrOutsiders(Connection connection) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET " + FIELD_DELETED + " = TRUE " +
                " WHERE DATEDIFF('MILLISECOND', " + FIELD_TIME + ", CURRENT_TIMESTAMP()) > ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setLong(1, matchingTime);
        return stmt.executeUpdate();
    }

    /**
     * Возвращает список игроков с истекшим временем ожидания.
     * @param connection соединение с БД
     * @return список игроков
     * @throws SQLException
     */
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

    /**
     * Удаляет из очереди игроков с истекшим временем ожидания.
     * @param connection соединение с БД
     * @return int (количество удаленных игроков)
     * @throws SQLException исключение
     */
    private int deleteOutsiders(Connection connection) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + FIELD_DELETED + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setBoolean(1, true);
        return stmt.executeUpdate();
    }

    /**
     * Удаление игрока из очереди.
     * @param connection Соединение с БД
     * @param player игрок
     * @return <em>true</em>, если игрок был удален
     * @throws SQLException исключение
     */
    private boolean deletePlayer(Connection connection, Player player) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + FIELD_ID + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, player.id);

        return stmt.executeUpdate() > 0;
    }

}
