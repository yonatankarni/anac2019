package ddejonge.bandana;

import ddejonge.bandana.tournament.MyTournamentRunner;
import ddejonge.bandana.tournament.TournamentRunnerFirstStage;
import org.junit.Test;

import java.io.IOException;

public class MyTournamentRunnerTest {
  @Test
  public void testRunTournamentOneOfSeven() throws IOException {
    MyTournamentRunner mtr = new MyTournamentRunner();
    mtr.runTournament();
  }

  @Test
  public void testRunTournamentFourVsThree() throws IOException {
    TournamentRunnerFirstStage mtr = new TournamentRunnerFirstStage();
    mtr.runTournament();
  }
}
