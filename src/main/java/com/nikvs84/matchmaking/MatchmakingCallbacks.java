package com.nikvs84.matchmaking;

import com.nikvs84.entity.Player;

public interface MatchmakingCallbacks {
    void onMatched(Player[] players);
    void onCancel(Player player);
}
