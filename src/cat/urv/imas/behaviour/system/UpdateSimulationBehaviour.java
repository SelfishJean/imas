/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.system;

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
public class UpdateSimulationBehaviour extends SimpleBehaviour {
    
    public UpdateSimulationBehaviour(Agent a) {
        super(a);
    }
    @Override
    public void action() { 
        System.out.println("(SysetmAgent) UpdateSimulationBehaviour starting...");
        moveAgents();
        

    }
    
     public void moveAgents() {
        InfoAgent info, infoCopy;
        boolean /*is*,*/ found;
        int i,j, ri, rj, imax, jmax;
        Random rand = new Random();

        Cell [][] mapa;
        SystemAgent agent = (SystemAgent)this.getAgent();
        
        mapa = agent.getGame().getMap();
        imax = mapa.length;
        jmax = mapa[0].length;
        Map listOfAgents = agent.getGame().getAgentList();
        Set<AgentType> setOfAgents = listOfAgents.keySet();
        HashMap newListOfAgents; // We need to create another list of agents and update it.
        newListOfAgents = new HashMap<AgentType, List<Cell>>();
        List<Cell> newCellsSC; // The list of agents is a map of AgentType and list of cells.
        List<Cell> newCellsH; // The list of agents is a map of AgentType and list of cells.
 
        newCellsSC = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        newCellsH = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        
        for (InfoAgent ag : agent.newInfoAgent) {
            i = ag.getPreRow();
            j = ag.getPreColumn();
            if (mapa[i][j] instanceof StreetCell) {
                info = ((StreetCell)mapa[i][j]).getAgent();
                ((StreetCell)mapa[i][j]).removeAgentWithAID(ag.getAID());
                ri = ag.getRow();
                rj = ag.getColumn();
                try {
                    ((StreetCell)mapa[ri][rj]).addAgent(ag);
                }catch(Exception e){
                //System.err.println(e);
                }
                
                if (ag.getType().equals(AgentType.SCOUT)) {
                    newCellsSC.add(mapa[ri][rj]);
                }else {
                    newCellsH.add(mapa[ri][rj]);
                }  
            }
        }
        newListOfAgents.put(AgentType.SCOUT, newCellsSC);
        newListOfAgents.put(AgentType.HARVESTER, newCellsH);

        agent.getGame().setAgentList(newListOfAgents);
    }
    
    public boolean done() {
            return true;
    }

    public int onEnd() {
            //System.out.println("STATE_3 return OK");
            return 0;
    }
}
