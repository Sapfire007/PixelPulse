# 🎨 PixelPulse

**PixelPulse** is a desktop Java application suite for encoding and decoding images using the **Robot 36 SSTV (Slow-Scan Television)** format. It includes two standalone tools:

* 📤 **Encoder** – Convert a JPG image into an SSTV-compliant `.wav` audio file.
* 📥 **Decoder** – Convert a Robot 36 `.wav` file back into an image.

Built with **Java AWT** and **Swing**, PixelPulse offers a minimal yet functional GUI experience for hobbyists, students, and radio enthusiasts experimenting with SSTV.

---

## ✨ Features

### ✅ Encoder

* Load a JPG image from disk.
* Encode it into a `.wav` file using the **Robot 36** SSTV standard.
* Real-time encoding progress.
* Output is a `.wav` audio file playable by SSTV decoders or transmittable via audio/radio.

### ✅ Decoder

* Load a `.wav` file (Robot 36 encoded).
* Decode and reconstruct the original image using:

  * FFT (Fast Fourier Transform)
  * Hilbert Transform for envelope extraction.
* View and save the resulting image as a PNG.

---

## 🖼 GUI Overview

The project consists of two independent Java Swing applications:

* **Encoder App**:

  * Select an image
  * Preview it
  * Click **Encode**
  * Save the generated `.wav`

* **Decoder App**:

  * Load a `.wav` file
  * Click **Decode**
  * View the reconstructed image
  * Save it as `.png`

---

## 🚀 How to Run

### 1. Clone the Repository

```bash
git clone https://github.com/Sapfire007/PixelPulse.git
cd PixelPulse
```

### 2. Compile the Source Code

Using `javac`:

```bash
javac -d bin src/encoder/*.java
javac -d bin src/decoder/*.java
```

### 3. Run the Applications

```bash
java -cp bin encoder.EncoderApp
java -cp bin decoder.DecoderApp
```

Alternatively, you can use the provided `.bat` files (on Windows) or run `.jar` files if available.

---

## 🧠 Technical Details

* **Language**: Java
* **GUI Framework**: AWT + Swing
* **Core Concepts**:

  * **SSTV Robot 36 protocol** implementation
  * Signal generation and decoding via **pure Java**
  * Use of **FFT** and **Hilbert transform** to decode frequencies into image lines


---

## 🧪 Example Usage

### Encoding an Image:

1. Open the Encoder App.
2. Select a `.jpg` image.
3. Click **Encode**.
4. Save the `.wav` output.

### Decoding Audio:

1. Open the Decoder App.
2. Load an SSTV `.wav` file.
3. Click **Decode**.
4. Save the reconstructed image.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

> Contributions, issues, and suggestions are welcome!
