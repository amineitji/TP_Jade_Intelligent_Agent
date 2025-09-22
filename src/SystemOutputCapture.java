import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Classe pour capturer la sortie système (System.out) et la rendre disponible
 * pour l'interface graphique en temps réel
 */
public class SystemOutputCapture {
    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream captureOut;
    private ByteArrayOutputStream captureErr;
    private PrintStream teeOut;
    private PrintStream teeErr;
    
    private Queue<String> messageBuffer = new ConcurrentLinkedQueue<>();
    private Set<String> processedMessages = new HashSet<>();
    private boolean isCapturing = false;
    
    // Buffer circulaire pour éviter la consommation excessive de mémoire
    private final int MAX_BUFFER_SIZE = 1000;
    
    public SystemOutputCapture() {
        originalOut = System.out;
        originalErr = System.err;
    }
    
    /**
     * Démarre la capture des sorties système
     */
    public void start() {
        if (isCapturing) return;
        
        // Création des flux de capture
        captureOut = new ByteArrayOutputStream();
        captureErr = new ByteArrayOutputStream();
        
        // Création des flux "tee" qui écrivent à la fois sur la sortie originale et notre capture
        teeOut = new PrintStream(new TeeOutputStream(originalOut, new PrintStream(captureOut)));
        teeErr = new PrintStream(new TeeOutputStream(originalErr, new PrintStream(captureErr)));
        
        // Redirection des sorties système
        System.setOut(teeOut);
        System.setErr(teeErr);
        
        isCapturing = true;
        
        // Démarrage du thread de traitement des messages
        startMessageProcessor();
    }
    
    /**
     * Arrête la capture des sorties système
     */
    public void stop() {
        if (!isCapturing) return;
        
        // Restauration des sorties originales
        System.setOut(originalOut);
        System.setErr(originalErr);
        
        // Fermeture des flux
        if (teeOut != null) teeOut.close();
        if (teeErr != null) teeErr.close();
        
        isCapturing = false;
    }
    
    /**
     * Démarre le thread de traitement des messages
     */
    private void startMessageProcessor() {
        Thread processor = new Thread(() -> {
            while (isCapturing) {
                try {
                    processNewOutput();
                    Thread.sleep(100); // Vérifier toutes les 100ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Ignorer les erreurs pour éviter de casser le système
                }
            }
        });
        processor.setDaemon(true);
        processor.setName("SystemOutputCapture-Processor");
        processor.start();
    }
    
    /**
     * Traite les nouvelles sorties capturées
     */
    private void processNewOutput() {
        if (captureOut.size() > 0) {
            String newOutput = captureOut.toString();
            captureOut.reset();
            processOutput(newOutput);
        }
        
        if (captureErr.size() > 0) {
            String newError = captureErr.toString();
            captureErr.reset();
            processOutput("[ERROR] " + newError);
        }
    }
    
    /**
     * Traite une chaîne de sortie en l'ajoutant au buffer de messages
     */
    private void processOutput(String output) {
        if (output == null || output.trim().isEmpty()) return;
        
        // Diviser en lignes
        String[] lines = output.split("\\r?\\n");
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !processedMessages.contains(line)) {
                // Ajouter horodatage
                String timestampedMessage = String.format("[%s] %s", 
                    java.time.LocalTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), 
                    line);
                
                messageBuffer.offer(timestampedMessage);
                processedMessages.add(line);
                
                // Maintenir la taille du buffer
                if (messageBuffer.size() > MAX_BUFFER_SIZE) {
                    String removed = messageBuffer.poll();
                    if (removed != null) {
                        // Retirer aussi du set des messages traités (en extrayant le message original)
                        String originalMessage = removed.substring(removed.indexOf("] ") + 2);
                        processedMessages.remove(originalMessage);
                    }
                }
            }
        }
    }
    
    /**
     * Récupère les nouveaux messages depuis le dernier appel
     */
    public java.util.List<String> getNewMessages() {
        java.util.List<String> messages = new ArrayList<>();
        
        String message;
        while ((message = messageBuffer.poll()) != null) {
            messages.add(message);
        }
        
        return messages;
    }
    
    /**
     * Récupère tous les messages actuels sans les retirer du buffer
     */
    public java.util.List<String> getAllMessages() {
        return new ArrayList<>(messageBuffer);
    }
    
    /**
     * Vide le buffer de messages
     */
    public void clearMessages() {
        messageBuffer.clear();
        processedMessages.clear();
    }
    
    /**
     * Indique si la capture est actuellement active
     */
    public boolean isCapturing() {
        return isCapturing;
    }
    
    /**
     * Récupère le nombre total de messages capturés
     */
    public int getMessageCount() {
        return messageBuffer.size();
    }
}

/**
 * Classe utilitaire pour créer un flux "tee" qui écrit sur plusieurs sorties
 */
class TeeOutputStream extends OutputStream {
    private final OutputStream[] outputStreams;
    
    public TeeOutputStream(OutputStream... outputStreams) {
        this.outputStreams = outputStreams;
    }
    
    @Override
    public void write(int b) throws IOException {
        for (OutputStream os : outputStreams) {
            if (os != null) {
                try {
                    os.write(b);
                } catch (IOException e) {
                    // Ignorer les erreurs sur les flux individuels
                }
            }
        }
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        for (OutputStream os : outputStreams) {
            if (os != null) {
                try {
                    os.write(b);
                } catch (IOException e) {
                    // Ignorer les erreurs sur les flux individuels
                }
            }
        }
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream os : outputStreams) {
            if (os != null) {
                try {
                    os.write(b, off, len);
                } catch (IOException e) {
                    // Ignorer les erreurs sur les flux individuels
                }
            }
        }
    }
    
    @Override
    public void flush() throws IOException {
        for (OutputStream os : outputStreams) {
            if (os != null) {
                try {
                    os.flush();
                } catch (IOException e) {
                    // Ignorer les erreurs sur les flux individuels
                }
            }
        }
    }
    
    @Override
    public void close() throws IOException {
        for (OutputStream os : outputStreams) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // Ignorer les erreurs sur les flux individuels
                }
            }
        }
    }
}