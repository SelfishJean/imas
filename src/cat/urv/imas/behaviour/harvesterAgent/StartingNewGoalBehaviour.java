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
public class StartingNewGoalBehaviour extends SimpleBehaviour {
    
    public StartingNewGoalBehaviour(Agent a) {
        super(a);
    }
    @Override
    public void action() { 
        Harvester agent = (Harvester) this.getAgent();
        
        agent.log("StartingNewGoalBehaviour...");
        
        /*
        In this behaviour we have to take the new goal (agent.getNewGoalCell()) and generate
        a path using the method "calculatePath". After that we need to save the Path
        in a variable which belongs to Harvester Agent. Since how FSM is designed, 
        after this behaviour we will move to the first position in the map.
        
        
        Of course, we need to set the value of our state as MOVING:
        (agent.setCurrentAgentState(MessageContent.MOVING))
        please, take into account that this value is set as FREE by default in the
        setup() of Harvester.java class.
        */
        
        // modified**
        Cell curPos = agent.getCurrentCell();
        Cell goalPos = agent.getNewGoalCell();

        //System.out.printf("%d %d ======================\n", goalPos.getRow(), goalPos.getCol());
        agent.calculatePath(curPos, goalPos);
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
