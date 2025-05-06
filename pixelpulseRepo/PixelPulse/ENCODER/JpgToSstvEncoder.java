import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class JpgToSstvEncoder {

    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;
    private static final float SAMPLE_RATE = 44100.0f;
    private static final int BITS_PER_SAMPLE = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private static final float BLACK_FREQUENCY = 1500.0f;
    private static final float WHITE_FREQUENCY = 2300.0f;

    public static void main(String[] args) {
        String inputImagePath  = (args.length > 0 && args[0] != null) ? args[0] : "input.jpg";
        String outputAudioPath = (args.length > 1 && args[1] != null) ? args[1] : "output.wav";

        try {
            BufferedImage image = loadImage(inputImagePath);
            if (image == null) {
                System.err.println("Error loading image: " + inputImagePath);
                return;
            }

            BufferedImage resizedImage = resizeImage(image, IMAGE_WIDTH, IMAGE_HEIGHT);
            float[][][] yuvData = convertRgbToYuv(resizedImage);
            byte[] sstvAudioData = generateRobot36Audio(yuvData);

            saveWaveFile(sstvAudioData, outputAudioPath);
            System.out.println("Successfully converted " + inputImagePath + " to " + outputAudioPath);

        } catch (IOException | LineUnavailableException e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private static float[][][] convertRgbToYuv(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[][][] yuvData = new float[3][height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convert to YUV using BT.601
                float y_val = 0.299f * r + 0.587f * g + 0.114f * b;
                float u_val = (b - y_val) * 0.492f;
                float v_val = (r - y_val) * 0.877f;

                yuvData[0][y][x] = y_val;        // Y
                yuvData[1][y][x] = u_val + 128;  // U centered at 128
                yuvData[2][y][x] = v_val + 128;  // V centered at 128
            }
        }
        return yuvData;
    }

    private static byte[] generateRobot36Audio(float[][][] yuvData) throws IOException, LineUnavailableException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        double[] phaseHolder = new double[]{0.0}; // Maintain phase continuity

        // Calibration header
        appendTone(out, 1900, 300, phaseHolder); // Leader
        appendTone(out, 1200, 10, phaseHolder);  // Break
        appendTone(out, 1900, 300, phaseHolder); // Leader

        // VIS code for Robot36 (8)
        appendVisCode(out, 8, phaseHolder);

        for (int line = 0; line < IMAGE_HEIGHT; line++) {
            // Sync pulse and porch
            appendTone(out, 1200, 9, phaseHolder);
            appendTone(out, 1500, 3, phaseHolder);

            // Y channel
            appendScanline(out, yuvData[0][line], 88, phaseHolder);

            if ((line % 2) == 0) {
                // Even line: V channel
                appendTone(out, 1500, 4.5f, phaseHolder);
                appendTone(out, 1900, 1.5f, phaseHolder);
                float[] vLine = downsampleLine(yuvData[2][line], IMAGE_WIDTH / 2);
                appendScanline(out, vLine, 44, phaseHolder);
            } else {
                // Odd line: U channel
                appendTone(out, 2300, 4.5f, phaseHolder);
                appendTone(out, 1900, 1.5f, phaseHolder);
                float[] uLine = downsampleLine(yuvData[1][line], IMAGE_WIDTH / 2);
                appendScanline(out, uLine, 44, phaseHolder);
            }
        }

        return out.toByteArray();
    }

    private static void appendVisCode(ByteArrayOutputStream out, int visCode, double[] phaseHolder) throws IOException {
        appendTone(out, 1200, 30, phaseHolder); // Start bit

        // Data bits (LSB first)
        for (int i = 0; i < 7; i++) {
            int bit = (visCode >> i) & 1;
            appendTone(out, bit == 1 ? 1100 : 1300, 30, phaseHolder);
        }

        appendTone(out, 1200, 30, phaseHolder); // Stop bit
    }

    private static void appendScanline(ByteArrayOutputStream out, float[] pixelData, float durationMs, double[] phaseHolder) throws IOException {
        int numSamples = (int) (durationMs / 1000 * SAMPLE_RATE);
        int numPixels = pixelData.length;
        if (numPixels == 0 || numSamples == 0) return;

        double phase = phaseHolder[0];
        for (int i = 0; i < numSamples; i++) {
            int index = Math.min((int) ((float) i / numSamples * numPixels), numPixels - 1);
            float pixel = pixelData[index];
            float freq = BLACK_FREQUENCY + (pixel / 255.0f) * (WHITE_FREQUENCY - BLACK_FREQUENCY);

            short sample = (short) (sin(phase) * Short.MAX_VALUE);
            phase += 2 * PI * freq / SAMPLE_RATE;

            ByteBuffer buffer = ByteBuffer.allocate(2)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putShort(sample);
            out.write(buffer.array());
        }
        phaseHolder[0] = phase % (2 * PI);
    }

    private static void appendTone(ByteArrayOutputStream out, float freq, float durationMs, double[] phaseHolder) throws IOException {
        int numSamples = (int) (durationMs / 1000 * SAMPLE_RATE);
        double phase = phaseHolder[0];

        for (int i = 0; i < numSamples; i++) {
            short sample = (short) (sin(phase) * Short.MAX_VALUE);
            phase += 2 * PI * freq / SAMPLE_RATE;

            ByteBuffer buffer = ByteBuffer.allocate(2)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putShort(sample);
            out.write(buffer.array());
        }
        phaseHolder[0] = phase % (2 * PI);
    }

    private static float[] downsampleLine(float[] line, int targetWidth) {
        float[] downsampled = new float[targetWidth];
        float ratio = (float) line.length / targetWidth;

        for (int i = 0; i < targetWidth; i++) {
            int start = (int) (i * ratio);
            int end = (int) ((i + 1) * ratio);
            end = Math.min(end, line.length);

            float sum = 0;
            for (int j = start; j < end; j++) sum += line[j];
            downsampled[i] = sum / (end - start);
        }
        return downsampled;
    }

    private static void saveWaveFile(byte[] audioData, String filename) throws IOException {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, BITS_PER_SAMPLE, CHANNELS, SIGNED, BIG_ENDIAN);
        try (AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(audioData), format, audioData.length)) {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, new File(filename));
        }
    }
}