package config;

import models.Tableau;
import java.util.*;

/**
 * Configuration centralisée de l'exposition
 */
public class ExhibitionConfig {
    
    // Configuration des tableaux
    public static final int NOMBRE_TABLEAUX = 8;
    public static final int TEMPS_EXPLICATION_PAR_TABLEAU = 5; // en secondes pour la simulation
    
    // Configuration des guides
    public static final int NOMBRE_GUIDES_INITIAL = 5;
    public static final int TAILLE_MAX_GROUPE = 15;
    public static final int TAILLE_MIN_GROUPE = 4;
    
    // Configuration du système
    public static final String POINT_RENDEZ_VOUS = "Point A - Entrée principale";
    public static final boolean INTERFACE_GRAPHIQUE_JADE = true;
    public static final int DELAI_BETWEEN_GROUPS = 2000; // ms
    
    // Horaires d'ouverture
    public static final int HEURE_OUVERTURE = 9;  // 9h
    public static final int HEURE_FERMETURE = 18; // 18h
    
    /**
     * Initialise la collection complète de tableaux avec toutes leurs informations
     */
    public static List<Tableau> initializeTableaux() {
        List<Tableau> tableaux = new ArrayList<>();
        
        // La Joconde
        tableaux.add(new Tableau(
            "TAB001", "La Joconde", "Léonard de Vinci", 1503, "Renaissance",
            "Portrait de Lisa Gherardini, épouse de Francesco del Giocondo",
            "Chef-d'œuvre de Léonard de Vinci (1503-1519), ce portrait révolutionnaire " +
            "utilise la technique du sfumato pour créer des transitions douces entre les couleurs. " +
            "Le sourire énigmatique et le regard direct créent une connexion unique avec l'observateur. " +
            "Volée en 1911 par Vincenzo Peruggia, elle est devenue l'œuvre la plus célèbre au monde. " +
            "La technique de perspective atmosphérique crée une profondeur remarquable dans le paysage arrière.",
            0.77, 0.53, "Salle Renaissance - Mur principal"
        ));
        
        // La Nuit Étoilée
        tableaux.add(new Tableau(
            "TAB002", "La Nuit Étoilée", "Vincent van Gogh", 1889, "Post-impressionnisme",
            "Vue nocturne depuis la fenêtre de l'asile de Saint-Rémy-de-Provence",
            "Peinte par Vincent van Gogh en juin 1889 à l'asile de Saint-Rémy. Cette œuvre " +
            "post-impressionniste capture un ciel nocturne turbulent avec des mouvements tourbillonnants " +
            "qui évoquent l'état mental agité de l'artiste. Les cyprès au premier plan, comme des flammes " +
            "noires, symbolisent le lien entre terre et ciel. Les étoiles brillantes et la lune rayonnante " +
            "contrastent avec le village endormi en contrebas.",
            0.73, 0.92, "Salle Impressionnisme - Mur est"
        ));
        
        // Guernica
        tableaux.add(new Tableau(
            "TAB003", "Guernica", "Pablo Picasso", 1937, "Cubisme",
            "Dénonciation du bombardement de Guernica pendant la guerre civile espagnole",
            "Pablo Picasso a créé cette œuvre monumentale en 1937 en réaction au bombardement " +
            "de Guernica le 26 avril 1937 pendant la guerre civile espagnole. Réalisée en style " +
            "cubiste et uniquement en noir, blanc et gris, elle dénonce les horreurs de la guerre " +
            "avec des symboles puissants : le taureau représente l'Espagne, le cheval blessé " +
            "symbolise le peuple souffrant, et l'ampoule évoque l'œil de Dieu témoin de la barbarie.",
            3.49, 7.76, "Salle Art Moderne - Mur principal"
        ));
        
        // Le Cri
        tableaux.add(new Tableau(
            "TAB004", "Le Cri", "Edvard Munch", 1893, "Expressionnisme",
            "Expression de l'angoisse existentielle de l'homme moderne",
            "Edvard Munch a peint cette œuvre expressionniste en 1893 après avoir vécu une " +
            "expérience troublante lors d'une promenade au coucher du soleil. Cette œuvre exprime " +
            "l'angoisse existentielle moderne et l'aliénation de l'individu dans la société industrielle. " +
            "Les lignes ondulantes du ciel rouge-orange et les couleurs vives créent une atmosphère " +
            "d'anxiété universelle. Le personnage déformé incarne la détresse humaine face à l'absurdité de l'existence.",
            0.91, 0.73, "Salle Expressionnisme - Mur central"
        ));
        
        // La Persistance de la Mémoire
        tableaux.add(new Tableau(
            "TAB005", "La Persistance de la Mémoire", "Salvador Dalí", 1931, "Surréalisme",
            "Exploration du temps et de la mémoire à travers des montres molles",
            "Salvador Dalí a peint cette œuvre surréaliste en 1931, inspiré par un camembert " +
            "qui fondait sous la chaleur. Ces montres molles illustrent la relativité du temps " +
            "selon la théorie d'Einstein, que Dalí avait découverte. L'œuvre questionne notre " +
            "perception de la réalité temporelle et explore les mécanismes de la mémoire. " +
            "Le paysage de Port Lligat, près de sa maison, ancre cette vision onirique dans un lieu réel.",
            0.24, 0.33, "Salle Surréalisme - Mur ouest"
        ));
        
        // Les Demoiselles d'Avignon
        tableaux.add(new Tableau(
            "TAB006", "Les Demoiselles d'Avignon", "Pablo Picasso", 1907, "Cubisme",
            "Œuvre fondatrice du cubisme révolutionnant la perspective traditionnelle",
            "Picasso a révolutionné l'art occidental avec cette œuvre de 1907, considérée comme " +
            "l'acte de naissance du cubisme. Elle rompt radicalement avec la perspective traditionnelle " +
            "en décomposant les formes en facettes géométriques. Les visages, inspirés de l'art " +
            "africain et ibérique, marquent une rupture avec l'esthétique européenne classique. " +
            "Cette œuvre influence encore aujourd'hui la représentation artistique contemporaine.",
            2.43, 2.33, "Salle Cubisme - Mur d'honneur"
        ));
        
        // La Grande Vague
        tableaux.add(new Tableau(
            "TAB007", "La Grande Vague de Kanagawa", "Katsushika Hokusai", 1830, "Ukiyo-e",
            "Estampe japonaise illustrant la puissance de la nature",
            "Cette estampe japonaise ukiyo-e d'Hokusai, créée vers 1830, fait partie de la série " +
            "'Trente-six vues du mont Fuji'. Elle montre la puissance destructrice et la beauté " +
            "de la nature japonaise. La vague, stylisée avec des 'griffes' d'écume, semble vouloir " +
            "engloutir les frêles embarcations. Le mont Fuji, petit à l'arrière-plan, symbolise " +
            "la permanence face aux forces changeantes de la nature. Cette œuvre a profondément influencé l'art occidental.",
            0.25, 0.37, "Salle Art Asiatique - Mur nord"
        ));
        
        // American Gothic
        tableaux.add(new Tableau(
            "TAB008", "American Gothic", "Grant Wood", 1930, "Réalisme américain",
            "Portrait emblématique de l'Amérique rurale du Midwest",
            "Grant Wood a peint cette œuvre en 1930, inspiré par une maison de style gothique " +
            "à Eldon, Iowa. Ce portrait de fermiers du Midwest américain capture l'esprit puritain " +
            "et l'austérité de l'Amérique rurale pendant la Grande Dépression. La femme et l'homme " +
            "(modèles : sa sœur et son dentiste) incarnent les valeurs traditionnelles américaines. " +
            "La maison gothique en arrière-plan contraste ironiquement avec le réalisme minutieux des personnages.",
            0.78, 0.63, "Salle Art Américain - Mur sud"
        ));
        
        return tableaux;
    }
    
