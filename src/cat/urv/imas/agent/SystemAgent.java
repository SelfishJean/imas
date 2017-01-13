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

import static cat.urv.imas.agent.UtilsAgents.createAgent;
import static cat.urv.imas.agent.UtilsAgents.searchAgent;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.InfoAgent;
import static cat.urv.imas.onthology.InitialGameSettings.H;
import static cat.urv.imas.onthology.InitialGameSettings.R;
import static cat.urv.imas.onthology.InitialGameSettings.S;
import static cat.urv.imas.onthology.InitialGameSettings.SC;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.util.ArrayList;
import jade.core.behaviours.*;
/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent {

    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private GameSettings game;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent;
    /**
     * InfoAgent. It contains the following information of all agents.
     * It has to be an ArrayList because List is not serializable.
     */
    public ArrayList<InfoAgent> newInfoAgent;
    /**
     * NewTurn. It is true when the Coordinator agent orders to update the map 
     * using the UpdateMapBehaviour.
     */
    public boolean newTurn = false;
    /**
     * SentMap. It is true when the Coordinator agent has already received the new map.
     */
    public boolean sentMap = false;
    

    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the 
     * standard output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName()+ ": " + log + "\n");
        }
        super.log(log);
    }
    
    /**
     * An error message is shown in the log area of the GUI, as well as in the 
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName()+ ": " + error + "\n");
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    /**
     * Gets the value of newTurn variable, which says if there is or not a new turn.
     *
     * @return newTurn.
     */
    public boolean getThereIsNewTurn() {
        return this.newTurn;
    }
    
    /**
     * Sets the value of newTurn variable, which says if there is or not a new turn.
     *
     */
    public void setThereIsNewTurn(boolean temp) {
        this.newTurn = temp;
    }
    
        /**
     * Gets the value of newMapPetition variable.
     *
     * @return newTurn.
     */
    public boolean getMapAlreadySent() {
        return this.sentMap;
    }
    
    /**
     * Sets the value of newMapPetition variable.
     *
     */
    public void setMapAlreadySent(boolean temp) {
        this.sentMap = temp;
    }
    
    /**
     * Gets the next information for all agents.
     *
     * @return info ALL agentS (is a list). It has to be an ArrayList because List is not serializable.
     */
    public ArrayList<InfoAgent> getNewInfoAgent() {
        return this.newInfoAgent;
    }
    
    /**
     * Update agent information.
     *
     * @param newInfo information of all agents for next simulation step.
     */
    public void setNewInfoAgent(ArrayList<InfoAgent> newInfo) {
        try {
            this.newInfoAgent.clear();
        } catch (Exception e) {
            
        }
        this.newInfoAgent = newInfo;
    }
    
    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);

        // 1. Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SYSTEM.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " failed registration to DF [ko]. Reason: " + e.getMessage());
            doDelete();
        }

        // 2. Load game settings.
        this.game = InitialGameSettings.load("game.settings");
        log("Initial configuration settings loaded");

        createAgent(this.getContainerController(), "Coordinator", CoordinatorAgent.class.getName(), null);
        createAgent(this.getContainerController(), "ScoutCoordinator", ScoutCoordinator.class.getName(), null);
        createAgent(this.getContainerController(), "HarvestCoordinator", HarvestCoordinator.class.getName(), null);
        
        
        // Scouts and Harvester agents initialization in JADE and setting of AID (to work with them).
        ServiceDescription searchCriterionSC = new ServiceDescription();
        searchCriterionSC.setType(AgentType.SCOUT.toString());
        ServiceDescription searchCriterionH = new ServiceDescription();
        searchCriterionH.setType(AgentType.HARVESTER.toString());
        String agentName = "";
        AID aid = null;
        
        InfoAgent info;
        Cell [][] mapa;
        mapa = game.getMap();
        int i, j;
        
        Map m = this.game.getAgentList(); 
        Set<AgentType> s = m.keySet();
        int numS = 1;
        int numH = 1;
        for (AgentType at : s){
            List<Cell> cells = (List<Cell>)m.get(at);
            
            if (at.toString().equals("SCOUT")){
                for (Cell c : cells) {
                    agentName = "Scout_"+(numS++);
                    createAgent(this.getContainerController(), agentName, Scout.class.getName(), new Object[]{c});
                    searchCriterionSC.setName(agentName);
                    aid = UtilsAgents.searchAgent(this, searchCriterionSC);
                    //System.out.println(aid);
                    
                    i = c.getRow();
                    j = c.getCol();
                    if (mapa[i][j] instanceof StreetCell) {
                        info = ((StreetCell)mapa[i][j]).getAgent();
                        info.setAID(aid);
                        info.setRow(i);
                        info.setColumn(j);
                    }
                }
                    
            }else{
                for (Cell c : cells) {
                    agentName = "Harvester_"+(numH++);
                    createAgent(this.getContainerController(), agentName, Harvester.class.getName(), new Object[]{c});
                    searchCriterionH.setName(agentName);
                    aid = UtilsAgents.searchAgent(this, searchCriterionH);
                    //System.out.println(aid);
                    
                    i = c.getRow();
                    j = c.getCol();
                    if (mapa[i][j] instanceof StreetCell) {
                        info = ((StreetCell)mapa[i][j]).getAgent();
                        info.setAID(aid);
                        info.setRow(i);
                        info.setColumn(j);
                    }
                }
            }
        }
       
        // 3. Load GUI
        try {
            this.gui = new GraphicInterface(game);
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        
        System.out.println("Setup almost finished\n");
        
        // Finite State Machine
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                            System.out.println("(SystemAgent) FSM behaviour completed.");
                            myAgent.doDelete();
                            return super.onEnd();
                    }
        };
        
        fsm.registerFirstState(new RequestResponseBehaviour(this), "STATE_1");
        //fsm.registerState(new UpdateMapBehaviour(this, NewStepRequest), "STATE_2");
        fsm.registerState(new UpdateMapResponseBehaviour(this), "STATE_2");
        fsm.registerState(new UpdateSimulationBehaviour(this), "STATE_3");
        
        
        
        fsm.registerDefaultTransition("STATE_1", "STATE_2");
        //fsm.registerDefaultTransition("STATE_2", "STATE_3");
        fsm.registerDefaultTransition("STATE_2", "STATE_3");
        fsm.registerDefaultTransition("STATE_3", "STATE_1");
        
        this.addBehaviour(fsm);
        
        
     

        
        
        
        
