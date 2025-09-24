package agents.guide;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Gestionnaire des visites guidées - responsable de la logistique et du contenu
 */
public class TourManager {
    private GuideAgent guide;
    private GuideProfile profile;
    private Map<String, String> tableauInfo;
    private String currentLocation;
    private int currentTableauIndex;
    private List<String> plannedRoute;
    private List<String> avoidedLocations;
    private boolean adaptiveMode;
    private Logger logger;
    
    private final String[] DEFAULT_ROUTE = {
        "PointA", "Tableau1", "Tableau2", "Tableau3", 
        "Tableau4", "Tableau5", "SalleRepos", "Sortie"
    };
    
    public TourManager(GuideAgent guide, GuideProfile profile) {
        this.guide = guide;
        this.profile = profile;
        this.logger = Logger.getLogger(TourManager.class.getName());
        this.currentLocation = "PointA";
        this.currentTableauIndex = 0;
        this.plannedRoute = new ArrayList<>(List.of(DEFAULT_ROUTE));
        this.avoidedLocations = new ArrayList<>();
        this.adaptiveMode = false;
        
        initializeTableauInfo();
    }
    
    /**
     * Initialise les informations sur les tableaux
     */
    private void initializeTableauInfo() {
        tableauInfo = new HashMap<>();
        tableauInfo.put("Tableau1", "La Joconde - Chef-d'œuvre de Léonard de Vinci, symbole de l'art Renaissance. " +
                "Technique de sfumato révolutionnaire, regard énigmatique qui fascine depuis 500 ans.");
        tableauInfo.put("Tableau2", "La Nuit étoilée - Œuvre emblématique de Van Gogh, post-impressionnisme. " +
                "Mouvement tourbillonnant du ciel, expression de l'état mental de l'artiste.");
        tableauInfo.put("Tableau3", "Guernica - Picasso, art moderne, dénonciation de la guerre. " +
                "Composition cubiste en noir et blanc, symbole universel contre la violence.");
        tableauInfo.put("Tableau4", "Les Demoiselles d'Avignon - Picasso, naissance du cubisme. " +
                "Révolution artistique, déconstruction de la forme traditionnelle.");
        tableauInfo.put("Tableau5", "L'École d'Athènes - Raphaël, Renaissance italienne, philosophie. " +
                "Synthèse parfaite de l'art et de la pensée, perspective architecturale magistrale.");
    }
    
    /**
     * Déplace le groupe vers un tableau spécifique
     */
    public void moveToTableau(String tableau) {
        if (avoidedLocations.contains(tableau)) {
            tableau = findAlternativeTableau(tableau);
        }
        
        moveToLocation(tableau);
        currentTableauIndex++;
        
        // Attendre puis commencer l'explication
        final String finalTableau = tableau;
        guide.addBehaviour(new jade.core.behaviours.WakerBehaviour(guide, 3000) {
            @Override
            protected void onWake() {
                startExplanation(finalTableau);
            }
        });
        
        logger.info("Groupe emmené vers " + tableau);
    }
    
    /**
     * Déplace le groupe vers une location générale
     */
    public void moveToLocation(String location) {
        currentLocation = location;
        guide.getStatus().setCurrentLocation(location);
        
        // Informer tous les touristes du déplacement
        for (AID tourist : guide.getAssignedTourists()) {
            guide.sendMessage(tourist, ACLMessage.INFORM, "MOVE_TO:" + location);
        }
    }
    
    /**
     * Commence l'explication d'un tableau
     */
    private void startExplanation(String tableau) {
        String baseExplanation = tableauInfo.get(tableau);
        if (baseExplanation == null) {
            baseExplanation = "Œuvre remarquable de notre collection permanente.";
        }
        
        // Adapter l'explication selon le profil du guide et le mode adaptatif
        String adaptedExplanation = adaptExplanation(baseExplanation, tableau);
        
        // Diffuser l'explication à tous les touristes
        for (AID tourist : guide.getAssignedTourists()) {
            guide.sendMessage(tourist, ACLMessage.INFORM, "EXPLANATION:" + adaptedExplanation);
        }
        
        logger.info("Explication donnée pour " + tableau);
    }
    
