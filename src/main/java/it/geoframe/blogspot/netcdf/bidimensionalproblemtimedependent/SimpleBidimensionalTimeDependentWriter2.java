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

package it.geoframe.blogspot.netcdf.bidimensionalproblemtimedependent;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
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

//import monodimensionalProblemTimeDependent.BufferWriter;

/**
 * @author Niccolo` Tubini
 *
 */
public class SimpleBidimensionalTimeDependentWriter2 {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {

		double[] tmp_x;
		double[] tmp_z;
		Map<String,double[]> psis;
		Map<String,double[]> thetas;
		Iterator<Entry<String, double[]>> it;
		BufferWriter buffWr = new BufferWriter();
		String timeUnits = "Seconds since 01/01/1970 01:00:00 UTC";
		/* Create pretend data with class buffer writer. T
		* Hydrostatic condition to test the output plot
		*/
		buffWr.set();

		tmp_x = buffWr.getX();
		tmp_z = buffWr.getZ();
		psis = buffWr.getPsis();
		thetas = buffWr.getThetas();

		/////////////////////////////////////////
		/////////////////////////////////////////
		/////////////////////////////////////////

		// this is the NetCDF writer

		final int NZ = tmp_z.length-1; 
		final int NX = tmp_x.length-1;
		final int NREC = psis.keySet().size();
		// human readable date will be converted in unix format, the format will be an input and it has to be consistent with that used in OMS
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date date;
		long unixTime;



		// Create the file.
		String filename = "resources/output/hydraulic_xz_time.nc";
		NetcdfFileWriter dataFile = null;

		try {
			// Create new netcdf-3 file with the given filename
			dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, filename);
			dataFile.addGroupAttribute(null, new Attribute("PROVA GLOBAL ATTRIBUTE",""));
			//add dimensions  where time dimension is unlimit
			// in 1D case dimension are time and the depth
			Dimension zDim = dataFile.addDimension(null, "z", NZ);
			Dimension xDim = dataFile.addDimension(null, "x", NX);
			Dimension timeDim = dataFile.addUnlimitedDimension("time");

			// Define the coordinate variables.
			Variable zVar = dataFile.addVariable(null, "z", DataType.DOUBLE, "z");
			Variable xVar = dataFile.addVariable(null, "x", DataType.DOUBLE, "x");
			Variable timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");

			// Define units attributes for data variables.
			dataFile.addVariableAttribute(zVar, new Attribute("units", "m"));
			dataFile.addVariableAttribute(zVar, new Attribute("long_name", "z coordinate"));
			dataFile.addVariableAttribute(xVar, new Attribute("units", "m"));
			dataFile.addVariableAttribute(xVar, new Attribute("long_name", "x coordinate"));
			dataFile.addVariableAttribute(timeVar, new Attribute("units",timeUnits));

			// Define the netCDF variables for the psi and theta data.
			String dims = "time x z";
//			String dims = "time x";

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
			ArrayFloat.D1 z = new ArrayFloat.D1(zDim.getLength());
			ArrayFloat.D1 x = new ArrayFloat.D1(xDim.getLength());
			Array times = Array.factory(DataType.LONG, new int[] {NREC});

			for (int i = 0; i < zDim.getLength(); i++) {
				z.set(i, (float) tmp_z[i+1]);
				x.set(i, (float) tmp_x[i+1]);
			}

			// These data are those created by bufferWriter class. This will write our hydraulic head (psi) and
			// adimensional water content (theta) data
			ArrayFloat.D3 dataTheta = new ArrayFloat.D3(NREC, xDim.getLength(), zDim.getLength());
			ArrayFloat.D3 dataPsi = new ArrayFloat.D3(NREC, xDim.getLength(), zDim.getLength());
//			ArrayFloat.D2 dataTheta = new ArrayFloat.D2(NREC, xDim.getLength());
//			ArrayFloat.D2 dataPsi = new ArrayFloat.D2(NREC, xDim.getLength());

			int i=0;
			//for (HashMap.Entry<String, float[]> entry : psis.entrySet()) {
			it = psis.entrySet().iterator();
			while (it.hasNext()) {

				Entry<String, double[]> entry = it.next();

				date = dateFormat.parse(entry.getKey());
				unixTime = (long) date.getTime()/1000;
				// think if there is a better way instead of using i
				times.setLong(i, unixTime);

				for (int n = 0; n < NZ; n++) {

					dataPsi.set(i, n, n, (float) entry.getValue()[n+1]);
//					dataPsi.set(i, n, (float) entry.getValue()[n+1]);

				}
				//System.out.println("\n\n");
				i++;
			}

			i = 0;
			it = thetas.entrySet().iterator();
			while (it.hasNext()) {

				Entry<String, double[]> entry = it.next();

				for (int n = 0; n < NZ; n++) {

					dataTheta.set(i, n, n, (float) entry.getValue()[n+1]);
//					dataTheta.set(i, n, (float) entry.getValue()[n+1]);

				}
				i++;
			}


			//Create the file. At this point the (empty) file will be written to disk
			dataFile.create();

			// A newly created Java integer array to be initialized to zeros.
			int[] origin = new int[3];

			dataFile.write(xVar, x);
			dataFile.write(zVar, z);
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
