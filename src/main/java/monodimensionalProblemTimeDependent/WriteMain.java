/**
 * 
 */
package monodimensionalProblemTimeDependent;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
// to deal with unit conversion
/*import ucar.nc2.units.*;
import ucar.units.BaseUnit;
import ucar.units.TimeScaleUnit;
import ucar.units.TimeScaleUnit.*;
import ucar.units.Unit;
import ucar.units.UnitFormat;
import ucar.units.UnitFormatManager;
*/
/**
 * @author Niccolo` Tubini
 *
 */
public class WriteMain {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {

		float[] depth;
		Map<String,float[]> psis;
		Map<String,float[]> thetas;
		Iterator<Entry<String, float[]>> it;
		BufferWriter buffWr = new BufferWriter();
		
		// create pretend data with class buffer writer. This class will be a component that stores all data of a OMS simulation that will be written at the end.
		buffWr.set();

		depth = buffWr.getDepth();
		psis = buffWr.getPsis();
		thetas = buffWr.getThetas();

		/////////////////////////////////////////
		/////////////////////////////////////////
		/////////////////////////////////////////

		// this is the NetCDF writer
		
		final int NLVL = depth.length;
		final int NREC = psis.keySet().size();
		// human readable date will be converted in unix format, the format will be an input and it has to be consistent with that used in OMS
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date date;
		long unixTime;
		
        

		// Create the file.
		String filename = "hydraulic_2D.nc";
		NetcdfFileWriter dataFile = null;

		try {
			// Create new netcdf-3 file with the given filename
			dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, filename);

			//add dimensions  where time dimension is unlimit
			// in 1D case dimension are time and the depth
			Dimension lvlDim = dataFile.addDimension(null, "depth", NLVL);
			Dimension timeDim = dataFile.addUnlimitedDimension("time");

			// Define the coordinate variables.
			Variable depthVar = dataFile.addVariable(null, "depth", DataType.FLOAT, "depth");
			Variable timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");

			// Define units attributes for data variables.
			dataFile.addVariableAttribute(depthVar, new Attribute("units", "m"));
			dataFile.addVariableAttribute(depthVar, new Attribute("long_name", "Soil depth"));
			dataFile.addVariableAttribute(timeVar, new Attribute("units","unix convention"));

			// Define the netCDF variables for the psi and theta data.
			String dims = "time depth";

			Variable psiVar = dataFile.addVariable(null, "psi", DataType.FLOAT, dims);
			Variable thetaVar = dataFile.addVariable(null, "theta", DataType.FLOAT, dims);

			// Define units attributes for data variables.
			dataFile.addVariableAttribute(psiVar, new Attribute("units", "m"));
			dataFile.addVariableAttribute(psiVar, new Attribute("long_name", "Hydraulic head"));
			dataFile.addVariableAttribute(thetaVar, new Attribute("units", "-"));
			dataFile.addVariableAttribute(thetaVar, new Attribute("long_name", "Adimensional water content"));

			// These data are those created by bufferWriter class. If this wasn't an example program, we
			// would have some real data to write for example, model output.
			// times variable is filled later
			ArrayFloat.D1 depths = new ArrayFloat.D1(lvlDim.getLength());
			Array times = Array.factory(DataType.LONG, new int[] {NREC});

			int z;

			for (z = 0; z < lvlDim.getLength(); z++) {
				depths.set(z, depth[z]);
			}

			// These data are those created by bufferWriter class. This will write our hydraulic head (psi) and
			// adimensional water content (theta) data
			ArrayFloat.D2 dataTheta = new ArrayFloat.D2(NREC, lvlDim.getLength());
			ArrayFloat.D2 dataPsi = new ArrayFloat.D2(NREC, lvlDim.getLength());
			
			int i=0;
			//for (HashMap.Entry<String, float[]> entry : psis.entrySet()) {
			it = psis.entrySet().iterator();
			while (it.hasNext()) {
				
				Entry<String, float[]> entry = it.next();
				
				date = dateFormat.parse(entry.getKey());
				unixTime = (long) date.getTime()/1000;
				// think if there is a better way instead of using i
				times.setLong(i, unixTime);
				
				for (int lvl = 0; lvl < NLVL; lvl++) {
					
					dataPsi.set(i, lvl, entry.getValue()[lvl]);

				}
				//System.out.println("\n\n");
				i++;
			}
			
			i = 0;
			it = thetas.entrySet().iterator();
			while (it.hasNext()) {
				
				Entry<String, float[]> entry = it.next();
				
				for (int lvl = 0; lvl < NLVL; lvl++) {
					
					dataTheta.set(i, lvl, entry.getValue()[lvl]);
					
				}
				i++;
			}


			//Create the file. At this point the (empty) file will be written to disk
			dataFile.create();

			// A newly created Java integer array to be initialized to zeros.
			int[] origin = new int[2];

			dataFile.write(depthVar, depths);
			dataFile.write(timeVar, origin, times);
			dataFile.write(psiVar, origin, dataPsi);
			dataFile.write(thetaVar, origin, dataTheta);


		} catch (IOException e) {
			e.printStackTrace(System.err);

		} catch (InvalidRangeException e) {
			e.printStackTrace(System.err);

		} finally {
			if (dataFile != null)
				try {
					dataFile.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
		}
		System.out.println("*** SUCCESS writing example file " + filename);
	}

}
