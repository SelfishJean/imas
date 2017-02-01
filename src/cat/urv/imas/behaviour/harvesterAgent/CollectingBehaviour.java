/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;

import cat.urv.imas.behaviour.harvesterAgent.*;
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
public class CollectingBehaviour extends SimpleBehaviour {
    
    public CollectingBehaviour(Agent a) {
        super(a);
    }
    @Override
    public void action() { 
        Harvester agent = (Harvester) this.getAgent();
        
        agent.log("CollectingBehaviour...");

        /*
        We have to take garbage until there is not or we are full. Basically,
        we need to look at the garbage the building has and if it is greater than one
        then we keep our state of collecting and we set our nextPosition as the one we 
        currently have. 

        It will be the HarvesterCoordinator which will know in which building we are
        because it ordered us that goal. So, every turn we say to HC that we are 
        collecting it will delete one unit from the variable in which it has and it 
        will send, somehow, the current garbage of all buildings in which we are
        collecting. Cordinator Agent will send that info to SystemAgent, who will
        update the quantity of garbage of those buildings every turn.
        */
        // modified**
        Cell curCell = agent.getCurrentCell();
        if(curCell == null)
        {
            agent.log("Skipped collecting");
            return;
        }
        
        if(agent.collectGarbage(curCell) == false)
        {
            return;
        }
        
        agent.findRCenter();
        agent.calculatePath(agent.getCurrentCell(), agent.getNewGoalCell());
        agent.setCurrentAgentState(MessageContent.MOVING);
        
    }
    
    @Override
    public boolean done() {
            return true;
    }

    @Override
    public int onEnd() {
            //System.out.println("STATE_3 return OK");
            return 0;
    }
}
