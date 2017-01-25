/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;
import cat.urv.imas.behaviour.scoutCoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
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
import cat.urv.imas.onthology.InfoDiscovery;
import jade.core.Agent;
/**
 * 
 * @author albertOlivares
 */
public class WaitingForNewDiscoveriesBehaviour extends SimpleBehaviour
{
    private ACLMessage msg;
    boolean hasReply;
    
    public WaitingForNewDiscoveriesBehaviour(Agent a) 
    {
        super(a);
    }
    
    @Override
    public void action() 
    { 
        System.out.println("(CoordinatorAgent) Starting WaitingForNewDiscoveriesBehaviour");
        CoordinatorAgent agent = (CoordinatorAgent)this.getAgent();

        hasReply = false;
        
        while(done() == false) 
        {
            ACLMessage response = myAgent.receive();
            //System.out.println("Waiting....");
            

            if(response != null) 
            {
                //System.out.println("Waiting...."+response);
                //System.out.println(response.getPerformative());
                //CoordinatorAgent agent = (CoordinatorAgent) myAgent;
                //CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
                switch(response.getPerformative()) 
                {
                    case ACLMessage.INFORM:
                        agent.log("INFORM (new discoveries) received from " + ((AID) response.getSender()).getLocalName());
                        
                        try 
                        {
                            ArrayList<InfoDiscovery> newDiscoveries = (ArrayList<InfoDiscovery>) response.getContentObject();
                            agent.setNewInfoDiscoveriesList(newDiscoveries);
                            agent.log("New discoveries saved");
                            
                            //ACLMessage reply = response.createReply(); 
                            // Sending an Agree..
                            //reply.setPerformative(ACLMessage.AGREE);
                            //agent.log("Sending Agreement");
                            //agent.send(reply);
                            hasReply = true;
                            
                        } 
                        catch (Exception e) 
                        {
                            agent.errorLog("Incorrect content: " + e.toString());
                        }
                        break;
                    case ACLMessage.FAILURE:
                        agent.log("The action has failed.");
                        break;
                    default:
                        agent.log("Failed to process the message");
                        break;
                }

                
            }
        }
    }
    
    @Override
    public boolean done() 
    {
        return hasReply;
    }
    
    public int onEnd() 
    {
        hasReply = false;
        //System.out.println("End of"+getBehaviourName());
        return 0;
    }

}