package models;

import java.util.*;

/**
 * Classe représentant un groupe de visiteurs
 */
public class VisitorGroup {
    private String groupId;
    private int taille;
    private String nationalite;
    private String langue;
    private Date heureArrivee;
    private Date heureDepart;
    private String pointRendezVous;
    private StatutGroupe statut;
    private List<String> interetsParticuliers;
    private int ageMin;
    private int ageMax;
    private boolean groupeVIP;
    private String commentaires;
    
    // Statistiques de visite
    private int nombreTableauxVus;
    private long tempsVisiteTotal; // en millisecondes
    private int nombreQuestions;
    private NiveauSatisfaction satisfaction;
    
    public enum StatutGroupe {
        EN_ATTENTE,
        ASSIGNE_GUIDE,
        EN_VISITE,
        VISITE_TERMINEE,
        PARTI
    }
    
    public enum NiveauSatisfaction {
        TRES_SATISFAIT(5),
        SATISFAIT(4),
        NEUTRE(3),
        INSATISFAIT(2),
        TRES_INSATISFAIT(1);
        
        private final int valeur;
        
        NiveauSatisfaction(int valeur) {
            this.valeur = valeur;
        }
        
        public int getValeur() { return valeur; }
    }
    
    public VisitorGroup(String groupId, int taille, String nationalite) {
        this.groupId = groupId;
        this.taille = taille;
        this.nationalite = nationalite;
        this.heureArrivee = new Date();
        this.statut = StatutGroupe.EN_ATTENTE;
        this.pointRendezVous = "Point A - Entrée principale";
        this.interetsParticuliers = new ArrayList<>();
        this.nombreTableauxVus = 0;
        this.nombreQuestions = 0;
        this.groupeVIP = false;
        
        // Définition de la langue par défaut selon la nationalité
        definirLangue();
        
        // Génération d'âges aléatoires réalistes
        genererTrancheAge();
    }
    
    private void definirLangue() {
        Map<String, String> langueParNationalite = new HashMap<>();
        langueParNationalite.put("Française", "Français");
        langueParNationalite.put("Allemande", "Allemand");
        langueParNationalite.put("Italienne", "Italien");
        langueParNationalite.put("Espagnole", "Espagnol");
        langueParNationalite.put("Britannique", "Anglais");
        langueParNationalite.put("Américaine", "Anglais");
        langueParNationalite.put("Japonaise", "Japonais");
        langueParNationalite.put("Chinoise", "Chinois");
        langueParNationalite.put("Brésilienne", "Portugais");
        langueParNationalite.put("Canadienne", "Français");
        langueParNationalite.put("Australienne", "Anglais");
        langueParNationalite.put("Hollandaise", "Néerlandais");
        langueParNationalite.put("Suédoise", "Suédois");
        langueParNationalite.put("Coréenne", "Coréen");
        
        this.langue = langueParNationalite.getOrDefault(nationalite, "Anglais");
    }
    
    private void genererTrancheAge() {
        // Génération réaliste des tranches d'âge
        double random = Math.random();
        if (random < 0.3) { // Familles avec enfants
            this.ageMin = 6;
            this.ageMax = 65;
        } else if (random < 0.6) { // Adultes
            this.ageMin = 25;
            this.ageMax = 55;
        } else if (random < 0.8) { // Seniors
            this.ageMin = 55;
            this.ageMax = 80;
        } else { // Groupes scolaires/étudiants
            this.ageMin = 16;
            this.ageMax = 25;
        }
    }
    
    // Méthodes de gestion du statut
    public void changerStatut(StatutGroupe nouveauStatut) {
        this.statut = nouveauStatut;
        
        if (nouveauStatut == StatutGroupe.EN_VISITE) {
            // Démarrer le chronomètre de visite
            this.tempsVisiteTotal = System.currentTimeMillis();
        } else if (nouveauStatut == StatutGroupe.VISITE_TERMINEE) {
            // Calculer le temps total de visite
            if (this.tempsVisiteTotal > 0) {
                this.tempsVisiteTotal = System.currentTimeMillis() - this.tempsVisiteTotal;
            }
            this.heureDepart = new Date();
            
            // Génération automatique de satisfaction
            genererSatisfaction();
        }
    }
    
    private void genererSatisfaction() {
        // Calcul de satisfaction basé sur plusieurs facteurs
        double scoreSatisfaction = 3.0; // Base neutre
        
        // Facteur temps de visite (idéalement 45-90 minutes)
        long tempsEnMinutes = tempsVisiteTotal / (1000 * 60);
        if (tempsEnMinutes >= 45 && tempsEnMinutes <= 90) {
            scoreSatisfaction += 0.5;
        } else if (tempsEnMinutes < 30 || tempsEnMinutes > 120) {
            scoreSatisfaction -= 0.5;
        }
        
        // Facteur nombre de tableaux vus
        if (nombreTableauxVus >= 6) {
            scoreSatisfaction += 0.3;
        } else if (nombreTableauxVus < 4) {
            scoreSatisfaction -= 0.3;
        }
        
        // Facteur taille du groupe (groupes trop grands = moins de satisfaction)
        if (taille <= 8) {
            scoreSatisfaction += 0.2;
        } else if (taille > 12) {
            scoreSatisfaction -= 0.2;
        }
        
        // Facteur VIP
        if (groupeVIP) {
            scoreSatisfaction += 0.5;
        }
        
        // Ajout d'un facteur aléatoire
        scoreSatisfaction += (Math.random() - 0.5) * 1.0;
        
        // Conversion en enum
        if (scoreSatisfaction >= 4.5) {
            this.satisfaction = NiveauSatisfaction.TRES_SATISFAIT;
        } else if (scoreSatisfaction >= 3.5) {
            this.satisfaction = NiveauSatisfaction.SATISFAIT;
        } else if (scoreSatisfaction >= 2.5) {
            this.satisfaction = NiveauSatisfaction.NEUTRE;
        } else if (scoreSatisfaction >= 1.5) {
            this.satisfaction = NiveauSatisfaction.INSATISFAIT;
        } else {
            this.satisfaction = NiveauSatisfaction.TRES_INSATISFAIT;
        }
    }
    
