package edu.ggc.lutz.dtmf;

import edu.ggc.lutz.dtmf.dsp.AudioDispatcher;
import edu.ggc.lutz.dtmf.dsp.AudioProcessor;
import edu.ggc.lutz.dtmf.dsp.BlockingAudioPlayer;
import edu.ggc.lutz.dtmf.goertzel.AudioFloatConverter;
import edu.ggc.lutz.dtmf.goertzel.DTMF;
import edu.ggc.lutz.dtmf.goertzel.Goertzel;
import edu.ggc.lutz.dtmf.goertzel.Goertzel.FrequenciesDetectedHandler;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;

import static java.lang.Short.*;
import static javax.swing.GroupLayout.*;
import static javax.swing.GroupLayout.Alignment.*;
import static javax.swing.LayoutStyle.ComponentPlacement.*;

public class DTMF_GUI extends JFrame {

    private static final long serialVersionUID = -1143769091770144461L;

    private final JLabel[] labels = new JLabel[11];
    private final JPanel[] panels = new JPanel[3];
    private final JProgressBar[] bars = new JProgressBar[8];
    private final GroupLayout[] layouts = new GroupLayout[3];
    private final int stepSize = 256;

    private KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent event) {
            if (DTMF.isDTMFCharacter(event.getKeyChar()))
                try {
                    process(event.getKeyChar());
                } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                    e.printStackTrace();
                }
        }
    };

    public DTMF_GUI() {
        initComponents();
        this.addKeyListener(keyAdapter);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DTMF_GUI().setVisible(true);
            }
        });
    }

    /**
     * Process a DTMF character: generate sound and decode the sound.
     *
     * @param character The character.
     * @throws UnsupportedAudioFileException
     * @throws LineUnavailableException
     */
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

    private final AudioProcessor goertzelAudioProcessor = new Goertzel(44100, stepSize, DTMF.DTMF_FREQUENCIES, new FrequenciesDetectedHandler() {
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
            labels[9].setText("" + DTMF.DTMF_CHARACTERS[row][col]);
            for (int i = 0; i < bars.length; i++)
                bars[i].setValue((int) allPowers[i]);
        }
    });

    private void initComponents() {

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        for (int i = 0; i < panels.length; i++)
            panels[i] = new JPanel();
        for (int i = 0; i < labels.length; i++)
            labels[i] = new JLabel();
        for (int i = 0; i < bars.length; i++)
            bars[i] = new JProgressBar();

        labels[0].setText("0.697 kHz");
        labels[1].setText("0.770 kHZ");
        labels[2].setText("0.852 kHz");
        labels[3].setText("0.941 kHz");
        labels[4].setText("1.209 kHz");
        labels[5].setText("1.336 kHz");
        labels[6].setText("1.477 kHz");
        labels[7].setText("1.633 kHz");

        layouts[0] = new GroupLayout(panels[0]);
        panels[0].setLayout(layouts[0]);
        layouts[0].setHorizontalGroup(
                layouts[0].createParallelGroup(LEADING)
                        .addGroup(layouts[0].createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[0])
                                        .addComponent(labels[1])
                                        .addComponent(labels[2])
                                        .addComponent(labels[3])
                                        .addComponent(labels[4])
                                        .addComponent(labels[5])
                                        .addComponent(labels[6])
                                        .addComponent(labels[7]))
                                .addGap(41, 41, 41)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(bars[7], PREFERRED_SIZE, 257, PREFERRED_SIZE)
                                        .addComponent(bars[6], PREFERRED_SIZE, 257, PREFERRED_SIZE)
                                        .addComponent(bars[5], PREFERRED_SIZE, 257, PREFERRED_SIZE)
                                        .addComponent(bars[4], PREFERRED_SIZE, 257, PREFERRED_SIZE)
                                        .addComponent(bars[3], PREFERRED_SIZE, 257, PREFERRED_SIZE)
                                        .addComponent(bars[2], PREFERRED_SIZE, 257, PREFERRED_SIZE)
                                        .addComponent(bars[1], PREFERRED_SIZE, 257, PREFERRED_SIZE)
                                        .addComponent(bars[0], PREFERRED_SIZE, 257, PREFERRED_SIZE))
                                .addContainerGap(24, MAX_VALUE))
        );
        layouts[0].setVerticalGroup(
                layouts[0].createParallelGroup(LEADING)
                        .addGroup(layouts[0].createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[0])
                                        .addComponent(bars[0], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(RELATED)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[1])
                                        .addComponent(bars[1], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(RELATED)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[2])
                                        .addComponent(bars[2], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(RELATED)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[3])
                                        .addComponent(bars[3], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(RELATED)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[4])
                                        .addComponent(bars[4], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(RELATED)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[5])
                                        .addComponent(bars[5], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(RELATED)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[6])
                                        .addComponent(bars[6], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(RELATED)
                                .addGroup(layouts[0].createParallelGroup(LEADING)
                                        .addComponent(labels[7])
                                        .addComponent(bars[7], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addContainerGap(DEFAULT_SIZE, MAX_VALUE))
        );

        labels[8].setText("Key Detected");
        labels[9].setText("Press Dial Pad");

        layouts[1] = new GroupLayout(panels[1]);
        panels[1].setLayout(layouts[1]);
        layouts[1].setHorizontalGroup(
                layouts[1].createParallelGroup(LEADING)
                        .addGroup(layouts[1].createSequentialGroup()
                                .addContainerGap()
                                .addComponent(labels[8])
                                .addGap(41, 41, 41)
                                .addComponent(labels[9], PREFERRED_SIZE, 143, PREFERRED_SIZE)
                                .addContainerGap(DEFAULT_SIZE, MAX_VALUE))
        );
        layouts[1].setVerticalGroup(
                layouts[1].createParallelGroup(LEADING)
                        .addGroup(TRAILING, layouts[1].createSequentialGroup()
                                .addContainerGap(DEFAULT_SIZE, MAX_VALUE)
                                .addGroup(layouts[1].createParallelGroup(BASELINE)
                                        .addComponent(labels[8])
                                        .addComponent(labels[9]))
                                .addGap(18, 18, 18))
        );

        labels[10].setText("               DTMF DETECTOR GOERTZEL ALGORITHM");

        layouts[2] = new GroupLayout(panels[2]);
        panels[2].setLayout(layouts[2]);
        layouts[2].setHorizontalGroup(
                layouts[2].createParallelGroup(LEADING)
                        .addGroup(TRAILING, layouts[2].createSequentialGroup()
                                .addContainerGap()
                                .addComponent(labels[10], DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE)
                                .addContainerGap())
        );
        layouts[2].setVerticalGroup(
                layouts[2].createParallelGroup(LEADING)
                        .addGroup(TRAILING, layouts[2].createSequentialGroup()
                                .addContainerGap(8, MAX_VALUE)
                                .addComponent(labels[10])
                                .addContainerGap())
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                        .addGroup(TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(TRAILING)
                                        .addComponent(panels[0], LEADING, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE)
                                        .addComponent(panels[1], LEADING, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE)
                                        .addComponent(panels[2], LEADING, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(panels[2], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(panels[0], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addPreferredGap(RELATED)
                                .addComponent(panels[1], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addContainerGap(DEFAULT_SIZE, MAX_VALUE))
        );
        pack();
    }
}
