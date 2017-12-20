package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

public interface MatchQuery {

    boolean addRequest(Player player);

    void increaseRange();

    Player[] getParty();

    void initQuery(int partySize, int defaultRange, int rangeIncrease);

    void initQuery(Matchmaking matchmaking);
}
