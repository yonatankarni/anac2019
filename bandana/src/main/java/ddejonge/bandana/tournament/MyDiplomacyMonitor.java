//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ddejonge.bandana.tournament;

import ddejonge.bandana.tools.DiplomacyMonitor;
import es.csic.iiia.fabregues.dip.board.Phase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

class MyDiplomacyMonitor extends DiplomacyMonitor {
  private final Logger logger = Logger.getLogger(MyDiplomacyMonitor.class.getSimpleName());
  private int currentGameNumber;
  private int numberOfGames;
  private Phase phase;
  private int year;
  private HashMap<String, String> power2agentName = new HashMap<>();
  private HashMap<String, Integer> power2numSupplyCenters = new HashMap<>();
  private TournamentResult tournamentResult;
  private ArrayList<ScoreCalculator> scoreCalculators;
  private String status;

  MyDiplomacyMonitor(int numParticipants, ArrayList<ScoreCalculator> scoreCalculators) {
    super("", numParticipants, scoreCalculators);
    this.power2agentName.put("AUS", null);
    this.power2agentName.put("ENG", null);
    this.power2agentName.put("FRA", null);
    this.power2agentName.put("GER", null);
    this.power2agentName.put("ITA", null);
    this.power2agentName.put("RUS", null);
    this.power2agentName.put("TUR", null);
    this.scoreCalculators = scoreCalculators;
    String[] columnNames = new String[scoreCalculators.size() + 1];
    columnNames[0] = "Agent";

    for (int i = 1; i < columnNames.length; ++i) {
      columnNames[i] = scoreCalculators.get(i - 1).getScoreSystemName();
    }

    Object[][] data = new Object[numParticipants][columnNames.length];

    for (int row = 0; row < data.length; ++row) {
      data[row][0] = "?";

      for (int i = 1; i < columnNames.length; ++i) {
        data[row][i] = "0";
      }
    }
  }

  public void update() {
    super.update();
    StringBuilder sb = new StringBuilder();
    sb.append("Game: " + this.currentGameNumber + "/" + this.numberOfGames);
    sb.append(System.lineSeparator());
    sb.append("Phase: " + this.year + " " + this.phase);
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());
    int longestNameSize = 1;
    Iterator it = this.power2agentName.keySet().iterator();

    String power;
    String name;
    while (it.hasNext()) {
      power = (String) it.next();
      name = this.power2agentName.get(power);
      if (name != null && name.length() > longestNameSize) {
        longestNameSize = name.length();
      }
    }

    it = this.power2agentName.keySet().iterator();

    while (it.hasNext()) {
      power = (String) it.next();
      name = this.power2agentName.get(power);
      if (name == null) {
        name = "?";
      }

      sb.append(name);

      int numSCs;
      for (numSCs = 0; numSCs < longestNameSize - name.length(); ++numSCs) {
        sb.append(" ");
      }

      numSCs = this.power2numSupplyCenters.get(power);
      sb.append("     " + power);
      if (numSCs < 10) {
        sb.append(" ");
      }

      sb.append("    " + numSCs);
      sb.append(System.lineSeparator());
    }

    sb.append(System.lineSeparator());
    sb.append("Status: " + this.status);
    if (this.tournamentResult != null) {
      sb.append(System.lineSeparator());
      if (this.tournamentResult.gameResults.size() == this.numberOfGames) {
        sb.append(System.lineSeparator());
        sb.append("TOURNAMENT FINISHED!");
      }
    }

    logger.info(sb.toString());
  }

  public void setTournamentStandings() {
    super.setTournamentStandings();

    if (this.tournamentResult == null) {
      return;
    }

    String headerRow = "agent";
    for (ScoreCalculator scoreCalculator : this.scoreCalculators) {
      headerRow = headerRow + "\t" + scoreCalculator.getClass().getSimpleName();
    }

    ArrayList<String> sortedNames = this.tournamentResult.sortNames(this.scoreCalculators);

    String powerSummaries = "";

    for (String name : sortedNames) {
      powerSummaries = powerSummaries + "\n" + name;
//      this.table.setValueAt(name, row, 0);

      for (ScoreCalculator scoreCalculator : this.scoreCalculators) {
        String score = scoreCalculator.getScoreString(name);
        powerSummaries = powerSummaries + "\t" + score;
//        this.table.setValueAt(score, row, j + 1);
      }
    }

    logger.info(headerRow + "\n" + powerSummaries);
  }

  public void setStatus(String status) {
    super.setStatus(status);
    this.status = status;
  }

  public void setNumSCs(String powerName, int numSCs) {
    super.setNumSCs(powerName, numSCs);
    this.power2numSupplyCenters.put(powerName, numSCs);
  }

  public void setPhase(Phase phase, int year) {
    super.setPhase(phase, year);
    this.phase = phase;
    this.year = year;
  }

  public void setCurrentGameNumber(int currentGameNumber) {
    super.setCurrentGameNumber(currentGameNumber);
    this.currentGameNumber = currentGameNumber;
  }

  public void setNumGames(int numGames) {
    super.setNumGames(numGames);
    this.numberOfGames = numGames;
  }

  public void setAgentName(String powerName, String agentName) {
    super.setAgentName(powerName, agentName);
    this.power2agentName.put(powerName, agentName);
  }

  public void setTournamentResult(TournamentResult tournamentResult) {
    super.setTournamentResult(tournamentResult);
    this.tournamentResult = tournamentResult;
    this.setTournamentStandings();
  }
}
