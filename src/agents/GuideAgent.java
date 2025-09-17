package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.*;

public class GuideAgent extends Agent {
    private List<String> tableaux;
    private Map<String, String> explanations;
    private boolean available;
    private String currentGroup;
    private int currentTableauIndex;
    private static final int MAX_GROUP_SIZE = 15;
    
    @Override
    protected void setup() {
        System.out.println("Agent Guide " + getAID().getName() + " démarré");
        
        // Initialisation des tableaux et explications
        initializeExhibition();
        available = true;
        currentGroup = null;
        currentTableauIndex = 0;
        
        // Enregistrement dans le service d'annuaire
        registerInYellowPages();
        
        // Ajout des comportements
        addBehaviour(new ReceiveTouristRequestsBehaviour());
        addBehaviour(new ConductTourBehaviour());
    }
    
    private void initializeExhibition() {
        tableaux = new ArrayList<>();
        explanations = new HashMap<>();
        
        // Configuration de l'exposition (peut être paramétré)
        tableaux.add("La Joconde");
        tableaux.add("La Nuit Étoilée");
        tableaux.add("Guernica");
        tableaux.add("Le Cri");
        tableaux.add("La Persistance de la Mémoire");
        tableaux.add("Les Demoiselles d'Avignon");
        tableaux.add("La Grande Vague");
        tableaux.add("American Gothic");
        
        explanations.put("La Joconde", 
            "Chef-d'œuvre de Léonard de Vinci (1503-1519), ce portrait révolutionnaire " +
            "utilise la technique du sfumato. Le sourire énigmatique et le regard direct " +
            "créent une connexion unique avec l'observateur. Volée en 1911, elle est " +
            "devenue l'œuvre la plus célèbre au monde.");
            
        explanations.put("La Nuit Étoilée", 
            "Peinte par Vincent van Gogh en 1889 à l'asile de Saint-Rémy. Cette œuvre " +
            "post-impressionniste capture un ciel nocturne turbulent avec des mouvements " +
            "tourbillonnants. Les cyprès au premier plan symbolisent le lien entre " +
            "terre et ciel.");
            
        explanations.put("Guernica", 
            "Pablo Picasso a créé cette œuvre en 1937 en réaction au bombardement de " +
            "Guernica pendant la guerre civile espagnole. Style cubiste en noir et blanc, " +
            "elle dénonce les horreurs de la guerre avec des symboles puissants : " +
            "taureau, cheval, ampoule.");
            
        explanations.put("Le Cri", 
            "Edvard Munch, 1893. Cette œuvre expressionniste norvégienne exprime " +
            "l'angoisse existentielle moderne. Les lignes ondulantes du ciel et les " +
            "couleurs vives créent une atmosphère d'anxiété universelle.");
            
        explanations.put("La Persistance de la Mémoire", 
            "Salvador Dalí, 1931. Ces montres molles illustrent la relativité du temps " +
            "selon Einstein. Œuvre surréaliste inspirée par un camembert qui fond, " +
            "elle questionne notre perception de la réalité temporelle.");
            
        explanations.put("Les Demoiselles d'Avignon", 
            "Picasso, 1907. Œuvre fondatrice du cubisme, elle rompt avec la perspective " +
            "traditionnelle. Les visages inspirés de l'art africain marquent une " +
            "révolution dans la représentation artistique occidentale.");
            
        explanations.put("La Grande Vague", 
            "Hokusai, vers 1830. Cette estampe japonaise ukiyo-e montre la puissance " +
            "de la nature face au mont Fuji. Les lignes fluides et la composition " +
            "dynamique influenceront l'art occidental.");
            
        explanations.put("American Gothic", 
            "Grant Wood, 1930. Ce portrait de fermiers du Midwest américain capture " +
            "l'esprit puritain et l'austérité rurale. La maison gothique en arrière-plan " +
            "contraste avec le réalisme des personnages.");
    }
    
    private void registerInYellowPages() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("guide-service");
        sd.setName("Guide d'exposition");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent Guide " + getAID().getName() + " terminé");
    }
    
    // Comportement pour recevoir les demandes de visite
    private class ReceiveTouristRequestsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                if (content.startsWith("TOUR_REQUEST")) {
                    handleTourRequest(msg);
                }
            } else {
                block();
            }
        }
        
        private void handleTourRequest(ACLMessage msg) {
            String[] parts = msg.getContent().split(":");
            String groupId = parts[1];
            int groupSize = Integer.parseInt(parts[2]);
            
            ACLMessage reply = msg.createReply();
            
            if (available && groupSize <= MAX_GROUP_SIZE) {
                reply.setPerformative(ACLMessage.AGREE);
                reply.setContent("TOUR_ACCEPTED:" + groupId);
                available = false;
                currentGroup = groupId;
                currentTableauIndex = 0;
                
                System.out.println("Guide " + getLocalName() + 
                    " accepte la visite du groupe " + groupId + 
                    " (" + groupSize + " personnes)");
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("TOUR_REFUSED:UNAVAILABLE");
            }
            
            send(reply);
        }
    }
    
    // Comportement pour conduire la visite
    private class ConductTourBehaviour extends TickerBehaviour {
        public ConductTourBehaviour() {
            super(GuideAgent.this, 5000); // Toutes les 5 secondes
        }
        
        @Override
        protected void onTick() {
            if (!available && currentGroup != null) {
                if (currentTableauIndex < tableaux.size()) {
                    presentTableau();
                    currentTableauIndex++;
                } else {
                    finishTour();
                }
            }
        }
        
        private void presentTableau() {
            String tableauName = tableaux.get(currentTableauIndex);
            String explanation = explanations.get(tableauName);
            
            System.out.println("\n=== GUIDE " + getLocalName() + " ===");
            System.out.println("Tableau " + (currentTableauIndex + 1) + "/" + tableaux.size() + 
                ": " + tableauName);
            System.out.println("Groupe: " + currentGroup);
            System.out.println("Explication: " + explanation);
            System.out.println("================================\n");
            
            // Envoie l'information au groupe de touristes
            notifyTouristGroup(tableauName, explanation);
        }
        
        private void notifyTouristGroup(String tableau, String explanation) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            // Recherche des agents touristes du groupe
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("tourist-service");
            template.addServices(sd);
            
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                for (DFAgentDescription dfd : result) {
                    msg.addReceiver(dfd.getName());
                }
                
                msg.setContent("TABLEAU_INFO:" + currentGroup + ":" + tableau + ":" + explanation);
                send(msg);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
        
        private void finishTour() {
            System.out.println("\n*** VISITE TERMINÉE ***");
            System.out.println("Guide " + getLocalName() + 
                " a terminé la visite du groupe " + currentGroup);
            System.out.println("Tous les " + tableaux.size() + 
                " tableaux ont été présentés avec succès !");
            
            // Notification de fin de visite
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("tourist-service");
            template.addServices(sd);
            
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                for (DFAgentDescription dfd : result) {
                    msg.addReceiver(dfd.getName());
                }
                
                msg.setContent("TOUR_FINISHED:" + currentGroup);
                send(msg);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
            
            // Redevient disponible
            available = true;
            currentGroup = null;
            currentTableauIndex = 0;
        }
    }
}