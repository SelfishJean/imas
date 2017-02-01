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

public class CollectingBehaviour extends SimpleBehaviour {

    public CollectingBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Harvester agent = (Harvester) this.getAgent();

        agent.log("CollectingBehaviour...");

        Cell curCell = agent.getCurrentCell();
        if (curCell == null) {
            agent.log("Skipped collecting");
            return;
        }

        if (agent.collectGarbage(curCell) == false) {
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
        return 0;
    }
}
