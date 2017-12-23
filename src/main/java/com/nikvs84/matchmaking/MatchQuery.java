package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

import java.util.List;

public interface MatchQuery {

    boolean addRequest(Player player);

    void increaseRange();

    List<Player[]> getParties();

    void setLastUpdateTime(long lastUpdateTime);

    void initQuery(int partySize, int defaultRange, int rangeIncrease, long matchingTime);

    void initQuery(Matchmaking matchmaking);

    Player[] getOutsiders();

    /**
     * Для отладки.
     * @return список всех игроков в очереди.
     */
    List<Player> getAllPlayers();
}
