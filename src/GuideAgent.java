import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.*;

public class GuideAgent extends Agent {
    private List<AID> touristAgents = new ArrayList<>();
    private AID coordinatorAgent;
    private String specialization; // "Renaissance", "Moderne", "Impressionniste"
    private int currentTableau = 0;
    private boolean isGuiding = false;
    private Map<String, String> tableauInfo = new HashMap<>();
    private int groupSize = 0;
    private String currentLocation = "PointA";
    
    // État du groupe
    private double averageSatisfaction = 0.5;
    private double averageFatigue = 0.0;
    private int visitDuration = 0;
    
    protected void setup() {
        System.out.println("Agent Guide " + getLocalName() + " démarré");
        
        // Initialisation de la spécialisation aléatoire
        String[] specializations = {"Renaissance", "Moderne", "Impressionniste", "Contemporain"};
        specialization = specializations[new Random().nextInt(specializations.length)];
        
        // Initialisation des informations sur les tableaux
        initializeTableauInfo();
        
        // Enregistrement du service
        registerService();
        
        // Ajout des comportements
        addBehaviour(new TouristRegistrationHandler());
        addBehaviour(new GroupManagementBehaviour());
        addBehaviour(new VisitConductor());
        addBehaviour(new CoordinatorCommunication());
        
        // Recherche du coordinateur
        findCoordinator();
    }
    
    private void initializeTableauInfo() {
        tableauInfo.put("Tableau1", "La Joconde - Chef-d'œuvre de Léonard de Vinci, symbole de l'art Renaissance");
        tableauInfo.put("Tableau2", "La Nuit étoilée - Œuvre emblématique de Van Gogh, post-impressionnisme");
        tableauInfo.put("Tableau3", "Guernica - Picasso, art moderne, dénonciation de la guerre");
        tableauInfo.put("Tableau4", "Les Demoiselles d'Avignon - Picasso, naissance du cubisme");
        tableauInfo.put("Tableau5", "L'École d'Athènes - Raphaël, Renaissance italienne, philosophie");
    }
    
    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("guide-service");
        sd.setName("guide-touristique");
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
            System.out.println("Guide " + getLocalName() + " enregistré avec spécialisation: " + specialization);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    private void findCoordinator() {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("coordinator-service");
                template.addServices(sd);
                
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        coordinatorAgent = result[0].getName();
                        System.out.println("Guide trouvé coordinateur: " + coordinatorAgent.getLocalName());
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }
    
    // Comportement pour gérer l'inscription des touristes
    private class TouristRegistrationHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                if ("JOIN_GROUP".equals(msg.getContent())) {
                    // Accepter le touriste dans le groupe
                    touristAgents.add(msg.getSender());
                    groupSize = touristAgents.size();
                    
                    // Répondre au touriste
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("WELCOME_GROUP:" + specialization + ":" + currentLocation);
                    send(reply);
                    
                    System.out.println("Guide " + getLocalName() + " accueille touriste " + 
                                     msg.getSender().getLocalName() + " (Groupe: " + groupSize + ")");
                    
