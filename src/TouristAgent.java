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

public class TouristAgent extends Agent {
    private AID guideAgent;
    private AID coordinatorAgent; // NOUVEAU
    private String currentLocation = "PointA";
    private String nationality;
    private String[] preferences;
    private int age;
    private double satisfaction = 0.5;
    private double fatigue = 0.0;
    private double interest = 0.7;
    private boolean inGroup = false;
    private boolean waitingForAssignment = false; // NOUVEAU
    private int questionsAsked = 0;
    private int toursCompleted = 0; // NOUVEAU
    private Map<String, Double> tableauRatings = new HashMap<>();
    
    // Caractéristiques de personnalité
    private double curiosity;
    private double socialness;
    private double patience;
    private boolean hasQuestions = false;
    
    // Temps limite pour attendre une assignation
    private final long MAX_WAIT_TIME = 60000; // 1 minute
    private long waitStartTime = 0;
    
    protected void setup() {
        System.out.println("Agent Touriste " + getLocalName() + " démarré avec support coordinateur");
        
        // Initialisation aléatoire du profil
        initializeProfile();
        
        // Comportements
        addBehaviour(new CoordinatorRegistration());
        addBehaviour(new GuideInteractionHandler());
        addBehaviour(new PersonalityBehaviour());
        addBehaviour(new SatisfactionMonitor());
        addBehaviour(new WaitingTimeoutHandler()); // NOUVEAU
        
        // Recherche et enregistrement auprès du coordinateur
        findAndRegisterWithCoordinator();
    }
    
    private void initializeProfile() {
        Random rand = new Random();
        
        String[] nationalities = {"Français", "Italien", "Anglais", "Allemand", "Espagnol", "Japonais"};
        nationality = nationalities[rand.nextInt(nationalities.length)];
        
        String[] artPrefs = {"Renaissance", "Moderne", "Impressionniste", "Contemporain", "Classique"};
        preferences = new String[2 + rand.nextInt(2)];
        for (int i = 0; i < preferences.length; i++) {
            preferences[i] = artPrefs[rand.nextInt(artPrefs.length)];
        }
        
        age = 18 + rand.nextInt(60);
        
        // Personnalité basée sur l'âge et la nationalité
        curiosity = 0.3 + rand.nextDouble() * 0.7;
        socialness = 0.2 + rand.nextDouble() * 0.8;
        patience = (age > 50) ? 0.6 + rand.nextDouble() * 0.4 : 0.3 + rand.nextDouble() * 0.7;
        
        // Ajustements culturels
        if ("Japonais".equals(nationality)) {
            patience += 0.2;
            socialness -= 0.1;
        } else if ("Italien".equals(nationality)) {
            socialness += 0.2;
        }
        
        // Normaliser les valeurs
        patience = Math.min(1.0, Math.max(0.0, patience));
        socialness = Math.min(1.0, Math.max(0.0, socialness));
        
        System.out.println("Touriste " + getLocalName() + " - " + nationality + 
                         ", âge: " + age + ", curiosité: " + String.format("%.2f", curiosity));
    }
    
