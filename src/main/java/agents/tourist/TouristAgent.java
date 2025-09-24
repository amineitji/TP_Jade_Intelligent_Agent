package agents.tourist;

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
import java.util.Random;

/**
 * Agent Touriste avec comportement de groupe amélioré - effet "mouton"
 */
public class TouristAgent extends Agent {
    // Propriétés de base
    private TouristProfile profile;
    private AID guideAgent;
    private AID coordinatorAgent;
    private String currentLocation;
    private boolean inGroup;
    private boolean waitingForAssignment;
    private long waitStartTime;
    private int questionsAsked;
    private int toursCompleted;
    
    // Comportement de groupe amélioré
    private GroupBehavior groupBehavior;
    private double cohesionRadius = 50.0; // Distance de cohésion avec le groupe
    private double separationRadius = 15.0; // Distance minimale entre touristes
    private boolean followingGuide = false;
    private double groupPosition = 0; // Position dans le groupe (0-1)
    
    private static final long MAX_WAIT_TIME = 60000; // 1 minute
    
    @Override
    protected void setup() {
        System.out.println("Agent Touriste " + getLocalName() + " démarré avec comportement de groupe");
        
        // Initialisation
        profile = new TouristProfile(getLocalName());
        currentLocation = "PointA";
        inGroup = false;
        waitingForAssignment = false;
        questionsAsked = 0;
        toursCompleted = 0;
        
        // Initialiser le comportement de groupe
        groupBehavior = new GroupBehavior();
        groupPosition = Math.random(); // Position aléatoire dans le groupe
        
        // Enregistrement du service
        registerService();
        
        // Comportements améliorés avec cohésion de groupe
        addBehaviour(new CoordinatorRegistrationBehavior());
        addBehaviour(new EnhancedGuideInteractionBehavior());
        addBehaviour(new GroupCohesionBehavior());
        addBehaviour(new PersonalityBehavior());
        addBehaviour(new WaitingTimeoutBehavior());
        
        // Recherche et enregistrement auprès du coordinateur
        findAndRegisterWithCoordinator();
    }
    
