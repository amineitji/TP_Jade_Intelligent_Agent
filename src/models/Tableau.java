package models;

import java.util.Date;

/**
 * Classe représentant un tableau dans l'exposition
 */
public class Tableau {
    private String id;
    private String nom;
    private String artiste;
    private int annee;
    private String style;
    private String description;
    private String explicationComplete;
    private double largeur; // en mètres
    private double hauteur; // en mètres
    private String emplacement;
    private boolean disponible;
    private int nombreVisiteurs; // compteur de visiteurs
    private Date derniereVisite;
    
    public Tableau(String id, String nom, String artiste, int annee, String style) {
        this.id = id;
        this.nom = nom;
        this.artiste = artiste;
        this.annee = annee;
        this.style = style;
        this.disponible = true;
        this.nombreVisiteurs = 0;
        this.derniereVisite = null;
    }
    
    // Constructeur complet
    public Tableau(String id, String nom, String artiste, int annee, String style,
                   String description, String explicationComplete, double largeur, 
                   double hauteur, String emplacement) {
        this(id, nom, artiste, annee, style);
        this.description = description;
        this.explicationComplete = explicationComplete;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.emplacement = emplacement;
    }
    
    // Méthodes pour la gestion des visites
    public void enregistrerVisite() {
        this.nombreVisiteurs++;
        this.derniereVisite = new Date();
    }
    
    public String getInfosDetaillees() {
        StringBuilder info = new StringBuilder();
        info.append("=== ").append(nom).append(" ===\n");
        info.append("Artiste: ").append(artiste).append("\n");
        info.append("Année: ").append(annee).append("\n");
        info.append("Style: ").append(style).append("\n");
        info.append("Dimensions: ").append(largeur).append("m x ").append(hauteur).append("m\n");
        info.append("Emplacement: ").append(emplacement).append("\n");
        if (description != null) {
            info.append("Description: ").append(description).append("\n");
        }
        info.append("Visiteurs: ").append(nombreVisiteurs).append("\n");
        return info.toString();
    }
    
    public String getExplicationPourGuide() {
        if (explicationComplete != null && !explicationComplete.isEmpty()) {
            return explicationComplete;
        }
        return "Œuvre remarquable de " + artiste + " datant de " + annee + 
               ", représentative du mouvement " + style + ".";
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getArtiste() { return artiste; }
    public void setArtiste(String artiste) { this.artiste = artiste; }
    
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getExplicationComplete() { return explicationComplete; }
    public void setExplicationComplete(String explicationComplete) { 
        this.explicationComplete = explicationComplete; 
    }
    
    public double getLargeur() { return largeur; }
    public void setLargeur(double largeur) { this.largeur = largeur; }
    
    public double getHauteur() { return hauteur; }
    public void setHauteur(double hauteur) { this.hauteur = hauteur; }
    
    public String getEmplacement() { return emplacement; }
    public void setEmplacement(String emplacement) { this.emplacement = emplacement; }
    
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    
    public int getNombreVisiteurs() { return nombreVisiteurs; }
    public void setNombreVisiteurs(int nombreVisiteurs) { this.nombreVisiteurs = nombreVisiteurs; }
    
    public Date getDerniereVisite() { return derniereVisite; }
    public void setDerniereVisite(Date derniereVisite) { this.derniereVisite = derniereVisite; }
    
    @Override
    public String toString() {
        return nom + " par " + artiste + " (" + annee + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tableau tableau = (Tableau) obj;
        return id != null ? id.equals(tableau.id) : tableau.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}