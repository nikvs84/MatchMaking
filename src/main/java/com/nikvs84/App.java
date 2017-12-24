package com.nikvs84;

import com.nikvs84.entity.Player;
import com.nikvs84.matchmaking.Matchmaking;
import com.nikvs84.matchmaking.MatchmakingCallbacks;
import com.nikvs84.util.Timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws ClassNotFoundException {
        System.out.println( "Hello World!" );
        emulator();
    }

    private static void emulator() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            MatchmakingCallbacks callbacks = new MatchmakingCallbacks() {
                @Override
                public void onMatched(Player[] players) {
                    System.out.println("callbacks.onMatched()");
                    for (Player p : players) {
                        System.out.println(p);
                    }
                }

                @Override
                public void onCancel(Player player) {
                    System.out.printf("callbacks.onCancel(%s)", player);
                    System.out.println();
                }
            };
            Matchmaking matchmaking = new Matchmaking(callbacks, 2, 1, 1, 1800000, 3600000);
            matchmaking.update(new Date().getTime());
            Timer timer = new Timer(matchmaking.getMatchQuery(), matchmaking.getRangeIncreaseTime());
            timer.start();
            System.out.println("Input: update, add, print or exit");
            boolean isRunning = true;
            while (isRunning) {
                String line = reader.readLine();
                String[] cmd = line.split(" ");
                switch (cmd[0].toLowerCase()) {
                    case "exit":
                        isRunning = false;
                        break;
                    case "update":
                        matchmaking.update(new Date().getTime());
                        break;
                    case "add":
                        Player player = new Player(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]));
                        matchmaking.addRequest(player);
                        break;
                    case "print":
                        List<Player> players = matchmaking.getMatchQuery().getAllPlayers();
                        printPlayers(players);
                        break;
                    case "cancel":
                        Player cancelPlayer = new Player(Integer.valueOf(cmd[1]), 0);
                        matchmaking.onCancel(cancelPlayer);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Player parsePlayer(String string, String separator) {
        Player result = null;
        String[] array = string.split(separator);
        if (array.length >= 2) {
            int id = Integer.valueOf(array[0]);
            int power = Integer.valueOf(array[1]);
            result = new Player(id, power);
        }

        return result;
    }

    private static void printPlayers(List<Player> players) {
        for (Player player : players) {
            System.out.println(player);
        }
    }

}