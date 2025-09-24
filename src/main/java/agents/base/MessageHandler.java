package agents.base;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Comportement cyclique pour la gestion des messages de base
 */
public class MessageHandler extends CyclicBehaviour {
    private final BaseAgent agent;
    
    public MessageHandler(BaseAgent agent) {
        this.agent = agent;
    }
    
    @Override
    public void action() {
        ACLMessage message = myAgent.receive();
        
        if (message != null) {
            // Log du message reçu
            agent.logger.fine("Message reçu de " + message.getSender().getLocalName() + 
                            ": " + message.getContent());
            
            // Déléguer le traitement à l'agent spécifique
            agent.handleMessage(message);
        } else {
            block();
        }
    }
    
    /**
     * Filtre les messages selon un template
     */
    protected ACLMessage receiveFiltered(MessageTemplate template) {
        return myAgent.receive(template);
    }
    
    /**
     * Vérifie si un message correspond à un pattern
     */
    protected boolean matchesPattern(ACLMessage message, String pattern) {
        return message.getContent() != null && message.getContent().startsWith(pattern);
    }
}