    // Méthodes utilitaires
    public void ajouterInteretParticulier(String interet) {
        if (!interetsParticuliers.contains(interet)) {
            interetsParticuliers.add(interet);
        }
    }
    
    public void incrementerTableauxVus() {
        this.nombreTableauxVus++;
    }
    
    public void incrementerQuestions() {
        this.nombreQuestions++;
    }
    
    public String getProfilGroupe() {
        StringBuilder profil = new StringBuilder();
        profil.append("Groupe ").append(groupId).append(" (").append(nationalite).append(")\n");
        profil.append("Taille: ").append(taille).append(" personnes\n");
        profil.append("Âge: ").append(ageMin).append("-").append(ageMax).append(" ans\n");
        profil.append("Langue: ").append(langue).append("\n");
        profil.append("Statut: ").append(statut).append("\n");
        if (groupeVIP) {
            profil.append("⭐ Groupe VIP\n");
        }
        if (!interetsParticuliers.isEmpty()) {
            profil.append("Intérêts: ").append(String.join(", ", interetsParticuliers)).append("\n");
        }
        return profil.toString();
    }
    
    public String getBilanVisite() {
        if (statut != StatutGroupe.VISITE_TERMINEE) {
            return "Visite en cours ou non commencée";
        }
        
        StringBuilder bilan = new StringBuilder();
        bilan.append("=== BILAN DE VISITE ===\n");
        bilan.append("Groupe: ").append(groupId).append("\n");
        bilan.append("Tableaux vus: ").append(nombreTableauxVus).append("\n");
        bilan.append("Questions posées: ").append(nombreQuestions).append("\n");
        bilan.append("Durée: ").append(tempsVisiteTotal / (1000 * 60)).append(" minutes\n");
        bilan.append("Satisfaction: ").append(satisfaction).append(" (").append(satisfaction.getValeur()).append("/5)\n");
        bilan.append("Arrivée: ").append(heureArrivee).append("\n");
        bilan.append("Départ: ").append(heureDepart).append("\n");
        
        return bilan.toString();
    }
    
    public boolean necessiteTraduction() {
        return !langue.equals("Français");
    }
    
    public boolean estGroupeFamilial() {
        return ageMax - ageMin > 30; // Grande différence d'âge = famille
    }
    
    // Getters et Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public int getTaille() { return taille; }
    public void setTaille(int taille) { this.taille = taille; }
    
    public String getNationalite() { return nationalite; }
    public void setNationalite(String nationalite) { this.nationalite = nationalite; }
    
    public String getLangue() { return langue; }
    public void setLangue(String langue) { this.langue = langue; }
    
    public Date getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(Date heureArrivee) { this.heureArrivee = heureArrivee; }
    
    public Date getHeureDepart() { return heureDepart; }
    public void setHeureDepart(Date heureDepart) { this.heureDepart = heureDepart; }
    
    public String getPointRendezVous() { return pointRendezVous; }
    public void setPointRendezVous(String pointRendezVous) { this.pointRendezVous = pointRendezVous; }
    
    public StatutGroupe getStatut() { return statut; }
    public void setStatut(StatutGroupe statut) { this.statut = statut; }
    
    public List<String> getInteretsParticuliers() { return new ArrayList<>(interetsParticuliers); }
    public void setInteretsParticuliers(List<String> interetsParticuliers) { 
        this.interetsParticuliers = new ArrayList<>(interetsParticuliers); 
    }
    
    public int getAgeMin() { return ageMin; }
    public void setAgeMin(int ageMin) { this.ageMin = ageMin; }
    
    public int getAgeMax() { return ageMax; }
    public void setAgeMax(int ageMax) { this.ageMax = ageMax; }
    
    public boolean isGroupeVIP() { return groupeVIP; }
    public void setGroupeVIP(boolean groupeVIP) { this.groupeVIP = groupeVIP; }
    
    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }
    
    public int getNombreTableauxVus() { return nombreTableauxVus; }
    public void setNombreTableauxVus(int nombreTableauxVus) { this.nombreTableauxVus = nombreTableauxVus; }
    
    public long getTempsVisiteTotal() { return tempsVisiteTotal; }
    public void setTempsVisiteTotal(long tempsVisiteTotal) { this.tempsVisiteTotal = tempsVisiteTotal; }
    
    public int getNombreQuestions() { return nombreQuestions; }
    public void setNombreQuestions(int nombreQuestions) { this.nombreQuestions = nombreQuestions; }
    
    public NiveauSatisfaction getSatisfaction() { return satisfaction; }
    public void setSatisfaction(NiveauSatisfaction satisfaction) { this.satisfaction = satisfaction; }
    
    @Override
    public String toString() {
        return groupId + " (" + taille + " " + nationalite + "s, " + statut + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VisitorGroup that = (VisitorGroup) obj;
        return Objects.equals(groupId, that.groupId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }
}