/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.harvesterAgent.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.core.*;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

public class Harvester extends ImasAgent {

    /**
     * Game settings in use.
     */
    public GameSettings game;
    /**
     * Harvester Coordinator id.
     */
    public AID harvesterCoordinator;
    /**
     * Agent state {FREE || MOVING || COLLECTING || DUMPING}.
     */
    private String state;
    /**
     * New goal Cell (with garbage to collect or a recycling center to dump).
     */
    private Cell newGoalCell;
    
    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    /**
     * Update the state.
     *
     * @param state current state of the agent.
     */
    public void setCurrentAgentState(String temp) {
        this.state = temp;
    }

    /**
     * Gets the current state of the agent.
     *
     * @return the current state of the agent.
     */
    public String getCurrentAgentState() {
        return this.state;
    }

    /**
     * Update the value of the Cell we want to go (to collect or to dump garbage).
     *
     * @param newGoalCell
     */
    public void setNewGoalCell(Cell temp) {
        this.newGoalCell = temp;
    }

    /**
     * Gets the value of the Cell we want to go (to collect or to dump garbage).
     *
     * @return the value of the Cell we want to go (to collect or to dump garbage).
     */
    public Cell getNewGoalCell() {
        return this.newGoalCell;
    }
    
    /**
     * Builds the coordinator agent.
     */
    public Harvester() {
        super(AgentType.HARVESTER);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.HARVESTER.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }

        // search HarvesterCoordinator
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.HARVESTER_COORDINATOR.toString());
        this.harvesterCoordinator = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID


        // We set the current state of the Agent as FREE.
        this.setCurrentAgentState(MessageContent.FREE);
        
        // Finite State Machine
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                            System.out.println("(HarvesterAgent) FSM behaviour completed.");
                            myAgent.doDelete();
                            return super.onEnd();
                    }
        };
        
        fsm.registerFirstState(new WaitingForMapBehaviour(this), "STATE_1");
        fsm.registerState(new InformStateAgentBehaviour(this), "STATE_2");
        fsm.registerState(new WaitingForMapBehaviour(this), "STATE_3");
        
        fsm.registerDefaultTransition("STATE_1", "STATE_2");
        fsm.registerTransition("STATE_2", "STATE_2", 0);
        fsm.registerTransition("STATE_2", "STATE_3", 1, new String[] {"STATE_3"});
        fsm.registerDefaultTransition("STATE_3", "STATE_2");

        this.addBehaviour(fsm);
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }
}