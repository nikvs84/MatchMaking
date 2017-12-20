package com.nikvs84.util;

import com.nikvs84.matchmaking.MatchQuery;

public class Timer implements Runnable {
    private MatchQuery matchQuery;
    private long rangeIncreaseTime;
    private int rangeIncrease;

    private boolean isRunning;

    public Timer(MatchQuery matchQuery, long rangeIncreaseTime, int rangeIncrease) {
        this.matchQuery = matchQuery;
        this.rangeIncreaseTime = rangeIncreaseTime;
        this.rangeIncrease = rangeIncrease;
        this.isRunning = true;
        Thread.currentThread().setDaemon(true);
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

    public int getRangeIncrease() {
        return rangeIncrease;
    }

    public void setRangeIncrease(int rangeIncrease) {
        this.rangeIncrease = rangeIncrease;
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
