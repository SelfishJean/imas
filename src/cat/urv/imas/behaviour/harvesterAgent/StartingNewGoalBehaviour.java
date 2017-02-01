/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;

import jade.core.behaviours.*;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.Cell;
import jade.core.Agent;
import cat.urv.imas.onthology.*;

public class StartingNewGoalBehaviour extends SimpleBehaviour {

    public StartingNewGoalBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Harvester agent = (Harvester) this.getAgent();
        agent.log("StartingNewGoalBehaviour...");
        Cell curPos = agent.getCurrentCell();
        Cell goalPos = agent.getNewGoalCell();
        agent.calculatePath(curPos, goalPos);
        agent.setCurrentAgentState(MessageContent.MOVING);
    }

    @Override
    public boolean done() {
        return true;
    }

    @Override
    public int onEnd() {
        return 0;
    }
}
