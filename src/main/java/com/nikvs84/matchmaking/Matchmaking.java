package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

public class Matchmaking {
    private MatchmakingCallbacks callbacks;
    private long lastUpdateTime;
    private final int partySize;
    private final int defaultRange;
    private final int rangeIncrease;
    private final long rangeIncreaseTime;
    private final long matchingTime;
    private MatchQuery matchQuery;

    public Matchmaking(MatchmakingCallbacks callbacks, int partySize, int defaultRange, int rangeIncrease, long rangeIncreaseTime, long matchingTime) {
        this.callbacks = callbacks;
        this.partySize = partySize;
        this.defaultRange = defaultRange;
        this.rangeIncrease = rangeIncrease;
        this.rangeIncreaseTime = rangeIncreaseTime;
        this.matchingTime = matchingTime;
    }

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

    public void setMatchQuery(MatchQuery matchQuery) {
        this.matchQuery = matchQuery;
        this.matchQuery.setPartySize(this.partySize);
    }

    public void addRequest(Player player) {
        matchQuery.addRequest(player);

    }

    public void cancelRequest(Player player) {
        callbacks.onCancel(player);
    }

    /**
     * Если время абсолютное (согласно ТЗ), то параметр &quot;time&quot; будет <em>long</em>, а не <em>int</em>.
     * @param time время последнего обновления.
     */
    public void update(long time) {
        this.lastUpdateTime = time;
    }
}
