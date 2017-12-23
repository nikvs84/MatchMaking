package com.nikvs84.util;

import com.nikvs84.matchmaking.MatchQuery;

public class Timer extends Thread implements Runnable {
    private MatchQuery matchQuery;
    private long rangeIncreaseTime;
    private boolean isRunning;

    public Timer(MatchQuery matchQuery, long rangeIncreaseTime) {
        this.matchQuery = matchQuery;
        this.rangeIncreaseTime = rangeIncreaseTime;
        this.isRunning = true;
        setDaemon(true);
    }

    public MatchQuery getMatchQuery() {
        return matchQuery;
    }

    public void setMatchQuery(MatchQuery matchQuery) {
        this.matchQuery = matchQuery;
    }

    public long getRangeIncreaseTime() {
        return rangeIncreaseTime;
    }

    public void setRangeIncreaseTime(long rangeIncreaseTime) {
        this.rangeIncreaseTime = rangeIncreaseTime;
    }


    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(rangeIncreaseTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (matchQuery) {
                this.matchQuery.increaseRange();
            }
        }
    }

    public void stopTimer() {
        this.isRunning = false;
    }
}
