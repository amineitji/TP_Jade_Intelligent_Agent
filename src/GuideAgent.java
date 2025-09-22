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
    private String specialization;
    private int currentTableau = 0;
    private boolean isGuiding = false;
    private boolean isAvailable = true; // NOUVEAU
    private Map<String, String> tableauInfo = new HashMap<>();
    private int groupSize = 0;
    private String currentLocation = "PointA";
    
    // État du groupe
    private double averageSatisfaction = 0.5;
    private double averageFatigue = 0.0;
    private int visitDuration = 0;
    private int completedTours = 0; // NOUVEAU
    
    // Configuration
    private final int REST_TIME_BETWEEN_TOURS = 5000; // 5 secondes de pause entre les visites
    
    protected void setup() {
        System.out.println("Agent Guide " + getLocalName() + " démarré avec système de recyclage");
        
        // Initialisation de la spécialisation aléatoire
        String[] specializations = {"Renaissance", "Moderne", "Impressionniste", "Contemporain"};
        specialization = specializations[new Random().nextInt(specializations.length)];
        
        // Initialisation des informations sur les tableaux
        initializeTableauInfo();
        
        // Enregistrement du service
        registerService();
        
        // Ajout des comportements
        addBehaviour(new CoordinatorCommunication());
        addBehaviour(new TouristAssignmentHandler()); // NOUVEAU
        addBehaviour(new GroupManagementBehaviour());
        addBehaviour(new VisitConductor());
        addBehaviour(new StatusReporter());
        
        // Recherche du coordinateur et enregistrement
        findAndRegisterWithCoordinator();
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
    
    private void findAndRegisterWithCoordinator() {
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
                        
                        // S'enregistrer auprès du coordinateur
                        registerWithCoordinator();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }
    
    private void registerWithCoordinator() {
        ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
        msg.addReceiver(coordinatorAgent);
        msg.setContent("REGISTER_GUIDE");
        send(msg);
        System.out.println("Guide " + getLocalName() + " s'enregistre auprès du coordinateur");
    }
    
    // NOUVEAU : Gestionnaire d'assignation de touristes par le coordinateur
    private class TouristAssignmentHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchContent("ASSIGN_TOURISTS:*")
            );
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                handleTouristAssignment(msg);
            } else {
                // Vérifier les autres types de messages
                mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
                msg = receive(mt);
                if (msg != null) {
                    handleCoordinatorConfirmation(msg);
                } else {
                    block();
                }
            }
        }
    }
    
    // Communication avec le coordinateur
    private class CoordinatorCommunication extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                if (content.startsWith("GUIDANCE:")) {
                    handleGuidance(msg);
                } else if (content.startsWith("ROUTE_ADVICE:")) {
                    handleRouteAdvice(msg);
                } else if (content.startsWith("EMERGENCY")) {
                    handleEmergency(msg);
                }
            } else {
                block();
            }
        }
    }
    
    // Comportement de gestion du groupe (amélioré)
    private class GroupManagementBehaviour extends TickerBehaviour {
        public GroupManagementBehaviour() {
            super(GuideAgent.this, 5000);
        }
        
        protected void onTick() {
            if (isGuiding && !touristAgents.isEmpty()) {
                checkGroupStatus();
                updateVisitProgress();
            } else if (isAvailable && touristAgents.isEmpty()) {
                // Signaler la disponibilité au coordinateur
                signalAvailability();
            }
        }
    }
    
    // Comportement principal de conduite de visite (amélioré)
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
                    processTouristStatus(msg);
                } else if (content.startsWith("QUESTION:")) {
                    answerQuestion(msg);
                } else if ("READY_NEXT".equals(content)) {
                    checkIfGroupReady();
                } else if (content.startsWith("JOIN_GROUP")) {
                    // Rediriger vers le coordinateur
                    redirectToCoordinator(msg.getSender());
                }
            } else {
                block();
            }
        }
    }
    
    // NOUVEAU : Rapporteur de statut périodique
    private class StatusReporter extends TickerBehaviour {
        public StatusReporter() {
            super(GuideAgent.this, 10000); // Rapport toutes les 10 secondes
        }
        
        protected void onTick() {
            if (coordinatorAgent != null && isGuiding) {
                sendStatusReport();
            }
        }
    }
    
    // NOUVEAU : Gestion de l'assignation de touristes par le coordinateur
    private void handleTouristAssignment(ACLMessage msg) {
        String content = msg.getContent();
        String[] parts = content.split(":");
        
        if (parts.length >= 2) {
            String[] touristNames = parts[1].split(",");
            
            if (!isAvailable) {
                // Refuser si pas disponible
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("GUIDE_BUSY");
                send(reply);
                return;
            }
            
            // Accepter les touristes
            touristAgents.clear();
            for (String name : touristNames) {
                if (!name.trim().isEmpty()) {
                    AID touristAID = new AID(name.trim(), AID.ISLOCALNAME);
                    touristAgents.add(touristAID);
                }
            }
            
            groupSize = touristAgents.size();
            isAvailable = false;
            
            // Confirmer l'acceptation
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.AGREE);
            reply.setContent("TOURISTS_ACCEPTED:" + groupSize);
            send(reply);
            
            System.out.println("Guide " + getLocalName() + " accepte un groupe de " + groupSize + " touristes");
            
            // Démarrer la visite après une courte attente
            addBehaviour(new WakerBehaviour(this, 3000) {
                protected void onWake() {
                    startGuidedTour();
                }
            });
        }
    }
    
    private void handleCoordinatorConfirmation(ACLMessage msg) {
        String content = msg.getContent();
        if ("REGISTRATION_CONFIRMED".equals(content)) {
            System.out.println("Guide " + getLocalName() + " : Enregistrement confirmé par le coordinateur");
            isAvailable = true;
        } else if ("TOUR_COMPLETION_ACKNOWLEDGED".equals(content)) {
            System.out.println("Guide " + getLocalName() + " : Fin de visite acknowledgée par le coordinateur");
            
            // Se préparer pour une nouvelle visite après le temps de repos
            addBehaviour(new WakerBehaviour(this, REST_TIME_BETWEEN_TOURS) {
                protected void onWake() {
                    prepareForNextTour();
                }
            });
        }
    }
    
    private void handleGuidance(ACLMessage msg) {
        String content = msg.getContent();
        System.out.println("Guide " + getLocalName() + " reçoit conseil du coordinateur: " + content);
        
        if (content.contains("LOW_SATISFACTION")) {
            adaptExplanationStyle(true); // Plus interactif
        } else if (content.contains("HIGH_FATIGUE")) {
            proposePause();
        }
    }
    
    private void handleRouteAdvice(ACLMessage msg) {
        String content = msg.getContent();
        if (content.contains("AVOID:")) {
            String[] parts = content.split(":");
            if (parts.length >= 3) {
                String locationToAvoid = parts[2];
                System.out.println("Guide " + getLocalName() + " évite la zone: " + locationToAvoid);
                // Adapter l'itinéraire si nécessaire
                adaptRoute(locationToAvoid);
            }
        }
    }
    
    private void handleEmergency(ACLMessage msg) {
        String content = msg.getContent();
        System.out.println("Guide " + getLocalName() + " reçoit alerte d'urgence: " + content);
        
        if (content.contains("AVOID_LOCATION")) {
            String[] parts = content.split(":");
            if (parts.length >= 3) {
                String emergencyLocation = parts[2];
                avoidLocation(emergencyLocation);
            }
        } else if (content.contains("LOCATION_AVAILABLE")) {
            String[] parts = content.split(":");
            if (parts.length >= 2) {
                String availableLocation = parts[1];
                System.out.println("Guide " + getLocalName() + " : " + availableLocation + " de nouveau disponible");
            }
        }
    }
    
    private void redirectToCoordinator(AID touristAID) {
        // Informer le touriste qu'il doit passer par le coordinateur
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(touristAID);
        msg.setContent("REDIRECT_TO_COORDINATOR");
        send(msg);
        
        System.out.println("Guide " + getLocalName() + " redirige " + touristAID.getLocalName() + " vers le coordinateur");
    }
    
    private void signalAvailability() {
        if (coordinatorAgent != null && isAvailable && touristAgents.isEmpty()) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(coordinatorAgent);
            msg.setContent("GUIDE_AVAILABLE");
            send(msg);
        }
    }
    
    private void sendStatusReport() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(coordinatorAgent);
        msg.setContent("REPORT:" + currentLocation + ":" + groupSize + ":" + 
                     currentTableau + ":" + averageSatisfaction + ":" + averageFatigue);
        send(msg);
    }
    
    private void startGuidedTour() {
        isGuiding = true;
        currentTableau = 0;
        visitDuration = 0;
        averageSatisfaction = 0.5;
        averageFatigue = 0.0;
        
        System.out.println("Guide " + getLocalName() + " commence la visite avec " + groupSize + " touristes");
        
        // Accueillir tous les touristes
        for (AID tourist : touristAgents) {
            ACLMessage welcome = new ACLMessage(ACLMessage.INFORM);
            welcome.addReceiver(tourist);
            welcome.setContent("WELCOME_GROUP:" + specialization + ":" + currentLocation);
            send(welcome);
        }
        
        // Déplacer vers le premier tableau
        addBehaviour(new WakerBehaviour(this, 2000) {
            protected void onWake() {
                moveToTableau("Tableau1");
            }
        });
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
        if (question.toLowerCase().contains("technique")) {
            return "Cette œuvre utilise une technique particulière de " + specialization.toLowerCase();
        } else if (question.toLowerCase().contains("histoire")) {
            return "L'histoire de cette œuvre remonte à la période " + specialization.toLowerCase();
        } else {
            return "C'est un aspect fascinant de l'art " + specialization.toLowerCase();
        }
    }
    
    private void checkGroupStatus() {
        visitDuration++;
        
        if (averageFatigue > 0.7) {
            proposePause();
        } else if (averageSatisfaction > 0.8 && currentTableau < 5) {
            if (new Random().nextDouble() < 0.3) {
                moveToNextTableau();
            }
        }
    }
    
    private void updateVisitProgress() {
        averageFatigue = Math.min(1.0, averageFatigue + 0.01);
        if (new Random().nextDouble() < 0.1) {
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
            System.out.println("Guide " + getLocalName() + " adapte: groupe fatigué, explications plus courtes");
        } else if (averageSatisfaction > 0.9) {
            System.out.println("Guide " + getLocalName() + " adapte: groupe très intéressé, explications approfondies");
        }
    }
    
    private void adaptExplanationStyle(boolean moreInteractive) {
        if (moreInteractive) {
            System.out.println("Guide " + getLocalName() + " adopte un style plus interactif");
        }
    }
    
    private void proposePause() {
        // Proposer une pause dans la salle de repos
        moveToLocation("SalleRepos");
        
        for (AID tourist : touristAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.addReceiver(tourist);
            msg.setContent("BREAK_PROPOSAL:5");
            send(msg);
        }
        System.out.println("Guide " + getLocalName() + " propose une pause au groupe");
    }
    
    private void moveToLocation(String location) {
        currentLocation = location;
        for (AID tourist : touristAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(tourist);
            msg.setContent("MOVE_TO:" + location);
            send(msg);
        }
    }
    
    private void adaptRoute(String locationToAvoid) {
        if (currentLocation.equals(locationToAvoid)) {
            // Changer d'itinéraire si on est dans la zone à éviter
            String[] alternatives = {"Tableau1", "Tableau2", "Tableau3", "Tableau4", "Tableau5"};
            for (String alt : alternatives) {
                if (!alt.equals(locationToAvoid)) {
                    moveToTableau(alt);
                    break;
                }
            }
        }
    }
    
    private void avoidLocation(String emergencyLocation) {
        System.out.println("Guide " + getLocalName() + " évite " + emergencyLocation + " en raison d'une urgence");
        adaptRoute(emergencyLocation);
    }
    
    private void checkIfGroupReady() {
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
    
    // NOUVEAU : Fin de visite avec notification au coordinateur
    private void endTour() {
        isGuiding = false;
        completedTours++;
        
        // Informer les touristes
        for (AID tourist : touristAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(tourist);
            msg.setContent("TOUR_END:Merci pour cette visite ! J'espère que vous avez apprécié.");
            send(msg);
        }
        
        System.out.println("Guide " + getLocalName() + " termine la visite. Satisfaction finale: " + 
                         String.format("%.2f", averageSatisfaction) + " (Tour #" + completedTours + ")");
        
        // Notifier le coordinateur de la fin de visite
        if (coordinatorAgent != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(coordinatorAgent);
            msg.setContent("TOUR_COMPLETED:" + groupSize + ":" + averageSatisfaction);
            send(msg);
        }
        
        // Se diriger vers la sortie
        moveToLocation("Sortie");
        
        // Préparer le recyclage
        addBehaviour(new WakerBehaviour(this, 2000) {
            protected void onWake() {
                prepareForNextTour();
            }
        });
    }
    
    // NOUVEAU : Préparation pour la prochaine visite
    private void prepareForNextTour() {
        // Réinitialiser pour un nouveau groupe
        touristAgents.clear();
        groupSize = 0;
        currentTableau = 0;
        currentLocation = "PointA";
        averageFatigue = 0.0;
        averageSatisfaction = 0.5;
        visitDuration = 0;
        isGuiding = false;
        isAvailable = true;
        
        System.out.println("Guide " + getLocalName() + " est prêt pour une nouvelle visite (Total complétées: " + completedTours + ")");
        
        // Signaler la disponibilité au coordinateur
        signalAvailability();
    }
    
    // Getters pour l'interface graphique
    public String getCurrentLocation() { return currentLocation; }
    public int getGroupSize() { return groupSize; }
    public boolean isGuiding() { return isGuiding; }
    public boolean isAvailable() { return isAvailable; }
    public double getAverageSatisfaction() { return averageSatisfaction; }
    public double getAverageFatigue() { return averageFatigue; }
    public String getSpecialization() { return specialization; }
    public int getCurrentTableau() { return currentTableau; }
    public int getCompletedTours() { return completedTours; }
    public List<AID> getTouristAgents() { return new ArrayList<>(touristAgents); }
    
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent Guide " + getLocalName() + " terminé (Tours complétés: " + completedTours + ")");
    }
}