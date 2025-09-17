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

public class TouristGroupAgent extends Agent {
    private String groupId;
    private int groupSize;
    private boolean hasGuide;
    private String currentGuide;
    private List<String> visitedTableaux;
    private String nationality;
    private String meetingPoint;
    
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 3) {
            groupId = (String) args[0];
            groupSize = (Integer) args[1];
            nationality = (String) args[2];
            meetingPoint = "Point A - Entrée principale";
        } else {
            groupId = "GROUP_" + System.currentTimeMillis() % 1000;
            groupSize = (int) (Math.random() * 12) + 4; // Entre 4 et 15 personnes
            nationality = getRandomNationality();
            meetingPoint = "Point A - Entrée principale";
        }
        
        hasGuide = false;
        currentGuide = null;
        visitedTableaux = new ArrayList<>();
        
        System.out.println("Groupe de touristes " + groupId + 
            " créé (" + groupSize + " personnes, " + nationality + ")");
        System.out.println("Point de rendez-vous: " + meetingPoint);
        
        // Enregistrement dans le service d'annuaire
        registerInYellowPages();
        
        // Ajout des comportements
        addBehaviour(new RequestGuideBehaviour());
        addBehaviour(new ReceiveGuideInfoBehaviour());
        addBehaviour(new GroupInteractionBehaviour());
    }
    
    private String getRandomNationality() {
        String[] nationalities = {
            "Française", "Allemande", "Italienne", "Espagnole", 
            "Britannique", "Américaine", "Japonaise", "Chinoise",
            "Brésilienne", "Canadienne", "Australienne", "Hollandaise"
        };
        return nationalities[(int) (Math.random() * nationalities.length)];
    }
    
    private void registerInYellowPages() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("tourist-service");
        sd.setName("Groupe de touristes");
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
        System.out.println("Groupe " + groupId + " a quitté l'exposition");
    }
    
    // Comportement pour demander un guide
    private class RequestGuideBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("Groupe " + groupId + " cherche un guide disponible...");
            
            // Recherche des guides disponibles
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("guide-service");
            template.addServices(sd);
            
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                
                if (result.length > 0) {
                    // Envoie une demande au premier guide trouvé
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(result[0].getName());
                    msg.setContent("TOUR_REQUEST:" + groupId + ":" + groupSize);
                    msg.setConversationId("tour-booking");
                    msg.setReplyWith("request" + System.currentTimeMillis());
                    send(msg);
                    
                    System.out.println("Demande de visite envoyée au guide " + 
                        result[0].getName().getLocalName());
                    
                    // Attendre la réponse
                    addBehaviour(new WaitGuideResponseBehaviour());
                } else {
                    System.out.println("Aucun guide disponible trouvé !");
                    // Réessayer après un délai
                    addBehaviour(new WakerBehaviour(myAgent, 10000) {
                        @Override
                        protected void onWake() {
                            addBehaviour(new RequestGuideBehaviour());
                        }
                    });
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }
    
    // Comportement pour attendre la réponse du guide
    private class WaitGuideResponseBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("tour-booking"),
                MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                )
            );
            
            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                if (reply.getPerformative() == ACLMessage.AGREE) {
                    hasGuide = true;
                    currentGuide = reply.getSender().getLocalName();
                    System.out.println("✓ Groupe " + groupId + 
                        " assigné au guide " + currentGuide);
                    System.out.println("La visite commence au " + meetingPoint + " !");
                } else {
                    System.out.println("✗ Guide indisponible, recherche d'un autre guide...");
                    // Réessayer avec un autre guide
                    addBehaviour(new WakerBehaviour(myAgent, 5000) {
                        @Override
                        protected void onWake() {
                            addBehaviour(new RequestGuideBehaviour());
                        }
                    });
                }
            } else {
                // Réessayer si pas de réponse
                addBehaviour(new WakerBehaviour(myAgent, 3000) {
                    @Override
                    protected void onWake() {
                        addBehaviour(new WaitGuideResponseBehaviour());
                    }
                });
            }
        }
    }
    
    // Comportement pour recevoir les informations du guide
    private class ReceiveGuideInfoBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.startsWith("TABLEAU_INFO")) {
                    handleTableauInfo(content);
                } else if (content.startsWith("TOUR_FINISHED")) {
                    handleTourFinished(content);
                }
            } else {
                block();
            }
        }
        
        private void handleTableauInfo(String content) {
            String[] parts = content.split(":", 4);
            if (parts.length >= 4 && parts[1].equals(groupId)) {
                String tableauName = parts[2];
                String explanation = parts[3];
                
                visitedTableaux.add(tableauName);
                
                System.out.println("\n👥 GROUPE " + groupId + " (" + nationality + ")");
                System.out.println("📍 Devant: " + tableauName);
                System.out.println("👂 Écoute les explications du guide " + currentGuide);
                System.out.println("📊 Progression: " + visitedTableaux.size() + " tableaux visités");
                
                // Simulation de réactions du groupe
                simulateGroupReactions(tableauName);
            }
        }
        
        private void handleTourFinished(String content) {
            String[] parts = content.split(":");
            if (parts.length >= 2 && parts[1].equals(groupId)) {
                System.out.println("\n🎉 VISITE TERMINÉE pour le groupe " + groupId);
                System.out.println("📋 Bilan de la visite:");
                System.out.println("   - Groupe: " + nationality + " (" + groupSize + " personnes)");
                System.out.println("   - Guide: " + currentGuide);
                System.out.println("   - Tableaux vus: " + visitedTableaux.size());
                System.out.println("   - Satisfaction: " + getGroupSatisfaction());
                
                // Reset pour une éventuelle nouvelle visite
                hasGuide = false;
                currentGuide = null;
                visitedTableaux.clear();
            }
        }
        
        private void simulateGroupReactions(String tableauName) {
            String[] reactions = {
                "Le groupe semble captivé par les explications",
                "Plusieurs personnes posent des questions",
                "Les touristes prennent des photos (autorisées)",
                "Un murmure d'admiration parcourt le groupe",
                "Certains prennent des notes dans leur carnet",
                "Le groupe acquiesce avec intérêt",
                "Des exclamations d'émerveillement se font entendre"
            };
            
            String reaction = reactions[(int) (Math.random() * reactions.length)];
            System.out.println("💬 " + reaction);
            
            // Simulation occasionnelle de questions
            if (Math.random() < 0.3) {
                String[] questions = {
                    "Quelle est la technique utilisée ici ?",
                    "Combien de temps l'artiste a-t-il mis pour peindre cela ?",
                    "Où se trouve l'original actuellement ?",
                    "Y a-t-il d'autres œuvres similaires dans cette exposition ?",
                    "Quelle est la valeur estimée de ce tableau ?"
                };
                String question = questions[(int) (Math.random() * questions.length)];
                System.out.println("❓ Un touriste demande: \"" + question + "\"");
            }
        }
        
        private String getGroupSatisfaction() {
            double satisfaction = Math.random();
            if (satisfaction > 0.8) return "Excellente ⭐⭐⭐⭐⭐";
            else if (satisfaction > 0.6) return "Très bonne ⭐⭐⭐⭐";
            else if (satisfaction > 0.4) return "Bonne ⭐⭐⭐";
            else return "Correcte ⭐⭐";
        }
    }
    
    // Comportement pour les interactions internes du groupe
    private class GroupInteractionBehaviour extends TickerBehaviour {
        public GroupInteractionBehaviour() {
            super(TouristGroupAgent.this, 15000); // Toutes les 15 secondes
        }
        
        @Override
        protected void onTick() {
            if (hasGuide && Math.random() < 0.4) {
                simulateGroupDynamics();
            }
        }
        
        private void simulateGroupDynamics() {
            String[] dynamics = {
                "Un enfant du groupe pose une question naïve mais pertinente",
                "Le groupe se rassemble pour mieux voir un détail",
                "Quelqu'un traduit pour un membre qui ne parle pas français",
                "Une personne âgée partage un souvenir lié à l'œuvre",
                "Un amateur d'art dans le groupe ajoute une information",
                "Le groupe respecte la distance de sécurité avec l'œuvre"
            };
            
            if (Math.random() < 0.7) {
                String dynamic = dynamics[(int) (Math.random() * dynamics.length)];
                System.out.println("🎭 Dynamique de groupe: " + dynamic);
            }
        }
    }
    
    // Getters
    public String getGroupId() { return groupId; }
    public int getGroupSize() { return groupSize; }
    public boolean hasGuide() { return hasGuide; }
    public String getCurrentGuide() { return currentGuide; }
    public List<String> getVisitedTableaux() { return new ArrayList<>(visitedTableaux); }
}