    // NOUVEAU : Recherche et enregistrement auprès du coordinateur
    private void findAndRegisterWithCoordinator() {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                // Attendre un peu avant de chercher (simulation arrivée échelonnée)
                try {
                    Thread.sleep(new Random().nextInt(3000) + 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Rechercher le coordinateur
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("coordinator-service");
                template.addServices(sd);
                
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        coordinatorAgent = result[0].getName();
                        System.out.println("Touriste " + getLocalName() + " trouvé coordinateur: " + coordinatorAgent.getLocalName());
                        
                        // S'enregistrer auprès du coordinateur
                        registerWithCoordinator();
                    } else {
                        // Fallback : chercher directement un guide (ancien système)
                        findGuideDirectly();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                    findGuideDirectly();
                }
            }
        });
    }
    
    // NOUVEAU : Enregistrement auprès du coordinateur
    private void registerWithCoordinator() {
        ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
        msg.addReceiver(coordinatorAgent);
        msg.setContent("REGISTER_TOURIST");
        send(msg);
        
        waitingForAssignment = true;
        waitStartTime = System.currentTimeMillis();
        
        System.out.println("Touriste " + getLocalName() + " s'enregistre auprès du coordinateur et attend une assignation");
    }
    
    // Fallback : recherche directe d'un guide (ancien système)
    private void findGuideDirectly() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("guide-service");
        template.addServices(sd);
        
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                guideAgent = result[0].getName();
                
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(guideAgent);
                msg.setContent("JOIN_GROUP");
                send(msg);
                
                System.out.println("Touriste " + getLocalName() + " demande directement à rejoindre le guide " +
                                 guideAgent.getLocalName());
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    // NOUVEAU : Enregistrement auprès du coordinateur (behaviour)
    private class CoordinatorRegistration extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                if ("WELCOME_MUSEUM".equals(content)) {
                    handleMuseumWelcome();
                } else if (content.startsWith("ASSIGNED_TO_GUIDE:")) {
                    handleGuideAssignment(content);
                } else {
                    // Remettre le message s'il ne nous concerne pas
                    putBack(msg);
                }
            } else {
                block();
            }
        }
    }
    
    // NOUVEAU : Gestionnaire de timeout d'attente
    private class WaitingTimeoutHandler extends TickerBehaviour {
        public WaitingTimeoutHandler() {
            super(TouristAgent.this, 15000); // Vérifier toutes les 15 secondes
        }
        
        protected void onTick() {
            if (waitingForAssignment) {
                long waitTime = System.currentTimeMillis() - waitStartTime;
                
                if (waitTime > MAX_WAIT_TIME) {
                    System.out.println("Touriste " + getLocalName() + " : Temps d'attente dépassé, tentative de connexion directe");
                    waitingForAssignment = false;
                    findGuideDirectly();
                } else if (waitTime > MAX_WAIT_TIME / 2) {
                    System.out.println("Touriste " + getLocalName() + " attend depuis " + (waitTime / 1000) + " secondes...");
                }
            }
        }
    }
    
    // Gestion des interactions avec le guide (améliorée)
    private class GuideInteractionHandler extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.startsWith("WELCOME_GROUP:")) {
                    handleWelcome(content);
                } else if (content.startsWith("MOVE_TO:")) {
                    handleMovement(content);
                } else if (content.startsWith("EXPLANATION:")) {
                    handleExplanation(content);
                } else if (content.startsWith("BREAK_PROPOSAL:")) {
                    handleBreakProposal(content);
                } else if (content.startsWith("TOUR_END:")) {
                    handleTourEnd(content);
                } else if (content.startsWith("ANSWER:")) {
                    handleAnswer(content);
                } else if ("REDIRECT_TO_COORDINATOR".equals(content)) {
                    handleRedirectToCoordinator();
                }
            } else {
                block();
            }
        }
    }
    
    // Comportement de personnalité (identique)
    private class PersonalityBehaviour extends TickerBehaviour {
        public PersonalityBehaviour() {
            super(TouristAgent.this, 8000 + new Random().nextInt(4000));
        }
        
        protected void onTick() {
            if (inGroup) {
                Random rand = new Random();
                
                if (curiosity > 0.6 && rand.nextDouble() < 0.3) {
                    askQuestion();
                }
                
                if (socialness > 0.7 && rand.nextDouble() < 0.2) {
                    expressOpinion();
                }
                
                updatePersonalState();
                sendStatusToGuide();
            }
        }
    }
    
    // Surveillance de la satisfaction (identique)
    private class SatisfactionMonitor extends TickerBehaviour {
        public SatisfactionMonitor() {
            super(TouristAgent.this, 15000);
        }
        
        protected void onTick() {
            if (inGroup) {
                evaluateExperience();
                
                if (satisfaction < 0.2 && new Random().nextDouble() < 0.1) {
                    considerLeaving();
                }
            }
        }
    }
    
    // NOUVEAU : Gestion de l'accueil du musée
    private void handleMuseumWelcome() {
        System.out.println("Touriste " + getLocalName() + " : Bienvenue au musée reçue du coordinateur");
    }
    
    // NOUVEAU : Gestion de l'assignation à un guide
    private void handleGuideAssignment(String content) {
        String[] parts = content.split(":");
        if (parts.length >= 2) {
            String guideName = parts[1];
            guideAgent = new AID(guideName, AID.ISLOCALNAME);
            waitingForAssignment = false;
            
            System.out.println("Touriste " + getLocalName() + " assigné au guide " + guideName + " par le coordinateur");
            
            // Envoyer un message de confirmation au guide
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(guideAgent);
            msg.setContent("TOURIST_READY:" + getLocalName());
            send(msg);
        }
    }
    
    // NOUVEAU : Gestion de la redirection vers le coordinateur
    private void handleRedirectToCoordinator() {
        System.out.println("Touriste " + getLocalName() + " : Redirigé vers le coordinateur");
        
        if (coordinatorAgent != null) {
            registerWithCoordinator();
        } else {
            // Chercher le coordinateur
            findAndRegisterWithCoordinator();
        }
    }
    
    private void handleWelcome(String content) {
        String[] parts = content.split(":");
        if (parts.length >= 3) {
            String guideSpecialization = parts[1];
            currentLocation = parts[2];
            inGroup = true;
            waitingForAssignment = false;
            
            // Évaluer la compatibilité avec la spécialisation du guide
            boolean compatibleSpecialization = false;
            for (String pref : preferences) {
                if (pref.equals(guideSpecialization)) {
                    compatibleSpecialization = true;
                    satisfaction += 0.2;
                    break;
                }
            }
            
            if (!compatibleSpecialization) {
                satisfaction -= 0.1;
            }
            
            System.out.println("Touriste " + getLocalName() + " rejoint le groupe du guide " +
                             guideAgent.getLocalName() + " (Spécialisation: " + guideSpecialization + ")");
        }
    }
    
    private void handleMovement(String content) {
        String destination = content.substring(8);
        currentLocation = destination;
        
        fatigue += 0.05;
        if (age > 60) fatigue += 0.05;
        
        System.out.println("Touriste " + getLocalName() + " se déplace vers " + destination);
        
        addBehaviour(new WakerBehaviour(this, 2000 + new Random().nextInt(2000)) {
            protected void onWake() {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(guideAgent);
                msg.setContent("READY_NEXT");
                send(msg);
            }
        });
    }
    
    private void handleExplanation(String content) {
        String explanation = content.substring(12);
        
        double explanationRating = evaluateExplanation(explanation);
        tableauRatings.put(currentLocation, explanationRating);
        
        satisfaction = (satisfaction + explanationRating) / 2.0;
        interest = Math.max(0.0, Math.min(1.0, interest + (explanationRating - 0.5) * 0.3));
        
        System.out.println("Touriste " + getLocalName() + " écoute l'explication (" +
                         currentLocation + ") - Note: " + String.format("%.2f", explanationRating));
        
        if (explanationRating > 0.8 && curiosity > 0.5 && new Random().nextDouble() < 0.4) {
            addBehaviour(new WakerBehaviour(this, 3000 + new Random().nextInt(2000)) {
                protected void onWake() {
                    askQuestion();
                }
            });
        }
    }
    
    private void handleBreakProposal(String content) {
        int breakDuration = Integer.parseInt(content.split(":")[1]);
        
        boolean acceptBreak = fatigue > 0.4 || (patience < 0.5 && new Random().nextDouble() < 0.7);
        
        ACLMessage reply = new ACLMessage(acceptBreak ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);
        reply.addReceiver(guideAgent);
        reply.setContent("BREAK_RESPONSE:" + (acceptBreak ? "ACCEPT" : "REJECT"));
        send(reply);
        
        if (acceptBreak) {
            fatigue = Math.max(0.0, fatigue - 0.3);
            System.out.println("Touriste " + getLocalName() + " accepte la pause");
        }
    }
    
    // NOUVEAU : Gestion améliorée de la fin de visite
    private void handleTourEnd(String content) {
        String finalMessage = content.substring(9);
        
        double finalSatisfaction = calculateFinalSatisfaction();
        toursCompleted++;
        
        System.out.println("Touriste " + getLocalName() + " termine la visite #" + toursCompleted + 
                         " - Satisfaction finale: " + String.format("%.2f", finalSatisfaction) + "/1.0");
        
        inGroup = false;
        guideAgent = null;
        
        // Décision : rester pour une autre visite ou quitter
        addBehaviour(new WakerBehaviour(this, 5000 + new Random().nextInt(5000)) {
            protected void onWake() {
                decideNextAction(finalSatisfaction);
            }
        });
    }
    
    private void handleAnswer(String content) {
        String answer = content.substring(7);
        
        satisfaction += 0.1;
        curiosity = Math.max(0.0, curiosity - 0.05);
        
        System.out.println("Touriste " + getLocalName() + " reçoit une réponse: " + answer);
    }
    
    // NOUVEAU : Décision après la fin d'une visite
    private void decideNextAction(double lastSatisfaction) {
        Random rand = new Random();
        
        // Facteurs de décision
        boolean stayForAnother = false;
        
        if (lastSatisfaction > 0.8 && toursCompleted < 3) {
            stayForAnother = rand.nextDouble() < 0.7; // 70% de chance de rester
        } else if (lastSatisfaction > 0.6 && toursCompleted < 2) {
            stayForAnother = rand.nextDouble() < 0.4; // 40% de chance
        } else if (lastSatisfaction > 0.3 && toursCompleted == 0) {
            stayForAnother = rand.nextDouble() < 0.2; // 20% de chance
        }
        
        if (stayForAnother && coordinatorAgent != null) {
            // Réinitialiser l'état pour une nouvelle visite
            resetForNewTour();
            
            System.out.println("Touriste " + getLocalName() + " décide de faire une autre visite");
            registerWithCoordinator();
        } else {
            // Quitter le musée
            leaveMuseum();
        }
    }
    
    // NOUVEAU : Réinitialisation pour une nouvelle visite
    private void resetForNewTour() {
        currentLocation = "PointA";
        satisfaction = 0.5;
        fatigue = Math.max(0.0, fatigue - 0.2); // Récupération partielle
        interest = 0.7;
        questionsAsked = 0;
        tableauRatings.clear();
        
        // La personnalité évolue légèrement
        curiosity = Math.min(1.0, curiosity + 0.1);
        patience = Math.max(0.0, patience - 0.05); // Un peu moins patient
    }
    
    // NOUVEAU : Quitter le musée
    private void leaveMuseum() {
        System.out.println("Touriste " + getLocalName() + " (" + nationality + ") quitte le musée après " + 
                         toursCompleted + " visite(s) - Expérience globale: " + 
                         String.format("%.2f", calculateOverallExperience()));
        
        // Signaler le départ au coordinateur
        if (coordinatorAgent != null) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(coordinatorAgent);
            msg.setContent("TOURIST_LEAVING:" + toursCompleted + ":" + calculateOverallExperience());
            send(msg);
        }
        
        // Programmer l'arrêt de l'agent
        addBehaviour(new WakerBehaviour(this, 2000) {
            protected void onWake() {
                doDelete();
            }
        });
    }
    
    // NOUVEAU : Calcul de l'expérience globale
    private double calculateOverallExperience() {
        if (tableauRatings.isEmpty()) return satisfaction;
        
        double totalRating = tableauRatings.values().stream().mapToDouble(Double::doubleValue).sum();
        double avgTableauRating = totalRating / tableauRatings.size();
        
        // Combinaison de facteurs
        double overallExp = (satisfaction * 0.4) + (avgTableauRating * 0.4) + ((1.0 - fatigue) * 0.2);
        
        // Bonus pour les visites multiples réussies
        if (toursCompleted > 1) {
            overallExp += 0.1 * (toursCompleted - 1);
        }
        
        return Math.max(0.0, Math.min(1.0, overallExp));
    }
    
    // Méthodes existantes (identiques)
    private void askQuestion() {
        if (questionsAsked < 3) {
            String[] questions = {
                "Quelle technique a été utilisée pour cette œuvre ?",
                "Quelle est l'histoire derrière ce tableau ?",
                "Y a-t-il des détails cachés dans cette œuvre ?",
                "Combien de temps l'artiste a-t-il mis pour créer cela ?",
                "Cette œuvre a-t-elle inspiré d'autres artistes ?"
            };
            
            String question = questions[new Random().nextInt(questions.length)];
            
            ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
            msg.addReceiver(guideAgent);
            msg.setContent("QUESTION:" + question);
            send(msg);
            
            questionsAsked++;
            System.out.println("Touriste " + getLocalName() + " pose une question: " + question);
        }
    }
    
    private void expressOpinion() {
        String[] opinions = {
            "Cette œuvre me rappelle quelque chose de mon pays",
            "Les couleurs sont vraiment magnifiques",
            "C'est très différent de ce que j'ai vu ailleurs",
            "L'artiste était vraiment doué",
            "Je ne connaissais pas cette technique"
        };
        
        String opinion = opinions[new Random().nextInt(opinions.length)];
        System.out.println("Touriste " + getLocalName() + " exprime: " + opinion);
    }
    
    private void updatePersonalState() {
        Random rand = new Random();
        
        fatigue = Math.min(1.0, fatigue + 0.02);
        
        double interestChange = (curiosity - 0.5) * 0.05 + (rand.nextDouble() - 0.5) * 0.1;
        interest = Math.max(0.0, Math.min(1.0, interest + interestChange));
        
        if (age > 65) {
            fatigue += 0.01;
        } else if (age < 25) {
            fatigue = Math.max(0.0, fatigue - 0.005);
        }
    }
    
    private void sendStatusToGuide() {
        if (guideAgent != null) {
            ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
            msg1.addReceiver(guideAgent);
            msg1.setContent("STATUS:SATISFACTION:" + satisfaction);
            send(msg1);
            
            ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
            msg2.addReceiver(guideAgent);
            msg2.setContent("STATUS:FATIGUE:" + fatigue);
            send(msg2);
        }
    }
    
    private double evaluateExplanation(String explanation) {
        double rating = 0.5;
        
        for (String pref : preferences) {
            if (explanation.toLowerCase().contains(pref.toLowerCase())) {
                rating += 0.2;
            }
        }
        
        if (explanation.length() > 100) {
            rating += 0.1;
        }
        
        if (explanation.length() > 150 && patience < 0.4) {
            rating -= 0.2;
        }
        
        rating += (curiosity - 0.5) * 0.3;
        
        return Math.max(0.0, Math.min(1.0, rating));
    }
    
    private void evaluateExperience() {
        double experienceRating = 0.0;
        
        if (!tableauRatings.isEmpty()) {
            double sum = tableauRatings.values().stream().mapToDouble(Double::doubleValue).sum();
            experienceRating = sum / tableauRatings.size();
        }
        
        experienceRating -= fatigue * 0.3;
        experienceRating += interest * 0.2;
        
        satisfaction = (satisfaction * 0.7) + (experienceRating * 0.3);
        satisfaction = Math.max(0.0, Math.min(1.0, satisfaction));
    }
    
    private void considerLeaving() {
        System.out.println("Touriste " + getLocalName() + " considère quitter le groupe (satisfaction très faible)");
    }
    
    private double calculateFinalSatisfaction() {
        double finalScore = satisfaction;
        
        if (questionsAsked >= 2) {
            finalScore += 0.1;
        }
        
        if (fatigue > 0.8) {
            finalScore -= 0.2;
        }
        
        long compatibleTableaux = tableauRatings.values().stream()
            .mapToLong(rating -> rating > 0.7 ? 1 : 0)
            .sum();
        
        if (compatibleTableaux >= 3) {
            finalScore += 0.15;
        }
        
        return Math.max(0.0, Math.min(1.0, finalScore));
    }
    
    // Getters pour l'interface graphique
    public String getCurrentLocation() { return currentLocation; }
    public String getNationality() { return nationality; }
    public String[] getPreferences() { return preferences; }
    public int getAge() { return age; }
    public double getSatisfaction() { return satisfaction; }
    public double getFatigue() { return fatigue; }
    public double getInterest() { return interest; }
    public boolean isInGroup() { return inGroup; }
    public boolean isWaitingForAssignment() { return waitingForAssignment; }
    public AID getGuideAgent() { return guideAgent; }
    public AID getCoordinatorAgent() { return coordinatorAgent; }
    public double getCuriosity() { return curiosity; }
    public double getSocialness() { return socialness; }
    public double getPatience() { return patience; }
    public int getQuestionsAsked() { return questionsAsked; }
    public int getToursCompleted() { return toursCompleted; }
    public Map<String, Double> getTableauRatings() { return new HashMap<>(tableauRatings); }
    
    protected void takeDown() {
        System.out.println("Agent Touriste " + getLocalName() + " (" + nationality + ") terminé après " + 
                         toursCompleted + " visite(s)");
    }
}