    private void registerService() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("tourist-service");
            sd.setName("museum-visitor");
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    private void findAndRegisterWithCoordinator() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                // Attendre un peu avant de chercher (simulation arrivée échelonnée)
                try {
                    Thread.sleep(new Random().nextInt(3000) + 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Rechercher le coordinateur
                coordinatorAgent = ServiceFinder.findCoordinator(TouristAgent.this);
                if (coordinatorAgent != null) {
                    System.out.println("Touriste " + getLocalName() + " trouvé coordinateur");
                    registerWithCoordinator();
                } else {
                    // Fallback : chercher directement un guide
                    findGuideDirectly();
                }
            }
        });
    }
    
    private void registerWithCoordinator() {
        ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
        msg.addReceiver(coordinatorAgent);
        msg.setContent("REGISTER_TOURIST");
        send(msg);
        
        waitingForAssignment = true;
        waitStartTime = System.currentTimeMillis();
        
        System.out.println("Touriste " + getLocalName() + " s'enregistre auprès du coordinateur");
    }
    
    private void findGuideDirectly() {
        guideAgent = ServiceFinder.findAgent(this, "guide-service");
        if (guideAgent != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(guideAgent);
            msg.setContent("JOIN_GROUP");
            send(msg);
            System.out.println("Touriste " + getLocalName() + " demande directement au guide");
        }
    }
    
    /**
     * Enregistrement auprès du coordinateur
     */
    private class CoordinatorRegistrationBehavior extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                if ("WELCOME_MUSEUM".equals(content)) {
                    System.out.println("Touriste " + getLocalName() + " : Bienvenue au musée reçue");
                } else if (content.startsWith("ASSIGNED_TO_GUIDE:")) {
                    handleGuideAssignment(content);
                } else if ("REDIRECT_TO_COORDINATOR".equals(content)) {
                    System.out.println("Touriste " + getLocalName() + " : Redirigé vers le coordinateur");
                    if (coordinatorAgent != null) {
                        registerWithCoordinator();
                    }
                }
            } else {
                block();
            }
        }
        
        private void handleGuideAssignment(String content) {
            String guideName = content.split(":")[1];
            guideAgent = new AID(guideName, AID.ISLOCALNAME);
            waitingForAssignment = false;
            followingGuide = true;
            
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(guideAgent);
            msg.setContent("TOURIST_READY:" + getLocalName());
            send(msg);
            
            System.out.println("Touriste " + getLocalName() + " assigné au guide " + guideName + 
                             " - Position dans le groupe: " + String.format("%.2f", groupPosition));
        }
    }
    
    /**
     * Interaction améliorée avec le guide avec comportement de groupe
     */
    private class EnhancedGuideInteractionBehavior extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);
            
            if (msg != null && guideAgent != null && msg.getSender().equals(guideAgent)) {
                handleGuideMessage(msg);
            } else {
                block();
            }
        }
        
        private void handleGuideMessage(ACLMessage msg) {
            String content = msg.getContent();
            
            if (content.startsWith("WELCOME_GROUP:")) {
                handleWelcomeWithGroupFormation(content);
            } else if (content.startsWith("MOVE_TO:")) {
                handleGroupMovement(content);
            } else if (content.startsWith("EXPLANATION:")) {
                handleExplanationInGroup(content);
            } else if (content.startsWith("BREAK_PROPOSAL:")) {
                handleBreakProposalWithGroup(content);
            } else if (content.startsWith("TOUR_END:")) {
                handleTourEnd(content);
            } else if (content.startsWith("ANSWER:")) {
                handleAnswer(content);
            } else if (content.startsWith("GROUP_FORMATION:")) {
                handleGroupFormation(content);
            }
        }
        
        private void handleWelcomeWithGroupFormation(String content) {
            String[] parts = content.split(":");
            if (parts.length >= 3) {
                String specialization = parts[1];
                currentLocation = parts[2];
                inGroup = true;
                waitingForAssignment = false;
                
                // Évaluer la compatibilité avec la spécialisation
                double compatibility = profile.getAffinityFor(specialization);
                profile.updateSatisfaction(compatibility);
                
                // Activer le comportement de groupe
                groupBehavior.activate();
                
                System.out.println("Touriste " + getLocalName() + " rejoint groupe spécialisé en " + 
                                 specialization + " - Comportement de groupe activé");
            }
        }
        
        private void handleGroupMovement(String content) {
            String destination = content.substring(8);
            currentLocation = destination;
            profile.increaseFatigue(0.05);
            
            // Comportement de mouton : se déplacer en groupe avec variation
            groupBehavior.setDestination(destination);
            groupBehavior.followGuide();
            
            System.out.println("Touriste " + getLocalName() + " suit le groupe vers " + destination);
            
            // Réponse avec délai variable selon la position dans le groupe
            long delay = (long) (1000 + (groupPosition * 2000)); // Entre 1-3 secondes
            addBehaviour(new WakerBehaviour(TouristAgent.this, delay) {
                @Override
                protected void onWake() {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(guideAgent);
                    msg.setContent("READY_NEXT");
                    send(msg);
                }
            });
        }
        
        private void handleExplanationInGroup(String content) {
            String explanation = content.substring(12);
            double rating = evaluateExplanationInGroupContext(explanation);
            
            profile.updateSatisfaction(rating);
            
            // Réaction en groupe : certains posent des questions, d'autres écoutent
            boolean shouldReact = groupBehavior.shouldReactToExplanation(profile);
            
            System.out.println("Touriste " + getLocalName() + " écoute en groupe - Note: " + 
                             String.format("%.2f", rating) + (shouldReact ? " (réaction active)" : " (écoute passive)"));
            
            // Possibilité de poser une question (influencée par la dynamique de groupe)
            if (shouldReact && profile.shouldAskQuestion() && questionsAsked < 3) {
                long questionDelay = (long) (2000 + (Math.random() * 3000)); // Délai aléatoire
                addBehaviour(new WakerBehaviour(TouristAgent.this, questionDelay) {
                    @Override
                    protected void onWake() {
                        askQuestionInGroup();
                    }
                });
            }
        }
        
        private void handleBreakProposalWithGroup(String content) {
            boolean acceptBreak = groupBehavior.shouldAcceptBreak(profile);
            
            int performative = acceptBreak ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL;
            ACLMessage reply = new ACLMessage(performative);
            reply.addReceiver(guideAgent);
            reply.setContent("BREAK_RESPONSE:" + (acceptBreak ? "ACCEPT" : "REJECT"));
            send(reply);
            
            if (acceptBreak) {
                profile.rest(0.3);
                System.out.println("Touriste " + getLocalName() + " accepte la pause avec le groupe");
            } else {
                System.out.println("Touriste " + getLocalName() + " préfère continuer avec le groupe");
            }
        }
        
        private void handleGroupFormation(String content) {
            // Le guide demande au groupe de se former
            String[] parts = content.split(":");
            if (parts.length >= 2) {
                String formation = parts[1]; // CIRCLE, LINE, CLUSTER
                groupBehavior.formGroup(formation);
                System.out.println("Touriste " + getLocalName() + " forme le groupe en " + formation);
            }
        }
        
        private void handleTourEnd(String content) {
            double finalSatisfaction = profile.calculateFinalSatisfaction();
            profile.completeTour();
            toursCompleted++;
            
            // Désactiver le comportement de groupe
            groupBehavior.deactivate();
            followingGuide = false;
            
            System.out.println("Touriste " + getLocalName() + " termine la visite en groupe #" + 
                             toursCompleted + " - Satisfaction finale: " + String.format("%.2f", finalSatisfaction));
            
            inGroup = false;
            guideAgent = null;
            
            // Décision pour la suite
            addBehaviour(new WakerBehaviour(TouristAgent.this, 5000) {
                @Override
                protected void onWake() {
                    decideNextAction(finalSatisfaction);
                }
            });
        }
        
        private void handleAnswer(String content) {
            String answer = content.substring(7);
            profile.setSatisfaction(profile.getSatisfaction() + 0.1);
            System.out.println("Touriste " + getLocalName() + " reçoit une réponse personnalisée: " + answer);
        }
    }
    
    /**
     * Nouveau comportement pour maintenir la cohésion de groupe
     */
    private class GroupCohesionBehavior extends TickerBehaviour {
        public GroupCohesionBehavior() {
            super(TouristAgent.this, 3000 + new Random().nextInt(2000)); // 3-5 secondes
        }
        
        @Override
        protected void onTick() {
            if (inGroup && followingGuide) {
                maintainGroupCohesion();
                updateGroupDynamics();
            }
        }
        
        private void maintainGroupCohesion() {
            // Simuler la cohésion du groupe
            if (Math.random() < 0.3) { // 30% de chance de vérifier la cohésion
                // Envoyer un signal de cohésion au guide
                ACLMessage cohesionMsg = new ACLMessage(ACLMessage.INFORM);
                cohesionMsg.addReceiver(guideAgent);
                cohesionMsg.setContent("GROUP_COHESION:" + 
                                     String.format("%.2f", groupBehavior.getCohesionLevel()));
                send(cohesionMsg);
            }
        }
        
        private void updateGroupDynamics() {
            // Mettre à jour la dynamique de groupe
            groupBehavior.updateDynamics(profile);
            
            // Ajuster la position dans le groupe selon la personnalité
            if (profile.getPersonality().getSocialness() > 0.7) {
                groupPosition = Math.min(0.9, groupPosition + 0.1); // Se rapprocher du guide
            } else if (profile.getPersonality().getSocialness() < 0.3) {
                groupPosition = Math.max(0.1, groupPosition - 0.1); // Rester en arrière
            }
        }
    }
    
    /**
     * Comportement de personnalité adapté au groupe
     */
    private class PersonalityBehavior extends TickerBehaviour {
        public PersonalityBehavior() {
            super(TouristAgent.this, 10000 + new Random().nextInt(5000));
        }
        
        @Override
        protected void onTick() {
            if (inGroup) {
                updatePersonalStateInGroup();
                sendStatusToGuide();
                
                // Comportements spécifiques au groupe
                if (groupBehavior.isActive()) {
                    executeGroupPersonalityBehaviors();
                }
            }
        }
        
        private void executeGroupPersonalityBehaviors() {
            Personality personality = profile.getPersonality();
            
            // Comportement social dans le groupe
            if (personality.getSocialness() > 0.7 && Math.random() < 0.2) {
                expressOpinionToGroup();
            }
            
            // Comportement d'aide aux autres membres du groupe
            if (personality.getPatience() > 0.6 && Math.random() < 0.15) {
                helpOtherTourists();
            }
            
            // Comportement de leadership naturel
            if (personality.getExperience() > 0.7 && Math.random() < 0.1) {
                suggestToGroup();
            }
        }
    }
    
    /**
     * Gestionnaire de timeout d'attente amélioré
     */
    private class WaitingTimeoutBehavior extends TickerBehaviour {
        public WaitingTimeoutBehavior() {
            super(TouristAgent.this, 15000);
        }
        
        @Override
        protected void onTick() {
            if (waitingForAssignment) {
                long waitTime = System.currentTimeMillis() - waitStartTime;
                
                if (waitTime > MAX_WAIT_TIME) {
                    System.out.println("Touriste " + getLocalName() + " : Timeout, recherche active d'un groupe");
                    waitingForAssignment = false;
                    findGuideDirectly();
                } else if (waitTime > MAX_WAIT_TIME / 2) {
                    System.out.println("Touriste " + getLocalName() + " attend un groupe depuis " + 
                                     (waitTime / 1000) + " secondes...");
                }
            }
        }
    }
    
    // Méthodes spécifiques au comportement de groupe
    
    private void askQuestionInGroup() {
        String[] groupQuestions = {
            "Cette œuvre est-elle de la même période que les précédentes ?",
            "Pouvez-vous nous expliquer cette technique ?",
            "Y a-t-il une histoire particulière derrière ce tableau ?",
            "Combien de temps l'artiste a-t-il passé sur cette œuvre ?",
            "Cette œuvre a-t-elle influencé d'autres artistes ?"
        };
        
        String question = groupQuestions[new Random().nextInt(groupQuestions.length)];
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
        msg.addReceiver(guideAgent);
        msg.setContent("QUESTION:" + question);
        send(msg);
        
        questionsAsked++;
        System.out.println("Touriste " + getLocalName() + " pose une question au groupe: " + question);
    }
    
    private void expressOpinionToGroup() {
        String[] groupOpinions = {
            "Cette collection est vraiment impressionnante",
            "J'aime beaucoup ce style artistique", 
            "Cette visite est très enrichissante",
            "Le guide explique très bien",
            "C'est exactement ce que j'espérais voir"
        };
        
        String opinion = groupOpinions[new Random().nextInt(groupOpinions.length)];
        System.out.println("Touriste " + getLocalName() + " partage avec le groupe: " + opinion);
    }
    
    private void helpOtherTourists() {
        System.out.println("Touriste " + getLocalName() + " aide les autres membres du groupe");
        // Améliorer légèrement la satisfaction du groupe
        profile.setSatisfaction(profile.getSatisfaction() + 0.05);
    }
    
    private void suggestToGroup() {
        String[] suggestions = {
            "Peut-être devrions-nous prendre plus de temps ici",
            "Cette œuvre mériterait une photo",
            "Il serait intéressant de comparer avec l'œuvre précédente"
        };
        
        String suggestion = suggestions[new Random().nextInt(suggestions.length)];
        System.out.println("Touriste " + getLocalName() + " suggère au groupe: " + suggestion);
    }
    
    private double evaluateExplanationInGroupContext(String explanation) {
        double baseRating = evaluateExplanation(explanation);
        
        // Bonus pour l'expérience de groupe
        double groupBonus = groupBehavior.getGroupSatisfactionBonus();
        
        // Ajustement selon la position dans le groupe
        double positionFactor = 1.0;
        if (groupPosition < 0.3) { // En arrière du groupe
            positionFactor = 0.9; // Moins bien entendu
        } else if (groupPosition > 0.7) { // Près du guide
            positionFactor = 1.1; // Meilleure expérience
        }
        
        return Math.min(1.0, (baseRating + groupBonus) * positionFactor);
    }
    
    private double evaluateExplanation(String explanation) {
        double rating = 0.5;
        
        // Évaluation basée sur les préférences
        for (String preference : profile.getArtPreferences()) {
            if (explanation.toLowerCase().contains(preference.toLowerCase())) {
                rating += 0.2;
            }
        }
        
        // Évaluation basée sur la personnalité
        Personality personality = profile.getPersonality();
        
        if (explanation.length() > 150 && personality.getPatience() < 0.4) {
            rating -= 0.2;
        } else if (explanation.length() > 100) {
            rating += 0.1;
        }
        
        rating += (personality.getCuriosity() - 0.5) * 0.3;
        
        return Math.max(0.0, Math.min(1.0, rating));
    }
    
    private void updatePersonalStateInGroup() {
        profile.increaseFatigue(0.02);
        
        // Effet de groupe sur la fatigue
        if (groupBehavior.isActive()) {
            double groupEnergy = groupBehavior.getGroupEnergyLevel();
            if (groupEnergy > 0.7) {
                profile.rest(0.01); // Le groupe énergique aide
            } else if (groupEnergy < 0.3) {
                profile.increaseFatigue(0.01); // Le groupe fatigué affecte
            }
        }
        
        // Ajustements basés sur l'âge dans le contexte de groupe
        if (profile.getAge() > 65) {
            profile.increaseFatigue(0.01);
        } else if (profile.getAge() < 25 && groupBehavior.getGroupEnergyLevel() > 0.5) {
            profile.rest(0.01); // Les jeunes profitent de l'énergie du groupe
        }
    }
    
    private void sendStatusToGuide() {
        if (guideAgent != null) {
            ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
            msg1.addReceiver(guideAgent);
            msg1.setContent("STATUS:SATISFACTION:" + profile.getSatisfaction());
            send(msg1);
            
            ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
            msg2.addReceiver(guideAgent);
            msg2.setContent("STATUS:FATIGUE:" + profile.getFatigue());
            send(msg2);
            
            // Nouveau: envoyer le niveau de cohésion de groupe
            if (groupBehavior.isActive()) {
                ACLMessage msg3 = new ACLMessage(ACLMessage.INFORM);
                msg3.addReceiver(guideAgent);
                msg3.setContent("STATUS:GROUP_COHESION:" + groupBehavior.getCohesionLevel());
                send(msg3);
            }
        }
    }
    
    private void decideNextAction(double lastSatisfaction) {
        boolean stayForAnother = false;
        
        // La satisfaction de groupe influence la décision
        double groupInfluence = groupBehavior.getGroupSatisfactionBonus();
        double totalSatisfaction = lastSatisfaction + groupInfluence;
        
        if (totalSatisfaction > 0.8 && toursCompleted < 3) {
            stayForAnother = Math.random() < 0.8; // Plus probable avec une bonne expérience de groupe
        } else if (totalSatisfaction > 0.6 && toursCompleted < 2) {
            stayForAnother = Math.random() < 0.5;
        }
        
        if (stayForAnother && coordinatorAgent != null) {
            resetForNewTour();
            registerWithCoordinator();
            System.out.println("Touriste " + getLocalName() + 
                             " décide de faire une autre visite (satisfaction groupe: " + 
                             String.format("%.2f", totalSatisfaction) + ")");
        } else {
            leaveMuseum();
        }
    }
    
    private void resetForNewTour() {
        currentLocation = "PointA";
        profile.rest(0.2);
        inGroup = false;
        questionsAsked = 0;
        followingGuide = false;
        
        // Réinitialiser le comportement de groupe
        groupBehavior.reset();
        groupPosition = Math.random();
        
        // La personnalité évolue avec l'expérience de groupe
        profile.getPersonality().increaseExperience(0.15); // Plus d'expérience avec les groupes
    }
    
    private void leaveMuseum() {
        System.out.println("Touriste " + getLocalName() + " (" + profile.getNationality() + 
                         ") quitte le musée après " + toursCompleted + " visite(s) en groupe");
        
        // Signaler le départ au coordinateur
        if (coordinatorAgent != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(coordinatorAgent);
            msg.setContent("TOURIST_LEAVING:" + toursCompleted + ":" + 
                         profile.calculateFinalSatisfaction() + ":" + 
                         groupBehavior.getFinalGroupRating());
            send(msg);
        }
        
        // Programmer l'arrêt de l'agent
        addBehaviour(new WakerBehaviour(this, 2000) {
            @Override
            protected void onWake() {
                doDelete();
            }
        });
    }
    
    // Getters pour compatibilité
    public TouristProfile getProfile() { return profile; }
    public String getCurrentLocation() { return currentLocation; }
    public boolean isInGroup() { return inGroup; }
    public boolean isWaitingForAssignment() { return waitingForAssignment; }
    public AID getGuideAgent() { return guideAgent; }
    public AID getCoordinatorAgent() { return coordinatorAgent; }
    public int getQuestionsAsked() { return questionsAsked; }
    public int getToursCompleted() { return toursCompleted; }
    public GroupBehavior getGroupBehavior() { return groupBehavior; }
    public double getGroupPosition() { return groupPosition; }
    public boolean isFollowingGuide() { return followingGuide; }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent Touriste " + getLocalName() + " terminé après " + 
                         toursCompleted + " visite(s) avec expérience de groupe: " + 
                         String.format("%.2f", groupBehavior.getFinalGroupRating()));
    }
    
    /**
     * Classe pour gérer le comportement de groupe spécifique
     */
    public class GroupBehavior {
        private boolean active = false;
        private double cohesionLevel = 0.7;
        private double groupEnergyLevel = 0.7;
        private double groupSatisfactionBonus = 0.0;
        private String currentFormation = "CLUSTER";
        private String currentDestination = "";
        private long lastUpdate = System.currentTimeMillis();
        
        public void activate() {
            active = true;
            cohesionLevel = 0.7;
            groupEnergyLevel = 0.7;
            lastUpdate = System.currentTimeMillis();
        }
        
        public void deactivate() {
            active = false;
        }
        
        public void reset() {
            active = false;
            cohesionLevel = 0.7;
            groupEnergyLevel = 0.7;
            groupSatisfactionBonus = 0.0;
            currentFormation = "CLUSTER";
        }
        
        public void setDestination(String destination) {
            this.currentDestination = destination;
        }
        
        public void followGuide() {
            if (active) {
                // Simuler le suivi du guide avec variation selon la personnalité
                Personality p = profile.getPersonality();
                
                // Les personnes sociales suivent mieux
                if (p.getSocialness() > 0.6) {
                    cohesionLevel = Math.min(1.0, cohesionLevel + 0.05);
                }
                
                // Les personnes patientes maintiennent mieux la cohésion
                if (p.getPatience() > 0.7) {
                    cohesionLevel = Math.min(1.0, cohesionLevel + 0.03);
                } else if (p.getPatience() < 0.3) {
                    cohesionLevel = Math.max(0.2, cohesionLevel - 0.02);
                }
            }
        }
        
        public void formGroup(String formation) {
            this.currentFormation = formation;
            
            // Ajuster la cohésion selon la formation demandée
            switch (formation) {
                case "CIRCLE":
                    cohesionLevel = Math.min(1.0, cohesionLevel + 0.1);
                    break;
                case "LINE":
                    cohesionLevel = Math.max(0.3, cohesionLevel - 0.05);
                    break;
                case "CLUSTER":
                default:
                    // Formation naturelle, pas d'ajustement
                    break;
            }
        }
        
        public boolean shouldReactToExplanation(TouristProfile profile) {
            if (!active) return true; // Comportement normal si pas en groupe
            
            // Dans un groupe, la réaction dépend de la personnalité et de la position
            Personality p = profile.getPersonality();
            
            // Les personnes sociales réagissent plus en groupe
            double socialBonus = p.getSocialness() * 0.3;
            
            // Les personnes en avant du groupe réagissent plus
            double positionBonus = groupPosition * 0.2;
            
            // Les curieux réagissent plus
            double curiosityBonus = p.getCuriosity() * 0.2;
            
            double reactionProbability = 0.3 + socialBonus + positionBonus + curiosityBonus;
            
            return Math.random() < reactionProbability;
        }
        
        public boolean shouldAcceptBreak(TouristProfile profile) {
            boolean personalNeed = profile.getFatigue() > 0.4 || 
                                  profile.getPersonality().getPatience() < 0.5;
            
            // Effet de groupe : tendance à suivre le groupe
            double groupPressure = cohesionLevel * 0.3;
            
            // Si très social, suit plus facilement le groupe
            if (profile.getPersonality().getSocialness() > 0.7) {
                return personalNeed || Math.random() < (0.6 + groupPressure);
            }
            
            return personalNeed || Math.random() < (0.4 + groupPressure);
        }
        
        public void updateDynamics(TouristProfile profile) {
            long now = System.currentTimeMillis();
            if (now - lastUpdate < 5000) return; // Mise à jour max toutes les 5 secondes
            
            // Mettre à jour le niveau d'énergie du groupe basé sur la satisfaction
            double satisfaction = profile.getSatisfaction();
            if (satisfaction > 0.7) {
                groupEnergyLevel = Math.min(1.0, groupEnergyLevel + 0.05);
                groupSatisfactionBonus = Math.min(0.2, groupSatisfactionBonus + 0.02);
            } else if (satisfaction < 0.4) {
                groupEnergyLevel = Math.max(0.2, groupEnergyLevel - 0.03);
                groupSatisfactionBonus = Math.max(-0.1, groupSatisfactionBonus - 0.01);
            }
            
            // La fatigue affecte l'énergie du groupe
            double fatigue = profile.getFatigue();
            if (fatigue > 0.8) {
                groupEnergyLevel = Math.max(0.3, groupEnergyLevel - 0.08);
            }
            
            // La cohésion diminue naturellement avec le temps si pas entretenue
            cohesionLevel = Math.max(0.3, cohesionLevel - 0.01);
            
            lastUpdate = now;
        }
        
        // Getters
        public boolean isActive() { return active; }
        public double getCohesionLevel() { return cohesionLevel; }
        public double getGroupEnergyLevel() { return groupEnergyLevel; }
        public double getGroupSatisfactionBonus() { return groupSatisfactionBonus; }
        public String getCurrentFormation() { return currentFormation; }
        public String getCurrentDestination() { return currentDestination; }
        
        public double getFinalGroupRating() {
            if (!active && cohesionLevel == 0.7) return 0.5; // Pas d'expérience de groupe
            
            // Note finale basée sur cohésion, énergie et satisfaction
            return (cohesionLevel * 0.4) + (groupEnergyLevel * 0.3) + 
                   ((groupSatisfactionBonus + 0.1) * 0.3);
        }
    }
}