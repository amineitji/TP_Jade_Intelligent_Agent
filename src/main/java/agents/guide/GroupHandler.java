package agents.guide;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Gestionnaire de groupe pour un guide - gère la dynamique et les statistiques du groupe
 */
public class GroupHandler {
    private GuideAgent guide;
    private Map<AID, TouristStats> touristStats;
    private GroupDynamics groupDynamics;
    private Logger logger;
    private int readyTourists;
    private long lastStatusUpdate;
    
    public GroupHandler(GuideAgent guide) {
        this.guide = guide;
        this.touristStats = new HashMap<>();
        this.groupDynamics = new GroupDynamics();
        this.logger = Logger.getLogger(GroupHandler.class.getName());
        this.readyTourists = 0;
        this.lastStatusUpdate = System.currentTimeMillis();
    }
    
    /**
     * Traite les status reçus des touristes
     */
    public void processTouristStatus(ACLMessage statusMsg) {
        AID touristAID = statusMsg.getSender();
        String[] parts = statusMsg.getContent().split(":");
        
        if (parts.length >= 3) {
            String statusType = parts[1];
            double value = Double.parseDouble(parts[2]);
            
            // Récupérer ou créer les stats du touriste
            TouristStats stats = touristStats.computeIfAbsent(touristAID, k -> new TouristStats());
            
            // Mettre à jour selon le type de status
            switch (statusType) {
                case "SATISFACTION":
                    stats.setSatisfaction(value);
                    break;
                case "FATIGUE":
                    stats.setFatigue(value);
                    break;
                case "INTEREST":
                    stats.setInterest(value);
                    break;
            }
            
            stats.updateTimestamp();
            lastStatusUpdate = System.currentTimeMillis();
            
            // Mettre à jour la dynamique de groupe
            updateGroupDynamics();
            
            logger.fine("Status mis à jour pour " + touristAID.getLocalName() + 
                       ": " + statusType + "=" + value);
        }
    }
    
    /**
     * Vérifie l'état général du groupe
     */
    public void checkGroupStatus() {
        if (touristStats.isEmpty()) return;
        
        double avgSatisfaction = getAverageSatisfaction();
        double avgFatigue = getAverageFatigue();
        double avgInterest = getAverageInterest();
        
        // Détection de problèmes dans le groupe
        if (avgSatisfaction < 0.3) {
            handleLowSatisfaction();
        } else if (avgFatigue > 0.8) {
            handleHighFatigue();
        } else if (avgInterest < 0.3) {
            handleLowInterest();
        }
        
        // Mise à jour de la dynamique
        groupDynamics.update(avgSatisfaction, avgFatigue, avgInterest);
        
        // Log périodique
        if (System.currentTimeMillis() - lastStatusUpdate > 30000) { // 30 secondes
            logger.info(String.format("Groupe: satisfaction=%.2f, fatigue=%.2f, intérêt=%.2f",
                    avgSatisfaction, avgFatigue, avgInterest));
        }
    }
    
    /**
     * Gère une satisfaction faible du groupe
     */
    private void handleLowSatisfaction() {
        logger.warning("Satisfaction faible du groupe détectée");
        
        // Adapter le style d'explication
        guide.getTourManager().adaptExplanationStyle(true);
        
        // Identifier les touristes les moins satisfaits
        for (Map.Entry<AID, TouristStats> entry : touristStats.entrySet()) {
            if (entry.getValue().getSatisfaction() < 0.2) {
                // Attention particulière à ce touriste
                logger.info("Attention particulière requise pour " + 
                           entry.getKey().getLocalName());
            }
        }
    }
    
    /**
     * Gère une fatigue élevée du groupe
     */
    private void handleHighFatigue() {
        logger.warning("Fatigue élevée du groupe détectée");
        guide.getTourManager().proposePause();
    }
    
    /**
     * Gère un intérêt faible du groupe
     */
    private void handleLowInterest() {
        logger.warning("Intérêt faible du groupe détecté");
        
        // Changer de tableau ou proposer une interaction
        // Cette logique peut être étendue selon les besoins
    }
    
