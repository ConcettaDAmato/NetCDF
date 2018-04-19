/**
 * 
 */
package monodimensionalProblemTimeDependent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Niccolo` Tubini
 *
 */
public class BufferWriter {
	
	public int N = 20;
	public LinkedHashMap<String,float[]> psis;
	public LinkedHashMap<String,float[]> thetas;
	public String datesString[] = {"1991-10-30 00:00", "1991-10-30 01:00"};
	public float[] psiValues = new float[N];
	public float[] thetaValues = new float[N];
	public float[] depth = new float[N];
	
    //private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;
    //private String formatterPattern = JGTConstants.utcDateFormatterYYYYMMDDHHMM_string;
	
	
	public BufferWriter() {
		
		
		psis = new LinkedHashMap<String,float[]>();
		thetas = new LinkedHashMap<String,float[]>();
		
		
	}
	
	public void set() {
		
		// initialize depth
		for(int j=0; j<N; j++) {
			depth[j] = -j;
		}
		
		
		for(int i=0; i<datesString.length; i++) {
				
			// collects ___Values in psis and thetas

			if(i==0) {
				psis.put(datesString[i],new float[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20});
				thetas.put(datesString[i],new float[] {3,6,9,12,15,18,21,24,27,30,33,36,39,42,45,48,51,54,57,60});

			}else {
				psis.put(datesString[i],new float[] {2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40});
				thetas.put(datesString[i],new float[] {6,12,18,24,30,36,42,48,54,60,66,72,78,84,90,96,102,108,114,120});

			}

			
		}
		
	}
	
	
	
	public Map<String,float[]> getPsis() {
		return psis;
	}
	
	
	
	public Map<String,float[]> getThetas() {
		return thetas;
	}
	
	
	
	public float[] getDepth() {
		return depth;
	}
	

}