/*
        	// State names
	String STATE_A = "A";
	String STATE_B = "B";
	String STATE_C = "C";
	String STATE_D = "D";
	String STATE_E = "E";
	String STATE_F = "F";
        
*/	
	/**
	   Inner class NamePrinter.
	   This behaviour just prints its name
	 */
/*
	class NamePrinter extends OneShotBehaviour {
		public void action() {
			System.out.println("Executing behaviour "+getBehaviourName());
		}
	}
/*        
	/*
	   Inner class RandomGenerator.
	   This behaviour prints its name and exits with a random value
	   between 0 and a given integer value
	 */

/*	class RandomGenerator extends NamePrinter {
		private int maxExitValue;
		private int exitValue;
		
		private RandomGenerator(int max) {
			super();
			maxExitValue = max;
		}
		
		public void action() {
			System.out.println("Executing behaviour "+getBehaviourName());
			exitValue = (int) (Math.random() * maxExitValue);
			System.out.println("Exit value is "+exitValue);
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
	

		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour completed.");
				myAgent.doDelete();
				return super.onEnd();
			}
		};
		
		// Register state A (first state)
		fsm.registerFirstState(new NamePrinter(), STATE_A);
		
		// Register state B
		fsm.registerState(new NamePrinter(), STATE_B);
		
		// Register state C
		fsm.registerState(new RandomGenerator(3), STATE_C);
		
		// Register state D
		fsm.registerState(new NamePrinter(), STATE_D);
		
		// Register state E
		fsm.registerState(new RandomGenerator(4), STATE_E);
		
		// Register state F (final state)
		fsm.registerLastState(new NamePrinter(), STATE_F);

		// Register the transitions
		fsm.registerDefaultTransition(STATE_A, STATE_B);
		fsm.registerDefaultTransition(STATE_B, STATE_C);
		fsm.registerTransition(STATE_C, STATE_C, 0);
		fsm.registerTransition(STATE_C, STATE_D, 1);
		fsm.registerTransition(STATE_C, STATE_A, 2);
		fsm.registerDefaultTransition(STATE_D, STATE_E);
		fsm.registerTransition(STATE_E, STATE_F, 3);
		fsm.registerDefaultTransition(STATE_E, STATE_B);
		
		addBehaviour(fsm);
	
*/

	System.out.println("Setup finished\n");
        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions  
    }
    
    
    public void updateGUI() {
        this.gui.updateGame();
    }

}
