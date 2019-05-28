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
  public void testRunTournamentOneOfSevenLong() throws IOException {
    MyTournamentRunner mtr = new MyTournamentRunner();
    mtr.runTournament(20, 1905);
  }

  @Test
  public void testRunTournamentFourVsThree() throws IOException {
    TournamentRunnerFirstStage mtr = new TournamentRunnerFirstStage();
    mtr.runTournament();
  }

  @Test
  public void testRunTournamentFourVsThreeLong() throws IOException {
    TournamentRunnerFirstStage mtr = new TournamentRunnerFirstStage();
    mtr.runTournament(20, 1917);
  }
}
