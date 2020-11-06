/*
 * GNU GPL v3 License
 *
 * Copyright 2019 Niccolo` Tubini
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geoframe.blogspot.bidimensionalproblemtimedependent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 
 * @author Niccolo` Tubini
 *
 */
public class BufferWriter {
	
	public int N = 36+1;
	public LinkedHashMap<String,double[]> psis;
	public LinkedHashMap<String,double[]> thetas;
	public String datesString[] = {"1991-10-30 00:00", "1991-10-30 01:00"};
	public double[] psiValues = new double[N];
	public double[] thetaValues = new double[N];
	public double[] x = new double[N];
	public double[] z = new double[N];
	
    //private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;
    //private String formatterPattern = JGTConstants.utcDateFormatterYYYYMMDDHHMM_string;
	
	
	public BufferWriter() {
		
		
		psis = new LinkedHashMap<String,double[]>();
		thetas = new LinkedHashMap<String,double[]>();
		
		
	}
	
	public void set() {
		
		/* Initialize the x and z vectors containing the coordinates 
		 * elements' centroids
		 */
		x[0] = -999.0; z[0] = -999.0;
		x[1] = 0.0833; z[1] = 0.9167;
		x[2] = 0.1388; z[2] = 0.8056;
		x[3] = 0.0555; z[3] = 0.6389;
		x[4] = 0.3333; z[4] = 0.25;
		x[5] = 0.0555; z[5] = 0.3611;
		x[6] = 0.1667; z[6] = 0.25;
		x[7] = 0.25;   z[7] = 0.8333;
		x[8] = 0.1111; z[8] = 0.5;
		x[9] = 0.3889; z[9] = 0.3611;
		x[10] = 0.4444; z[10] = 0.1389;
		x[11] = 0.6667; z[11] = 0.25;
		x[12] = 0.7222; z[12] = 0.5;
		x[13] = 0.3333; z[13] = 0.75;
		x[14] = 0.6111; z[14] = 0.6389;
		x[15] = 0.5556; z[15] = 0.8611;
		x[16] = 0.6389; z[16] = 0.0556;
		x[17] = 0.8056; z[17] = 0.1389;
		x[18] = 0.6111; z[18] = 0.3611;
		x[19] = 0.9444; z[19] = 0.3611;
		x[20] = 0.75;   z[20] = 0.8333;
		x[21] = 0.6667; z[21] = 0.75;
		x[22] = 0.9444; z[22] = 0.6389;
		x[23] = 0.9167; z[23] = 0.0833;
		x[24] = 0.8333; z[24] = 0.25;
		x[25] = 0.9167; z[25] = 0.9617;
		x[26] = 0.8611; z[26] = 0.8056;
		x[27] = 0.8889; z[27] = 0.5;
		x[28] = 0.6389; z[28] = 0.9444;
		x[29] = 0.3889; z[29] = 0.6389;
		x[30] = 0.4444; z[30] = 0.8611;
		x[31] = 0.3611; z[31] = 0.9444;
		x[32] = 0.0833; z[32] = 0.8333;
		x[33] = 0.3611; z[33] = 0.0556;
		x[34] = 0.5556; z[34] = 0.1389;
		x[35] = 0.2778; z[35] = 0.5;
		x[36] = 0.1944; z[36] = 0.1389;
		
		for(int i=0; i<datesString.length; i++) {
				
			// collects ___Values in psis and thetas

			if(i==0) {
				psis.put(datesString[i],new double[] {-999.0,-0.9167,-0.8056,-0.6389,-0.25,-0.3611,-0.25,-0.8333,-0.5,-0.3611,-0.1389,-0.25,-0.5,-0.75,-0.6389,-0.8611,-0.0556,
														-0.1389,-0.3611,-0.3611,-0.8333,-0.75,-0.6389,-0.0833,-0.25,-0.9617,-0.8056,-0.5,-0.9444,-0.6389,-0.8611,-0.9444,-0.8333,-0.0556,
														-0.1389,-0.5,-0.1389});
				//psis.put(datesString[i], z);
				thetas.put(datesString[i],new double[] {-999,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,
														30,31,32,33,34,35,36});

			}else {
				psis.put(datesString[i],new double[] {-999.0,-0.9167,-0.8056,-0.6389,-0.25,-0.3611,-0.25,-0.8333,-0.5,-0.3611,-0.1389,-0.25,-0.5,-0.75,-0.6389,-0.8611,-0.0556,
														-0.1389,-0.3611,-0.3611,-0.8333,-0.75,-0.6389,-0.0833,-0.25,-0.9617,-0.8056,-0.5,-0.9444,-0.6389,-0.8611,-0.9444,-0.8333,-0.0556,
														-0.1389,-0.5,-0.1389});
				//psis.put(datesString[i], z);
				thetas.put(datesString[i],new double[] {-999,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,
														30,31,32,33,34,35,36});

			}

			
		}
		
	}
	
	
	
	public Map<String,double[]> getPsis() {
		return psis;
	}
	
	
	
	public Map<String,double[]> getThetas() {
		return thetas;
	}
	
	
	
	public double[] getX() {
		return x;
	}
	
	
	
	public double[] getZ() {
		return z;
	}
	

}
