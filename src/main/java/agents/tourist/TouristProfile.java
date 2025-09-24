package agents.tourist;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Profil complet d'un touriste avec ses caractéristiques personnelles
 */
public class TouristProfile {
    private String nationality;
    private int age;
    private List<String> artPreferences;
    private Personality personality;
    private double satisfaction;
    private double fatigue;
    private double interest;
    private int toursCompleted;
    
    public TouristProfile(String name) {
        initializeRandomProfile(name);
    }
    
    public TouristProfile(String nationality, int age, List<String> preferences) {
        this.nationality = nationality;
        this.age = age;
        this.artPreferences = new ArrayList<>(preferences);
        this.personality = new Personality();
        this.satisfaction = 0.5;
        this.fatigue = 0.0;
        this.interest = 0.7;
        this.toursCompleted = 0;
    }
    
    /**
     * Initialise un profil aléatoire basé sur le nom
     */
    private void initializeRandomProfile(String name) {
        Random rand = new Random(name.hashCode());
        
        String[] nationalities = {"Français", "Italien", "Anglais", "Allemand", 
                                "Espagnol", "Japonais", "Américain", "Chinois"};
        this.nationality = nationalities[rand.nextInt(nationalities.length)];
        
        this.age = 18 + rand.nextInt(60);
        
        // Préférences artistiques
        String[] artTypes = {"Renaissance", "Moderne", "Impressionniste", 
                           "Contemporain", "Classique", "Baroque"};
        this.artPreferences = new ArrayList<>();
        int numPrefs = 2 + rand.nextInt(3);
        for (int i = 0; i < numPrefs; i++) {
            String pref = artTypes[rand.nextInt(artTypes.length)];
            if (!artPreferences.contains(pref)) {
                artPreferences.add(pref);
            }
        }
        
        this.personality = new Personality(nationality, age);
        this.satisfaction = 0.4 + rand.nextDouble() * 0.2;
        this.fatigue = rand.nextDouble() * 0.1;
        this.interest = 0.6 + rand.nextDouble() * 0.3;
        this.toursCompleted = 0;
    }
    
    /**
     * Met à jour la satisfaction après une expérience
     */
    public void updateSatisfaction(double experienceRating) {
        satisfaction = (satisfaction * 0.7) + (experienceRating * 0.3);
        satisfaction = Math.max(0.0, Math.min(1.0, satisfaction));
    }
    
    /**
     * Augmente la fatigue
     */
    public void increaseFatigue(double amount) {
        fatigue = Math.min(1.0, fatigue + amount);
        
        // La fatigue affecte l'intérêt
        if (fatigue > 0.7) {
            interest = Math.max(0.1, interest - 0.1);
        }
    }
    
    /**
     * Réduit la fatigue (repos)
     */
    public void rest(double amount) {
        fatigue = Math.max(0.0, fatigue - amount);
        
        // Le repos peut augmenter l'intérêt
        if (fatigue < 0.3) {
            interest = Math.min(1.0, interest + 0.05);
        }
    }
    
    /**
     * Évalue l'affinité avec un type d'art
     */
    public double getAffinityFor(String artType) {
        if (artPreferences.contains(artType)) {
            return 0.8 + (Math.random() * 0.2);
        }
        return 0.3 + (Math.random() * 0.4);
    }
    
    /**
     * Détermine si le touriste pose des questions
     */
    public boolean shouldAskQuestion() {
        return personality.getCuriosity() > 0.6 && 
               interest > 0.5 && 
               Math.random() < 0.3;
    }
    
    /**
     * Calcule la satisfaction finale d'une visite
     */
    public double calculateFinalSatisfaction() {
        double finalScore = satisfaction;
        
        // Bonus pour l'engagement
        if (personality.getCuriosity() > 0.7) {
            finalScore += 0.1;
        }
        
        // Pénalité pour la fatigue excessive
        if (fatigue > 0.8) {
            finalScore -= 0.2;
        }
        
        // Bonus pour les préférences satisfaites
        finalScore += (interest - 0.5) * 0.2;
        
        return Math.max(0.0, Math.min(1.0, finalScore));
    }
    
    /**
     * Marque la fin d'une visite
     */
    public void completeTour() {
        toursCompleted++;
        // Légère augmentation de l'expérience
        personality.increaseExperience(0.05);
    }
    
    // Getters et Setters
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public List<String> getArtPreferences() { return new ArrayList<>(artPreferences); }
    public void addArtPreference(String preference) { artPreferences.add(preference); }
    
    public Personality getPersonality() { return personality; }
    
    public double getSatisfaction() { return satisfaction; }
    public void setSatisfaction(double satisfaction) { 
        this.satisfaction = Math.max(0.0, Math.min(1.0, satisfaction)); 
    }
    
    public double getFatigue() { return fatigue; }
    public void setFatigue(double fatigue) { 
        this.fatigue = Math.max(0.0, Math.min(1.0, fatigue)); 
    }
    
    public double getInterest() { return interest; }
    public void setInterest(double interest) { 
        this.interest = Math.max(0.0, Math.min(1.0, interest)); 
    }
    
    public int getToursCompleted() { return toursCompleted; }
    
    @Override
    public String toString() {
        return String.format("TouristProfile[%s, %d ans, satisfaction=%.2f, fatigue=%.2f]",
                nationality, age, satisfaction, fatigue);
    }
}