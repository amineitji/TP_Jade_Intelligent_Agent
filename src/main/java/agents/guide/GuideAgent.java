package agents.guide;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import utils.ServiceFinder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import agents.base.AgentStatus;

/**
 * Agent Guide avec gestion de groupe améliorée - comportement de berger
 */
public class GuideAgent extends Agent {
    // Propriétés de base
    private GuideProfile profile;
    private AID coordinatorAgent;
    private List<AID> assignedTourists;
    private boolean isGuiding;
    private boolean isAvailable;
    private String currentLocation;
    private int currentTableau;
    
    // Gestion de groupe améliorée
    private GroupManager groupManager;
    private Map<AID, Double> touristSatisfaction;
    private Map<AID, Double> touristFatigue;
    private Map<AID, Double> touristCohesion; // Nouveau: niveau de cohésion individuel
    
    // Stratégie de guidage
    private GroupFormation currentFormation = GroupFormation.CLUSTER;
    private double groupCohesionThreshold = 0.6;
    private boolean waitingForGroup = false;
    private int groupCheckCounter = 0;
    
    private static final int REST_TIME_BETWEEN_TOURS = 5000;
    private static final String[] TABLEAU_SEQUENCE = {
        "Tableau1", "Tableau2", "Tableau3", "Tableau4", "Tableau5"
    };
    
    public enum GroupFormation {
        CLUSTER,    // Regroupement naturel
        CIRCLE,     // Cercle autour du guide
        LINE        // File indienne
    }
    
    @Override
    protected void setup() {
        System.out.println("Agent Guide " + getLocalName() + " démarré avec gestion de groupe avancée");
        
        // Initialisation
        profile = new GuideProfile(getLocalName());
        assignedTourists = new ArrayList<>();
        touristSatisfaction = new HashMap<>();
        touristFatigue = new HashMap<>();
        touristCohesion = new HashMap<>();
        isGuiding = false;
        isAvailable = true;
        currentLocation = "PointA";
        currentTableau = 0;
        
        // Gestionnaire de groupe amélioré
        groupManager = new GroupManager();
        
        // Enregistrement du service
        registerService();
        
        // Ajout des comportements améliorés
        addBehaviour(new CoordinatorCommunicationBehavior());
        addBehaviour(new EnhancedGroupManagementBehavior());
        addBehaviour(new GroupCohesionMonitorBehavior());
        addBehaviour(new PerformanceMonitorBehavior());
        
        // Recherche du coordinateur
        findAndRegisterWithCoordinator();
    }
    
