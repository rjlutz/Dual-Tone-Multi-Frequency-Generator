package edu.ggc.lutz.dtmf.gui;

import edu.ggc.lutz.dtmf.dsp.AudioDispatcher;
import edu.ggc.lutz.dtmf.dsp.AudioProcessor;
import edu.ggc.lutz.dtmf.dsp.BlockingAudioPlayer;
import edu.ggc.lutz.dtmf.goertzel.AudioFloatConverter;
import edu.ggc.lutz.dtmf.goertzel.DTMF;
import edu.ggc.lutz.dtmf.goertzel.Goertzel;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;

public class Controller {

    private final int stepSize = 256;

    @FXML
    private void handleKeyPressed(KeyEvent ke){
        System.out.println("Key Pressed: " + ke.getCode());

        char c = ke.getText().charAt(0);
        System.out.println("char: " + c);

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
            for (int i = 0; i < 4; i++) // note work if 3 or more tones are present
                if (frequencies[0] == DTMF.DTMF_FREQUENCIES[i] || frequencies[1] == DTMF.DTMF_FREQUENCIES[i]) {
                    row = i;
                    for (int j = 4; j < DTMF.DTMF_FREQUENCIES.length; j++)
                        if (frequencies[0] == DTMF.DTMF_FREQUENCIES[j] || frequencies[1] == DTMF.DTMF_FREQUENCIES[j]) {
                            col = j - 4;
                            break;
                        }
                }

            if (row == -1 && col == -1) return;

            //labels[9].setText("" + DTMF.DTMF_CHARACTERS[row][col]);
            System.out.print("" + DTMF.DTMF_CHARACTERS[row][col]);
            //for (int i = 0; i < bars.length; i++)
                //bars[i].setValue((int) allPowers[i]);
            for (double intensity : allPowers)
                System.out.print(" " + intensity);
            System.out.println("\n");
        }
    });

}
