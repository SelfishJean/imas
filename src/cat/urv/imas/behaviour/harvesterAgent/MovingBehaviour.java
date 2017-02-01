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
import cat.urv.imas.map.CellType;
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
public class MovingBehaviour extends SimpleBehaviour {
    
    public MovingBehaviour(Agent a) {
        super(a);
    }
    @Override
    public void action() { 
        Harvester agent = (Harvester) this.getAgent();
        
        agent.log("MovingBehaviour...");

        /*
        We need to work with a path (if we do not have it, we need to create it and
        also to create two methods to get and set the variable). This variable contains
        next steps/movements we need to do. 
        Basically, we need to read the first next movement and save it in the variable
        called "newInfoAgent". newInfoAgent contains two variables "column" and "row"
        that will be read by SystemAgent to modify the positions. 

        Here we do not need in any case to change the state of the agent (even though
        se send the last movement of the path) WHY? (SEE BELOW)

        IMPORTANT. We cannot delete the movement after having assigned it because with the
        current heuristics SystemAgent could decide not to move the agent (if there is a colission).
        So, what should we do? At the very beginning of InformStateAgentBehaviour if our state
        if MOVING (there is already an if doing it) then, check if we actually have moved
        or not in the last turn, if not, then we do nothing. But if we have moved we need to
        delete the first position in the path. Also we should check in this point if after
        deleting that position the path is empty, if so, we should change our state. 
        The next state can be DUMPING or COLLECTING. This depends on if we were moving to
        a next goal (to collect) or to a recycle center (to dump). We should have a variable
        in our agent which knows if our goal after completing the path is to dump or to collect.
        */
        
        // modified**
        // TODO: implement traffic rules
        Cell nextMove = agent.getNextMove();
        Cell [][]map = agent.game.getMap();
        Cell curCell = agent.getCurrentCell();
        if(nextMove != null && curCell != null 
                && curCell.getCol() == nextMove.getCol() 
                && curCell.getRow() == nextMove.getRow())
        {
            agent.removeNextMove();
            nextMove = agent.getNextMove();
            
            if(nextMove != null)
            {
                agent.log("Prev move:" + agent.newInfoAgent.getRow() + " "
                    + agent.newInfoAgent.getColumn() + "; Next move:" 
                    + nextMove.getRow() + " " + nextMove.getCol());
            }
            else
            {
                agent.log("Moving is finished");
            }
        }
        else
        {
            agent.log("Next move: fail\n");
        }    
        
        if(nextMove != null && map[nextMove.getRow()][nextMove.getCol()].getCellType() == CellType.STREET)
        {
            StreetCell street = (StreetCell) map[nextMove.getRow()][nextMove.getCol()];
            
            if(street.isThereAnAgent() == false)
            {
                InfoAgent infoCopy = agent.newInfoAgent;
                infoCopy.setPreRow(agent.newInfoAgent.getRow());
                infoCopy.setPreColumn(agent.newInfoAgent.getColumn());
                infoCopy.setRow(nextMove.getRow());
                infoCopy.setColumn(nextMove.getCol());

                agent.setNewInfoAgent(infoCopy);
                //agent.newInfoAgent.setPreRow(agent.newInfoAgent.getRow());
                //agent.newInfoAgent.setPreColumn(agent.newInfoAgent.getColumn());
                //agent.newInfoAgent.setRow(nextMove.getRow());
                //agent.newInfoAgent.setColumn(nextMove.getCol());
                
            }
        }
        
 
        if(nextMove == null && agent.dumping == false)
        {
            agent.setCurrentAgentState(MessageContent.COLLECTING);
            agent.dumping = true;
        }
        else if(nextMove == null && agent.dumping == true)
        {
            agent.setCurrentAgentState(MessageContent.DUMPING);
            agent.dumping = false;
        }
        
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
