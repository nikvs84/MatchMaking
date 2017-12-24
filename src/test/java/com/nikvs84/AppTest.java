package com.nikvs84;

import com.nikvs84.entity.Player;
import com.nikvs84.matchmaking.Matchmaking;
import com.nikvs84.matchmaking.MatchmakingCallbacks;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junitx.framework.ListAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    private Matchmaking matchmaking;
    private List<Player> allPlayers;
    private List<Player[]> parties;
    private List<Player> cancelled;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MatchmakingCallbacks callbacks = new MatchmakingCallbacks() {
            @Override
            public void onMatched(Player[] players) {
                parties.add(players);
            }

            @Override
            public void onCancel(Player player) {
                cancelled.add(player);
            }
        };

        allPlayers = new ArrayList();
        parties = new ArrayList<>();
        cancelled = new ArrayList<>();

        matchmaking = new Matchmaking(callbacks, 2, 1, 1, 5000, 60000);
        matchmaking.startMatchMaking();

        for (int i = 1; i < 11; i++) {
            Player player = new Player(i, 10 + i);
            allPlayers.add(player);
            sleep(i * 300);
            matchmaking.addRequest(player);
        }
    }

    public void testMatchQuery() {
        ListAssert.assertEquals(matchmaking.getMatchQuery().getAllPlayers(), allPlayers);
        matchmaking.update(new Date().getTime());
        matchmaking.onCancel(allPlayers.get(5));
        assertTrue(allPlayers.get(5) == cancelled.get(0));
        printParties(parties);
        List<Player[]> partiesAssert = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            partiesAssert.add(new Player[]{allPlayers.get(2 * i), allPlayers.get(2 * i + 1)});
        }
        printParties(partiesAssert);
        assertTrue(equalsParties(partiesAssert, parties));
    }

    private boolean equalsParties(List<Player[]> parties1, List<Player[]> parties2) {
        boolean result = true;
        for (Player[] players : parties1) {
            if (!containsInParties(players, parties2)) {
                result = false;
            }
        }

        return result;
    }

    private boolean containsInParties(Player[] party, List<Player[]> parties) {
        boolean result = false;
        for (Player[] players : parties) {
            if (Arrays.equals(players, party)) {
                result = true;
                break;
            }
        }

        return result;
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printParties(List<Player[]> parties) {
        System.out.println("Parties:");
        for (Player[] players : parties) {
            for (Player p : players) {
                System.out.println(p);
            }
            System.out.println("==================");
        }
    }

}
