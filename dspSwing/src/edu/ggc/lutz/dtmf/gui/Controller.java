package edu.ggc.lutz.dtmf.gui;

import edu.ggc.lutz.dtmf.dsp.AudioDispatcher;
import edu.ggc.lutz.dtmf.dsp.AudioProcessor;
import edu.ggc.lutz.dtmf.dsp.BlockingAudioPlayer;
import edu.ggc.lutz.dtmf.goertzel.AudioFloatConverter;
import edu.ggc.lutz.dtmf.goertzel.DTMF;
import edu.ggc.lutz.dtmf.goertzel.Goertzel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {

    private final int stepSize = 256;

    @FXML
    private List<ProgressBar> bars;

    @FXML
    private Label detectedKeyLabel;

    @FXML
    private void handleKeyPressed(KeyEvent ke){
        if (! (ke.getText().length() > 0))
            return;

        char c = ke.getText().charAt(0);
        if (DTMF.isDTMFCharacter(c))
            try {
                process(c);
            } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }
    }

    public void process(char character) throws UnsupportedAudioFileException, LineUnavailableException {
        final float[] floatBuffer = DTMF.generateDTMFTone(character);
        final AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        final AudioFloatConverter converter = AudioFloatConverter.getConverter(format);
        final byte[] byteBuffer = new byte[floatBuffer.length * format.getFrameSize()];
        converter.toByteArray(floatBuffer, byteBuffer);
        final ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
        final AudioInputStream inputStream = new AudioInputStream(bais, format, floatBuffer.length);
        final AudioDispatcher dispatcher = new AudioDispatcher(inputStream, stepSize, 0);
        dispatcher.addAudioProcessor(goertzelAudioProcessor);
        dispatcher.addAudioProcessor(new BlockingAudioPlayer(format, stepSize, 0));
        new Thread(dispatcher).start();
    }

    private final AudioProcessor goertzelAudioProcessor = new Goertzel(44100, stepSize, DTMF.DTMF_FREQUENCIES, new Goertzel.FrequenciesDetectedHandler() {
        @Override
        public void handleDetectedFrequencies(final double[] frequencies, final double[] powers, final double[] allFrequencies, final double allPowers[]) {
            if (frequencies.length != 2) return;
            int row = -1, col = -1;

            for (int i = 0; i < 4; i++) // note doesn't work if 3 or more tones are present
                if (frequencies[0] == DTMF.DTMF_FREQUENCIES[i] || frequencies[1] == DTMF.DTMF_FREQUENCIES[i]) {
                    row = i;
                    for (int j = 4; j < DTMF.DTMF_FREQUENCIES.length; j++)
                        if (frequencies[0] == DTMF.DTMF_FREQUENCIES[j] || frequencies[1] == DTMF.DTMF_FREQUENCIES[j]) {
                            col = j - 4;
                            break;
                        }
                }

            if (row == -1 && col == -1) return;

            final String detected =
                    String.valueOf(DTMF.DTMF_CHARACTERS[row][col]);

            Platform.runLater(new Runnable() {
                @Override public void run() {
                    for (int i = 0; i < bars.size(); i++) {
                        ProgressBar bar = bars.get(i);
                        double value = Math.max(allPowers[i] / 100.0D, 0); // clamp at 0, no negatives
                        bar.setProgress(value);
                        String color;
                        color = (allPowers[i]>Goertzel.POWER_THRESHOLD) ? "forestgreen" : "gray";
                        bar.setStyle("-fx-accent: "+ color +";");
                    }
                    detectedKeyLabel.setText(detected);
                }
            });
        }
    });
}
