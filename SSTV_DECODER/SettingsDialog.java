import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;


public class SettingsDialog extends JDialog {
    
    private int imageWidth = 800;
    private int imageHeight = 320;
    private double blackFreq = 1500.0;
    private double whiteFreq = 2300.0;
    private String outputDirectory = ".";
    private String outputFormat = "png";
    private boolean showGrid = true;
    
    
    private JTextField widthField;
    private JTextField heightField;
    private JTextField blackFreqField;
    private JTextField whiteFreqField;
    private JTextField outputDirField;
    private JComboBox<String> formatComboBox;
    private JCheckBox showGridCheckbox;
    
    private boolean settingsChanged = false;
    
    public SettingsDialog(Frame owner) {
        super(owner, "SSTV Decoder Settings", true);
        initComponents();
        loadSettings();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        
        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        
        imagePanel.add(new JLabel("Image Width:"), gbc);
        gbc.gridx = 1;
        widthField = new JTextField(5);
        imagePanel.add(widthField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        imagePanel.add(new JLabel("Image Height:"), gbc);
        gbc.gridx = 1;
        heightField = new JTextField(5);
        imagePanel.add(heightField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        imagePanel.add(new JLabel("Show Grid:"), gbc);
        gbc.gridx = 1;
        showGridCheckbox = new JCheckBox();
        imagePanel.add(showGridCheckbox, gbc);
        
        
        JPanel freqPanel = new JPanel(new GridBagLayout());
        freqPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        freqPanel.add(new JLabel("Black Frequency (Hz):"), gbc);
        gbc.gridx = 1;
        blackFreqField = new JTextField(8);
        freqPanel.add(blackFreqField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        freqPanel.add(new JLabel("White Frequency (Hz):"), gbc);
        gbc.gridx = 1;
        whiteFreqField = new JTextField(8);
        freqPanel.add(whiteFreqField, gbc);
        
        
        JPanel outputPanel = new JPanel(new GridBagLayout());
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        outputPanel.add(new JLabel("Output Directory:"), gbc);
        gbc.gridx = 1;
        outputDirField = new JTextField(20);
        outputPanel.add(outputDirField, gbc);
        
        gbc.gridx = 2;
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseOutputDirectory());
        outputPanel.add(browseButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        outputPanel.add(new JLabel("Output Format:"), gbc);
        gbc.gridx = 1;
        formatComboBox = new JComboBox<>(new String[]{"png", "jpg", "bmp"});
        outputPanel.add(formatComboBox, gbc);
        
        
        tabbedPane.addTab("Image", imagePanel);
        tabbedPane.addTab("Frequency", freqPanel);
        tabbedPane.addTab("Output", outputPanel);
        
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            if (saveSettings()) {
                settingsChanged = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        
        setSize(400, 300);
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }
    
    private void loadSettings() {
        widthField.setText(String.valueOf(imageWidth));
        heightField.setText(String.valueOf(imageHeight));
        blackFreqField.setText(String.valueOf(blackFreq));
        whiteFreqField.setText(String.valueOf(whiteFreq));
        outputDirField.setText(outputDirectory);
        formatComboBox.setSelectedItem(outputFormat);
        showGridCheckbox.setSelected(showGrid);
    }
    
    private boolean saveSettings() {
        try {
            int width = Integer.parseInt(widthField.getText().trim());
            int height = Integer.parseInt(heightField.getText().trim());
            double blackF = Double.parseDouble(blackFreqField.getText().trim());
            double whiteF = Double.parseDouble(whiteFreqField.getText().trim());
            
            if (width <= 0 || height <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Image dimensions must be positive numbers.", 
                    "Invalid Settings", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (blackF >= whiteF) {
                JOptionPane.showMessageDialog(this, 
                    "Black frequency must be less than white frequency.", 
                    "Invalid Settings", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            imageWidth = width;
            imageHeight = height;
            blackFreq = blackF;
            whiteFreq = whiteF;
            outputDirectory = outputDirField.getText().trim();
            outputFormat = (String) formatComboBox.getSelectedItem();
            showGrid = showGridCheckbox.isSelected();
            
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for all numeric fields.", 
                "Invalid Settings", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void browseOutputDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Output Directory");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dir = fileChooser.getSelectedFile();
            outputDirField.setText(dir.getAbsolutePath());
        }
    }
    
    
    public int getImageWidth() {
        return imageWidth;
    }
    
    public int getImageHeight() {
        return imageHeight;
    }
    
    public double getBlackFreq() {
        return blackFreq;
    }
    
    public double getWhiteFreq() {
        return whiteFreq;
    }
    
    public String getOutputDirectory() {
        return outputDirectory;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }
    
    public boolean isShowGrid() {
        return showGrid;
    }
    
    public boolean isSettingsChanged() {
        return settingsChanged;
    }
}