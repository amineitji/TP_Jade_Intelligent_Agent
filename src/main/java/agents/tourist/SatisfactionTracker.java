package agents.tourist;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * Classe pour suivre et analyser la satisfaction d'un touriste
 */
public class SatisfactionTracker {
    private TouristProfile profile;
    private List<ExperienceRecord> experiences;
    private Map<String, Double> categoryRatings;
    private double currentSatisfaction;
    private LocalDateTime sessionStart;
    
    public SatisfactionTracker(TouristProfile profile) {
        this.profile = profile;
        this.experiences = new ArrayList<>();
        this.categoryRatings = new HashMap<>();
        this.currentSatisfaction = profile.getSatisfaction();
        this.sessionStart = LocalDateTime.now();
        
        initializeCategories();
    }
    
    /**
     * Initialise les catégories d'expérience
     */
    private void initializeCategories() {
        categoryRatings.put("guide_compatibility", 0.5);
        categoryRatings.put("explanation", 0.5);
        categoryRatings.put("artwork", 0.5);
        categoryRatings.put("interaction", 0.5);
        categoryRatings.put("group_dynamics", 0.5);
        categoryRatings.put("fatigue_management", 0.5);
    }
    
    /**
     * Enregistre une nouvelle expérience
     */
    public void recordExperience(String category, double rating) {
        recordExperience(category, rating, null);
    }
    
    /**
     * Enregistre une nouvelle expérience avec contexte
     */
    public void recordExperience(String category, double rating, String context) {
        ExperienceRecord record = new ExperienceRecord(category, rating, context);
        experiences.add(record);
        
        // Mise à jour de la moyenne de catégorie
        updateCategoryRating(category, rating);
        
        // Recalcul de la satisfaction globale
        recalculateSatisfaction();
    }
    
    /**
     * Met à jour la note d'une catégorie
     */
    private void updateCategoryRating(String category, double rating) {
        Double currentRating = categoryRatings.get(category);
        if (currentRating == null) {
            categoryRatings.put(category, rating);
        } else {
            // Moyenne pondérée avec l'historique
            double newRating = (currentRating * 0.7) + (rating * 0.3);
            categoryRatings.put(category, newRating);
        }
    }
    
    /**
     * Recalcule la satisfaction globale
     */
    private void recalculateSatisfaction() {
        if (experiences.isEmpty()) {
            return;
        }
        
        // Pondération des différentes catégories
        Map<String, Double> weights = getCategoryWeights();
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (Map.Entry<String, Double> entry : categoryRatings.entrySet()) {
            String category = entry.getKey();
            double rating = entry.getValue();
            double weight = weights.getOrDefault(category, 1.0);
            
            weightedSum += rating * weight;
            totalWeight += weight;
        }
        
        currentSatisfaction = weightedSum / totalWeight;
        
        // Ajustements basés sur la personnalité
        applyPersonalityAdjustments();
        
        // Mise à jour du profil
        profile.setSatisfaction(currentSatisfaction);
    }
    
    /**
     * Retourne les poids des catégories basés sur la personnalité
     */
    private Map<String, Double> getCategoryWeights() {
        Map<String, Double> weights = new HashMap<>();
        Personality personality = profile.getPersonality();
        
        // Ajustement des poids selon la personnalité
        weights.put("guide_compatibility", 1.0);
        weights.put("explanation", 0.8 + (personality.getCuriosity() * 0.4));
        weights.put("artwork", 1.2); // Toujours important
        weights.put("interaction", 0.6 + (personality.getSocialness() * 0.6));
        weights.put("group_dynamics", 0.7 + (personality.getSocialness() * 0.4));
        weights.put("fatigue_management", 0.5 + (personality.getEnergy() * 0.5));
        
        return weights;
    }
    
    /**
     * Applique des ajustements basés sur la personnalité
     */
    private void applyPersonalityAdjustments() {
        Personality personality = profile.getPersonality();
        
        // Les personnes très curieuses sont plus exigeantes
        if (personality.getCuriosity() > 0.8) {
            currentSatisfaction *= 0.95;
        }
        
        // Les personnes très sociales apprécient l'interaction
        if (personality.getSocialness() > 0.8 && 
            categoryRatings.get("interaction") > 0.7) {
            currentSatisfaction *= 1.05;
        }
        
        // Les personnes peu patientes sont affectées par la fatigue
        if (personality.getPatience() < 0.4 && profile.getFatigue() > 0.6) {
            currentSatisfaction *= 0.9;
        }
        
        // Normaliser
        currentSatisfaction = Math.max(0.0, Math.min(1.0, currentSatisfaction));
    }
    
    /**
     * Analyse les tendances de satisfaction
     */
    public SatisfactionAnalysis analyzeTrends() {
        if (experiences.size() < 2) {
            return new SatisfactionAnalysis("Insufficient data", currentSatisfaction);
        }
        
        // Calcul de la tendance
        double trend = calculateTrend();
        String trendDescription = describeTrend(trend);
        
        // Identification des points forts et faibles
        String strongestCategory = findStrongestCategory();
        String weakestCategory = findWeakestCategory();
        
        // Recommandations
        List<String> recommendations = generateRecommendations();
        
        return new SatisfactionAnalysis(trendDescription, currentSatisfaction, 
                                      strongestCategory, weakestCategory, recommendations);
    }
    
