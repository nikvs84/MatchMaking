package com.nikvs84.entity;

public class Player {
    public final int id;
    public final int power;

    public Player(int id, int power) {
        this.id = id;
        this.power = power;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return id == player.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", power=" + power +
                '}';
    }
}
