import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * A status bar component for displaying application status information
 */
public class StatusBar extends JPanel {
    private JLabel statusLabel;
    private JLabel progressLabel;
    private JLabel timeLabel;
    private JProgressBar progressBar;
    private long startTime;
    private DecimalFormat percentFormat;
    private DecimalFormat timeFormat;
    
    public StatusBar() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        // Initialize components
        statusLabel = new JLabel("Ready");
        progressLabel = new JLabel("0%");
        timeLabel = new JLabel("00:00");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        
        // Format for percentage and time display
        percentFormat = new DecimalFormat("0.0%");
        timeFormat = new DecimalFormat("00");
        
        // Create left panel for status
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(statusLabel);
        
        // Create right panel for progress and time
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.add(new JLabel("Progress:"));
        rightPanel.add(progressBar);
        rightPanel.add(progressLabel);
        rightPanel.add(new JLabel("Elapsed:"));
        rightPanel.add(timeLabel);
        
        // Add panels to status bar
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        
        // Set preferred size
        setPreferredSize(new Dimension(getWidth(), 28));
    }
    
    /**
     * Set the current status message
     */
    public void setStatus(String status) {
        statusLabel.setText(status);
    }
    
    /**
     * Start the decoding process timer
     */
    public void startTimer() {
        startTime = System.currentTimeMillis();
        updateElapsedTime();
    }
    
    /**
     * Stop the decoding process timer
     */
    public void stopTimer() {
        updateElapsedTime();
    }
    
    /**
     * Update the progress display
     * @param progress Value between 0.0 and 1.0
     */
    public void updateProgress(double progress) {
        // Update progress bar
        int progressValue = (int)(progress * 100);
        progressBar.setValue(progressValue);
        
        // Update percentage label
        progressLabel.setText(percentFormat.format(progress));
        
        // Update elapsed time
        updateElapsedTime();
    }
    
    /**
     * Update the elapsed time display
     */
    private void updateElapsedTime() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = (elapsedTime / 1000) % 60;
        long minutes = (elapsedTime / (1000 * 60)) % 60;
        
        timeLabel.setText(timeFormat.format(minutes) + ":" + timeFormat.format(seconds));
    }
    
    /**
     * Reset the status bar to its initial state
     */
    public void reset() {
        statusLabel.setText("Ready");
        progressBar.setValue(0);
        progressLabel.setText("0%");
        timeLabel.setText("00:00");
    }
}
