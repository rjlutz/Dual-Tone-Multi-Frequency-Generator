
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ggc.lutz.dtmf.dsp;
import javax.sound.sampled.*;
/**
 * BlockingAudioPlayer can be used to sync events with sound. 
 * Variable line is the Java Sound Object that actually makes the sound.
 The write method on line blocks until it is ready for more data. 
 If this AudioProcessor chained with other AudioProcessors the others 
 should be able to operate in real time or process the signal on a separate
 thread.
 * @author Nigel Mukandi @2018
 */
    public final class BlockingAudioPlayer implements AudioProcessor {
	

	/**
	 * The line to send sound to. Is also used to keep everything in sync.
	 */
	private SourceDataLine line;

	/**
	 * The overlap and step size defined not in samples but in bytes. So it
	 * depends on the bit depth. Since the integer data type is used only
	 * 8,16,24,... bits or 1,2,3,... bytes are supported.
	 */
	private final int byteOverlap, byteStepSize;

	/**
	 * Creates a new BlockingAudioPlayer.
	 * 
	 * @param format
	 *            The AudioFormat of the buffer.
	 * @param bufferSize
	 *            The size of each buffer in samples (not in bytes).
	 * @param overlap
	 *            Defines how much consecutive buffers overlap in samples (not
	 *            in bytes).
	 * @throws LineUnavailableException
	 *             If no output line is available.
	 */
	public BlockingAudioPlayer(final AudioFormat format, final int bufferSize, final int overlap)
			throws LineUnavailableException {
		final DataLine.Info info = new DataLine.Info(SourceDataLine.class,format);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open();
		line.start();	
		// overlap in samples * nr of bytes / sample = bytes overlap
		this.byteOverlap = overlap * format.getFrameSize();
		this.byteStepSize = bufferSize * format.getFrameSize() - byteOverlap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * be.hogent.tarsos.util.RealTimeAudioProcessor.AudioProcessor#processFull
	 * (float[], byte[])
	 */
        @Override
	public boolean processFull(final float[] audioFloatBuffer, final byte[] audioByteBuffer) {
		// Play the first full buffer
		line.write(audioByteBuffer, 0, audioByteBuffer.length);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * be.hogent.tarsos.util.RealTimeAudioProcessor.AudioProcessor#proccess(
	 * float[], byte[])
	 */
        @Override
	public boolean processOverlapping(final float[] audioBuffer, final byte[] audioByteBuffer) {
		// Play only the audio that has not been played yet.
		line.write(audioByteBuffer, byteOverlap, byteStepSize);
		return true;
	}

        
	/*
	 * (non-Javadoc)
	 * 
	 * @see be.hogent.tarsos.util.RealTimeAudioProcessor.AudioProcessor#
	 * processingFinished()
	 */
        @Override
	public void processingFinished() {
		// cleanup
		line.drain();
		line.close();
	}
    }
