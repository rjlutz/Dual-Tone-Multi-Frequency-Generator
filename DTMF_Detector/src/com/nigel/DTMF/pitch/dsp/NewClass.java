/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nigel.DTMF.pitch.dsp;

/**
 *
 * @author DjMadd
 */
public class NewClass {
double sampleRate = 44100.0;
double frequency = 440.0;
double amplitude = 0.8;
double seconds = 2.0;
double twoPiF = 2*Math.PI*frequency;
int num_samples = (int)(seconds * sampleRate);
float [] buffer = new float [num_samples];
for(int i =0; i<3;i++){

}
}
