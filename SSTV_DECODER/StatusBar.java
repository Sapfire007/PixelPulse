import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


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
        
        
        statusLabel = new JLabel("Ready");
        progressLabel = new JLabel("0%");
        timeLabel = new JLabel("00:00");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        
        
        percentFormat = new DecimalFormat("0.0%");
        timeFormat = new DecimalFormat("00");
        
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(statusLabel);
        
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.add(new JLabel("Progress:"));
        rightPanel.add(progressBar);
        rightPanel.add(progressLabel);
        rightPanel.add(new JLabel("Elapsed:"));
        rightPanel.add(timeLabel);
        
        
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        
        
        setPreferredSize(new Dimension(getWidth(), 28));
    }
    
    
    public void setStatus(String status) {
        statusLabel.setText(status);
    }
    
    
    public void startTimer() {
        startTime = System.currentTimeMillis();
        updateElapsedTime();
    }
    
    
    public void stopTimer() {
        updateElapsedTime();
    }
    
    
    public void updateProgress(double progress) {
        
        int progressValue = (int)(progress * 100);
        progressBar.setValue(progressValue);
        
        
        progressLabel.setText(percentFormat.format(progress));
        
        
        updateElapsedTime();
    }
    
    
    private void updateElapsedTime() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = (elapsedTime / 1000) % 60;
        long minutes = (elapsedTime / (1000 * 60)) % 60;
        
        timeLabel.setText(timeFormat.format(minutes) + ":" + timeFormat.format(seconds));
    }
    
    
    public void reset() {
        statusLabel.setText("Ready");
        progressBar.setValue(0);
        progressLabel.setText("0%");
        timeLabel.setText("00:00");
    }
}