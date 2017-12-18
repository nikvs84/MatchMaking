package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

public interface MatchQuery {

    Player[] addRequest(Player player);

    void increasePowerRange(int rangeIncrease);

    void initQuery(int partySize, int defaultRange, int rangeIncrease);
}
