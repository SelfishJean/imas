/**
 *  IMAS implemented (Group 8) code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.behaviour.coordinator;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import java.util.concurrent.TimeUnit;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author albertOlivares
 */
public class UpdateMapBehaviour extends AchieveREInitiator {
    
    public UpdateMapBehaviour(CoordinatorAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
        agent.log("Started behaviour to deal with AGREEs and to send New positions.");
    }

    /**
     * Handle AGREE messages
     *
     * @param msg Message to handle
     */
    @Override
    protected void handleAgree(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("AGREE received from " + ((AID) msg.getSender()).getLocalName());
    }

    /**
     * Handle INFORM messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
        try {
            Object content = (Object) msg.getContent();
            if (content.equals(MessageContent.NEXT_STEP)) {
                agent.log("System Agent has updated the map.");
                //agent.isMapUpdated = false;
                //agent.sentNewPositions = true;
            }
        } catch (Exception e) {
            agent.errorLog("Incorrect content: " + e.toString());
        }
    }

    /**
     * Handle NOT-UNDERSTOOD messages
     *
     * @param msg Message
     */
    @Override
    protected void handleNotUnderstood(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("This message NOT UNDERSTOOD.");
    }

    /**
     * Handle FAILURE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleFailure(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("The action has failed.");

    } //End of handleFailure

    /**
     * Handle REFUSE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleRefuse(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("Action refused.");
    }
    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }
    
}
