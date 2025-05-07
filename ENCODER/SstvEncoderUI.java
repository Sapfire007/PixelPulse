import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class SstvEncoderUI extends JFrame {
    private JTextField inputPathField;
    private JTextField outputPathField;
    private JLabel imagePreview;
    private JButton browseInputButton;
    private JButton browseOutputButton;
    private JButton convertButton;
    private JProgressBar progressBar;
    private JTextArea logArea;

    public SstvEncoderUI() {
        super("SSTV Encoder");
        initUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        JPanel filePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        inputPathField = new JTextField();
        browseInputButton = new JButton("Browse Image");
        outputPathField = new JTextField();
        browseOutputButton = new JButton("Browse Output Dir");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        filePanel.add(new JLabel("Input Image:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        filePanel.add(inputPathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.1;
        filePanel.add(browseInputButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        filePanel.add(new JLabel("Output WAV:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        filePanel.add(outputPathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.1;
        filePanel.add(browseOutputButton, gbc);

        imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(320, 240));
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(320, 240));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(new JScrollPane(imagePreview));
        centerPanel.add(logScroll);

        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        convertButton = new JButton("Convert");
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        controlPanel.add(convertButton, BorderLayout.WEST);
        controlPanel.add(progressBar, BorderLayout.CENTER);

        setLayout(new BorderLayout(10, 10));
        add(filePanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        browseInputButton.addActionListener(this::onBrowseInput);
        browseOutputButton.addActionListener(this::onBrowseOutput);
        convertButton.addActionListener(this::onConvert);
    }

    private void onBrowseInput(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            inputPathField.setText(file.getAbsolutePath());
            try {
                BufferedImage img = ImageIO.read(file);
                Image scaled = img.getScaledInstance(imagePreview.getWidth(), imagePreview.getHeight(), Image.SCALE_SMOOTH);
                imagePreview.setIcon(new ImageIcon(scaled));
            } catch (IOException ex) {
                log("Failed to load image preview: " + ex.getMessage());
            }
        }
    }

    private void onBrowseOutput(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            outputPathField.setText(new File(dir, "output.wav").getAbsolutePath());
        }
    }

    private void onConvert(ActionEvent e) {
        String inputPath = inputPathField.getText().trim();
        String outputPath = outputPathField.getText().trim();
        if (inputPath.isEmpty() || outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both input and output paths.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        convertButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        log("Starting conversion...");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {

                    JpgToSstvEncoder.main(new String[]{inputPath, outputPath});
                    publish("Conversion complete: " + outputPath);
                } catch (Exception ex) {
                    publish("Error: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String msg : chunks) log(msg);
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                convertButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SstvEncoderUI().setVisible(true));
    }
}
