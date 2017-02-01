/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;

import cat.urv.imas.behaviour.scoutCoordinator.*;
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
        Harvester agent = (Harvester) this.getAgent();
        agent.log("GenerationNewPositionsBehaviour starting...");
        changeAgentsPositions();

    }
    

    public void changeAgentsPositions() { // It changes position of agent randomly.
        InfoAgent info, infoCopy;
        boolean /*is*,*/ found;
        int i,j, ri, rj, imax, jmax, movement;
        Random rand = new Random();

        Cell [][] mapa;
        Harvester agent = (Harvester) this.getAgent();
        
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
        if (at.toString() == "HARVESTER") 
        {
            //System.out.println("(GenerateNewPosition) "+at);
            for (Cell c : cells){
                i = c.getRow();
                j = c.getCol();
                //is = x[i][j].isThereAnAgent();
                if (mapa[i][j] instanceof StreetCell) {
                    info = ((StreetCell)mapa[i][j]).getAgent();
                    infoCopy = ((StreetCell)mapa[i][j]).getAgent();
                    
                    if (info.getAID().equals(agent.getAID()))
                    {
                        while (!found) {
                            movement = Math.abs(rand.nextInt(4)) + 1; // 4 is the maximum and 1 is our minimum

                            switch(movement)
                            {
                                case 1: // UP
                                    ri = i-1;
                                    rj = j;
                                    break;
                                case 2: // DOWN
                                    ri = i+1;
                                    rj = j;
                                    break;
                                case 3: // RIGHT
                                    ri = i;
                                    rj = j+1;
                                    break;
                                case 4: // LEFT
                                    ri = i;
                                    rj = j-1;
                                    break;
                                default: // This case should never happen but it is necessary to initialize ri and rj.
                                    ri = 0;
                                    rj= 0;
                            }

                            if (mapa[ri][rj].getCellType().toString() == "STREET" && !((StreetCell)mapa[ri][rj]).isThereAnAgent()) {
                                found = true;

                                infoCopy.setRow(ri);
                                infoCopy.setColumn(rj);
                                infoCopy.setPreRow(i);
                                infoCopy.setPreColumn(j);
                                agent.setNewInfoAgent(infoCopy);
                            }
                        }
                        found = false;
                    }

                }
            }
        }
        }
    }
    
    
    public void changeAgentsPositions2(){ // It changes positions randomly but cell by cell (moving UP, DOWN, RIGHT OR LEFT)
        InfoAgent info, infoCopy;
        boolean /*is*,*/ found;
        int i,j, ri, rj, imax, jmax, movement;
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
                        movement = Math.abs(rand.nextInt(4)) + 1; // 4 is the maximum and 1 is our minimum
                        
                        switch(movement)
                        {
                            case 1: // UP
                                ri = i-1;
                                rj = j;
                                break;
                            case 2: // DOWN
                                ri = i+1;
                                rj = j;
                                break;
                            case 3: // RIGHT
                                ri = i;
                                rj = j+1;
                                break;
                            case 4: // LEFT
                                ri = i;
                                rj = j-1;
                                break;
                            default: // This case should never happen but it is necessary to initialize ri and rj.
                                ri = 0;
                                rj= 0;
                        }
                        
                        if (mapa[ri][rj].getCellType().toString() == "STREET" && !((StreetCell)mapa[ri][rj]).isThereAnAgent()) {
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
