/*
 *  Copyright 2006-2007 Columbia University.
 *
 *  This file is part of MEAPsoft.
 *
 *  MEAPsoft is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  MEAPsoft is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MEAPsoft; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *
 *  See the file "COPYING" for the text of the license.
 */


package com.meapsoft.disgraced;


/**
 * An onset detector that estimates the beat of the piece from onset
 * information and then calls OnsetListeners at the beginning of each
 * beat.
 *
 * @author Mike Mandel (mim@ee.columbia.edu)
 */

/*
public class BeatOnsetDetector extends com.meapsoft.OnsetDetector {

  double bpmMin = 60;
  double bpmMax = 150;
  int minDelay = 10;
//   int minOnset = 10;
  double entThresh = 10;

  double framesPerSecond;
  double[] real, imag;
  FFT fft;
  long nextOnset;
  int zeroFrames;

  public BeatOnsetDetector(STFT stft, double bandThresh, double bandFrac) {
    super(stft, bandThresh, bandFrac);

    // calculate frames per second
    //...

    real = new double[onsets.length];
    imag = new double[onsets.length];
    fft = new FFT(onsets.length);

    zeroFrames = 0;
    nextOnset = 256;
  }

  // Takes the FFT of the number of onsets per frame for the
  // last however many frames (256), finds the peak magnitude between
  // 60 and 150 bpm, uses the phase to predict where the next onset
  // will be.  If that onset is between lastSeen and newestFrame,
  // trigger an onset there.  Otherwise, keep quiet.  Need some way to
  // make sure predictions don't change too drastically from one call
  // to the next...

  // Assumption: this will be called more often than once per beat, so
  // we can safely assume that there will only be at most one beat
  // present per call.
  public void checkOnsets(long lastSeen, long newestFrame) {

//     // Update calculation of how many frames of silence we've heard
//     for(long fr=lastSeen+1; fr <= newestFrame; fr++) {
//       if(onsets[(int)(fr % onsets.length)] < minOnset) {
// 	zeroFrames++;
//       } else {
// 	zeroFrames = 0;
//       }
//     }

    if(nextOnset > lastSeen && nextOnset <= newestFrame) {
      // If the next beat is here, trigger an onset
//      System.out.print("*");
//       System.out.print("*"+zeroFrames+" ");
      notifyListeners(nextOnset, zeroFrames);

    } else if(nextOnset > newestFrame) {
      // haven't gotten to the beat yet

    } else if(nextOnset <= lastSeen) {
      // need to find the next beat

      // Copy onset array into FFT buffers, unwrap the circular array
      for(int i=0; i<onsets.length; i++) {
	real[i] = onsets[(int)((newestFrame+1+i) % onsets.length)];
	imag[i] = 0;
      }
      
      fft.fft(real, imag);
      
      double maxMag = 0, mag, meanMag = 0;
      int argMaxMag = 0;
 
      // ignore DC component
      for(int i=1; i<real.length/2; i++) {
	mag = real[i]*real[i] + imag[i]*imag[i];
	meanMag += mag;
	if(mag > maxMag) {
	  maxMag = mag;
	  argMaxMag = i;
	}
      }
      meanMag /= real.length/2.0 - 1;

      // Use a parabolic model to get a better idea of where the
      // frequency peak actually falls.  This is the offset added to
      // the integral frequency peak location.
      double magP = real[argMaxMag+1]*real[argMaxMag+1] 
	+ imag[argMaxMag+1]*imag[argMaxMag+1];
      double mag0 = real[argMaxMag]*real[argMaxMag] 
	+ imag[argMaxMag]*imag[argMaxMag];
      double magN = real[argMaxMag-1]*real[argMaxMag-1] 
	+ imag[argMaxMag-1]*imag[argMaxMag-1];
      double rem = -(magP - magN) / (magP - 2.0*mag0 + magN);
      
//       System.out.print((argMaxMag+rem) + " ");
      int period = (int)((double)real.length / (rem + argMaxMag));
      double angle = Math.atan2(imag[argMaxMag], real[argMaxMag]);

      int delay = (int)(-angle * period/(2*Math.PI));
      delay = (delay+period) % period;
      if(delay < minDelay)
	delay = delay + period;

//       System.out.print(period + ":" + delay + " ");
      
      // Should be an onset when angle = 2pi n = omega t = 2pi k/N t
      nextOnset = newestFrame + delay;

//       System.out.print(newestFrame + ":" + nextOnset + " ");

      // If the max doesn't stand out enough, assume the signal is
      // silence
//       System.out.print((maxMag/meanMag) + " ");
      if(maxMag < entThresh*meanMag)
	zeroFrames += delay;
      else
	zeroFrames = 0;
    }
  }
}
*/
