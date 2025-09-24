package agents.guide;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * Profil d'un guide avec ses spécialisations et performances
 */
public class GuideProfile {
    private String specialization;
    private int experienceLevel;        // Niveau d'expérience (0-10)
    private double efficiency;          // Efficacité générale (0.0-1.0)
    private double knowledge;           // Connaissance artistique (0.0-1.0)
    private double communication;       // Compétences de communication (0.0-1.0)
    private double adaptability;        // Capacité d'adaptation aux groupes (0.0-1.0)
    private int completedTours;
    private double averageSatisfaction;
    private double averageFatigue;
    private Map<String, Double> tableauExpertise;  // Expertise par tableau
    
    public GuideProfile(String name) {
        initializeRandomProfile(name);
    }
    
    public GuideProfile(String specialization, int experienceLevel) {
        this.specialization = specialization;
        this.experienceLevel = Math.max(0, Math.min(10, experienceLevel));
        initializeSkills();
    }
    
    /**
     * Initialise un profil aléatoire basé sur le nom
     */
    private void initializeRandomProfile(String name) {
        String[] specializations = {"Renaissance", "Moderne", "Impressionniste", 
                                  "Contemporain", "Classique", "Baroque"};
        
        Random rand = new Random(name.hashCode());
        this.specialization = specializations[rand.nextInt(specializations.length)];
        this.experienceLevel = rand.nextInt(8) + 2; // 2-10
        
        initializeSkills();
    }
    
    /**
     * Initialise les compétences basées sur l'expérience et la spécialisation
     */
    private void initializeSkills() {
        Random rand = new Random();
        
        // Base sur le niveau d'expérience
        double baseSkill = 0.4 + (experienceLevel / 10.0) * 0.4;
        
        this.efficiency = baseSkill + rand.nextDouble() * 0.2;
        this.knowledge = baseSkill + rand.nextDouble() * 0.2;
        this.communication = baseSkill + rand.nextDouble() * 0.2;
        this.adaptability = baseSkill + rand.nextDouble() * 0.2;
        
        // Ajustements selon la spécialisation
        applySpecializationBonus();
        
        // Normaliser les valeurs
        normalizeSkills();
        
        // Initialiser les statistiques
        this.completedTours = 0;
        this.averageSatisfaction = 0.5;
        this.averageFatigue = 0.0;
        
        // Initialiser l'expertise par tableau
        initializeTableauExpertise();
    }
    
    /**
     * Applique des bonus selon la spécialisation
     */
    private void applySpecializationBonus() {
        switch (specialization) {
            case "Renaissance":
                knowledge += 0.15;
                communication += 0.1;
                break;
            case "Moderne":
                adaptability += 0.15;
                efficiency += 0.1;
                break;
            case "Impressionniste":
                communication += 0.2;
                break;
            case "Contemporain":
                adaptability += 0.2;
                break;
            case "Classique":
                knowledge += 0.1;
                communication += 0.15;
                break;
        }
    }
    
    /**
     * Normalise toutes les compétences entre 0 et 1
     */
    private void normalizeSkills() {
        efficiency = Math.max(0.0, Math.min(1.0, efficiency));
        knowledge = Math.max(0.0, Math.min(1.0, knowledge));
        communication = Math.max(0.0, Math.min(1.0, communication));
        adaptability = Math.max(0.0, Math.min(1.0, adaptability));
    }
    
    /**
     * Initialise l'expertise pour chaque tableau
     */
    private void initializeTableauExpertise() {
        tableauExpertise = new HashMap<>();
        String[] tableaux = {"Tableau1", "Tableau2", "Tableau3", "Tableau4", "Tableau5"};
        
        for (String tableau : tableaux) {
            // Expertise plus élevée pour les tableaux de sa spécialisation
            double expertise = knowledge * 0.8 + Math.random() * 0.2;
            
            // Bonus pour les tableaux correspondant à la spécialisation
            if (isTableauInSpecialization(tableau)) {
                expertise += 0.2;
            }
            
            tableauExpertise.put(tableau, Math.max(0.0, Math.min(1.0, expertise)));
        }
    }
    
    /**
     * Vérifie si un tableau correspond à la spécialisation du guide
     */
    private boolean isTableauInSpecialization(String tableau) {
        // Mapping simple tableau -> style artistique
        switch (tableau) {
            case "Tableau1": // La Joconde
            case "Tableau5": // L'École d'Athènes
                return "Renaissance".equals(specialization);
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
     * Met à jour les performances après une visite
     */
    public void updatePerformance(double groupSatisfaction, double groupFatigue) {
        completedTours++;
        
        // Mise à jour des moyennes
        averageSatisfaction = (averageSatisfaction * (completedTours - 1) + groupSatisfaction) / completedTours;
        averageFatigue = (averageFatigue * (completedTours - 1) + groupFatigue) / completedTours;
        
        // Amélioration des compétences avec l'expérience
        if (completedTours % 5 == 0) { // Tous les 5 tours
            improveSkills(groupSatisfaction);
        }
    }
    
    /**
     * Améliore les compétences basé sur les performances
     */
    private void improveSkills(double satisfaction) {
        double improvement = 0.01; // Amélioration de base
        
        if (satisfaction > 0.8) {
            improvement *= 2; // Double amélioration pour excellentes performances
        } else if (satisfaction < 0.4) {
            improvement *= 0.5; // Amélioration réduite pour mauvaises performances
        }
        
        // Amélioration aléatoire d'une compétence
        Random rand = new Random();
        switch (rand.nextInt(4)) {
            case 0:
                efficiency = Math.min(1.0, efficiency + improvement);
                break;
            case 1:
                knowledge = Math.min(1.0, knowledge + improvement);
                break;
            case 2:
                communication = Math.min(1.0, communication + improvement);
                break;
            case 3:
                adaptability = Math.min(1.0, adaptability + improvement);
                break;
        }
    }
    
    /**
     * Calcule l'efficacité pour un tableau spécifique
     */
    public double getTableauEfficiency(String tableau) {
        Double expertise = tableauExpertise.get(tableau);
        if (expertise == null) {
            expertise = knowledge * 0.5; // Efficacité par défaut
        }
        
        return (efficiency * 0.5) + (expertise * 0.5);
    }
    
    /**
     * Calcule un score global de performance
     */
    public double getOverallScore() {
        return (efficiency * 0.3) + (knowledge * 0.25) + 
               (communication * 0.25) + (adaptability * 0.2);
    }
    
    /**
     * Détermine le style d'explication optimal pour ce guide
     */
    public String getOptimalExplanationStyle() {
        if (communication > 0.8) return "interactive";
        if (knowledge > 0.8) return "detailed";
        if (adaptability > 0.8) return "adaptive";
        return "standard";
    }
    
    // Getters et Setters
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public int getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(int experienceLevel) { 
        this.experienceLevel = Math.max(0, Math.min(10, experienceLevel)); 
    }
    
    public double getEfficiency() { return efficiency; }
    public double getKnowledge() { return knowledge; }
    public double getCommunication() { return communication; }
    public double getAdaptability() { return adaptability; }
    
    public int getCompletedTours() { return completedTours; }
    public double getAverageSatisfaction() { return averageSatisfaction; }
    public double getAverageFatigue() { return averageFatigue; }
    
    public Map<String, Double> getTableauExpertise() { 
        return new HashMap<>(tableauExpertise); 
    }
    
    @Override
    public String toString() {
        return String.format("GuideProfile[%s, niveau=%d, efficacité=%.2f, tours=%d]",
                specialization, experienceLevel, efficiency, completedTours);
    }
}