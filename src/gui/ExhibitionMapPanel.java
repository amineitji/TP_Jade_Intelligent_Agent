package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class ExhibitionMapPanel extends JPanel {
    private Map<String, Point> tableauPositions;
    private Map<String, Point> guidePositions;
    private Map<String, String> guideTableauMapping; // Guide -> Tableau actuel
    private List<String> tableaux;
    private Point entryPoint;
    
    public ExhibitionMapPanel() {
        setBackground(Color.WHITE);
        initializeLayout();
        guidePositions = new HashMap<>();
        guideTableauMapping = new HashMap<>();
        
        // Timer pour animation
        javax.swing.Timer animationTimer = new javax.swing.Timer(100, e -> repaint());
        animationTimer.start();
    }
    
    private void initializeLayout() {
        tableauPositions = new HashMap<>();
        tableaux = Arrays.asList(
            "La Joconde", "La Nuit Étoilée", "Guernica", "Le Cri",
            "La Persistance de la Mémoire", "Les Demoiselles d'Avignon",
            "La Grande Vague", "American Gothic"
        );
        
        // Point d'entrée
        entryPoint = new Point(50, 350);
        
        // Disposition des tableaux en circuit logique
        tableauPositions.put("La Joconde", new Point(150, 100));
        tableauPositions.put("La Nuit Étoilée", new Point(350, 100));
        tableauPositions.put("Guernica", new Point(550, 100));
        tableauPositions.put("Le Cri", new Point(550, 250));
        tableauPositions.put("La Persistance de la Mémoire", new Point(550, 400));
        tableauPositions.put("Les Demoiselles d'Avignon", new Point(350, 400));
        tableauPositions.put("La Grande Vague", new Point(150, 400));
        tableauPositions.put("American Gothic", new Point(150, 250));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBackground(g2d);
        drawTableaux(g2d);
        drawPaths(g2d);
        drawEntryPoint(g2d);
        drawGuides(g2d);
        drawLegend(g2d);
    }
    
    private void drawBackground(Graphics2D g2d) {
        // Fond de l'exposition
        g2d.setColor(new Color(248, 248, 255));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Bordures des salles
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        
        // Salle Renaissance
        g2d.drawRect(100, 50, 100, 100);
        g2d.drawString("Renaissance", 105, 45);
        
        // Salle Impressionnisme
        g2d.drawRect(300, 50, 100, 100);
        g2d.drawString("Impressionnisme", 305, 45);
        
        // Salle Art Moderne
        g2d.drawRect(500, 50, 100, 100);
        g2d.drawString("Art Moderne", 505, 45);
        
        // Salle Expressionnisme
        g2d.drawRect(500, 200, 100, 100);
        g2d.drawString("Expressionnisme", 505, 195);
        
        // Salle Surréalisme
        g2d.drawRect(500, 350, 100, 100);
        g2d.drawString("Surréalisme", 505, 345);
        
        // Salle Cubisme
        g2d.drawRect(300, 350, 100, 100);
        g2d.drawString("Cubisme", 305, 345);
        
        // Salle Art Asiatique
        g2d.drawRect(100, 350, 100, 100);
        g2d.drawString("Art Asiatique", 105, 345);
        
        // Salle Art Américain
        g2d.drawRect(100, 200, 100, 100);
        g2d.drawString("Art Américain", 105, 195);
    }
    
    private void drawTableaux(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1));
        
        for (String tableau : tableaux) {
            Point pos = tableauPositions.get(tableau);
            if (pos != null) {
                // Cadre du tableau
                g2d.setColor(new Color(139, 69, 19)); // Marron pour le cadre
                g2d.fillRect(pos.x - 15, pos.y - 10, 30, 20);
                
                // Toile
                g2d.setColor(Color.WHITE);
                g2d.fillRect(pos.x - 12, pos.y - 7, 24, 14);
                
                // Numéro du tableau
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                int numero = tableaux.indexOf(tableau) + 1;
                g2d.drawString(String.valueOf(numero), pos.x - 3, pos.y + 3);
                
                // Nom du tableau (abrégé)
                g2d.setFont(new Font("Arial", Font.PLAIN, 8));
                String nom = getShortName(tableau);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(nom);
                g2d.drawString(nom, pos.x - textWidth/2, pos.y + 25);
            }
        }
    }
    
    private String getShortName(String fullName) {
        switch (fullName) {
            case "La Joconde": return "Joconde";
            case "La Nuit Étoilée": return "Nuit Étoilée";
            case "Guernica": return "Guernica";
            case "Le Cri": return "Le Cri";
            case "La Persistance de la Mémoire": return "Persistance";
            case "Les Demoiselles d'Avignon": return "Demoiselles";
            case "La Grande Vague": return "Grande Vague";
            case "American Gothic": return "Gothic";
            default: return fullName;
        }
    }
    
    private void drawPaths(Graphics2D g2d) {
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
            0, new float[]{5, 5}, 0));
        
        // Chemin de visite suggéré
        Point prev = entryPoint;
        for (String tableau : tableaux) {
            Point curr = tableauPositions.get(tableau);
            if (curr != null) {
                g2d.drawLine(prev.x, prev.y, curr.x, curr.y);
                
                // Flèche directionnelle
                drawArrow(g2d, prev, curr);
                prev = curr;
            }
        }
    }
    
    private void drawArrow(Graphics2D g2d, Point from, Point to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double angle = Math.atan2(dy, dx);
        
        int arrowLength = 8;
        double arrowAngle = Math.PI / 6;
        
        // Point milieu pour la flèche
        int midX = (from.x + to.x) / 2;
        int midY = (from.y + to.y) / 2;
        
        // Pointes de la flèche
        int x1 = (int) (midX - arrowLength * Math.cos(angle - arrowAngle));
        int y1 = (int) (midY - arrowLength * Math.sin(angle - arrowAngle));
        int x2 = (int) (midX - arrowLength * Math.cos(angle + arrowAngle));
        int y2 = (int) (midY - arrowLength * Math.sin(angle + arrowAngle));
        
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(midX, midY, x1, y1);
        g2d.drawLine(midX, midY, x2, y2);
    }
    
    private void drawEntryPoint(Graphics2D g2d) {
        // Point d'entrée
        g2d.setColor(new Color(0, 128, 0));
        g2d.fillOval(entryPoint.x - 8, entryPoint.y - 8, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.drawString("A", entryPoint.x - 4, entryPoint.y + 4);
        
        // Label
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("Point A", entryPoint.x - 15, entryPoint.y + 20);
        g2d.drawString("Entrée", entryPoint.x - 15, entryPoint.y + 32);
    }
    
    private void drawGuides(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 9));
        
        for (Map.Entry<String, Point> entry : guidePositions.entrySet()) {
            String guideId = entry.getKey();
            Point pos = entry.getValue();
            
            // Avatar du guide
            g2d.setColor(new Color(0, 100, 200));
            g2d.fillOval(pos.x - 6, pos.y - 6, 12, 12);
            
            // Icône guide
            g2d.setColor(Color.WHITE);
            g2d.drawString("G", pos.x - 3, pos.y + 3);
            
            // Nom du guide
            g2d.setColor(Color.BLACK);
            String shortName = guideId.replace("Guide", "");
            g2d.drawString(shortName, pos.x - 10, pos.y - 10);
            
            // Ligne vers le tableau si en visite
            String currentTableau = guideTableauMapping.get(guideId);
            if (currentTableau != null) {
                Point tableauPos = tableauPositions.get(currentTableau);
                if (tableauPos != null) {
                    g2d.setColor(new Color(255, 0, 0, 100));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawLine(pos.x, pos.y, tableauPos.x, tableauPos.y);
                    
                    // Cercle autour du tableau actuel
                    g2d.setColor(new Color(255, 0, 0, 150));
                    g2d.drawOval(tableauPos.x - 20, tableauPos.y - 15, 40, 30);
                }
            }
        }
    }
    
    private void drawLegend(Graphics2D g2d) {
        int x = getWidth() - 150;
        int y = 20;
        
        // Fond de la légende
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(x - 10, y - 10, 140, 120, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x - 10, y - 10, 140, 120, 10, 10);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Légende", x, y);
        y += 20;
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        
        // Point d'entrée
        g2d.setColor(new Color(0, 128, 0));
        g2d.fillOval(x, y - 5, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Point d'entrée", x + 15, y);
        y += 15;
        
        // Tableaux
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(x, y - 5, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Tableaux (1-8)", x + 15, y);
        y += 15;
        
        // Guides disponibles
        g2d.setColor(new Color(0, 100, 200));
        g2d.fillOval(x, y - 5, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Guides", x + 15, y);
        y += 15;
        
        // Chemin de visite
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
            0, new float[]{3, 3}, 0));
        g2d.drawLine(x, y - 2, x + 10, y - 2);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Parcours", x + 15, y);
        y += 15;
        
        // Visite en cours
        g2d.setColor(new Color(255, 0, 0, 100));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(x, y - 2, x + 10, y - 2);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Visite active", x + 15, y);
    }
    
    // Méthodes publiques pour mise à jour depuis l'interface
    public void updateGuidePosition(String guideId, String tableau) {
        SwingUtilities.invokeLater(() -> {
            if (tableau != null && tableauPositions.containsKey(tableau)) {
                Point tableauPos = tableauPositions.get(tableau);
                // Positionner le guide près du tableau avec un léger décalage
                Point guidePos = new Point(tableauPos.x + 25, tableauPos.y);
                guidePositions.put(guideId, guidePos);
                guideTableauMapping.put(guideId, tableau);
            } else {
                // Guide disponible, le positionner au point d'entrée
                guidePositions.put(guideId, new Point(entryPoint.x + 30, entryPoint.y));
                guideTableauMapping.remove(guideId);
            }
            repaint();
        });
    }
    
    public void removeGuide(String guideId) {
        SwingUtilities.invokeLater(() -> {
            guidePositions.remove(guideId);
            guideTableauMapping.remove(guideId);
            repaint();
        });
    }
    
    public void highlightTableau(String tableau) {
        // Animation de surbrillance pour un tableau spécifique
        SwingUtilities.invokeLater(() -> {
            // Cette méthode pourrait être étendue pour des animations spéciales
            repaint();
        });
    }
    
    public void setGuideAtEntry(String guideId) {
        SwingUtilities.invokeLater(() -> {
            guidePositions.put(guideId, new Point(entryPoint.x + 30, entryPoint.y));
            guideTableauMapping.remove(guideId);
            repaint();
        });
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 500);
    }
    
    // Méthode pour obtenir la position d'un tableau (utilisée par d'autres composants)
    public Point getTableauPosition(String tableau) {
        return tableauPositions.get(tableau);
    }
    
    // Méthode pour obtenir la liste des tableaux dans l'ordre
    public List<String> getTableauOrder() {
        return new ArrayList<>(tableaux);
    }
}