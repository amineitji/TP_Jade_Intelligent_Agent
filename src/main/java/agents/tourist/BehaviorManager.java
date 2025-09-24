package agents.tourist;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * Gestionnaire des comportements adaptatifs d'un touriste
 */
public class BehaviorManager {
    private List<TouristBehavior> activeBehaviors;
    private Map<String, Double> behaviorProbabilities;
    private Random random;
    private long lastUpdate;
    
    public BehaviorManager() {
        this.activeBehaviors = new ArrayList<>();
        this.behaviorProbabilities = new HashMap<>();
        this.random = new Random();
        this.lastUpdate = System.currentTimeMillis();
        
        initializeBehaviorProbabilities();
    }
    
    /**
     * Initialise les probabilités de base des comportements
     */
    private void initializeBehaviorProbabilities() {
        behaviorProbabilities.put("ASK_QUESTION", 0.2);
        behaviorProbabilities.put("EXPRESS_OPINION", 0.15);
        behaviorProbabilities.put("TAKE_PHOTO", 0.1);
        behaviorProbabilities.put("DISCUSS_WITH_OTHERS", 0.1);
        behaviorProbabilities.put("REQUEST_BREAK", 0.05);
        behaviorProbabilities.put("SHOW_IMPATIENCE", 0.05);
        behaviorProbabilities.put("SHOW_ENTHUSIASM", 0.15);
        behaviorProbabilities.put("REQUEST_DETAILS", 0.1);
    }
    
    /**
     * Met à jour les comportements basés sur le profil actuel
     */
    public void updateBehaviors(TouristProfile profile) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate < 5000) { // Mise à jour max toutes les 5 secondes
            return;
        }
        
        adjustProbabilities(profile);
        generateNewBehaviors(profile);
        cleanupExpiredBehaviors();
        
        lastUpdate = currentTime;
    }
    
    /**
     * Ajuste les probabilités selon le profil et l'état actuel
     */
    private void adjustProbabilities(TouristProfile profile) {
        Personality personality = profile.getPersonality();
        
        // Ajustements basés sur la curiosité
        double curiosityFactor = personality.getCuriosity();
        behaviorProbabilities.put("ASK_QUESTION", 0.1 + (curiosityFactor * 0.3));
        behaviorProbabilities.put("REQUEST_DETAILS", 0.05 + (curiosityFactor * 0.2));
        
        // Ajustements basés sur la sociabilité
        double socialFactor = personality.getSocialness();
        behaviorProbabilities.put("EXPRESS_OPINION", 0.1 + (socialFactor * 0.2));
        behaviorProbabilities.put("DISCUSS_WITH_OTHERS", 0.05 + (socialFactor * 0.15));
        
        // Ajustements basés sur la fatigue
        double fatigue = profile.getFatigue();
        behaviorProbabilities.put("REQUEST_BREAK", fatigue * 0.3);
        behaviorProbabilities.put("SHOW_IMPATIENCE", Math.max(0, (fatigue - 0.6) * 0.5));
        
        // Ajustements basés sur la satisfaction
        double satisfaction = profile.getSatisfaction();
        behaviorProbabilities.put("SHOW_ENTHUSIASM", satisfaction * 0.3);
        behaviorProbabilities.put("TAKE_PHOTO", satisfaction * 0.2);
        
        // Ajustements basés sur la patience
        double patience = personality.getPatience();
        behaviorProbabilities.put("SHOW_IMPATIENCE", 
            behaviorProbabilities.getOrDefault("SHOW_IMPATIENCE", 0.0) + 
            ((1.0 - patience) * 0.1));
    }
    
    /**
     * Génère de nouveaux comportements si nécessaire
     */
    private void generateNewBehaviors(TouristProfile profile) {
        for (Map.Entry<String, Double> entry : behaviorProbabilities.entrySet()) {
            String behaviorType = entry.getKey();
            double probability = entry.getValue();
            
            if (random.nextDouble() < probability && !hasBehaviorType(behaviorType)) {
                TouristBehavior behavior = createBehavior(behaviorType, profile);
                if (behavior != null) {
                    activeBehaviors.add(behavior);
                }
            }
        }
    }
    
    /**
     * Vérifie si un type de comportement est déjà actif
     */
    private boolean hasBehaviorType(String behaviorType) {
        return activeBehaviors.stream()
                .anyMatch(behavior -> behavior.getType().equals(behaviorType));
    }
    
    /**
     * Crée un comportement spécifique
     */
    private TouristBehavior createBehavior(String behaviorType, TouristProfile profile) {
        switch (behaviorType) {
            case "ASK_QUESTION":
                return new QuestionBehavior(profile);
            case "EXPRESS_OPINION":
                return new OpinionBehavior(profile);
            case "TAKE_PHOTO":
                return new PhotoBehavior(profile);
            case "DISCUSS_WITH_OTHERS":
                return new DiscussionBehavior(profile);
            case "REQUEST_BREAK":
                return new BreakRequestBehavior(profile);
            case "SHOW_IMPATIENCE":
                return new ImpatienceBehavior(profile);
            case "SHOW_ENTHUSIASM":
                return new EnthusiasmBehavior(profile);
            case "REQUEST_DETAILS":
                return new DetailsRequestBehavior(profile);
            default:
                return null;
        }
    }
    
    /**
     * Nettoie les comportements expirés
     */
    private void cleanupExpiredBehaviors() {
        activeBehaviors.removeIf(behavior -> !behavior.isActive());
    }
    
    /**
     * Exécute tous les comportements actifs
     */
    public List<BehaviorAction> executeActiveBehaviors() {
        List<BehaviorAction> actions = new ArrayList<>();
        
        for (TouristBehavior behavior : activeBehaviors) {
            if (behavior.shouldExecute()) {
                BehaviorAction action = behavior.execute();
                if (action != null) {
                    actions.add(action);
                }
            }
        }
        
        return actions;
    }
    
    /**
     * Force l'activation d'un comportement spécifique
     */
    public void forceBehavior(String behaviorType, TouristProfile profile) {
        TouristBehavior behavior = createBehavior(behaviorType, profile);
        if (behavior != null) {
            activeBehaviors.add(behavior);
        }
    }
    
    /**
     * Supprime tous les comportements d'un type donné
     */
    public void suppressBehaviorType(String behaviorType) {
        activeBehaviors.removeIf(behavior -> behavior.getType().equals(behaviorType));
        behaviorProbabilities.put(behaviorType, 0.0);
    }
    
    // Getters
    public List<TouristBehavior> getActiveBehaviors() {
        return new ArrayList<>(activeBehaviors);
    }
    
    public Map<String, Double> getBehaviorProbabilities() {
        return new HashMap<>(behaviorProbabilities);
    }
    
    public int getActiveBehaviorCount() {
        return activeBehaviors.size();
    }
}

