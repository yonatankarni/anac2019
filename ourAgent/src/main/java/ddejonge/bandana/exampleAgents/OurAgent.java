package ddejonge.bandana.exampleAgents;

import ddejonge.bandana.anac.ANACNegotiator;
import ddejonge.bandana.dbraneTactics.DBraneTactics;
import ddejonge.bandana.dbraneTactics.Plan;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyNegoClient;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.orders.HLDOrder;
import es.csic.iiia.fabregues.dip.orders.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class OurAgent extends ANACNegotiator {

  private Random random = new Random();
  DBraneTactics dBraneTactics;

  private boolean sentSync = false;
  private final List<Power> sameAgentCoalition = new LinkedList<>();
  //Constructor

  /**
   * You must implement a Constructor with exactly this signature.
   * The body of the Constructor must start with the line <code>super(args)</code>
   * but below that line you can put whatever you like.
   *
   * @param args
   */
  public OurAgent(String[] args) {
    super(args);

    dBraneTactics = this.getTacticalModule();
  }


  /**
   * This method is automatically called at the start of the game, after the 'game' field is set.
   * <p>
   * It is called when the first NOW message is received from the game server.
   * The NOW message contains the current phase and the positions of all the units.
   * <p>
   * You are allowed, but not required, to implement this method
   */
  @Override
  public void start() {

    //You can use the logger to write stuff to the log file.
    //The location of the log file can be set through the command line option -log.
    // it is not necessary to call getLogger().enable() because this is already automatically done by the ANACNegotiator class.

    boolean printToConsole = true; //if set to true the text will be written to file, as well as printed to the standard output stream. If set to false it will only be written to file.
    this.getLogger().logln("game is starting!", printToConsole);

  }


  @Override
  public void negotiate(long negotiationDeadline) {
    BasicDeal newDealToPropose = null;


    //This loop repeats 2 steps. The first step is to handle any incoming messages,
    // while the second step tries to find deals to propose to the other negotiators.
    while (System.currentTimeMillis() < negotiationDeadline) {


      //STEP 1: Handle incoming messages.


      //See if we have received any message from any of the other negotiators.
      // e.g. a new proposal or an acceptance of a proposal made earlier.
      while (hasMessage()) {
        //Warning: you may want to add some extra code to break out of this loop,
        // just in case the other agents send so many proposals that your agent can't get
        // the chance to make any proposals itself.

        //if yes, remove it from the message queue.
        Message receivedMessage = removeMessageFromQueue();

        String senderName = receivedMessage.getSender();
        if (receivedMessage.getPerformative().equals(DiplomacyNegoClient.ACCEPT)) {

          DiplomacyProposal acceptedProposal = (DiplomacyProposal) receivedMessage.getContent();

          this.getLogger().logln("ANACExampleNegotiator.negotiate() Received acceptance from " + senderName + ": " + acceptedProposal, true);

          // Here we can handle any incoming acceptances.
          // This random negotiator doesn't do anything with such messages however.

          // Note: if a certain proposal has been accepted by all players it is still not considered
          // officially binding until the protocol manager has sent a CONFIRM message.

          // Note: if all agents involved in a proposal have accepted the proposal, then you will not receive an ACCEPT
          // message from the last agent that accepted it. Instead, you will directly receive a CONFIRM message from the
          // Protocol Manager.

        } else if (receivedMessage.getPerformative().equals(DiplomacyNegoClient.PROPOSE)) {

          DiplomacyProposal receivedProposal = (DiplomacyProposal) receivedMessage.getContent();

          this.getLogger().logln("ANACExampleNegotiator.negotiate() Received proposal: " + receivedProposal, true);

          BasicDeal deal = (BasicDeal) receivedProposal.getProposedDeal();

          // check if this is a sync proposal
          if (deal.getDemilitarizedZones().size() == game.getProvinces().size()) {
            sameAgentCoalition.add(game.getPower(receivedMessage.getSender()));
            this.rejectProposal(receivedProposal.getId());
            continue;
          }

          boolean outDated = false;

          for (DMZ dmz : deal.getDemilitarizedZones()) {

            // Sometimes we may receive messages too late, so we check if the proposal does not
            // refer to some round of the game that has already passed.
            if (isHistory(dmz.getPhase(), dmz.getYear())) {
              outDated = true;
              break;
            }

            //TODO: decide whether this DMZ is acceptable or not (in combination with the rest of the proposed deal).
						/*
						List<Power> powers = dmz.getPowers();
						List<Province> provinces = dmz.getProvinces();
						*/

          }
          for (OrderCommitment orderCommitment : deal.getOrderCommitments()) {


            // Sometimes we may receive messages too late, so we check if the proposal does not
            // refer to some round of the game that has already passed.
            if (isHistory(orderCommitment.getPhase(), orderCommitment.getYear())) {
              outDated = true;
              break;
            }

            //TODO: decide whether this order commitment is acceptable or not (in combination with the rest of the proposed deal).
            /*Order order = orderCommitment.getOrder();*/
          }

          //If the deal is not outdated, then check that it is consistent with the deals we are already committed to.
          String consistencyReport = null;
          if (!outDated) {

            List<BasicDeal> commitments = new ArrayList<BasicDeal>();
            commitments.addAll(this.getConfirmedDeals());
            commitments.add(deal);
            consistencyReport = Utilities.testConsistency(game, commitments);
          }

          if (!outDated && consistencyReport == null) {
            if (senderInCoalition(senderName)) {
              this.acceptProposal(receivedProposal.getId());
              this.getLogger().logln("ANACExampleNegotiator.negotiate()  Accepting: " + receivedProposal, true);
            }
          }

        } else if (receivedMessage.getPerformative().equals(DiplomacyNegoClient.CONFIRM)) {

          // The protocol manager confirms that a certain proposal has been accepted by all players involved in it.
          // From now on we consider the deal as a binding agreement.

          DiplomacyProposal confirmedProposal = (DiplomacyProposal) receivedMessage.getContent();

          this.getLogger().logln("ANACExampleNegotiator.negotiate() RECEIVED CONFIRMATION OF: " + confirmedProposal, true);

          BasicDeal confirmedDeal = (BasicDeal) confirmedProposal.getProposedDeal();


          //Reject any proposal that has not yet been confirmed and that is inconsistent with the confirmed deal.
          // NOTE that normally this is not really necessary because the Notary will already check that
          // any deal is consistent with earlier confirmed deals before it becomes confirmed.
          List<BasicDeal> deals = new ArrayList<BasicDeal>(2);
          deals.add(confirmedDeal);
          for (DiplomacyProposal standingProposal : this.getUnconfirmedProposals()) {

            //add this proposal to the list of deals.
            deals.add((BasicDeal) standingProposal.getProposedDeal());

            if (Utilities.testConsistency(game, deals) != null) {
              this.rejectProposal(standingProposal.getId());
            }

            //remove the deal again from the list, so that we can add the next standing deal to the list in the next iteration.
            deals.remove(1);
          }


        } else if (receivedMessage.getPerformative().equals(DiplomacyNegoClient.REJECT)) {

          DiplomacyProposal rejectedProposal = (DiplomacyProposal) receivedMessage.getContent();

          // Some player has rejected a certain proposal.
          // This example agent doesn't do anything with such messages however.

          //If a player first accepts a proposal and then rejects the same proposal the reject message cancels
          // his earlier accept proposal.
          // However, this is not true if the reject message is sent after the Notary has already sent a confirm
          // message for that proposal. Once a proposal is confirmed it cannot be undone anymore.
        } else {

          //We have received any other kind of message.

          this.getLogger().logln("Received a message of unhandled type: " + receivedMessage.getPerformative() + ". Message content: " + receivedMessage.getContent().toString(), true);

        }

      }

      if (!sentSync) {
        sentSync = true;
        final List<OrderCommitment> orderCommitments = Collections.emptyList();
        final DMZ dmz = new DMZ(game.getYear(), game.getPhase(), game.getPowers(), game.getProvinces());
        BasicDeal syncDeal = new BasicDeal(orderCommitments, Collections.singletonList(dmz));
        this.proposeDeal(syncDeal);
      }

      //STEP 2:  try to find a proposal to make, and if we do find one, propose it.

      if (newDealToPropose == null) { //we only make one proposal per round, so we skip this if we have already proposed something.
        newDealToPropose = searchForNewDealToPropose();

        if (newDealToPropose != null) {

          this.getLogger().logln("ANACExampleNegotiator.negotiate() Proposing: " + newDealToPropose, true);
          this.proposeDeal(newDealToPropose);

        }
      }

      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
      }


    }

  //whenever you like, you can also propose a draw to all other surviving players:
    //this.proposeDraw();
  }

  private boolean senderInCoalition(final String senderName) {
    return sameAgentCoalition.contains(game.getPower(senderName));
  }


  private BasicDeal searchForNewDealToPropose() {

    //Get a copy of our list of current commitments.
    List<BasicDeal> commitments = this.getConfirmedDeals();

    //generate a deal.
    BasicDeal dealCandidate = generateDealCandidate();

    //add it to the list containing our existing commitments so that dBraneTactics can determine a plan.
    commitments.add(dealCandidate);


    //Ask the D-Brane Tactical Module what it would do under these commitments.
    final Plan plan = this.dBraneTactics.determineBestPlan(game, me, commitments);

    if (Objects.nonNull(plan)) {
      return dealCandidate;
    } else {
      return null;
    }
  }


  private BasicDeal generateDealCandidate() {
    //Get the names of all the powers that are connected to the negotiation server and which have not been eliminated.
    List<Power> aliveNegotiatingPowers = this.getNegotiatingPowers();

    //if there are less than 2 negotiating powers left alive (only me), then it makes no sense to negotiate.
    int numAliveNegoPowers = aliveNegotiatingPowers.size();
    if (numAliveNegoPowers < 2) {
      return null;
    }

    final List<OrderCommitment> myOrderCommitments = new ArrayList<>();


    //get all units of the negotiating powers.
    List<Region> units = new ArrayList<Region>();
    for (Power power : aliveNegotiatingPowers) {
      units.addAll(power.getControlledRegions());
    }

    final List<Power> allies = new LinkedList<>();
    int k = 0;
    for (final Power power: aliveNegotiatingPowers) {
      if (k < 3) {
        allies.add(power);
      }
      k = k + 1;
//      plan.get
      final Plan plan = this.dBraneTactics.determineBestPlan(this.game, power, this.getConfirmedDeals(), allies);

//      Plan plan = this.dBraneTactics.determineBestPlan(this.game, this.me, this.getConfirmedDeals(), allies);
//      System.out.println(plan.getMyOrders());
      for (final Order order : plan.getMyOrders()) {
        final OrderCommitment orderCommitment = new OrderCommitment(game.getYear(), game.getPhase(), order);
        myOrderCommitments.add(orderCommitment);
      }
    }

    final BasicDeal deal = new BasicDeal(myOrderCommitments, Collections.<DMZ>emptyList());

    return deal;

//    //Let's generate 3 random demilitarized zones.
//    List<DMZ> demilitarizedZones = new ArrayList<DMZ>(3);
////    for (int i = 0; i < 3; i++) {
////
////      //1. Create a list of powers
////      ArrayList<Power> powers = new ArrayList<Power>(2);
////
////      //1a. add myself to the list
////      powers.add(me);
////
////      //1b. add a random other power to the list.
////      Power randomPower = me;
////      while (randomPower.equals(me)) {
////
////        int numNegoPowers = aliveNegotiatingPowers.size();
////        randomPower = aliveNegotiatingPowers.get(random.nextInt(numNegoPowers));
////      }
////      powers.add(randomPower);
////
////      //2. Create a list containing 3 random provinces.
////      ArrayList<Province> provinces = new ArrayList<Province>();
////      for (int j = 0; j < 3; j++) {
////        int numProvinces = this.game.getProvinces().size();
////        Province randomProvince = this.game.getProvinces().get(random.nextInt(numProvinces));
////        provinces.add(randomProvince);
////      }
////
////
////      //This agent only generates deals for the current year and phase.
////      // However, you can pick any year and phase here, as long as they do not lie in the past.
////      // (actually, you can also propose deals for rounds in the past, but it doesn't make any sense
////      //  since you obviously cannot obey such deals).
////      demilitarizedZones.add(new DMZ(game.getYear(), game.getPhase(), powers, provinces));
////
////    }
//
//    //let's generate 3 random OrderCommitments
//    List<OrderCommitment> randomOrderCommitments = new ArrayList<OrderCommitment>();
//
//
//    //get all units of the negotiating powers.
//    List<Region> units = new ArrayList<Region>();
//    for (Power power : aliveNegotiatingPowers) {
//      units.addAll(power.getControlledRegions());
//    }
//
//
//    for (int i = 0; i < 3; i++) {
//
//      //Pick a random unit and remove it from the list
//      if (units.size() == 0) {
//        break;
//      }
//      Region randomUnit = units.remove(random.nextInt(units.size()));
//
//      //Get the corresponding power
//      Power power = game.getController(randomUnit);
//
//      //Determine a list of potential destinations for the unit.
//      // a Region is a potential destination for a unit if it is adjacent to that unit (or it is the current location of the unit)
//      //  and the Province is not demilitarized for the Power controlling that unit.
//      List<Region> potentialDestinations = new ArrayList<Region>();
//
//      //Create a list of adjacent regions, including the current location of the unit.
//      List<Region> adjacentRegions = new ArrayList<>(randomUnit.getAdjacentRegions());
//      adjacentRegions.add(randomUnit);
//
//      for (Region adjacentRegion : adjacentRegions) {
//
//        Province adjacentProvince = adjacentRegion.getProvince();
//
//        //Check that the adjacent Region is not demilitarized for the power controlling the unit.
//        boolean isDemilitarized = false;
//        for (DMZ dmz : demilitarizedZones) {
//          if (dmz.getPowers().contains(power) && dmz.getProvinces().contains(adjacentProvince)) {
//            isDemilitarized = true;
//            break;
//          }
//
//        }
//
//        //If it is not demilitarized, then we can add the region to the list of potential destinations.
//        if (!isDemilitarized) {
//          potentialDestinations.add(adjacentRegion);
//        }
//      }
//
//
//      int numPotentialDestinations = potentialDestinations.size();
//      if (numPotentialDestinations > 0) {
//
//        Region randomDestination = potentialDestinations.get(random.nextInt(numPotentialDestinations));
//
//        Order randomOrder;
//        if (randomDestination.equals(randomUnit)) {
//          randomOrder = new HLDOrder(power, randomUnit);
//        } else {
//          randomOrder = new MTOOrder(power, randomUnit, randomDestination);
//        }
//        // Of course we could also propose random support orders, but we don't do that here.
//
//
//        //We only generate deals for the current year and phase.
//        // However, you can pick any year and phase here, as long as they do not lie in the past.
//        // (actually, you can also propose deals for rounds in the past, but it doesn't make any sense
//        //  since you obviously cannot obey such deals).
//        randomOrderCommitments.add(new OrderCommitment(game.getYear(), game.getPhase(), randomOrder));
//      }
//
//    }
//
//    BasicDeal deal = new BasicDeal(randomOrderCommitments, demilitarizedZones);
//
//
//    return deal;

  }


  /**
   * Each round, after each power has submitted its orders, this method is called several times:
   * once for each order submitted by any other power.
   *
   * @param orderSubmittedByOtherPlayer An order submitted by any of the other powers.
   */
  @Override
  public void receivedOrder(Order orderSubmittedByOtherPlayer) {
    // TODO Auto-generated method stub

  }

}
