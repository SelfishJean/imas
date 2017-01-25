/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scoutCoordinator;

import cat.urv.imas.behaviour.coordinator.*;
import jade.core.behaviours.*;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import cat.urv.imas.onthology.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;




/**
 *
 * @author albertOlivares
 */
public class GenerateNewPositionsBehaviour extends SimpleBehaviour {
    
    public GenerateNewPositionsBehaviour(Agent a) {
        super(a);
    }
    @Override
    public void action() { 
        System.out.println("(ScoutCoordinator) GenerationNewPositionsBehaviour starting...");
        changeAgentsPositions();

    }
    

    public void changeAgentsPositions() {
        InfoAgent info, infoCopy;
        boolean /*is*,*/ found;
        int i,j, ri, rj, imax, jmax;
        Random rand = new Random();

        Cell [][] mapa;
        ScoutCoordinator agent = (ScoutCoordinator) this.getAgent();
        
        mapa = agent.getGame().getMap();
        imax = mapa.length;
        jmax = mapa[0].length;
        Map listOfAgents = agent.getGame().getAgentList();
        Set<AgentType> setOfAgents = listOfAgents.keySet();
        HashMap newListOfAgents; // We need to create another list of agents and update it.
        newListOfAgents = new HashMap<AgentType, List<Cell>>();
        List<Cell> newCells; // The list of agents is a map of AgentType and list of cells.
        StreetCell ce;
        ArrayList<InfoAgent> newPositions;
        newPositions = new ArrayList<InfoAgent>();
        found = false;
        
        for (AgentType at : setOfAgents){
        List<Cell> cells = (List<Cell>)listOfAgents.get(at);  
        newCells = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        if (at.toString() == "SCOUT") 
        {
            //System.out.println("(GenerateNewPosition) "+at);
            for (Cell c : cells){
                i = c.getRow();
                j = c.getCol();
                //is = x[i][j].isThereAnAgent();
                if (mapa[i][j] instanceof StreetCell) {
                    info = ((StreetCell)mapa[i][j]).getAgent();
                    //System.out.println(info.getAID());
                    infoCopy = ((StreetCell)mapa[i][j]).getAgent();
                    
                    while (!found) {
                        ri  = Math.abs(rand.nextInt(imax)) + 0;
                        rj  = Math.abs(rand.nextInt(jmax)) + 0;
                        //*max is the maximum and the 0 is our minimum 
                        if (mapa[ri][rj].getCellType().toString() == "STREET" && !((StreetCell)mapa[ri][rj]).isThereAnAgent() && ri!=i && rj!=j) {
                            found = true;
                            
                            
                            infoCopy.setRow(ri);
                            infoCopy.setColumn(rj);
                            infoCopy.setPreRow(i);
                            infoCopy.setPreColumn(j);
                            newPositions.add(infoCopy);
                            //System.out.println(newPositions.size());
                            //((StreetCell)mapa[ri][rj]).addAgent(infoCopy);

                        }
                    }
                    found = false;
                }
            }
        }
        }
        agent.setNewInfoAgent(newPositions);
    }
    
    @Override
    public boolean done() {
            return true;
    }
    
    @Override
    public int onEnd() {
            //System.out.println("End of"+getBehaviourName());
            return 0;
    }
}