/**
 * Classe abstraite pour les comportements de touriste
 */
abstract class TouristBehavior {
    protected String type;
    protected TouristProfile profile;
    protected long creationTime;
    protected long duration;
    protected boolean executed;
    
    public TouristBehavior(String type, TouristProfile profile, long duration) {
        this.type = type;
        this.profile = profile;
        this.duration = duration;
        this.creationTime = System.currentTimeMillis();
        this.executed = false;
    }
    
    /**
     * Détermine si le comportement doit être exécuté maintenant
     */
    public abstract boolean shouldExecute();
    
    /**
     * Exécute le comportement et retourne l'action à effectuer
     */
    public abstract BehaviorAction execute();
    
    /**
     * Vérifie si le comportement est toujours actif
     */
    public boolean isActive() {
        return !executed && (System.currentTimeMillis() - creationTime) < duration;
    }
    
    public String getType() {
        return type;
    }
}

/**
 * Comportement de question
 */
class QuestionBehavior extends TouristBehavior {
    private static final String[] QUESTIONS = {
        "Quelle technique a été utilisée ?",
        "Qui était l'artiste ?",
        "Quelle est la signification ?",
        "Quand cette œuvre a-t-elle été créée ?"
    };
    
    public QuestionBehavior(TouristProfile profile) {
        super("ASK_QUESTION", profile, 30000); // 30 secondes
    }
    
    @Override
    public boolean shouldExecute() {
        return profile.getPersonality().getCuriosity() > 0.5 && 
               profile.getInterest() > 0.4 && !executed;
    }
    
    @Override
    public BehaviorAction execute() {
        executed = true;
        String question = QUESTIONS[new Random().nextInt(QUESTIONS.length)];
        return new BehaviorAction("SEND_MESSAGE", "QUESTION:" + question);
    }
}

/**
 * Comportement d'expression d'opinion
 */
