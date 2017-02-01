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
public class DumpingBehaviour extends SimpleBehaviour 
{
    
    public DumpingBehaviour(Agent a) {
        super(a);
    }
    @Override
    public void action() { 
        Harvester agent = (Harvester) this.getAgent();
        
        agent.log("DumpingBehaviour...");

        /*
        From the variable in which we have the quantity of garbage a Harvester have,
        we delete an unit and if after doing that we do not have more garbage, then
        we have to change our state to FREE and of course to set our next position as 
        the one we have now.
        */
        // modified**
        Cell curCell = agent.getCurrentCell();
        if(curCell == null)
        {
            agent.log("Skipped dumping");
            return;
        }
        
        if(agent.dumpGarbage(curCell) == false)
        {
            return;
        }
        
        agent.setCurrentAgentState(MessageContent.FREE);
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
