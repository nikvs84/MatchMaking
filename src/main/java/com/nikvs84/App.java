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
                }
            };
            Matchmaking matchmaking = new Matchmaking(callbacks, 2, 1, 1, 1000, 5);
            Timer timer = new Timer(matchmaking.getMatchQuery(), matchmaking.getRangeIncreaseTime());
            timer.start();
            System.out.println("Input: update, add, print or exit");
            boolean isRunning = true;
            while (isRunning) {
                String line = reader.readLine();
                switch (line.toLowerCase()) {
                    case "exit":
                        isRunning = false;
                        break;
                    case "update":
                        matchmaking.update(new Date().getTime());
                        break;
                    case "add":
                        System.out.println("Input player: id power");
                        String playerValues = reader.readLine();
                        Player player = parsePlayer(playerValues, " ");
                        matchmaking.addRequest(player);
                        break;
                    case "print":
                        List<Player> players = matchmaking.getMatchQuery().getAllPlayers();
                        printPlayers(players);
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