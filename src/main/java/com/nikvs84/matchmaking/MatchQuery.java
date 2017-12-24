package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

import java.util.List;

public interface MatchQuery {
    /**
     * Добавляет игрока в очередь.
     * @param player игрок
     * @return <em>true</em>, если игрок добавлен
     */
    boolean addRequest(Player player);

    /**
     * Удаляет игрока <em>player</em> из очереди.
     * @param player игрок
     * @return <em>true</em>, если игрок удален
     */
    boolean cancelRequest(Player player);

    /**
     * Увеличивает интервал допустимой мощности для всех игроков в очереди.
     */
    void increaseRange();

    /**
     * Возвращает список с массивами (отобранными игроками) для игр.
     * @return список массивов игроков
     */
    List<Player[]> getParties();

    /**
     * Устанавливает время последнего обновления в очереди.
     * @param lastUpdateTime абсолютное время
     */
    void setLastUpdateTime(long lastUpdateTime);

    /**
     * Инициализация очереди игроков.
     * @param partySize количество игроков в игре.
     * @param defaultRange интервал допустимой мощности
     * @param rangeIncrease значение, на которое будет увелечиваться интеравл допустимой мощности
     * @param matchingTime максимальное время ожидания для игроков
     */
    void initQuery(int partySize, int defaultRange, int rangeIncrease, long matchingTime);

    /**
     * Инициализация очереди игроков.
     * @param matchmaking экземпляр класса {@link Matchmaking}
     */
    void initQuery(Matchmaking matchmaking);

    /**
     * Возвращает массив игроков с истекшим временем ожидания в очереди.
     * @return массив игроков с истекшим временем ожидания
     */
    Player[] getOutsiders();

    /**
     * Для отладки.
     * @return список всех игроков в очереди.
     */
    List<Player> getAllPlayers();
}
