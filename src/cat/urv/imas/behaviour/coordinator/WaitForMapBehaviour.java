/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;
import jade.core.behaviours.*;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.onthology.MessageContent;
import jade.lang.acl.*;
import java.util.ArrayList;
import cat.urv.imas.agent.*;
import cat.urv.imas.onthology.GameSettings;
/**
 *
 * @author Alex
 */
public class WaitForMapBehaviour extends SimpleBehaviour
{
    private ACLMessage msg;
    boolean hasReply;
    
    public WaitForMapBehaviour(Agent a) 
    {
        super(a);
        
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        
        CoordinatorAgent agent = (CoordinatorAgent) myAgent;
        initialRequest.addReceiver(agent.systemAgent);
        initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        agent.log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            agent.log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        this.msg = initialRequest;
        hasReply = false;
        myAgent.send(msg);
    }
    
    @Override
    public void action() 
    { 
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();

            if(response != null) 
            {
                CoordinatorAgent agent = (CoordinatorAgent) myAgent;
                switch(response.getPerformative()) 
                {
                    case ACLMessage.INFORM:
                        agent.log("INFORM (new map) received from " + ((AID) msg.getSender()).getLocalName());
                        try 
                        {
                            GameSettings game = (GameSettings) msg.getContentObject();
                            agent.setGame(game);
                            agent.log(game.getShortString());
                            agent.sentNewPositions = false;
                        } 
                        catch (Exception e) 
                        {
                            agent.errorLog("Incorrect content: " + e.toString());
                        }
                        break;
                    default:
                        agent.log("Failed to process the message");
                        break;
                }

                hasReply = true;
            }
        }
    }
    
    public boolean done() 
    {
        return hasReply;
    }
    
    public int onEnd() 
    {
        hasReply = false;
        return 0;
    }
}