    private void registerService() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("guide-service");
            sd.setName("guide-touristique");
            dfd.addServices(sd);
            DFService.register(this, dfd);
            System.out.println("Guide " + getLocalName() + " enregistré avec spécialisation: " + 
                             profile.getSpecialization());
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    private void findAndRegisterWithCoordinator() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                coordinatorAgent = ServiceFinder.findCoordinator(GuideAgent.this);
                if (coordinatorAgent != null) {
                    sendMessage(coordinatorAgent, ACLMessage.SUBSCRIBE, "REGISTER_GUIDE");
                    System.out.println("Guide " + getLocalName() + " s'enregistre auprès du coordinateur");
                } else {
                    System.out.println("Coordinateur non trouvé");
                }
            }
        });
    }
    
    /**
     * Communication avec le coordinateur
     */
    private class CoordinatorCommunicationBehavior extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
            );
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                handleCoordinatorMessage(msg);
            } else {
                block();
            }
        }
        
        private void handleCoordinatorMessage(ACLMessage msg) {
            String content = msg.getContent();
            
            if ("REGISTRATION_CONFIRMED".equals(content)) {
                System.out.println("Guide " + getLocalName() + " : Enregistrement confirmé");
                isAvailable = true;
            } else if (content.startsWith("ASSIGN_TOURISTS:")) {
                handleTouristAssignment(msg);
            } else if ("TOUR_COMPLETION_ACKNOWLEDGED".equals(content)) {
                System.out.println("Guide " + getLocalName() + " : Fin de visite acknowledgée");
                addBehaviour(new WakerBehaviour(GuideAgent.this, REST_TIME_BETWEEN_TOURS) {
                    @Override
                    protected void onWake() {
                        prepareForNextTour();
                    }
                });
            }
        }
        
        private void handleTouristAssignment(ACLMessage msg) {
            if (!isAvailable) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("GUIDE_BUSY");
                send(reply);
                return;
            }
            
            String content = msg.getContent();
            String[] parts = content.split(":");
            
            if (parts.length >= 2) {
                String[] touristNames = parts[1].split(",");
                
                // Initialiser le groupe
                assignedTourists.clear();
                touristSatisfaction.clear();
                touristFatigue.clear();
                touristCohesion.clear();
                
                for (String name : touristNames) {
                    if (!name.trim().isEmpty()) {
                        AID touristAID = new AID(name.trim(), AID.ISLOCALNAME);
                        assignedTourists.add(touristAID);
                        touristSatisfaction.put(touristAID, 0.5);
                        touristFatigue.put(touristAID, 0.0);
                        touristCohesion.put(touristAID, 0.7); // Cohésion initiale
                    }
                }
                
                isAvailable = false;
                groupManager.initialize(assignedTourists.size());
                
                // Confirmer l'acceptation
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.AGREE);
                reply.setContent("TOURISTS_ACCEPTED:" + assignedTourists.size());
                send(reply);
                
                System.out.println("Guide " + getLocalName() + " accepte un groupe de " + 
                                 assignedTourists.size() + " touristes");
                
                // Démarrer la visite avec formation de groupe
                addBehaviour(new WakerBehaviour(GuideAgent.this, 3000) {
                    @Override
                    protected void onWake() {
                        startGuidedTour();
                    }
                });
            }
        }
    }
    
    /**
     * Gestion de groupe améliorée avec comportement de berger
     */
    private class EnhancedGroupManagementBehavior extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)
            );
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.startsWith("STATUS:")) {
                    processTouristStatusWithCohesion(msg);
                } else if (content.startsWith("QUESTION:")) {
                    answerQuestionToGroup(msg);
                } else if ("READY_NEXT".equals(content)) {
                    handleTouristReady(msg.getSender());
                } else if (content.startsWith("TOURIST_READY:")) {
                    handleNewTouristInGroup(msg);
                } else if (content.startsWith("GROUP_COHESION:")) {
                    updateTouristCohesion(msg);
                } else if (content.startsWith("JOIN_GROUP")) {
                    redirectToCoordinator(msg.getSender());
                }
            } else {
                block();
            }
        }
        
        private void handleNewTouristInGroup(ACLMessage msg) {
            AID touristAID = msg.getSender();
            
            // Accueillir le nouveau membre et expliquer la formation de groupe
            String welcomeMsg = String.format("WELCOME_GROUP:%s:%s", 
                    profile.getSpecialization(), currentLocation);
            sendMessage(touristAID, ACLMessage.INFORM, welcomeMsg);
            
            // Donner des instructions de formation de groupe
            String formationMsg = "GROUP_FORMATION:" + currentFormation.toString();
            sendMessage(touristAID, ACLMessage.INFORM, formationMsg);
            
            System.out.println("Guide " + getLocalName() + " accueille " + 
                             touristAID.getLocalName() + " dans le groupe en formation " + 
                             currentFormation);
        }
        
        private void updateTouristCohesion(ACLMessage msg) {
            String[] parts = msg.getContent().split(":");
            if (parts.length >= 2) {
                double cohesion = Double.parseDouble(parts[1]);
                touristCohesion.put(msg.getSender(), cohesion);
                
                // Vérifier si le groupe a besoin de regroupement
                checkGroupCohesionAndAdjust();
            }
        }
    }
    
    /**
     * Surveillance de la cohésion de groupe
     */
    private class GroupCohesionMonitorBehavior extends TickerBehaviour {
        public GroupCohesionMonitorBehavior() {
            super(GuideAgent.this, 8000); // Vérification toutes les 8 secondes
        }
        
        @Override
        protected void onTick() {
            if (isGuiding && !assignedTourists.isEmpty()) {
                monitorGroupCohesion();
                adjustGuidanceStrategy();
            }
        }
        
        private void monitorGroupCohesion() {
            double averageCohesion = getAverageGroupCohesion();
            
            System.out.println("Guide " + getLocalName() + " surveille le groupe - Cohésion: " + 
                             String.format("%.2f", averageCohesion));
            
            if (averageCohesion < groupCohesionThreshold) {
                regroupTourists();
            } else if (averageCohesion > 0.8) {
                // Groupe très cohésif, on peut accélérer un peu
                groupManager.setOptimalPace(true);
            }
        }
        
        private void adjustGuidanceStrategy() {
            double avgSatisfaction = getAverageSatisfaction();
            double avgFatigue = getAverageFatigue();
            double avgCohesion = getAverageGroupCohesion();
            
            // Ajuster la stratégie selon l'état du groupe
            if (avgFatigue > 0.7) {
                proposePause();
                currentFormation = GroupFormation.CLUSTER; // Formation plus relaxée
            } else if (avgCohesion < 0.5) {
                // Groupe dispersé, formation plus stricte
                changeGroupFormation(GroupFormation.LINE);
                slowDownForCohesion();
            } else if (avgSatisfaction > 0.8 && avgCohesion > 0.7) {
                // Groupe engagé et cohésif
                currentFormation = GroupFormation.CIRCLE;
                encourageParticipation();
            }
        }
    }
    
    /**
     * Surveillance des performances avec métriques de groupe
     */
    private class PerformanceMonitorBehavior extends TickerBehaviour {
        public PerformanceMonitorBehavior() {
            super(GuideAgent.this, 12000);
        }
        
        @Override
        protected void onTick() {
            if (coordinatorAgent != null && isGuiding) {
                sendEnhancedPerformanceReport();
            }
        }
        
        private void sendEnhancedPerformanceReport() {
            double avgSatisfaction = getAverageSatisfaction();
            double avgFatigue = getAverageFatigue();
            double avgCohesion = getAverageGroupCohesion();
            
            String report = String.format("ENHANCED_REPORT:%s:%d:%d:%.2f:%.2f:%.2f:%s",
                    currentLocation,
                    assignedTourists.size(),
                    currentTableau,
                    avgSatisfaction,
                    avgFatigue,
                    avgCohesion,
                    currentFormation.toString());
            
            sendMessage(coordinatorAgent, ACLMessage.INFORM, report);
        }
    }
    
    // Méthodes principales améliorées
    
    private void startGuidedTour() {
        isGuiding = true;
        currentTableau = 0;
        waitingForGroup = false;
        
        System.out.println("Guide " + getLocalName() + " commence la visite guidée avec " + 
                         assignedTourists.size() + " touristes");
        
        // Formation initiale du groupe
        formInitialGroup();
        
        // Démarrer la visite
        addBehaviour(new WakerBehaviour(this, 3000) {
            @Override
            protected void onWake() {
                moveToTableau(TABLEAU_SEQUENCE[0]);
            }
        });
    }
    
    private void formInitialGroup() {
        // Demander à tous les touristes de se former en groupe
        currentFormation = GroupFormation.CLUSTER;
        
        for (AID tourist : assignedTourists) {
            String welcomeMsg = String.format("WELCOME_GROUP:%s:%s",
                    profile.getSpecialization(), currentLocation);
            sendMessage(tourist, ACLMessage.INFORM, welcomeMsg);
            
            // Instructions de formation
            String formationMsg = "GROUP_FORMATION:" + currentFormation.toString();
            sendMessage(tourist, ACLMessage.INFORM, formationMsg);
        }
        
        System.out.println("Guide " + getLocalName() + " forme le groupe initial en " + currentFormation);
    }
    
    private void moveToTableau(String tableau) {
        currentLocation = tableau;
        currentTableau++;
        
        // Vérifier la cohésion avant le déplacement
        ensureGroupCohesion();
        
        // Informer tous les touristes du déplacement avec instruction de groupe
        for (AID tourist : assignedTourists) {
            sendMessage(tourist, ACLMessage.INFORM, "MOVE_TO:" + tableau);
        }
        
        // Attendre que le groupe soit prêt avant de commencer l'explication
        waitingForGroup = true;
        groupCheckCounter = 0;
        
        addBehaviour(new WakerBehaviour(this, 4000) {
            @Override
            protected void onWake() {
                if (isGroupReady()) {
                    startExplanation(tableau);
                } else {
                    waitForGroup(tableau);
                }
            }
        });
        
        System.out.println("Guide " + getLocalName() + " emmène le groupe vers " + tableau);
    }
    
    private void waitForGroup(String tableau) {
        groupCheckCounter++;
        
        if (groupCheckCounter >= 3) { // Après 3 tentatives
            regroupTourists();
            addBehaviour(new WakerBehaviour(this, 3000) {
                @Override
                protected void onWake() {
                    startExplanation(tableau);
                }
            });
        } else {
            // Réessayer
            addBehaviour(new WakerBehaviour(this, 2000) {
                @Override
                protected void onWake() {
                    if (isGroupReady()) {
                        startExplanation(tableau);
                    } else {
                        waitForGroup(tableau);
                    }
                }
            });
        }
    }
    
    private void startExplanation(String tableau) {
        waitingForGroup = false;
        
        Map<String, String> tableauInfo = initializeTableauInfo();
        String explanation = tableauInfo.get(tableau);
        if (explanation == null) {
            explanation = "Œuvre remarquable de notre collection permanente.";
        }
        
        // Adapter selon la spécialisation et la cohésion du groupe
        if (isTableauInSpecialization(tableau)) {
            explanation += " - Explication spécialisée en " + profile.getSpecialization();
        }
        
        // Adapter le style selon la cohésion du groupe
        double avgCohesion = getAverageGroupCohesion();
        if (avgCohesion > 0.8) {
            explanation += " [Version interactive pour groupe cohésif]";
        } else if (avgCohesion < 0.5) {
            explanation += " [Version structurée pour regrouper l'attention]";
        }
        
        // Formation optimale pour l'écoute
        changeGroupFormation(GroupFormation.CIRCLE);
        
        // Diffuser l'explication
        for (AID tourist : assignedTourists) {
            sendMessage(tourist, ACLMessage.INFORM, "EXPLANATION:" + explanation);
        }
        
        System.out.println("Guide " + getLocalName() + " explique " + tableau + 
                         " au groupe en formation " + currentFormation);
    }
    
    private void processTouristStatusWithCohesion(ACLMessage msg) {
        String[] parts = msg.getContent().split(":");
        if (parts.length >= 3) {
            String status = parts[1];
            double value = Double.parseDouble(parts[2]);
            AID touristAID = msg.getSender();
            
            switch (status) {
                case "SATISFACTION":
                    touristSatisfaction.put(touristAID, value);
                    break;
                case "FATIGUE":
                    touristFatigue.put(touristAID, value);
                    break;
                case "GROUP_COHESION":
                    touristCohesion.put(touristAID, value);
                    break;
            }
            
            // Réaction adaptée selon l'état du groupe
            double avgCohesion = getAverageGroupCohesion();
            if (avgCohesion < groupCohesionThreshold) {
                regroupTourists();
            }
            
            if (getAverageFatigue() > 0.8) {
                proposePause();
            }
        }
    }
    
    private void answerQuestionToGroup(ACLMessage questionMsg) {
        String question = questionMsg.getContent().substring(9);
        
        // Répondre à tout le groupe, pas seulement à celui qui a posé la question
        String answer = generateAnswer(question);
        
        for (AID tourist : assignedTourists) {
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.addReceiver(tourist);
            reply.setContent("ANSWER:" + answer);
            send(reply);
        }
        
        System.out.println("Guide " + getLocalName() + " répond à une question pour tout le groupe");
    }
    
    // Méthodes de gestion de groupe
    
    private void ensureGroupCohesion() {
        double avgCohesion = getAverageGroupCohesion();
        if (avgCohesion < groupCohesionThreshold) {
            regroupTourists();
        }
    }
    
    private void regroupTourists() {
        System.out.println("Guide " + getLocalName() + " regroupe les touristes (cohésion faible)");
        
        // Changer vers une formation plus stricte
        changeGroupFormation(GroupFormation.LINE);
        
        // Envoyer un message de regroupement
        for (AID tourist : assignedTourists) {
            sendMessage(tourist, ACLMessage.INFORM, "REGROUP_PLEASE:Veuillez vous rapprocher du groupe");
        }
        
        // Attendre un peu pour le regroupement
        addBehaviour(new WakerBehaviour(this, 3000) {
            @Override
            protected void onWake() {
                // Revenir à la formation normale
                changeGroupFormation(GroupFormation.CLUSTER);
            }
        });
    }
    
    private void changeGroupFormation(GroupFormation newFormation) {
        if (currentFormation != newFormation) {
            currentFormation = newFormation;
            
            // Informer tous les touristes du changement
            for (AID tourist : assignedTourists) {
                sendMessage(tourist, ACLMessage.INFORM, 
                          "GROUP_FORMATION:" + newFormation.toString());
            }
            
            System.out.println("Guide " + getLocalName() + " change la formation du groupe vers " + 
                             newFormation);
        }
    }
    
    private void slowDownForCohesion() {
        groupManager.setSlowMode(true);
        
        // Informer les touristes de ralentir
        for (AID tourist : assignedTourists) {
            sendMessage(tourist, ACLMessage.INFORM, "SLOW_DOWN:Ralentissons le rythme");
        }
        
        System.out.println("Guide " + getLocalName() + " ralentit pour maintenir la cohésion");
    }
    
    private void encourageParticipation() {
        // Encourager les questions et interactions
        for (AID tourist : assignedTourists) {
            sendMessage(tourist, ACLMessage.INFORM, 
                      "ENCOURAGE_PARTICIPATION:N'hésitez pas à poser vos questions");
        }
        
        System.out.println("Guide " + getLocalName() + " encourage la participation du groupe");
    }
    
    private void checkGroupCohesionAndAdjust() {
        double avgCohesion = getAverageGroupCohesion();
        
        if (avgCohesion < 0.4) {
            // Cohésion très faible, intervention nécessaire
            regroupTourists();
        } else if (avgCohesion < groupCohesionThreshold) {
            // Cohésion faible, ajustements légers
            if (currentFormation == GroupFormation.CLUSTER) {
                changeGroupFormation(GroupFormation.LINE);
            }
        }
    }
    
    private boolean isGroupReady() {
        double avgCohesion = getAverageGroupCohesion();
        return avgCohesion >= groupCohesionThreshold;
    }
    
    private void handleTouristReady(AID touristAID) {
        System.out.println("Guide " + getLocalName() + " : " + 
                         touristAID.getLocalName() + " est prêt");
        
        // Vérifier si tout le groupe est prêt
        // Pour simplifier, on continue après quelques secondes
        addBehaviour(new WakerBehaviour(this, 2000) {
            @Override
            protected void onWake() {
                checkIfGroupReadyToContinue();
            }
        });
    }
    
    private void checkIfGroupReadyToContinue() {
        if (currentTableau < TABLEAU_SEQUENCE.length) {
            addBehaviour(new WakerBehaviour(this, 5000) {
                @Override
                protected void onWake() {
                    moveToNextTableau();
                }
            });
        } else {
            endTour();
        }
    }
    
    private void moveToNextTableau() {
        if (currentTableau < TABLEAU_SEQUENCE.length) {
            String nextTableau = TABLEAU_SEQUENCE[currentTableau];
            moveToTableau(nextTableau);
        } else {
            endTour();
        }
    }
    
    protected void endTour() {
        isGuiding = false;
        double groupSatisfaction = getAverageSatisfaction();
        double groupCohesion = getAverageGroupCohesion();
        
        // Mettre à jour le profil avec les métriques de groupe
        profile.updatePerformance(groupSatisfaction, getAverageFatigue());
        groupManager.recordTourCompletion(groupSatisfaction, groupCohesion);
        
        // Formation finale pour les remerciements
        changeGroupFormation(GroupFormation.CIRCLE);
        
        // Informer les touristes de la fin
        for (AID tourist : assignedTourists) {
            String endMessage = String.format("TOUR_END:Merci pour cette visite guidée ! " +
                    "Satisfaction du groupe: %.2f - Cohésion: %.2f", 
                    groupSatisfaction, groupCohesion);
            sendMessage(tourist, ACLMessage.INFORM, endMessage);
        }
        
        System.out.println("Guide " + getLocalName() + " termine la visite de groupe - " +
                         "Satisfaction: " + String.format("%.2f", groupSatisfaction) +
                         ", Cohésion: " + String.format("%.2f", groupCohesion));
        
        // Notifier le coordinateur avec métriques de groupe
        if (coordinatorAgent != null) {
            String completionMsg = String.format("TOUR_COMPLETED:%d:%.2f:%.2f:%s",
                    assignedTourists.size(), groupSatisfaction, groupCohesion, 
                    currentFormation.toString());
            sendMessage(coordinatorAgent, ACLMessage.INFORM, completionMsg);
        }
        
        // Se diriger vers la sortie avec le groupe
        currentLocation = "Sortie";
        for (AID tourist : assignedTourists) {
            sendMessage(tourist, ACLMessage.INFORM, "MOVE_TO:Sortie");
        }
        
        // Préparer le recyclage
        addBehaviour(new WakerBehaviour(this, 3000) {
            @Override
            protected void onWake() {
                prepareForNextTour();
            }
        });
    }
    
    private void proposePause() {
        currentLocation = "SalleRepos";
        changeGroupFormation(GroupFormation.CLUSTER); // Formation relaxée pour la pause
        
        for (AID tourist : assignedTourists) {
            sendMessage(tourist, ACLMessage.INFORM, "MOVE_TO:SalleRepos");
            sendMessage(tourist, ACLMessage.PROPOSE, "BREAK_PROPOSAL:5");
        }
        System.out.println("Guide " + getLocalName() + " propose une pause au groupe fatigué");
    }
    
    private void prepareForNextTour() {
        // Réinitialiser pour un nouveau groupe
        assignedTourists.clear();
        touristSatisfaction.clear();
        touristFatigue.clear();
        touristCohesion.clear();
        currentTableau = 0;
        currentLocation = "PointA";
        isGuiding = false;
        isAvailable = true;
        waitingForGroup = false;
        groupCheckCounter = 0;
        
        // Réinitialiser la formation et le gestionnaire
        currentFormation = GroupFormation.CLUSTER;
        groupManager.reset();
        
        System.out.println("Guide " + getLocalName() + " est prêt pour une nouvelle visite de groupe " +
                         "(Tours complétés: " + profile.getCompletedTours() + 
                         ", Efficacité groupe: " + String.format("%.2f", groupManager.getGroupEfficiency()) + ")");
        
        // Signaler la disponibilité
        if (coordinatorAgent != null) {
            sendMessage(coordinatorAgent, ACLMessage.INFORM, "GUIDE_AVAILABLE");
        }
    }
    
    private void redirectToCoordinator(AID touristAID) {
        sendMessage(touristAID, ACLMessage.INFORM, "REDIRECT_TO_COORDINATOR");
        System.out.println("Guide " + getLocalName() + " redirige " + 
                         touristAID.getLocalName() + " vers le coordinateur");
    }
    
    // Méthodes utilitaires améliorées
    
    protected void sendMessage(AID receiver, int performative, String content) {
        ACLMessage message = new ACLMessage(performative);
        message.addReceiver(receiver);
        message.setContent(content);
        send(message);
    }
    
    private double getAverageSatisfaction() {
        if (touristSatisfaction.isEmpty()) return 0.5;
        return touristSatisfaction.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.5);
    }
    
    private double getAverageFatigue() {
        if (touristFatigue.isEmpty()) return 0.0;
        return touristFatigue.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    private double getAverageGroupCohesion() {
        if (touristCohesion.isEmpty()) return 0.7; // Valeur par défaut
        return touristCohesion.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.7);
    }
    
    private String generateAnswer(String question) {
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.contains("technique")) {
            return "Cette œuvre utilise une technique particulière de " + 
                   profile.getSpecialization().toLowerCase() + 
                   ". [Explication adaptée au niveau du groupe]";
        } else if (lowerQuestion.contains("histoire")) {
            return "L'histoire de cette œuvre remonte à la période " + 
                   profile.getSpecialization().toLowerCase() + 
                   ". [Contexte historique pour le groupe]";
        } else if (lowerQuestion.contains("artiste")) {
            return "L'artiste était un maître de son époque, particulièrement reconnu pour " +
                   "son approche innovante dans le style " + profile.getSpecialization().toLowerCase();
        } else {
            return "Excellente question ! C'est un aspect fascinant de l'art " + 
                   profile.getSpecialization().toLowerCase() + 
                   " que nous pouvons explorer ensemble";
        }
    }
    
    private boolean isTableauInSpecialization(String tableau) {
        String specialization = profile.getSpecialization();
        
        switch (tableau) {
            case "Tableau1": // La Joconde
            case "Tableau5": // L'École d'Athènes
                return "Renaissance".equals(specialization);
            case "Tableau3": // Guernica
            case "Tableau4": // Les Demoiselles d'Avignon
                return "Moderne".equals(specialization);
            case "Tableau2": // La Nuit étoilée
                return "Impressionniste".equals(specialization);
            default:
                return false;
        }
    }
    
    private Map<String, String> initializeTableauInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("Tableau1", "La Joconde - Chef-d'œuvre de Léonard de Vinci, symbole de l'art Renaissance");
        info.put("Tableau2", "La Nuit étoilée - Œuvre emblématique de Van Gogh, post-impressionnisme");
        info.put("Tableau3", "Guernica - Picasso, art moderne, dénonciation de la guerre");
        info.put("Tableau4", "Les Demoiselles d'Avignon - Picasso, naissance du cubisme");
        info.put("Tableau5", "L'École d'Athènes - Raphaël, Renaissance italienne, philosophie");
        return info;
    }
    
    // Getters pour compatibilité avec d'autres classes
    public GuideProfile getProfile() { return profile; }
    public List<AID> getAssignedTourists() { return new ArrayList<>(assignedTourists); }
    public boolean isGuiding() { return isGuiding; }
    public boolean isAvailable() { return isAvailable; }
    public AID getCoordinatorAgent() { return coordinatorAgent; }
    public String getCurrentLocation() { return currentLocation; }
    public int getCurrentTableau() { return currentTableau; }
    public GroupFormation getCurrentFormation() { return currentFormation; }
    public double getGroupCohesionThreshold() { return groupCohesionThreshold; }
    public GroupManager getGroupManager() { return groupManager; }
    
    // Méthodes pour compatibilité avec l'ancien système
    public TourManager getTourManager() {
        return new TourManager(this, profile) {
            @Override
            public void moveToNextTableau() {
                GuideAgent.this.moveToNextTableau();
            }
            
            @Override
            public void proposePause() {
                GuideAgent.this.proposePause();
            }
        };
    }

    public GroupHandler getGroupHandler() {
        return new GroupHandler(this) {
            @Override
            public boolean isGroupReady() { 
                return GuideAgent.this.isGroupReady(); 
            }
            @Override
            public double getAverageSatisfaction() { 
                return GuideAgent.this.getAverageSatisfaction(); 
            }
            @Override
            public double getAverageFatigue() { 
                return GuideAgent.this.getAverageFatigue(); 
            }
        };
    }

    public AgentStatus getStatus() {
        return new AgentStatus() {
            public void setCurrentLocation(String location) {
                currentLocation = location;
            }
        };
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent Guide " + getLocalName() + " terminé - " +
                         "Efficacité de groupe finale: " + 
                         String.format("%.2f", groupManager.getGroupEfficiency()));
    }
    
    /**
     * Gestionnaire spécialisé pour les groupes
     */
    public class GroupManager {
        private int totalGroups = 0;
        private double totalGroupSatisfaction = 0.0;
        private double totalGroupCohesion = 0.0;
        private boolean slowMode = false;
        private boolean optimalPace = false;
        private int currentGroupSize = 0;
        
        public void initialize(int groupSize) {
            this.currentGroupSize = groupSize;
            this.slowMode = false;
            this.optimalPace = false;
        }
        
        public void recordTourCompletion(double satisfaction, double cohesion) {
            totalGroups++;
            totalGroupSatisfaction += satisfaction;
            totalGroupCohesion += cohesion;
        }
        
        public void setSlowMode(boolean slow) {
            this.slowMode = slow;
            if (slow) {
                this.optimalPace = false;
            }
        }
        
        public void setOptimalPace(boolean optimal) {
            this.optimalPace = optimal;
            if (optimal) {
                this.slowMode = false;
            }
        }
        
        public double getGroupEfficiency() {
            if (totalGroups == 0) return 0.5;
            double avgSatisfaction = totalGroupSatisfaction / totalGroups;
            double avgCohesion = totalGroupCohesion / totalGroups;
            return (avgSatisfaction * 0.6) + (avgCohesion * 0.4);
        }
        
        public void reset() {
            slowMode = false;
            optimalPace = false;
            currentGroupSize = 0;
        }
        
        // Getters
        public int getTotalGroups() { return totalGroups; }
        public boolean isSlowMode() { return slowMode; }
        public boolean isOptimalPace() { return optimalPace; }
        public int getCurrentGroupSize() { return currentGroupSize; }
        public double getAverageGroupSatisfaction() { 
            return totalGroups > 0 ? totalGroupSatisfaction / totalGroups : 0.5; 
        }
        public double getAverageGroupCohesion() { 
            return totalGroups > 0 ? totalGroupCohesion / totalGroups : 0.7; 
        }
    }
}