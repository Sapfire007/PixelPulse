import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class Main {
    private static final int SAMPLE_RATE = 44100; 
    private static int IMAGE_WIDTH = 800;
    private static int IMAGE_HEIGHT = 320;
    private static double BLACK_FREQ = 1500.0; 
    private static double WHITE_FREQ = 2300.0; 
    private static double FREQ_RANGE = WHITE_FREQ - BLACK_FREQ;

    private static JFrame frame;
    private static JTextArea logArea;
    private static File selectedFile;
    private static File outputFile;
    private static LoadingPanel loadingPanel;
    private static JLayeredPane layeredPane;
    private static Thread decodingThread;
    private static volatile boolean cancelDecoding;

    
    private static ToolBar toolBar;
    private static StatusBar statusBar;
    private static ImagePreviewPanel previewPanel;
    private static SettingsDialog settingsDialog;
    private static WaveformPanel waveformPanel;

    public static void main(String[] args) {
        createAndShowGUI();
    }

    private static void createAndShowGUI() {
        frame = new JFrame("SSTV Decoder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        
        layeredPane = new JLayeredPane();
        frame.setContentPane(layeredPane);

        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBounds(0, 0, 900, 600);

        
        toolBar = new ToolBar();

        
        statusBar = new StatusBar();

        
        previewPanel = new ImagePreviewPanel();

        
        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        
        waveformPanel = new WaveformPanel();
        waveformPanel.setPreferredSize(new Dimension(900, 100));

        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScrollPane, previewPanel);
        splitPane.setResizeWeight(0.5);

        
        contentPanel.add(toolBar, BorderLayout.NORTH);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(waveformPanel, BorderLayout.SOUTH);
        contentPanel.add(statusBar, BorderLayout.SOUTH);

        
        layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER);

        
        loadingPanel = new LoadingPanel("Decoding SSTV");
        loadingPanel.setBounds(0, 0, 900, 600);
        loadingPanel.setVisible(false);
        loadingPanel.setCancelAction(e -> cancelDecodingProcess());
        layeredPane.add(loadingPanel, JLayeredPane.MODAL_LAYER);

        
        toolBar.setOpenAction(e -> selectFile());
        toolBar.setDecodeAction(e -> decodeFile());
        toolBar.setViewAction(e -> viewOutput());
        toolBar.setSettingsAction(e -> openSettings());
        toolBar.setSaveAsAction(e -> saveImageAs());

        
        new FileDropHandler(contentPanel, files -> handleDroppedFiles(files));

        
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = frame.getContentPane().getSize();
                contentPanel.setBounds(0, 0, size.width, size.height);
                loadingPanel.setBounds(0, 0, size.width, size.height);
            }
        });

        

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        
        statusBar.setStatus("Ready - Drop WAV files or use Open button");
    }

    
    private static void openSettings() {
        if (settingsDialog == null) {
            settingsDialog = new SettingsDialog(frame);
        }

        settingsDialog.setVisible(true);

        if (settingsDialog.isSettingsChanged()) {
            IMAGE_WIDTH = settingsDialog.getImageWidth();
            IMAGE_HEIGHT = settingsDialog.getImageHeight();
            BLACK_FREQ = settingsDialog.getBlackFreq();
            WHITE_FREQ = settingsDialog.getWhiteFreq();
            FREQ_RANGE = WHITE_FREQ - BLACK_FREQ;

            previewPanel.setShowGrid(settingsDialog.isShowGrid());

            logArea.append("Settings updated\n");
        }
    }

    
    private static void saveImageAs() {
        if (outputFile == null || !outputFile.exists()) {
            logArea.append("No decoded image available to save\n");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image As");

        
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Images", "jpg", "jpeg"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP Images", "bmp"));

        

        fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[0]);


        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            
            String extension = "";
            FileNameExtensionFilter filter = (FileNameExtensionFilter) fileChooser.getFileFilter();
            String[] extensions = filter.getExtensions();
            if (extensions.length > 0) {
                extension = extensions[0];
            }

            if (!file.getName().toLowerCase().endsWith("." + extension)) {
                file = new File(file.getAbsolutePath() + "." + extension);
            }

            try {
                BufferedImage img = ImageIO.read(outputFile);
                ImageIO.write(img, extension, file);
                logArea.append("Image saved as: " + file.getAbsolutePath() + "\n");
            } catch (IOException e) {
                logArea.append("Error saving image: " + e.getMessage() + "\n");
            }
        }
    }

    private static void handleDroppedFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".wav")) {
                selectedFile = file;
                logArea.append("Selected file: " + selectedFile.getName() + "\n");
                toolBar.setFileSelected(true);
                return;
            }
        }

        logArea.append("No WAV files found in dropped files\n");
    }

    private static void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("WAV files", "wav"));

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            logArea.append("Selected file: " + selectedFile.getName() + "\n");
            toolBar.setFileSelected(true);
            statusBar.setStatus("File selected: " + selectedFile.getName());
        }
    }

    
    private static void cancelDecodingProcess() {
        if (decodingThread != null && decodingThread.isAlive()) {
            cancelDecoding = true;
            logArea.append("Cancelling decoding process...\n");
            statusBar.setStatus("Cancelling...");

            

            SwingUtilities.invokeLater(() -> {
                loadingPanel.setVisible(false);
                toolBar.setFileSelected(true);
                statusBar.stopTimer();
            });
        }
    }

    private static void decodeFile() {
        
        cancelDecoding = false;

        
        decodingThread = new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    toolBar.setFileSelected(false);
                    loadingPanel.setVisible(true);
                    statusBar.reset();
                    statusBar.startTimer();
                    statusBar.setStatus("Decoding " + selectedFile.getName());
                });

                logArea.append("Starting SSTV audio decoding...\n");

                
                final BufferedImage[] imgRef = new BufferedImage[1];
                imgRef[0] = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

                
                SwingUtilities.invokeLater(() -> {
                    previewPanel.setImage(imgRef[0]);
                });

                
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(selectedFile);
                AudioFormat format = audioInputStream.getFormat();

                
                int numBytes = (int) audioInputStream.getFrameLength() * format.getFrameSize();
                byte[] audioBytes = new byte[numBytes];
                audioInputStream.read(audioBytes); 

                
                double[] audio = bytesToSamples(audioBytes, format);

                waveformPanel.setWaveform(audio);

               
                if (format.getChannels() > 1) {
                    logArea.append("Converting stereo to mono...\n");
                    audio = stereoToMono(audio);
                    waveformPanel.setWaveform(audio);
                }

                
                double[] instantaneousFrequency = calculateInstantaneousFrequency(audio, SAMPLE_RATE);

                
                double[] smoothedFrequency = smoothArray(instantaneousFrequency, 5);

                
                int lineCount = 0;
                int sampleBuffer = 0;

                
                for (int i = 0; i < smoothedFrequency.length - 2800; i++) {
                    if (cancelDecoding) {
                        logArea.append("Decoding cancelled by user.\n");
                        return;
                    }

                    
                    if (smoothedFrequency[i] < 1300) {
                        sampleBuffer++;
                    }

                    
                    if (sampleBuffer > 200) {
                        lineCount++;
                        sampleBuffer = 0;

                        int lineBuffer = 0;
                        int lineIndex = 0;

                        
                        for (int j = i; j < Math.min(i + 5000, smoothedFrequency.length - 1); j++) {
                            lineBuffer++;

                            
                            if (lineBuffer > 12) {
                                lineBuffer = 0;

                                
                                if (lineIndex < IMAGE_WIDTH && lineCount < IMAGE_HEIGHT) {
                                    int pixelValue;
                                    if (smoothedFrequency[j] < BLACK_FREQ) {
                                        pixelValue = 0; 
                                    } else if (smoothedFrequency[j] > WHITE_FREQ) {
                                        pixelValue = 255; 
                                    } else {
                                        pixelValue = (int) (((smoothedFrequency[j] - BLACK_FREQ) / FREQ_RANGE) * 255.0);
                                    }

                                    
                                    Color color = new Color(pixelValue, pixelValue, pixelValue);
                                    imgRef[0].setRGB(lineIndex, lineCount, color.getRGB());
                                }

                                
                                lineIndex++;
                            }
                        }
                    }

                    
                    if (smoothedFrequency[i] > 1300) {
                        sampleBuffer = 0;
                    }
                    if (i % 1000 == 0) {
                        final int progressIndex = i;
                        final int currentLine = lineCount;
                        final double progress = (double)i / (smoothedFrequency.length - 2800);
                        SwingUtilities.invokeLater(() -> {
                            waveformPanel.setProgress(progressIndex);
                            previewPanel.setCurrentLine(currentLine);
                            statusBar.updateProgress(progress);
                        });
                    }

                }

                
                if (cancelDecoding) {
                    logArea.append("Decoding cancelled by user.\n");
                    return;
                }



                
                System.out.println("-- Line Count --");
                System.out.println(lineCount);
                System.out.println("-- Sample Rate --");
                System.out.println(SAMPLE_RATE);
                System.out.println("-- Number of Samples --");
                System.out.println(smoothedFrequency.length);

                
                BufferedImage croppedImg = cropImage(imgRef[0]);
                if (croppedImg != null) {
                    System.out.println(
                            "Cropped image to dimensions: " + croppedImg.getWidth() + "x" + croppedImg.getHeight());
                    imgRef[0] = croppedImg;
                }

                
                outputFile = new File("decoded_sstv.png");
                ImageIO.write(imgRef[0], "png", outputFile);
                logArea.append("Image saved as 'decoded_sstv.png'\n");
                logArea.append("SSTV decoding completed successfully!\n");

                
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.setVisible(false);
                    toolBar.setFileSelected(true);
                    toolBar.setImageAvailable(true);
                    statusBar.stopTimer();
                    statusBar.setStatus("Decoding completed successfully");
                });

            } catch (Exception e) {
                logArea.append("Error processing SSTV audio: " + e.getMessage() + "\n");
                e.printStackTrace();

                
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.setVisible(false);
                    toolBar.setFileSelected(true);
                    statusBar.stopTimer();
                    statusBar.setStatus("Error: " + e.getMessage());
                });
            }
        });

        
        decodingThread.start();
    }

    private static void viewOutput() {
        if (outputFile != null && outputFile.exists()) {
            try {
                Desktop.getDesktop().open(outputFile);
                statusBar.setStatus("Viewing decoded image");
            } catch (IOException e) {
                logArea.append("Error opening output file: " + e.getMessage() + "\n");
                statusBar.setStatus("Error opening image");
            }
        } else {
            logArea.append("No decoded image available to view\n");
            statusBar.setStatus("No image available");
        }
    }

    
    private static double[] bytesToSamples(byte[] audioBytes, AudioFormat format) {
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        int numSamples = audioBytes.length / bytesPerSample;
        double[] samples = new double[numSamples];

        
        boolean bigEndian = format.isBigEndian();
        boolean signed = format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED;

        for (int i = 0; i < numSamples; i++) {
            int sampleIndex = i * bytesPerSample;
            int sample = 0;

            
            if (bytesPerSample == 1) {
                sample = audioBytes[sampleIndex] & 0xFF;
                if (signed && sample > 127)
                    sample -= 256;
            } else if (bytesPerSample == 2) {
                if (bigEndian) {
                    sample = ((audioBytes[sampleIndex] & 0xFF) << 8) | (audioBytes[sampleIndex + 1] & 0xFF);
                } else {
                    sample = ((audioBytes[sampleIndex + 1] & 0xFF) << 8) | (audioBytes[sampleIndex] & 0xFF);
                }
                if (signed && sample > 32767)
                    sample -= 65536;
            }

            
            if (signed) {
                samples[i] = sample / (double) (1 << (format.getSampleSizeInBits() - 1));
            } else {
                samples[i] = (sample / (double) ((1 << format.getSampleSizeInBits()) - 1)) * 2.0 - 1.0;
            }
        }

        return samples;
    }

    
    private static double[] stereoToMono(double[] stereoSamples) {
        int monoLength = stereoSamples.length / 2;
        double[] monoSamples = new double[monoLength];

        for (int i = 0; i < monoLength; i++) {
            monoSamples[i] = stereoSamples[i * 2];
        }

        return monoSamples;
    }

    
    private static double[] calculateInstantaneousFrequency(double[] signal, int sampleRate) {
        
        Complex[] complexSignal = new Complex[signal.length];
        for (int i = 0; i < signal.length; i++) {
            complexSignal[i] = new Complex(signal[i], 0);
        }

        
        int paddedLength = nextPowerOfTwo(signal.length);
        Complex[] paddedSignal = new Complex[paddedLength];
        for (int i = 0; i < paddedLength; i++) {
            if (i < signal.length) {
                paddedSignal[i] = complexSignal[i];
            } else {
                paddedSignal[i] = Complex.ZERO;
            }
        }

        
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftResult = transformer.transform(paddedSignal, TransformType.FORWARD);

        
        int halfLength = paddedLength / 2;
        for (int i = halfLength + 1; i < paddedLength; i++) {
            fftResult[i] = Complex.ZERO;
        }
        
        fftResult[0] = fftResult[0].multiply(0.5);
        fftResult[halfLength] = fftResult[halfLength].multiply(0.5);

        
        Complex[] ifftResult = transformer.transform(fftResult, TransformType.INVERSE);

        
        double[] real = new double[signal.length];
        double[] imag = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            real[i] = ifftResult[i].getReal();
            imag[i] = ifftResult[i].getImaginary();
        }

        
        double[] phase = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            phase[i] = Math.atan2(imag[i], real[i]);
        }

        
        double[] unwrappedPhase = unwrapPhase(phase);

        
        double[] instFreq = new double[signal.length - 1];
        for (int i = 0; i < instFreq.length; i++) {
            double phaseDiff = unwrappedPhase[i + 1] - unwrappedPhase[i];
            instFreq[i] = (phaseDiff / (2.0 * Math.PI)) * sampleRate;
        }

        return instFreq;
    }

    
    private static double[] unwrapPhase(double[] phase) {
        double[] unwrapped = new double[phase.length];
        unwrapped[0] = phase[0];

        for (int i = 1; i < phase.length; i++) {
            double diff = phase[i] - phase[i - 1];

            
            if (diff > Math.PI) {
                diff -= 2 * Math.PI;
            } else if (diff < -Math.PI) {
                diff += 2 * Math.PI;
            }

            unwrapped[i] = unwrapped[i - 1] + diff;
        }

        return unwrapped;
    }

    
    private static double[] smoothArray(double[] array, int windowSize) {
        double[] smoothed = new double[array.length];

        for (int i = 0; i < array.length; i++) {
            double sum = 0;
            int count = 0;

            for (int j = Math.max(0, i - windowSize / 2); j <= Math.min(array.length - 1, i + windowSize / 2); j++) {
                sum += array[j];
                count++;
            }

            smoothed[i] = sum / count;
        }

        return smoothed;
    }

    
    private static int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }



    private static BufferedImage cropImage(BufferedImage img) {
        int minX = img.getWidth();
        int minY = img.getHeight();
        int maxX = 0;
        int maxY = 0;
        boolean foundNonWhite = false;

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color color = new Color(img.getRGB(x, y));
                int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;

                if (brightness < 255) { 
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    foundNonWhite = true;
                }
            }
        }

        
        if (!foundNonWhite) {
            return img;
        }

        int croppedWidth = maxX - minX + 1;
        int croppedHeight = maxY - minY + 1;

        return img.getSubimage(minX, minY, croppedWidth, croppedHeight);
    }
}
class WaveformPanel extends JPanel {
    private double[] waveform;
    private int progress = 0;

    public void setWaveform(double[] waveform) {
        this.waveform = waveform;
        repaint();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (waveform == null || waveform.length == 0) return;

        int width = getWidth();
        int height = getHeight();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.LIGHT_GRAY);
        int centerY = height / 2;
        int step = waveform.length / width;

        for (int i = 0; i < width && i * step < waveform.length; i++) {
            int idx = i * step;
            int y = (int) (waveform[idx] * centerY);
            g.drawLine(i, centerY - y, i, centerY + y);
        }

        g.setColor(new Color(0, 255, 0, 120));
        int progressX = (int) ((progress / (double) waveform.length) * width);
        g.fillRect(0, 0, progressX, height);
    }
}