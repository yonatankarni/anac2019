package ddejonge.bandana.tournament;

import ddejonge.bandana.tools.ProcessRunner;

import java.io.File;

public class TournamentRunnerFirstStage extends MyTournamentRunner {
  Process createPlayerProcess(int finalYear, String tournamentLogFolderPath, int gameNumber, int playerIdx) {
    String name;
    String[] command;

    //make sure that each player has a different name.
    if (playerIdx < 1) {
      name = "OurAgent" + playerIdx;
      if ("true".equals(System.getProperty("debug"))) {
        command = ourAgentCommandWithDebug;
      } else {
        command = ourAgentCommand;
      }
    } else if (playerIdx < 4) {
      name = "OurAgent" + playerIdx;
      command = ourAgentCommand;
    } else {
      name = "D-Brane " + playerIdx;
      command = dbrane_1_1_Command;
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
