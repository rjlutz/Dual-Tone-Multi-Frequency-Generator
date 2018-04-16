/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nigel.DTMF.pitch.dsp;
/**
 * Utility class to generate Dual-tone multi-frequency (DTMF) signaling tones.
 * This class also contains a list of valid DTMF frequencies and characters.
 * @author Nigel Mukandi
 */
public class DTMF {

	/**
	 * The list of valid DTMF frequencies. 
	 */
	public static final double[] DTMF_FREQUENCIES = { 697, 770, 852, 941, 1209,
			1336, 1477, 1633 };
	/**
	 * The list of valid DTMF characters.
	 */
	public static final char[][] DTMF_CHARACTERS = { { '1', '2', '3', 'A' },
			{ '4', '5', '6', 'B' }, { '7', '8', '9', 'C' },
			{ '*', '0', '#', 'D' } };
        
        
	/**
         * Is 1 is pressed first frequency is 697Hz and second frequency is 1209Hz
	 * Generate a DTMF - tone for a valid DTMF character. 
	 * @param character a valid DTMF character (present in {@link DTMF.DTMF_CHARACTERS}}
	 * @return a float buffer of predefined length (7168 samples) with the correct DTMF tone representing the character.
	 */
	public static float[] generateDTMFTone(char character){
		double firstFrequency = -1;
		double secondFrequency = -1;
		for(int row = 0 ; row < DTMF_CHARACTERS.length ; row++){
			for(int col = 0 ; col < DTMF_CHARACTERS[row].length ; col++){
				if(DTMF_CHARACTERS[row][col] == character){
					firstFrequency = DTMF_FREQUENCIES[row];
					secondFrequency = DTMF_FREQUENCIES[col + 4];
				}
			}
		}
                //pass the first frequency and second frequency and number of samples into audiobuffer defined 
                //in this class .audiobuffer static method
		return DTMF.audioBufferDTMF(firstFrequency,secondFrequency,7168);
	}
	
	/**
	 * Checks if the given character is present in {@link DTMF.DTMF_CHARACTERS}.
	 * @param character the character to check.
	 * @return True if the given character is present in {@link DTMF.DTMF_CHARACTERS}, false otherwise.
	 */
	public static boolean isDTMFCharacter(char character){
		double firstFrequency = -1;
		double secondFrequency = -1;
		for(int row = 0 ; row < DTMF_CHARACTERS.length ; row++){
			for(int col = 0 ; col < DTMF_CHARACTERS[row].length ; col++){
				if(DTMF_CHARACTERS[row][col] == character){
					firstFrequency = DTMF_FREQUENCIES[row];
					secondFrequency = DTMF_FREQUENCIES[col + 4];
				}
			}
		}
		return (firstFrequency!=-1 && secondFrequency!=-1);
	}
        
    /**
     *
     * @param f0 first frequency from pair
     * @param f1 second frequency from pair   
     * @param size number of samples to be taken
     * @return buffer array with discrete samples of signal
     */
    public static float[] audioBufferDTMF(final double f0,final double f1,int size) {
		final double sampleRate = 44100.0;
		final double amplitudeF0 = 0.5;
		final double amplitudeF1 = 0.5;
		final double twoPiF0 = 2 * Math.PI * f0;
		final double twoPiF1 = 2 * Math.PI * f1;
		final float[] buffer = new float[size];
		for (int sample = 0; sample < buffer.length; sample++) {
                    final double time = sample / sampleRate;
                    double f0Component = amplitudeF0 * Math.sin(twoPiF0 * time);
		    double f1Component = amplitudeF1 * Math.sin(twoPiF1 * time);
		    buffer[sample] = (float) (f0Component + f1Component);
		}
		return buffer;
    }
}

