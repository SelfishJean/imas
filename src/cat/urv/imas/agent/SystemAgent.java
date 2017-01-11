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
import cat.urv.imas.behaviour.system.RequestResponseBehaviour;
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
       

        Map m = this.game.getAgentList();
        Set<AgentType> s = m.keySet();
        int numS = 1;
        int numH = 1;
        for (AgentType at : s){
            List<Cell> cells = (List<Cell>)m.get(at);
            
            if (at.toString().equals("SCOUT")){
                for (Cell c : cells)
                    createAgent(this.getContainerController(), "Scout_"+(numS++), Scout.class.getName(), new Object[]{c});
                    
            }else{
                for (Cell c : cells)
                    createAgent(this.getContainerController(), "Harvester_"+(numH++), Harvester.class.getName(), new Object[]{c});
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

        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));
        
        
	this.addBehaviour(new MainLoopBehaviour(this, 1000));
		
	System.out.println("Setup finished\n");
        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
        
  
        Cell [][] x;
        x = game.getMap();
        
        
        System.out.println(x.length);
        
        System.out.println(x[3][3]);
        
        System.out.println(x[16][9].getCellType());
        
        moveAgent();
        
    }
    
    public void moveAgent() {
        InfoAgent info, info2;
        boolean is, found;
        int i,j, ri, rj, end, imax, jmax;
        end = 0;
        Random rand = new Random();

        Cell [][] mapa;
        mapa = game.getMap();
        imax = mapa.length;
        jmax = mapa[0].length;
        Map listOfAgents = game.getAgentList();
        Set<AgentType> setOfAgents = listOfAgents.keySet();
        HashMap newListOfAgents; // We need to create another list of agents and update it.
        newListOfAgents = new HashMap<AgentType, List<Cell>>();
        List<Cell> newCells; // The list of agents is a map of AgentType and list of cells.
        StreetCell ce;

        for (AgentType at : setOfAgents){
        List<Cell> cells = (List<Cell>)listOfAgents.get(at);  
        newCells = new ArrayList<Cell>(); // For every agentType we change the list of cells.
            for (Cell c : cells){
                found = false;
                i = c.getRow();
                j = c.getCol();
                //is = x[i][j].isThereAnAgent();
                if (mapa[i][j] instanceof StreetCell) {
                    info = ((StreetCell)mapa[i][j]).getAgent();
                    info2 = ((StreetCell)mapa[i][j]).getAgent();
                    is = ((StreetCell)mapa[i][j]).isThereAnAgent();
                    System.out.println(is);
                    System.out.println(info);
                    try {
                        ((StreetCell)mapa[i][j]).removeAgent(info);
                    }catch(Exception e){
                        //System.err.println(e);
                    }
                    is = ((StreetCell)mapa[i][j]).isThereAnAgent();
                    System.out.println(is);

                    while (!found) {
                        ri  = rand.nextInt(imax) + 0;
                        rj  = rand.nextInt(jmax) + 0;
                        //*max is the maximum and the 0 is our minimum 
                        if (mapa[ri][rj].getCellType().toString() == "STREET" && !((StreetCell)mapa[ri][rj]).isThereAnAgent()) {
                            found = true;
                            try {
                                ((StreetCell)mapa[ri][rj]).addAgent(info2);
                            }catch(Exception e){
                            //System.err.println(e);
                            }
                            newCells.add(mapa[ri][rj]);
                        }
                    }
                }
            }
            newListOfAgents.put(at, newCells);
        }  
        game.setAgentList(newListOfAgents);
        end++;
    }
    
    
    private class MainLoopBehaviour extends TickerBehaviour {

		public MainLoopBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {		
                   moveAgent();
                }
    }
    
    public void updateGUI() {
        this.gui.updateGame();
    }

}