    /**
     * Vérifie si le groupe est prêt à continuer
     */
    public void checkIfGroupReady() {
        readyTourists++;
        
        if (readyTourists >= guide.getAssignedTourists().size()) {
            // Tout le groupe est prêt
            readyTourists = 0;
            
            // Décider de l'action suivante basée sur l'état du groupe
            if (shouldContinueTour()) {
                guide.getTourManager().moveToNextTableau();
            }
        }
    }
    
    /**
     * Détermine si le groupe peut continuer la visite
     */
    private boolean shouldContinueTour() {
        double avgFatigue = getAverageFatigue();
        double avgSatisfaction = getAverageSatisfaction();
        
        return avgFatigue < 0.8 && avgSatisfaction > 0.3;
    }
    
    /**
     * Acknowledge qu'un touriste est prêt
     */
    public void acknowledgeTouristReady(AID touristAID) {
        logger.fine("Touriste " + touristAID.getLocalName() + " signale être prêt");
        checkIfGroupReady();
    }
    
    /**
     * Met à jour la dynamique de groupe
     */
    private void updateGroupDynamics() {
        if (touristStats.size() < 2) return;
        
        // Calcul de l'homogénéité du groupe
        double satisfactionVariance = calculateVariance("satisfaction");
        double fatigueVariance = calculateVariance("fatigue");
        
        groupDynamics.setHomogeneity(1.0 - Math.max(satisfactionVariance, fatigueVariance));
        
        // Calcul de l'engagement
        double avgInterest = getAverageInterest();
        groupDynamics.setEngagement(avgInterest);
        
        // Détection de touristes problématiques
        identifyProblematicTourists();
    }
    
    /**
     * Calcule la variance pour une métrique donnée
     */
    private double calculateVariance(String metric) {
        if (touristStats.isEmpty()) return 0.0;
        
        double sum = 0.0;
        double sumSquares = 0.0;
        int count = 0;
        
        for (TouristStats stats : touristStats.values()) {
            double value = 0.0;
            switch (metric) {
                case "satisfaction":
                    value = stats.getSatisfaction();
                    break;
                case "fatigue":
                    value = stats.getFatigue();
                    break;
                case "interest":
                    value = stats.getInterest();
                    break;
            }
            
            sum += value;
            sumSquares += value * value;
            count++;
        }
        
        if (count <= 1) return 0.0;
        
        double mean = sum / count;
        double variance = (sumSquares / count) - (mean * mean);
        
        return Math.max(0.0, Math.min(1.0, variance));
    }
    
    /**
     * Identifie les touristes qui posent des problèmes dans le groupe
     */
    private void identifyProblematicTourists() {
        double avgSatisfaction = getAverageSatisfaction();
        
        for (Map.Entry<AID, TouristStats> entry : touristStats.entrySet()) {
            TouristStats stats = entry.getValue();
            
            // Touriste très en dessous de la moyenne
            if (stats.getSatisfaction() < avgSatisfaction - 0.3) {
                groupDynamics.addProblematicTourist(entry.getKey());
                logger.info("Touriste problématique identifié: " + 
                           entry.getKey().getLocalName());
            }
        }
    }
    
    /**
     * Calcule la satisfaction moyenne du groupe
     */
    public double getAverageSatisfaction() {
        return touristStats.values().stream()
                .mapToDouble(TouristStats::getSatisfaction)
                .average()
                .orElse(0.5);
    }
    
