/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.harvesterAgent;

import jade.core.behaviours.*;
import cat.urv.imas.agent.*;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.StreetCell;
import jade.core.Agent;
import cat.urv.imas.onthology.*;

public class MovingBehaviour extends SimpleBehaviour {

    public MovingBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        Harvester agent = (Harvester) this.getAgent();

        agent.log("MovingBehaviour...");

        Cell nextMove = agent.getNextMove();
        Cell[][] map = agent.game.getMap();
        Cell curCell = agent.getCurrentCell();
        if (nextMove != null && curCell != null
                && curCell.getCol() == nextMove.getCol()
                && curCell.getRow() == nextMove.getRow()) {
            agent.removeNextMove();
            nextMove = agent.getNextMove();

            if (nextMove != null) {
                agent.log("Prev move:" + agent.newInfoAgent.getRow() + " "
                        + agent.newInfoAgent.getColumn() + "; Next move:"
                        + nextMove.getRow() + " " + nextMove.getCol());
            } else {
                agent.log("Moving is finished");
            }
        } else {
            agent.log("Next move: fail\n");
        }

        if (nextMove != null && map[nextMove.getRow()][nextMove.getCol()].getCellType() == CellType.STREET) {
            StreetCell street = (StreetCell) map[nextMove.getRow()][nextMove.getCol()];

            if (street.isThereAnAgent() == false) {
                InfoAgent infoCopy = agent.newInfoAgent;
                infoCopy.setPreRow(agent.newInfoAgent.getRow());
                infoCopy.setPreColumn(agent.newInfoAgent.getColumn());
                infoCopy.setRow(nextMove.getRow());
                infoCopy.setColumn(nextMove.getCol());
                agent.setNewInfoAgent(infoCopy);
            }
        }

        if (nextMove == null && agent.dumping == false) {
            agent.setCurrentAgentState(MessageContent.COLLECTING);
            agent.dumping = true;
        } else if (nextMove == null && agent.dumping == true) {
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
        return 0;
    }
}
