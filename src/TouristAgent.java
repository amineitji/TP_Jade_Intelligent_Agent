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
    private String currentLocation = "PointA";
    private String nationality; // "Français", "Italien", "Anglais", "Allemand"
    private String[] preferences; // Préférences artistiques
    private int age;
    private double satisfaction = 0.5;
    private double fatigue = 0.0;
    private double interest = 0.7;
    private boolean inGroup = false;
    private int questionsAsked = 0;
    private Map<String, Double> tableauRatings = new HashMap<>();
    
    // Caractéristiques de personnalité
    private double curiosity; // 0.0 à 1.0
    private double socialness; // 0.0 à 1.0  
    private double patience; // 0.0 à 1.0
    private boolean hasQuestions = false;
    
    protected void setup() {
        System.out.println("Agent Touriste " + getLocalName() + " démarré");
        
        // Initialisation aléatoire du profil
        initializeProfile();
        
        // Comportements
        addBehaviour(new GuideFinder());
        addBehaviour(new GuideInteractionHandler());
        addBehaviour(new PersonalityBehaviour());
        addBehaviour(new SatisfactionMonitor());
    }
    
    private void initializeProfile() {
        Random rand = new Random();
        
        String[] nationalities = {"Français", "Italien", "Anglais", "Allemand", "Espagnol", "Japonais"};
        nationality = nationalities[rand.nextInt(nationalities.length)];
        
        String[] artPrefs = {"Renaissance", "Moderne", "Impressionniste", "Contemporain", "Classique"};
        preferences = new String[2 + rand.nextInt(2)]; // 2 à 3 préférences
        for (int i = 0; i < preferences.length; i++) {
            preferences[i] = artPrefs[rand.nextInt(artPrefs.length)];
        }
        
        age = 18 + rand.nextInt(60); // 18 à 77 ans
        
        // Personnalité basée sur l'âge et la nationalité
        curiosity = 0.3 + rand.nextDouble() * 0.7;
        socialness = 0.2 + rand.nextDouble() * 0.8;
        patience = (age > 50) ? 0.6 + rand.nextDouble() * 0.4 : 0.3 + rand.nextDouble() * 0.7;
        
        // Ajustements culturels simplifiés
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
    
    // Recherche et inscription auprès d'un guide
    private class GuideFinder extends OneShotBehaviour {
        public void action() {
            // Attendre un peu avant de chercher (simulation arrivée échelonnée)
            try {
                Thread.sleep(new Random().nextInt(3000) + 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Rechercher les guides disponibles
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("guide-service");
            template.addServices(sd);
            
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    // Choisir un guide (ici le premier disponible)
                    guideAgent = result[0].getName();
                    
                    // Demander à rejoindre le groupe
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(guideAgent);
                    msg.setContent("JOIN_GROUP");
                    send(msg);
                    
                    System.out.println("Touriste " + getLocalName() + " demande à rejoindre le guide " +
                                     guideAgent.getLocalName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }
    
    // Gestion des interactions avec le guide
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
                }
            } else {
                block();
            }
        }
    }
    
    // Comportement de personnalité
    private class PersonalityBehaviour extends TickerBehaviour {
        public PersonalityBehaviour() {
            super(TouristAgent.this, 8000 + new Random().nextInt(4000)); // 8-12 secondes
        }
        
        protected void onTick() {
            if (inGroup) {
                // Comportements basés sur la personnalité
                Random rand = new Random();
                
                // Les curieux posent plus de questions
                if (curiosity > 0.6 && rand.nextDouble() < 0.3) {
                    askQuestion();
                }
                
                // Les sociaux interagissent plus
                if (socialness > 0.7 && rand.nextDouble() < 0.2) {
                    expressOpinion();
                }
                
                // Mise à jour de l'état personnel
                updatePersonalState();
                sendStatusToGuide();
            }
        }
    }
    
    // Surveillance de la satisfaction
    private class SatisfactionMonitor extends TickerBehaviour {
        public SatisfactionMonitor() {
            super(TouristAgent.this, 15000); // Toutes les 15 secondes
        }
        
        protected void onTick() {
            if (inGroup) {
                evaluateExperience();
                
                // Si très insatisfait, possibilité de quitter
                if (satisfaction < 0.2 && new Random().nextDouble() < 0.1) {
                    considerLeaving();
                }
            }
        }
    }
    
    private void handleWelcome(String content) {
        String[] parts = content.split(":");
        if (parts.length >= 3) {
            String guideSpecialization = parts[1];
            currentLocation = parts[2];
            inGroup = true;
            
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
        String destination = content.substring(8); // Enlever "MOVE_TO:"
        currentLocation = destination;
        
        // Simulation du déplacement et impact sur la fatigue
        fatigue += 0.05;
        if (age > 60) fatigue += 0.05; // Les plus âgés se fatiguent plus
        
        System.out.println("Touriste " + getLocalName() + " se déplace vers " + destination);
        
        // Signaler qu'on est prêt après le déplacement
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
        String explanation = content.substring(12); // Enlever "EXPLANATION:"
        
        // Évaluer l'explication selon les préférences
        double explanationRating = evaluateExplanation(explanation);
        tableauRatings.put(currentLocation, explanationRating);
        
        // Impact sur la satisfaction
        satisfaction = (satisfaction + explanationRating) / 2.0;
        
        // Impact sur l'intérêt
        interest = Math.max(0.0, Math.min(1.0, interest + (explanationRating - 0.5) * 0.3));
        
        System.out.println("Touriste " + getLocalName() + " écoute l'explication (" +
                         currentLocation + ") - Note: " + String.format("%.2f", explanationRating));
        
        // Possibilité de poser une question si très intéressé
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
        
        // Décision basée sur la fatigue et la patience
        boolean acceptBreak = fatigue > 0.4 || (patience < 0.5 && new Random().nextDouble() < 0.7);
        
        ACLMessage reply = new ACLMessage(acceptBreak ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);
        reply.addReceiver(guideAgent);
        reply.setContent("BREAK_RESPONSE:" + (acceptBreak ? "ACCEPT" : "REJECT"));
        send(reply);
        
        if (acceptBreak) {
            fatigue = Math.max(0.0, fatigue - 0.3); // Se reposer
            System.out.println("Touriste " + getLocalName() + " accepte la pause");
        }
    }
    
    private void handleTourEnd(String content) {
        String finalMessage = content.substring(9); // Enlever "TOUR_END:"
        
        // Évaluation finale
        double finalSatisfaction = calculateFinalSatisfaction();
        
        System.out.println("Touriste " + getLocalName() + " termine la visite - Satisfaction finale: " +
                         String.format("%.2f", finalSatisfaction) + "/1.0");
        
        inGroup = false;
        // Le touriste pourrait chercher un autre guide ou quitter le musée
    }
    
    private void handleAnswer(String content) {
        String answer = content.substring(7); // Enlever "ANSWER:"
        
        // La réponse augmente la satisfaction
        satisfaction += 0.1;
        curiosity = Math.max(0.0, curiosity - 0.05); // Curiosité temporairement satisfaite
        
        System.out.println("Touriste " + getLocalName() + " reçoit une réponse: " + answer);
    }
    
    private void askQuestion() {
        if (questionsAsked < 3) { // Limite le nombre de questions
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
        
        // Évolution naturelle de la fatigue
        fatigue = Math.min(1.0, fatigue + 0.02);
        
        // Variation de l'intérêt selon la personnalité
        double interestChange = (curiosity - 0.5) * 0.05 + (rand.nextDouble() - 0.5) * 0.1;
        interest = Math.max(0.0, Math.min(1.0, interest + interestChange));
        
        // L'âge influence la fatigue
        if (age > 65) {
            fatigue += 0.01;
        } else if (age < 25) {
            fatigue = Math.max(0.0, fatigue - 0.005); // Les jeunes récupèrent mieux
        }
    }
    
    private void sendStatusToGuide() {
        if (guideAgent != null) {
            // Envoyer la satisfaction
            ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
            msg1.addReceiver(guideAgent);
            msg1.setContent("STATUS:SATISFACTION:" + satisfaction);
            send(msg1);
            
            // Envoyer la fatigue
            ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
            msg2.addReceiver(guideAgent);
            msg2.setContent("STATUS:FATIGUE:" + fatigue);
            send(msg2);
        }
    }
    
    private double evaluateExplanation(String explanation) {
        double rating = 0.5; // Note de base
        
        // Bonus si l'explication mentionne nos préférences
        for (String pref : preferences) {
            if (explanation.toLowerCase().contains(pref.toLowerCase())) {
                rating += 0.2;
            }
        }
        
        // Bonus si explication détaillée (approximation par la longueur)
        if (explanation.length() > 100) {
            rating += 0.1;
        }
        
        // Malus si pas assez patient pour les longues explications
        if (explanation.length() > 150 && patience < 0.4) {
            rating -= 0.2;
        }
        
        // Variation selon la curiosité
        rating += (curiosity - 0.5) * 0.3;
        
        // Normaliser
        return Math.max(0.0, Math.min(1.0, rating));
    }
    
    private void evaluateExperience() {
        // Évaluation globale basée sur plusieurs facteurs
        double experienceRating = 0.0;
        
        // Moyenne des notes des tableaux vus
        if (!tableauRatings.isEmpty()) {
            double sum = tableauRatings.values().stream().mapToDouble(Double::doubleValue).sum();
            experienceRating = sum / tableauRatings.size();
        }
        
        // Ajustement selon l'état personnel
        experienceRating -= fatigue * 0.3; // La fatigue diminue l'appréciation
        experienceRating += interest * 0.2; // L'intérêt améliore l'expérience
        
        // Mise à jour progressive de la satisfaction
        satisfaction = (satisfaction * 0.7) + (experienceRating * 0.3);
        satisfaction = Math.max(0.0, Math.min(1.0, satisfaction));
    }
    
    private void considerLeaving() {
        // Un touriste très insatisfait peut envisager de quitter
        System.out.println("Touriste " + getLocalName() + " considère quitter le groupe (satisfaction très faible)");
        
        // Pour l'instant, on ne fait que signaler - dans une version plus avancée,
        // on pourrait implémenter la sortie du groupe
    }
    
    private double calculateFinalSatisfaction() {
        double finalScore = satisfaction;
        
        // Bonus si beaucoup de questions ont été posées et répondues
        if (questionsAsked >= 2) {
            finalScore += 0.1;
        }
        
        // Malus si trop fatigué à la fin
        if (fatigue > 0.8) {
            finalScore -= 0.2;
        }
        
        // Bonus selon la compatibilité des préférences
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
    public AID getGuideAgent() { return guideAgent; }
    public double getCuriosity() { return curiosity; }
    public double getSocialness() { return socialness; }
    public double getPatience() { return patience; }
    public int getQuestionsAsked() { return questionsAsked; }
    public Map<String, Double> getTableauRatings() { return new HashMap<>(tableauRatings); }
    
    protected void takeDown() {
        System.out.println("Agent Touriste " + getLocalName() + " (" + nationality + ") terminé");
    }
}