class OpinionBehavior extends TouristBehavior {
    private static final String[] OPINIONS = {
        "Cette œuvre me touche beaucoup",
        "Les couleurs sont magnifiques",
        "C'est très différent de ce que j'ai l'habitude de voir",
        "L'artiste avait un talent exceptionnel"
    };
    
    public OpinionBehavior(TouristProfile profile) {
        super("EXPRESS_OPINION", profile, 20000); // 20 secondes
    }
    
    @Override
    public boolean shouldExecute() {
        return profile.getPersonality().getSocialness() > 0.6 && 
               profile.getSatisfaction() > 0.5 && !executed;
    }
    
    @Override
    public BehaviorAction execute() {
        executed = true;
        String opinion = OPINIONS[new Random().nextInt(OPINIONS.length)];
        return new BehaviorAction("EXPRESS", opinion);
    }
}

/**
 * Comportement de prise de photo
 */
class PhotoBehavior extends TouristBehavior {
    public PhotoBehavior(TouristProfile profile) {
        super("TAKE_PHOTO", profile, 10000); // 10 secondes
    }
    
    @Override
    public boolean shouldExecute() {
        return profile.getSatisfaction() > 0.6 && !executed;
    }
    
    @Override
    public BehaviorAction execute() {
        executed = true;
        return new BehaviorAction("TAKE_PHOTO", "Photo prise de l'œuvre");
    }
}

/**
 * Autres comportements (implémentation simplifiée)
 */
class DiscussionBehavior extends TouristBehavior {
    public DiscussionBehavior(TouristProfile profile) {
        super("DISCUSS_WITH_OTHERS", profile, 45000);
    }
    
    @Override
    public boolean shouldExecute() {
        return profile.getPersonality().getSocialness() > 0.7 && !executed;
    }
    
    @Override
    public BehaviorAction execute() {
        executed = true;
        return new BehaviorAction("SOCIAL_INTERACTION", "Discussion avec d'autres visiteurs");
    }
}

class BreakRequestBehavior extends TouristBehavior {
    public BreakRequestBehavior(TouristProfile profile) {
        super("REQUEST_BREAK", profile, 60000);
    }
    
    @Override
    public boolean shouldExecute() {
        return profile.getFatigue() > 0.7 && !executed;
    }
    
    @Override
    public BehaviorAction execute() {
        executed = true;
        return new BehaviorAction("SEND_MESSAGE", "REQUEST_BREAK:Besoin de faire une pause");
    }
}

class ImpatienceBehavior extends TouristBehavior {
    public ImpatienceBehavior(TouristProfile profile) {
        super("SHOW_IMPATIENCE", profile, 30000);
    }
    
    @Override
    public boolean shouldExecute() {
        return profile.getPersonality().getPatience() < 0.4 && 
               profile.getFatigue() > 0.5 && !executed;
    }
    
    @Override
    public BehaviorAction execute() {
        executed = true;
        return new BehaviorAction("SHOW_EMOTION", "Impatience visible");
    }
}

class EnthusiasmBehavior extends TouristBehavior {
    public EnthusiasmBehavior(TouristProfile profile) {
        super("SHOW_ENTHUSIASM", profile, 25000);
    }
    
    @Override
    public boolean shouldExecute() {
        return profile.getSatisfaction() > 0.8 && 
               profile.getInterest() > 0.7 && !executed;
    }
    
    @Override
    public BehaviorAction execute() {
        executed = true;
        return new BehaviorAction("SHOW_EMOTION", "Enthousiasme visible");
    }
}

class DetailsRequestBehavior extends TouristBehavior {
    public DetailsRequestBehavior(TouristProfile profile) {
        super("REQUEST_DETAILS", profile, 40000);
    }
    
    @Override
    public boolean shouldExecute() {
        return profile.getPersonality().getCuriosity() > 0.8 && 
               profile.getPersonality().getExperience() > 0.5 && !executed;
    }
    
    @Override
    public BehaviorAction execute() {
        executed = true;
        return new BehaviorAction("SEND_MESSAGE", "REQUEST_MORE_DETAILS:Plus d'informations techniques");
    }
}

/**
 * Classe représentant une action générée par un comportement
 */
class BehaviorAction {
    private String actionType;
    private String actionData;
    private long timestamp;
    
    public BehaviorAction(String actionType, String actionData) {
        this.actionType = actionType;
        this.actionData = actionData;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public String getActionType() { return actionType; }
    public String getActionData() { return actionData; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("BehaviorAction[%s: %s]", actionType, actionData);
    }
}