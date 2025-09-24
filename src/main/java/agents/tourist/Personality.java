package agents.tourist;
import java.util.Random;

/**
 * Classe représentant la personnalité d'un touriste
 */
public class Personality {
    private double curiosity;      // Tendance à poser des questions
    private double socialness;     // Tendance à interagir socialement
    private double patience;       // Tolérance aux explications longues
    private double openness;       // Ouverture à de nouveaux types d'art
    private double energy;         // Niveau d'énergie général
    private double experience;     // Expérience accumulée en art
    
    public Personality() {
        Random rand = new Random();
        this.curiosity = 0.3 + rand.nextDouble() * 0.7;
        this.socialness = 0.2 + rand.nextDouble() * 0.8;
        this.patience = 0.3 + rand.nextDouble() * 0.7;
        this.openness = 0.4 + rand.nextDouble() * 0.6;
        this.energy = 0.5 + rand.nextDouble() * 0.5;
        this.experience = rand.nextDouble() * 0.3;
    }
    
    public Personality(String nationality, int age) {
        this();
        applyNationalityAdjustments(nationality);
        applyAgeAdjustments(age);
    }
    
    /**
     * Ajustements culturels basés sur la nationalité
     */
    private void applyNationalityAdjustments(String nationality) {
        switch (nationality) {
            case "Japonais":
                patience += 0.2;
                socialness -= 0.1;
                break;
            case "Italien":
                socialness += 0.2;
                energy += 0.1;
                break;
            case "Allemand":
                patience += 0.15;
                curiosity += 0.1;
                break;
            case "Français":
                openness += 0.1;
                curiosity += 0.05;
                break;
            case "Américain":
                socialness += 0.1;
                energy += 0.15;
                break;
            case "Chinois":
                patience += 0.1;
                experience += 0.05;
                break;
        }
        normalizeValues();
    }
    
    /**
     * Ajustements basés sur l'âge
     */
    private void applyAgeAdjustments(int age) {
        if (age > 60) {
            patience += 0.2;
            energy -= 0.15;
            experience += 0.1;
        } else if (age > 40) {
            patience += 0.1;
            experience += 0.05;
        } else if (age < 25) {
            energy += 0.2;
            socialness += 0.1;
            openness += 0.15;
        }
        normalizeValues();
    }
    
    /**
     * Normalise toutes les valeurs entre 0 et 1
     */
    private void normalizeValues() {
        curiosity = Math.max(0.0, Math.min(1.0, curiosity));
        socialness = Math.max(0.0, Math.min(1.0, socialness));
        patience = Math.max(0.0, Math.min(1.0, patience));
        openness = Math.max(0.0, Math.min(1.0, openness));
        energy = Math.max(0.0, Math.min(1.0, energy));
        experience = Math.max(0.0, Math.min(1.0, experience));
    }
    
    /**
     * Augmente l'expérience après une visite
     */
    public void increaseExperience(double amount) {
        experience = Math.min(1.0, experience + amount);
        
        // L'expérience peut affecter d'autres traits
        if (experience > 0.7) {
            patience = Math.min(1.0, patience + 0.05);
            openness = Math.min(1.0, openness + 0.03);
        }
    }
    
    /**
     * Calcule un score de compatibilité avec un type d'explication
     */
    public double getCompatibilityScore(String explanationType) {
        double score = 0.5;
        
        switch (explanationType.toLowerCase()) {
            case "detailed":
                score += (patience * 0.4) + (curiosity * 0.3);
                break;
            case "interactive":
                score += (socialness * 0.5) + (energy * 0.2);
                break;
            case "technical":
                score += (experience * 0.4) + (curiosity * 0.3);
                break;
            case "historical":
                score += (patience * 0.3) + (experience * 0.2);
                break;
            default:
                score = 0.5;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Détermine la probabilité de poser une question
     */
    public double getQuestionProbability() {
        return (curiosity * 0.6) + (socialness * 0.3) + (experience * 0.1);
    }
    
    /**
     * Détermine la tolérance à la fatigue
     */
    public double getFatigueTolerance() {
        return (energy * 0.5) + (patience * 0.3) + (experience * 0.2);
    }
    
    // Getters
    public double getCuriosity() { return curiosity; }
    public double getSocialness() { return socialness; }
    public double getPatience() { return patience; }
    public double getOpenness() { return openness; }
    public double getEnergy() { return energy; }
    public double getExperience() { return experience; }
    
    @Override
    public String toString() {
        return String.format("Personality[curiosity=%.2f, social=%.2f, patience=%.2f, openness=%.2f]",
                curiosity, socialness, patience, openness);
    }
}