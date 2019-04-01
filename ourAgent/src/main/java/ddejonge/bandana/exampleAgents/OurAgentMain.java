package ddejonge.bandana.exampleAgents;

import java.io.File;

public class OurAgentMain {
  public static void main(String[] args){
    //set the default name, game server port, and log path for the agent.
    String name = "Star Butterfly";
    String logPath = "log/";
    int gameServerPort = OurAgent.DEFAULT_GAME_SERVER_PORT;
    int negoPort = OurAgent.DEFAULT_NEGO_SERVER_PORT;
    int finalYear = OurAgent.DEFAULT_FINAL_YEAR;

    //Overwrite these values if specified by the arguments.
    for(int i=0; i<args.length; i++){

      //set the name of this agent
      if(args[i].equals("-name") && args.length > i+1){
        name = args[i+1];
      }

      //set the path to store the log file
      if(args[i].equals("-log") && args.length > i+1){
        logPath = args[i+1];
      }

      //set the final year
      if(args[i].equals("-fy") && args.length > i+1){
        try{
          finalYear = Integer.parseInt(args[i+1]);
        }catch (NumberFormatException e) {
          System.out.println("main() The final year argument is not a valid integer: " + args[i+1]);
          return;
        }
      }

      //set the port number of the game server
      if(args[i].equals("-gamePort") && args.length > i+1){

        try{
          gameServerPort = Integer.parseInt(args[i+1]);
        }catch (NumberFormatException e) {
          System.out.println("The port number argument is not a valid integer: " + args[i+1]);
          return;
        }
      }

      //set the port number of the negotiation server
      if(args[i].equals("-negoPort") && args.length > i+1){

        try{
          negoPort = Integer.parseInt(args[i+1]);
        }catch (NumberFormatException e) {
          System.out.println("The port number argument is not a valid integer: " + args[i+1]);
        }
      }

    }

    //Create the folder to store its log files.
    File logFolder = new File(logPath);
    logFolder.mkdirs();

    OurAgent ourAgent = new OurAgent(name, finalYear, logPath, gameServerPort, negoPort);

    //Connect to the game server.
    try{
      ourAgent.start(ourAgent.getComm());
    }catch (Exception e) {
      e.printStackTrace();
    }


    //Make sure the log file is written to hard disk when the agent is shut down.
    final OurAgent rn = ourAgent;
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        rn.getLogger().writeToFile();
      }
    }));
  }
}
