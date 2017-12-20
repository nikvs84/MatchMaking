package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

import java.util.List;

public interface MatchQuery {

    boolean addRequest(Player player);

    void increaseRange();

    Player[] getParty();

    void initQuery(int partySize, int defaultRange, int rangeIncrease, long matchingTime);

    void initQuery(Matchmaking matchmaking);

    List<Player> getOutsiders();
}
