package ddejonge.bandana.tournament;

import ddejonge.bandana.tools.Logger;
import ddejonge.bandana.tools.ProcessRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MyTournamentRunner {

  //Command lines to start the various agents provided with the Bandana framework.
  // Add your own line here to run your own bot.
  private static final String[] randomNegotiatorCommand = {"java", "-jar", "agents/RandomNegotiator.jar", "-log", "log", "-name", "RandomNegotiator", "-fy", "1905"};
  private static final String[] dumbBot_1_4_Command = {"java", "-jar", "agents/DumbBot-1.4.jar", "-log", "log", "-name", "DumbBot", "-fy", "1905"};
  static final String[] dbrane_1_1_Command = {"java", "-jar", "agents/D-Brane-1.1.jar", "-log", "log", "-name", "D-Brane", "-fy", "1905"};
  private static final String[] dbraneExampleBotCommand = {"java", "-jar", "agents/D-BraneExampleBot.jar", "-log", "log", "-name", "DBraneExampleBot", "-fy", "1905"};

  static final String[] biu3141NegotiatorCommand = {"java", "-jar", "agents/biu3141Negotiator-spring-boot.jar", "-log", "log", "-name", "biu3141Negotiator", "-fy", "1905"};
  static final String[] biu3141NegotiatorCommandWithDebug = {"java", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,address=5005", "agents/biu3141Negotiator-spring-boot.jar", "-log", "log", "-name", "biu3141Negotiator", "-fy", "1905"};

  //Main folder where all the logs are stored. For each tournament a new folder will be created inside this folder
  // where the results of the tournament will be logged.
  private final String LOG_FOLDER = "log";


  public void runTournament() throws IOException {
    this.runTournament(3, 1905);
  }

  public void runTournament(final int numberOfGames, final int finalYear) throws IOException {
    int deadlineForMovePhases = 60;  //60 seconds for each SPR and FAL phases
    int deadlineForRetreatPhases = 30;  //30 seconds for each SUM and AUT phases
    int deadlineForBuildPhases = 30;    //30 seconds for each WIN phase

    run(numberOfGames, deadlineForMovePhases, deadlineForRetreatPhases, deadlineForBuildPhases, finalYear);
    Runtime.getRuntime().addShutdownHook(new Thread() {

      //NOTE: unfortunately, Shutdownhooks don't work on windows if the program was started in eclipse and
      // you stop it by clicking the red button (on MAC it seems to work fine).

      @Override
      public void run() {
        NegoServerRunner.stop();
        ParlanceRunner.stop();
      }
    });
  }


  private static List<Process> players = new ArrayList<Process>();

  private void run(int numberOfGames, int moveTimeLimit, int retreatTimeLimit, int buildTimeLimit, int finalYear) throws IOException {

    //Create a folder to store all the results of the tournament.
    // This folder will be placed inside the LOG_FOLDER and will have the current date and time as its name.
    // You can change this line if you prefer it differently.
    String tournamentLogFolderPath = LOG_FOLDER + File.separator + Logger.getDateString();
    File logFile = new File(tournamentLogFolderPath);
    logFile.mkdirs();


    //1. Run the Parlance game server.
    ParlanceRunner.runParlanceServer(numberOfGames, moveTimeLimit, retreatTimeLimit, buildTimeLimit);

    //Create a list of ScoreCalculators to determine how the players should be ranked in the tournament.
    ArrayList<ScoreCalculator> scoreCalculators = new ArrayList<ScoreCalculator>();
    scoreCalculators.add(new SoloVictoryCalculator());
    scoreCalculators.add(new SupplyCenterCalculator());
    scoreCalculators.add(new PointsCalculator());
    scoreCalculators.add(new RankCalculator());

    //2. Create a TournamentObserver to monitor the games and accumulate the results.
    MyTournamentObserver tournamentObserver = new MyTournamentObserver(tournamentLogFolderPath, scoreCalculators, numberOfGames, 7);

    //3. Run the Negotiation Server.
    NegoServerRunner.run(tournamentObserver, tournamentLogFolderPath, numberOfGames);

    for (int gameNumber = 1; gameNumber <= numberOfGames; gameNumber++) {

      System.out.println();
      System.out.println("GAME " + gameNumber);

      NegoServerRunner.notifyNewGame(gameNumber);

      //4. Start the players:
      for (int i = 0; i < 7; i++) {
        Process playerProcess = createPlayerProcess(finalYear, tournamentLogFolderPath, gameNumber, i);


        //store the Process object in a list.
        players.add(playerProcess);
      }

      //5. Let the tournament observer (re-)connect to the game server.
      tournamentObserver.connectToServer();


      //NOW WAIT TILL THE GAME IS FINISHED
      while (tournamentObserver.getGameStatus() == TournamentObserver.GAME_ACTIVE || tournamentObserver.getGameStatus() == TournamentObserver.CONNECTED_WAITING_TO_START) {

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        if (tournamentObserver.playerFailed()) {
          // One or more players did not send its orders in in time.
          //
        }

      }

      //Kill the player processes.
      // (if everything is implemented okay this isn't necessary because the players should kill themselves. But just to be sure..)
      for (Process playerProces : players) {
        playerProces.destroy();
      }

    }

    System.out.println("TOURNAMENT FINISHED");

    //Get the results of all the games played in this tournament.
    // Each GameResult object contains the results of one game.
    // The tournamentObserver already automatically prints these results to a text file,
    //  as well as the processed overall results of the tournament.
    // However, you may want to do your own processing of the results, for which
    // you can use this list.
    ArrayList<GameResult> results = tournamentObserver.getGameResults();

    tournamentObserver.exit();
    ParlanceRunner.stop();
    NegoServerRunner.stop();
  }

  Process createPlayerProcess(int finalYear, String tournamentLogFolderPath, int gameNumber, int playerIdx) {
    String name;
    String[] command;

    //make sure that each player has a different name.
    if (playerIdx < 1) {
      name = "biu3141Negotiator" + playerIdx;
      if ("true".equals(System.getProperty("debug"))) {
        command = biu3141NegotiatorCommandWithDebug;
      } else {
        command = biu3141NegotiatorCommand;
      }
    } else if (playerIdx < 2) {
      name = "D-Brane " + playerIdx;
      command = dbrane_1_1_Command;
    } else if (playerIdx < 4) {
      name = "D-BraneExampleBot " + playerIdx;
      command = dbraneExampleBotCommand;
    } else if (playerIdx < 6) {
      name = "RandomNegotiator " + playerIdx;
      command = randomNegotiatorCommand;
    } else {
      name = "DumbBot " + playerIdx;
      command = dumbBot_1_4_Command;
    }

    //set the log folder for this agent to be a subfolder of the tournament log folder.
    command[4] = tournamentLogFolderPath + File.separator + name + File.separator + "Game " + gameNumber + File.separator;

    //set the name of the agent.
    command[6] = name;

    //set the year after which the agent will propose a draw to the other agents.
    command[8] = "" + finalYear;

    //start the process
    String processName = name;
    // We give  a name to the process so that we can see in the console where its output comes from.
    // This name does not have to be the same as the name given to the agent, but it would be confusing
    // to do otherwise.
    return ProcessRunner.exec(command, processName);
  }
}