                    // Si assez de touristes, commencer la visite
                    if (groupSize >= 3 && !isGuiding) {
                        startGuidedTour();
                    }
                }
            } else {
                block();
            }
        }
    }
    
    // Comportement de gestion du groupe
    private class GroupManagementBehaviour extends TickerBehaviour {
        public GroupManagementBehaviour() {
            super(GuideAgent.this, 5000); // Vérifier toutes les 5 secondes
        }
        
        protected void onTick() {
            if (isGuiding && !touristAgents.isEmpty()) {
                // Vérifier l'état du groupe
                checkGroupStatus();
                updateVisitProgress();
            }
        }
    }
    
    // Comportement principal de conduite de visite
    private class VisitConductor extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)
            );
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.startsWith("STATUS:")) {
                    // Traiter les mises à jour de statut des touristes
                    processTouristStatus(msg);
                } else if (content.startsWith("QUESTION:")) {
                    // Répondre aux questions
                    answerQuestion(msg);
                } else if ("READY_NEXT".equals(content)) {
                    // Touriste prêt pour le prochain tableau
                    checkIfGroupReady();
                }
            } else {
                block();
            }
        }
    }
    
    // Communication avec le coordinateur
    private class CoordinatorCommunication extends TickerBehaviour {
        public CoordinatorCommunication() {
            super(GuideAgent.this, 10000); // Rapport toutes les 10 secondes
        }
        
        protected void onTick() {
            if (coordinatorAgent != null && isGuiding) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(coordinatorAgent);
                msg.setContent("REPORT:" + currentLocation + ":" + groupSize + ":" + 
                             currentTableau + ":" + averageSatisfaction + ":" + averageFatigue);
                send(msg);
            }
        }
    }
    
    private void startGuidedTour() {
        isGuiding = true;
        System.out.println("Guide " + getLocalName() + " commence la visite avec " + groupSize + " touristes");
        
        // Déplacer vers le premier tableau
        moveToTableau("Tableau1");
    }
    
    private void moveToTableau(String tableau) {
        currentLocation = tableau;
        currentTableau++;
        
        // Informer tous les touristes du déplacement
        for (AID tourist : touristAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(tourist);
            msg.setContent("MOVE_TO:" + tableau);
            send(msg);
        }
        
        // Attendre puis commencer l'explication
        addBehaviour(new WakerBehaviour(this, 3000) {
            protected void onWake() {
                startExplanation(tableau);
            }
        });
        
        System.out.println("Guide " + getLocalName() + " emmène le groupe vers " + tableau);
    }
    
    private void startExplanation(String tableau) {
        String explanation = tableauInfo.get(tableau);
        if (explanation != null) {
            // Adapter l'explication selon la spécialisation
            if (tableau.contains("Renaissance") && "Renaissance".equals(specialization)) {
                explanation += " - [Explication approfondie Renaissance]";
            }
            
            // Diffuser l'explication
            for (AID tourist : touristAgents) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(tourist);
                msg.setContent("EXPLANATION:" + explanation);
                send(msg);
            }
            
            System.out.println("Guide " + getLocalName() + " explique: " + tableau);
        }
    }
    
    private void processTouristStatus(ACLMessage msg) {
        String[] parts = msg.getContent().split(":");
        if (parts.length >= 3) {
            String status = parts[1];
            double value = Double.parseDouble(parts[2]);
            
            if ("SATISFACTION".equals(status)) {
                updateAverageSatisfaction(value);
            } else if ("FATIGUE".equals(status)) {
                updateAverageFatigue(value);
            }
            
            // Adapter le comportement selon l'état du groupe
            adaptToGroupState();
        }
    }
    
    private void answerQuestion(ACLMessage msg) {
        String question = msg.getContent().substring(9); // Enlever "QUESTION:"
        
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent("ANSWER:Excellente question ! " + generateAnswer(question));
        send(reply);
        
        System.out.println("Guide " + getLocalName() + " répond à une question de " + 
                         msg.getSender().getLocalName());
    }
    
    private String generateAnswer(String question) {
        // Génération simple de réponses basée sur des mots-clés
        if (question.toLowerCase().contains("technique")) {
            return "Cette œuvre utilise une technique particulière de " + specialization.toLowerCase();
        } else if (question.toLowerCase().contains("histoire")) {
            return "L'histoire de cette œuvre remonte à la période " + specialization.toLowerCase();
        } else {
            return "C'est un aspect fascinant de l'art " + specialization.toLowerCase();
        }
    }
    
    private void checkGroupStatus() {
        // Simuler la vérification de l'état du groupe
        visitDuration++;
        
        if (averageFatigue > 0.7) {
            proposeBreak();
        } else if (averageSatisfaction > 0.8 && currentTableau < 5) {
            // Le groupe est satisfait, on peut continuer
            if (new Random().nextDouble() < 0.3) { // 30% de chance de passer au suivant
                moveToNextTableau();
            }
        }
    }
    
    private void updateVisitProgress() {
        // Simulation de l'évolution de la fatigue et satisfaction
        averageFatigue = Math.min(1.0, averageFatigue + 0.01);
        if (new Random().nextDouble() < 0.1) { // Variation aléatoire de satisfaction
            averageSatisfaction = Math.max(0.0, Math.min(1.0, 
                averageSatisfaction + (new Random().nextDouble() - 0.5) * 0.1));
        }
    }
    
    private void updateAverageSatisfaction(double newSatisfaction) {
        averageSatisfaction = (averageSatisfaction + newSatisfaction) / 2.0;
    }
    
    private void updateAverageFatigue(double newFatigue) {
        averageFatigue = (averageFatigue + newFatigue) / 2.0;
    }
    
    private void adaptToGroupState() {
        if (averageFatigue > 0.8) {
            // Raccourcir les explications
            System.out.println("Guide " + getLocalName() + " adapte: groupe fatigué, explications plus courtes");
        } else if (averageSatisfaction > 0.9) {
            // Approfondir les explications
            System.out.println("Guide " + getLocalName() + " adapte: groupe très intéressé, explications approfondies");
        }
    }
    
    private void proposeBreak() {
        for (AID tourist : touristAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.addReceiver(tourist);
            msg.setContent("BREAK_PROPOSAL:5");
            send(msg);
        }
        System.out.println("Guide " + getLocalName() + " propose une pause au groupe");
    }
    
    private void checkIfGroupReady() {
        // Logique simplifiée : si le message vient d'au moins la moitié du groupe
        // on passe au tableau suivant
        if (new Random().nextDouble() < 0.5) {
            moveToNextTableau();
        }
    }
    
    private void moveToNextTableau() {
        if (currentTableau < 5) {
            String nextTableau = "Tableau" + (currentTableau + 1);
            moveToTableau(nextTableau);
        } else {
            endTour();
        }
    }
    
    private void endTour() {
        isGuiding = false;
        
        for (AID tourist : touristAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(tourist);
            msg.setContent("TOUR_END:Merci pour cette visite ! J'espère que vous avez apprécié.");
            send(msg);
        }
        
        System.out.println("Guide " + getLocalName() + " termine la visite. Satisfaction finale: " + 
                         String.format("%.2f", averageSatisfaction));
        
        // Réinitialiser pour un nouveau groupe
        touristAgents.clear();
        groupSize = 0;
        currentTableau = 0;
        currentLocation = "PointA";
        averageFatigue = 0.0;
        averageSatisfaction = 0.5;
        visitDuration = 0;
    }
    
    // Getters pour l'interface graphique
    public String getCurrentLocation() { return currentLocation; }
    public int getGroupSize() { return groupSize; }
    public boolean isGuiding() { return isGuiding; }
    public double getAverageSatisfaction() { return averageSatisfaction; }
    public double getAverageFatigue() { return averageFatigue; }
    public String getSpecialization() { return specialization; }
    public int getCurrentTableau() { return currentTableau; }
    public List<AID> getTouristAgents() { return new ArrayList<>(touristAgents); }
    
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent Guide " + getLocalName() + " terminé");
    }
}