    /**
     * Adapte l'explication selon les capacités du guide et le contexte
     */
    private String adaptExplanation(String baseExplanation, String tableau) {
        StringBuilder adapted = new StringBuilder(baseExplanation);
        
        // Adaptation selon l'expertise du guide pour ce tableau
        double expertise = profile.getTableauEfficiency(tableau);
        if (expertise > 0.8) {
            adapted.append(" [Détails approfondis basés sur l'expertise du guide]");
        }
        
        // Adaptation selon la spécialisation
        if (isTableauInSpecialization(tableau)) {
            adapted.append(" - Explication spécialisée en ").append(profile.getSpecialization());
        }
        
        // Adaptation selon le style optimal du guide
        String style = profile.getOptimalExplanationStyle();
        switch (style) {
            case "interactive":
                adapted.append(" [Style interactif encouragé]");
                break;
            case "detailed":
                adapted.append(" [Version détaillée technique]");
                break;
            case "adaptive":
                adapted.append(" [Adaptation au groupe en cours]");
                break;
        }
        
        return adapted.toString();
    }
    
    /**
     * Vérifie si un tableau correspond à la spécialisation du guide
     */
    private boolean isTableauInSpecialization(String tableau) {
        String specialization = profile.getSpecialization();
        
        switch (tableau) {
            case "Tableau1": // La Joconde
            case "Tableau5": // L'École d'Athènes
                return "Renaissance".equals(specialization) || "Classique".equals(specialization);
            case "Tableau3": // Guernica
            case "Tableau4": // Les Demoiselles d'Avignon
                return "Moderne".equals(specialization) || "Contemporain".equals(specialization);
            case "Tableau2": // La Nuit étoilée
                return "Impressionniste".equals(specialization);
            default:
                return false;
        }
    }
    
    /**
     * Répond à une question de touriste
     */
    public void answerQuestion(ACLMessage questionMsg) {
        String question = questionMsg.getContent().substring(9); // Enlever "QUESTION:"
        
        ACLMessage reply = questionMsg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent("ANSWER:" + generateAnswer(question));
        guide.send(reply);
        
        logger.info("Réponse à la question de " + questionMsg.getSender().getLocalName());
    }
    
    /**
     * Génère une réponse adaptée à la question
     */
    private String generateAnswer(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // Réponses basées sur l'expertise et la spécialisation
        if (lowerQuestion.contains("technique")) {
            if (profile.getKnowledge() > 0.7) {
                return "Cette œuvre utilise une technique particulière de " + 
                       profile.getSpecialization().toLowerCase() + 
                       ". [Explication technique détaillée basée sur l'expertise]";
            } else {
                return "La technique utilisée est caractéristique de cette période artistique.";
            }
        } else if (lowerQuestion.contains("histoire")) {
            return "L'histoire de cette œuvre remonte à la période " + 
                   profile.getSpecialization().toLowerCase() + 
                   ". [Contexte historique selon l'expertise du guide]";
        } else if (lowerQuestion.contains("artiste")) {
            return "L'artiste était un maître de son époque, particulièrement reconnu pour " +
                   "son approche innovante dans le style " + profile.getSpecialization().toLowerCase();
        } else {
            return "Excellente question ! C'est un aspect fascinant de l'art " + 
                   profile.getSpecialization().toLowerCase() + 
                   " que je serais ravi d'approfondir avec vous.";
        }
    }
    
    /**
     * Propose une pause au groupe
     */
    public void proposePause() {
        moveToLocation("SalleRepos");
        
        for (AID tourist : guide.getAssignedTourists()) {
            guide.sendMessage(tourist, ACLMessage.PROPOSE, "BREAK_PROPOSAL:5");
        }
        
        logger.info("Pause proposée au groupe");
    }
    
    /**
     * Adapte le style d'explication
     */
    public void adaptExplanationStyle(boolean moreInteractive) {
        adaptiveMode = true;
        
        if (moreInteractive) {
            logger.info("Style adapté vers plus d'interactivité");
        } else {
            logger.info("Style adapté vers des explications plus concises");
        }
    }
    
    /**
     * Adapte l'itinéraire pour éviter une zone
     */
    public void adaptRoute(String locationToAvoid) {
        if (!avoidedLocations.contains(locationToAvoid)) {
            avoidedLocations.add(locationToAvoid);
        }
        
        // Si on est actuellement dans la zone à éviter
        if (currentLocation.equals(locationToAvoid)) {
            String alternative = findAlternativeTableau(locationToAvoid);
            moveToTableau(alternative);
        }
        
        logger.info("Itinéraire adapté pour éviter " + locationToAvoid);
    }
    
    /**
     * Évite une location en cas d'urgence
     */
    public void avoidLocation(String emergencyLocation) {
        logger.warning("Évitement d'urgence de " + emergencyLocation);
        adaptRoute(emergencyLocation);
    }
    
