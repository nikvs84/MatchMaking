package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

import java.util.*;

public class MatchQueryImpl implements MatchQuery {

    public static final int LOAD_FACTOR = 1;
    private Map<Player, Integer> waitingMap;
    private int partySize;
    private int defaultRange;
    private int rangeIncrease;

    public void initQuery(int partySize, int defaultRange, int rangeIncrease) {
        this.partySize = partySize;
        this.waitingMap = new HashMap<Player, Integer>(partySize * LOAD_FACTOR);
        this.defaultRange = defaultRange;
        this.rangeIncrease = rangeIncrease;
    }

    public Player[] addRequest(Player player) {
        Player[] result = null;
        if (waitingMap.size() + 1 < partySize) {
            waitingMap.put(player, defaultRange);
            result = (Player[]) getMatchList().toArray();
        }
        return result;
    }

    public void increasePowerRange(int rangeIncrease) {
        for (Map.Entry<Player, Integer> entry : this.waitingMap.entrySet()) {
            this.waitingMap.put(entry.getKey(), entry.getValue() + rangeIncrease);
        }
    }

    private List<Player> getMatchList() {
        List<Player> result = new ArrayList<Player>();

        return result;
    }

}