    /**
     * Calcule la fatigue moyenne du groupe
     */
    public double getAverageFatigue() {
        return touristStats.values().stream()
                .mapToDouble(TouristStats::getFatigue)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Calcule l'intérêt moyen du groupe
     */
    public double getAverageInterest() {
        return touristStats.values().stream()
                .mapToDouble(TouristStats::getInterest)
                .average()
                .orElse(0.7);
    }
    
    /**
     * Vérifie si le groupe est prêt (tous les touristes ont répondu)
     */
    public boolean isGroupReady() {
        return readyTourists >= guide.getAssignedTourists().size();
    }
    
    /**
     * Réinitialise le gestionnaire pour un nouveau groupe
     */
    public void reset() {
        touristStats.clear();
        groupDynamics.reset();
        readyTourists = 0;
        lastStatusUpdate = System.currentTimeMillis();
        
        logger.info("GroupHandler réinitialisé pour nouveau groupe");
    }
    
    /**
     * Génère un rapport de groupe
     */
    public GroupReport generateReport() {
        return new GroupReport(
            touristStats.size(),
            getAverageSatisfaction(),
            getAverageFatigue(),
            getAverageInterest(),
            groupDynamics.getHomogeneity(),
            groupDynamics.getEngagement(),
            groupDynamics.getProblematicTourists().size()
        );
    }
    
    // Getters
    public Map<AID, TouristStats> getTouristStats() { 
        return new HashMap<>(touristStats); 
    }
    
    public GroupDynamics getGroupDynamics() { return groupDynamics; }
    
    /**
     * Classe pour stocker les statistiques d'un touriste
     */
    public static class TouristStats {
        private double satisfaction = 0.5;
        private double fatigue = 0.0;
        private double interest = 0.7;
        private long lastUpdate = System.currentTimeMillis();
        
        public void setSatisfaction(double satisfaction) {
            this.satisfaction = Math.max(0.0, Math.min(1.0, satisfaction));
        }
        
        public void setFatigue(double fatigue) {
            this.fatigue = Math.max(0.0, Math.min(1.0, fatigue));
        }
        
        public void setInterest(double interest) {
            this.interest = Math.max(0.0, Math.min(1.0, interest));
        }
        
        public void updateTimestamp() {
            this.lastUpdate = System.currentTimeMillis();
        }
        
        // Getters
        public double getSatisfaction() { return satisfaction; }
        public double getFatigue() { return fatigue; }
        public double getInterest() { return interest; }
        public long getLastUpdate() { return lastUpdate; }
        
        @Override
        public String toString() {
            return String.format("TouristStats[sat=%.2f, fatigue=%.2f, intérêt=%.2f]",
                    satisfaction, fatigue, interest);
        }
    }
    
    /**
     * Classe pour gérer la dynamique de groupe
     */
    public static class GroupDynamics {
        private double homogeneity = 0.5;
        private double engagement = 0.7;
        private java.util.Set<AID> problematicTourists = new java.util.HashSet<>();
        
        public void update(double avgSatisfaction, double avgFatigue, double avgInterest) {
            // La logique de mise à jour de la dynamique peut être étendue
        }
        
        public void reset() {
            homogeneity = 0.5;
            engagement = 0.7;
            problematicTourists.clear();
        }
        
        public void addProblematicTourist(AID tourist) {
            problematicTourists.add(tourist);
        }
        
        // Getters et Setters
        public double getHomogeneity() { return homogeneity; }
        public void setHomogeneity(double homogeneity) { 
            this.homogeneity = Math.max(0.0, Math.min(1.0, homogeneity)); 
        }
        
        public double getEngagement() { return engagement; }
        public void setEngagement(double engagement) { 
            this.engagement = Math.max(0.0, Math.min(1.0, engagement)); 
        }
        
        public java.util.Set<AID> getProblematicTourists() { 
            return new java.util.HashSet<>(problematicTourists); 
        }
    }
    
    /**
     * Rapport de groupe
     */
    public static class GroupReport {
        private int groupSize;
        private double avgSatisfaction;
        private double avgFatigue;
        private double avgInterest;
        private double homogeneity;
        private double engagement;
        private int problematicCount;
        
        public GroupReport(int groupSize, double avgSatisfaction, double avgFatigue,
                          double avgInterest, double homogeneity, double engagement,
                          int problematicCount) {
            this.groupSize = groupSize;
            this.avgSatisfaction = avgSatisfaction;
            this.avgFatigue = avgFatigue;
            this.avgInterest = avgInterest;
            this.homogeneity = homogeneity;
            this.engagement = engagement;
            this.problematicCount = problematicCount;
        }
        
        // Getters
        public int getGroupSize() { return groupSize; }
        public double getAvgSatisfaction() { return avgSatisfaction; }
        public double getAvgFatigue() { return avgFatigue; }
        public double getAvgInterest() { return avgInterest; }
        public double getHomogeneity() { return homogeneity; }
        public double getEngagement() { return engagement; }
        public int getProblematicCount() { return problematicCount; }
        
        @Override
        public String toString() {
            return String.format("GroupReport[taille=%d, satisfaction=%.2f, homogénéité=%.2f]",
                    groupSize, avgSatisfaction, homogeneity);
        }
    }
}