    /**
     * Trouve un tableau alternatif
     */
    private String findAlternativeTableau(String avoidedTableau) {
        String[] alternatives = {"Tableau1", "Tableau2", "Tableau3", "Tableau4", "Tableau5"};
        
        for (String alternative : alternatives) {
            if (!alternative.equals(avoidedTableau) && !avoidedLocations.contains(alternative)) {
                return alternative;
            }
        }
        
        return "SalleRepos"; // Fallback en cas de problème
    }
    
    /**
     * Détermine le prochain tableau de l'itinéraire
     */
    public void moveToNextTableau() {
        if (currentTableauIndex < 5) {
            String nextTableau = "Tableau" + (currentTableauIndex + 1);
            moveToTableau(nextTableau);
        } else {
            // Fin de visite
            concludeTour();
        }
    }
    
    /**
     * Conclut la visite
     */
    private void concludeTour() {
        logger.info("Conclusion de la visite");
        
        // Aller vers la sortie
        moveToLocation("Sortie");
        
        // Programmer la fin de visite
        guide.addBehaviour(new jade.core.behaviours.WakerBehaviour(guide, 3000) {
            @Override
            protected void onWake() {
                guide.endTour();
            }
        });
    }
    
    /**
     * Met à jour le progrès de la visite
     */
    public void updateProgress() {
        // Logique de progression automatique basée sur l'état du groupe
        // Cette méthode peut être appelée périodiquement pour faire avancer la visite
        
        if (guide.getGroupHandler().isGroupReady() && currentTableauIndex < 5) {
            // Si le groupe est prêt et il reste des tableaux à voir
            double groupSatisfaction = guide.getGroupHandler().getAverageSatisfaction();
            double groupFatigue = guide.getGroupHandler().getAverageFatigue();
            
            if (groupFatigue > 0.8) {
                proposePause();
            } else if (groupSatisfaction > 0.6) {
                // Le groupe apprécie, continuer
                guide.addBehaviour(new jade.core.behaviours.WakerBehaviour(guide, 5000) {
                    @Override
                    protected void onWake() {
                        moveToNextTableau();
                    }
                });
            }
        }
    }
    
    /**
     * Réinitialise le gestionnaire pour une nouvelle visite
     */
    public void reset() {
        currentLocation = "PointA";
        currentTableauIndex = 0;
        avoidedLocations.clear();
        adaptiveMode = false;
        plannedRoute = new ArrayList<>(List.of(DEFAULT_ROUTE));
        
        logger.info("TourManager réinitialisé pour nouvelle visite");
    }
    
    /**
     * Génère un rapport de performance de la visite
     */
    public TourReport generateTourReport() {
        return new TourReport(
            currentTableauIndex,
            guide.getGroupHandler().getAverageSatisfaction(),
            guide.getGroupHandler().getAverageFatigue(),
            adaptiveMode,
            avoidedLocations.size()
        );
    }
    
    // Getters
    public String getCurrentLocation() { return currentLocation; }
    public int getCurrentTableauIndex() { return currentTableauIndex; }
    public List<String> getPlannedRoute() { return new ArrayList<>(plannedRoute); }
    public List<String> getAvoidedLocations() { return new ArrayList<>(avoidedLocations); }
    public boolean isAdaptiveMode() { return adaptiveMode; }
    
    /**
     * Classe pour les rapports de visite
     */
    public static class TourReport {
        private int tableauxVisited;
        private double averageSatisfaction;
        private double averageFatigue;
        private boolean adaptiveMode;
        private int avoidedLocations;
        
        public TourReport(int tableauxVisited, double averageSatisfaction, 
                         double averageFatigue, boolean adaptiveMode, int avoidedLocations) {
            this.tableauxVisited = tableauxVisited;
            this.averageSatisfaction = averageSatisfaction;
            this.averageFatigue = averageFatigue;
            this.adaptiveMode = adaptiveMode;
            this.avoidedLocations = avoidedLocations;
        }
        
        // Getters
        public int getTableauxVisited() { return tableauxVisited; }
        public double getAverageSatisfaction() { return averageSatisfaction; }
        public double getAverageFatigue() { return averageFatigue; }
        public boolean isAdaptiveMode() { return adaptiveMode; }
        public int getAvoidedLocations() { return avoidedLocations; }
        
        @Override
        public String toString() {
            return String.format("TourReport[tableaux=%d, satisfaction=%.2f, fatigue=%.2f, adaptatif=%s]",
                    tableauxVisited, averageSatisfaction, averageFatigue, adaptiveMode);
        }
    }
}