    /**
     * Obtient les explications détaillées pour un tableau
     */
    public static Map<String, String> getExplanationsMap() {
        Map<String, String> explanations = new HashMap<>();
        List<Tableau> tableaux = initializeTableaux();
        
        for (Tableau tableau : tableaux) {
            explanations.put(tableau.getNom(), tableau.getExplicationPourGuide());
        }
        
        return explanations;
    }
    
    /**
     * Configuration des nationalités supportées
     */
    public static List<String> getSupportedNationalities() {
        return Arrays.asList(
            "Française", "Allemande", "Italienne", "Espagnole", 
            "Britannique", "Américaine", "Japonaise", "Chinoise",
            "Brésilienne", "Canadienne", "Australienne", "Hollandaise",
            "Suédoise", "Coréenne", "Russe", "Indienne"
        );
    }
    
    /**
     * Mapping nationalité -> langue
     */
    public static Map<String, String> getLanguageMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("Française", "Français");
        mapping.put("Allemande", "Allemand");
        mapping.put("Italienne", "Italien");
        mapping.put("Espagnole", "Espagnol");
        mapping.put("Britannique", "Anglais");
        mapping.put("Américaine", "Anglais");
        mapping.put("Japonaise", "Japonais");
        mapping.put("Chinoise", "Chinois");
        mapping.put("Brésilienne", "Portugais");
        mapping.put("Canadienne", "Français");
        mapping.put("Australienne", "Anglais");
        mapping.put("Hollandaise", "Néerlandais");
        mapping.put("Suédoise", "Suédois");
        mapping.put("Coréenne", "Coréen");
        mapping.put("Russe", "Russe");
        mapping.put("Indienne", "Hindi");
        return mapping;
    }
    
    /**
     * Configuration des intérêts possibles pour les groupes
     */
    public static List<String> getPossibleInterests() {
        return Arrays.asList(
            "Renaissance", "Impressionnisme", "Art moderne", "Sculpture",
            "Photographie", "Art contemporain", "Histoire de l'art",
            "Techniques picturales", "Biographies d'artistes", "Art asiatique",
            "Symbolisme", "Architecture", "Art religieux", "Portraits"
        );
    }
    
    /**
     * Génère un nom de guide aléatoire
     */
    public static String generateRandomGuideName() {
        String[] prenoms = {
            "Marie", "Pierre", "Sophie", "Jean", "Isabelle", "Michel", 
            "Catherine", "Alain", "Françoise", "Bernard", "Martine", "Philippe",
            "Nathalie", "Daniel", "Sylvie", "Christian", "Valérie", "Thierry"
        };
        
        String[] noms = {
            "Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard",
            "Petit", "Durand", "Leroy", "Moreau", "Simon", "Laurent",
            "Lefebvre", "Michel", "Garcia", "David", "Bertrand", "Roux"
        };
        
        String prenom = prenoms[(int) (Math.random() * prenoms.length)];
        String nom = noms[(int) (Math.random() * noms.length)];
        
        return prenom + nom;
    }
    
    /**
     * Paramètres de simulation
     */
    public static class SimulationParams {
        public static final int DUREE_SIMULATION_MINUTES = 3;
        public static final double PROBABILITE_NOUVEAU_GROUPE = 0.3;
        public static final double PROBABILITE_EVENEMENT = 0.1;
        public static final int INTERVALLE_SURVEILLANCE_MS = 10000;
        public static final int INTERVALLE_STATISTIQUES_MS = 30000;
    }
    
    /**
     * Messages du système
     */
    public static class Messages {
        public static final String TOUR_REQUEST = "TOUR_REQUEST";
        public static final String TOUR_ACCEPTED = "TOUR_ACCEPTED";
        public static final String TOUR_REFUSED = "TOUR_REFUSED";
        public static final String TOUR_STARTED = "TOUR_STARTED";
        public static final String TOUR_FINISHED = "TOUR_FINISHED";
        public static final String TABLEAU_INFO = "TABLEAU_INFO";
        public static final String EMERGENCY = "EMERGENCY";
        public static final String PRIORITY_GROUP = "PRIORITY_GROUP";
        public static final String GUIDE_PROBLEM = "GUIDE_PROBLEM";
    }
}