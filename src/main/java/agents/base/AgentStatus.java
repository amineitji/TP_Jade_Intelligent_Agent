package agents.base;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * Classe représentant l'état et les statistiques d'un agent
 */
public class AgentStatus {
    private String currentLocation;
    private boolean isActive;
    private boolean isAvailable;
    private LocalDateTime lastUpdate;
    private Map<String, Object> properties;
    
    public AgentStatus() {
        this.currentLocation = "PointA";
        this.isActive = false;
        this.isAvailable = true;
        this.lastUpdate = LocalDateTime.now();
        this.properties = new HashMap<>();
    }
    
    public AgentStatus(String location, boolean active, boolean available) {
        this.currentLocation = location;
        this.isActive = active;
        this.isAvailable = available;
        this.lastUpdate = LocalDateTime.now();
        this.properties = new HashMap<>();
    }
    
    // Getters et Setters
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
        updateTimestamp();
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
        updateTimestamp();
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean available) {
        isAvailable = available;
        updateTimestamp();
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    private void updateTimestamp() {
        this.lastUpdate = LocalDateTime.now();
    }
    
    // Gestion des propriétés dynamiques
    public void setProperty(String key, Object value) {
        properties.put(key, value);
        updateTimestamp();
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    public void removeProperty(String key) {
        properties.remove(key);
        updateTimestamp();
    }
    
    public Map<String, Object> getAllProperties() {
        return new HashMap<>(properties);
    }
    
    @Override
    public String toString() {
        return String.format("AgentStatus[location=%s, active=%s, available=%s, lastUpdate=%s]",
                currentLocation, isActive, isAvailable, lastUpdate);
    }
    
    /**
     * Clone le statut actuel
     */
    public AgentStatus clone() {
        AgentStatus clone = new AgentStatus(currentLocation, isActive, isAvailable);
        clone.properties = new HashMap<>(this.properties);
        return clone;
    }
}