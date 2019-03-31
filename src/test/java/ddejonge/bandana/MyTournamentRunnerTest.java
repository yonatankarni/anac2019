package ddejonge.bandana;

import ddejonge.bandana.tournament.MyTournamentRunner;
import org.junit.Test;

import java.io.IOException;

public class MyTournamentRunnerTest {
  @Test
  public void testRunTournament() throws IOException {
    MyTournamentRunner mtr = new MyTournamentRunner();
    mtr.runTournament();
  }
}
