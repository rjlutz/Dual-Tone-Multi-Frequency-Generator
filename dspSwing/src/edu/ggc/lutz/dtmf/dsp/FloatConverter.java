/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ggc.lutz.dtmf.dsp;
import javax.sound.sampled.AudioFormat;
import edu.ggc.lutz.dtmf.goertzel.AudioFloatConverter;

/**
 * <p>
 * Converts the float buffer to a byte buffer.
 * </p>
 * <p>
 * If somewhere in the processor chain the float buffer is altered but the same
 * operation is not performed on the byte buffer this should be added to the end
 * of the chain.
 * </p>
 * 
 * @author Nigel Mukandi
 */
public class FloatConverter implements AudioProcessor {	
	private final AudioFloatConverter converter;	
	/**
	 * Initialize a new converter for a format.
	 * 
	 * @param format
	 *            The format the float information should be converted to.
	 */
	public FloatConverter(AudioFormat format) {
		converter = AudioFloatConverter.getConverter(format);
	}
	@Override
	public boolean processFull(float[] audioFloatBuffer, byte[] audioByteBuffer) {
		converter.toByteArray(audioFloatBuffer, audioByteBuffer);
		return true;
	}
	@Override
	public boolean processOverlapping(float[] audioFloatBuffer,
			byte[] audioByteBuffer) {
		converter.toByteArray(audioFloatBuffer, audioByteBuffer);
		return true;
	}
	@Override
	public void processingFinished() {		
	}
}