    /**
     * Calcule la tendance de satisfaction
     */
    private double calculateTrend() {
        if (experiences.size() < 3) {
            return 0.0;
        }
        
        // Régression linéaire simple sur les dernières expériences
        int n = Math.min(experiences.size(), 5); // 5 dernières expériences
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = experiences.size() - n; i < experiences.size(); i++) {
            double x = i;
            double y = experiences.get(i).getRating();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }
    
    /**
     * Décrit la tendance en mots
     */
    private String describeTrend(double trend) {
        if (Math.abs(trend) < 0.01) {
            return "Stable";
        } else if (trend > 0.05) {
            return "En forte amélioration";
        } else if (trend > 0.01) {
            return "En amélioration";
        } else if (trend < -0.05) {
            return "En forte dégradation";
        } else {
            return "En légère dégradation";
        }
    }
    
    /**
     * Trouve la catégorie la mieux notée
     */
    private String findStrongestCategory() {
        return categoryRatings.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Aucune");
    }
    
    /**
     * Trouve la catégorie la moins bien notée
     */
    private String findWeakestCategory() {
        return categoryRatings.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Aucune");
    }
    
    /**
     * Génère des recommandations pour améliorer l'expérience
     */
    private List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        // Recommandations basées sur les faiblesses
        for (Map.Entry<String, Double> entry : categoryRatings.entrySet()) {
            String category = entry.getKey();
            double rating = entry.getValue();
            
            if (rating < 0.4) {
                switch (category) {
                    case "explanation":
                        recommendations.add("Adapter le style d'explication au profil du visiteur");
                        break;
                    case "interaction":
                        recommendations.add("Encourager davantage d'interactions avec le guide");
                        break;
                    case "fatigue_management":
                        recommendations.add("Proposer des pauses plus fréquentes");
                        break;
                    case "group_dynamics":
                        recommendations.add("Améliorer la cohésion du groupe");
                        break;
                }
            }
        }
        
        // Recommandations basées sur la personnalité
        Personality personality = profile.getPersonality();
        if (personality.getCuriosity() > 0.7 && categoryRatings.get("explanation") < 0.6) {
            recommendations.add("Proposer des explications plus détaillées et techniques");
        }
        
        if (personality.getSocialness() > 0.7 && categoryRatings.get("interaction") < 0.6) {
            recommendations.add("Augmenter les opportunités d'interaction sociale");
        }
        
        return recommendations;
    }
    
    /**
     * Remet à zéro le tracker pour une nouvelle visite
     */
    public void reset() {
        experiences.clear();
        initializeCategories();
        sessionStart = LocalDateTime.now();
        currentSatisfaction = 0.5;
    }
    
    /**
     * Retourne un résumé de la session
     */
    public String getSessionSummary() {
        long durationMinutes = java.time.Duration.between(sessionStart, LocalDateTime.now()).toMinutes();
        
        return String.format("Session: %d expériences en %d minutes, satisfaction finale: %.2f",
                experiences.size(), durationMinutes, currentSatisfaction);
    }
    
    // Getters
    public double getCurrentSatisfaction() { return currentSatisfaction; }
    public List<ExperienceRecord> getExperiences() { return new ArrayList<>(experiences); }
    public Map<String, Double> getCategoryRatings() { return new HashMap<>(categoryRatings); }
    
    /**
     * Classe interne pour représenter une expérience
     */
    public static class ExperienceRecord {
        private String category;
        private double rating;
        private String context;
        private LocalDateTime timestamp;
        
        public ExperienceRecord(String category, double rating, String context) {
            this.category = category;
            this.rating = rating;
            this.context = context;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public String getCategory() { return category; }
        public double getRating() { return rating; }
        public String getContext() { return context; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * Classe pour l'analyse de satisfaction
     */
    public static class SatisfactionAnalysis {
        private String trend;
        private double currentLevel;
        private String strongestAspect;
        private String weakestAspect;
        private List<String> recommendations;
        
        public SatisfactionAnalysis(String trend, double currentLevel) {
            this.trend = trend;
            this.currentLevel = currentLevel;
            this.recommendations = new ArrayList<>();
        }
        
        public SatisfactionAnalysis(String trend, double currentLevel, 
                                  String strongest, String weakest, 
                                  List<String> recommendations) {
            this.trend = trend;
            this.currentLevel = currentLevel;
            this.strongestAspect = strongest;
            this.weakestAspect = weakest;
            this.recommendations = new ArrayList<>(recommendations);
        }
        
        // Getters
        public String getTrend() { return trend; }
        public double getCurrentLevel() { return currentLevel; }
        public String getStrongestAspect() { return strongestAspect; }
        public String getWeakestAspect() { return weakestAspect; }
        public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
    }
}