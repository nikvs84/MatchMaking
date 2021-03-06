package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;
import com.nikvs84.util.Timer;

import java.util.Date;
import java.util.List;

public class Matchmaking {
    private MatchmakingCallbacks callbacks;
    private long lastUpdateTime;
    private final int partySize;
    private final int defaultRange;
    private final int rangeIncrease;
    private final long rangeIncreaseTime;
    private final long matchingTime;
    private MatchQuery matchQuery;
    private Timer timer;

    public Matchmaking(MatchmakingCallbacks callbacks, int partySize, int defaultRange, int rangeIncrease, long rangeIncreaseTime, long matchingTime) {
        this.callbacks = callbacks;
        this.partySize = partySize;
        this.defaultRange = defaultRange;
        this.rangeIncrease = rangeIncrease;
        this.rangeIncreaseTime = rangeIncreaseTime;
        this.matchingTime = matchingTime;
        this.matchQuery = new MatchQueryImpl();
        this.matchQuery.initQuery(this);
        this.timer = new Timer(matchQuery, rangeIncreaseTime);
    }

    /**
     * Запуск системы подбора игроков.
     */
    public void startMatchMaking() {
        this.update(new Date().getTime());
        this.timer.start();
    }

    //  Gettets and Setters
    public MatchmakingCallbacks getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(MatchmakingCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getPartySize() {
        return partySize;
    }

    public int getDefaultRange() {
        return defaultRange;
    }

    public int getRangeIncrease() {
        return rangeIncrease;
    }

    public long getRangeIncreaseTime() {
        return rangeIncreaseTime;
    }

    public long getMatchingTime() {
        return matchingTime;
    }

    public MatchQuery getMatchQuery() {
        return matchQuery;
    }

    public void setMatchQuery(MatchQuery matchQuery) {
        this.matchQuery = matchQuery;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    //  Functional

    /**
     * Добавляет игрока <em>player</em> в очередь.
     * @param player игрок
     */
    public void addRequest(Player player) {
        boolean isNotCandelled = matchQuery.addRequest(player);
        if (!isNotCandelled) {
            onCancel(player);
        }
    }

    /**
     * Удаление игрока <em>player</em> из очереди.
     * @param player игрок
     */
    public void cancelRequest(Player player) {
        onCancel(player);
    }

    /**
     * Обновление.
     * Сбор партий игроков, удаление из очереди игроков с истекшим временем ожидания.
     * Если время абсолютное (согласно ТЗ), то параметр <em>time</em> будет <em>long</em>, а не <em>int</em>.
     * @param time время последнего обновления.
     */
    public void update(long time) {
        this.lastUpdateTime = time;
        this.matchQuery.setLastUpdateTime(lastUpdateTime);
        List<Player[]> parties = this.matchQuery.getParties();

        for (Player[] players: parties) {
            callbacks.onMatched(players);
        }

        Player[] outsiders = matchQuery.getOutsiders();
        for (Player player : outsiders) {
            onCancel(player);
        }
    }

    /**
     * Удаление игрока из очереди.
     * @param player игрок
     */
    public void onCancel(Player player) {
        matchQuery.cancelRequest(player);
        callbacks.onCancel(player);
    }
}
