/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mukandi.dtmf.dsp;
/**
 * AudioProcessor is responsible for the actual data processing
 Interface : A buffer with some floats and the same information in raw bytes.
 * AudioProcessors are meant to be chained e.g execute an effect and then play
 * the sound. The chain of audio processor can be interrupted by returning false
 * in the process method
 * @author Nigel Mukandi @2018
 */
public interface AudioProcessor { 
    /**
    * Process the first (complete) buffer. Once the first complete buffer is
    * processed the remaining buffers are overlapping buffers and processed
    * using the processOverlapping method (Even if overlap is zero).
    * @param audioFloatBuffer The buffer to process using the float data type.
    * @param audioByteBuffer  The buffer to process using raw bytes.
    * @return                 False if the chain needs to stop here, true otherwise. This can be used to implement e.g. a silence detector. 
    */
    boolean processFull(final float[] audioFloatBuffer, final byte[] audioByteBuffer);

    /**
    * Do the actual signal processing on an overlapping buffer. Once the
    * first complete buffer is processed the remaining buffers are
    * overlapping buffers and are processed using the processOverlapping
    * method. Even if overlap is zero.
    * @param audioFloatBuffer The buffer to process using the float data type.
    * @param audioByteBuffer  The buffer to process using raw bytes.
    * @return False if the chain needs to stop here, true otherwise. This can be used to implement e.g. a silence detector.
    */
    
    boolean processOverlapping(final float[] audioFloatBuffer, final byte[] audioByteBuffer);

    /**
    * Notify the AudioProcessor that no more data is available and processing
 has finished. Can be used to deallocate resources or cleanup.
    */
    void processingFinished();
}    