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

package monodimensionalProblemTimeDependent;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.Finalize;
import oms3.annotations.In;
import oms3.annotations.Initialize;
import oms3.annotations.Keywords;
import oms3.annotations.License;
import oms3.annotations.Unit;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

@Description("This class writes a NetCDF with energy 1D equation outputs. Before writing, outputs are stored in a buffer writer "
		+ "and as simulation is ended they are written in a NetCDF file.")
@Documentation("")
@Author(name = "Niccolo' Tubini, Riccardo Rigon", contact = "tubini.niccolo@gmail.com")
@Keywords("Hydrology, energy equation")
//@Label(JGTConstants.HYDROGEOMORPHOLOGY)
//@Name("shortradbal")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")


public class WriteNetCDFEnergy1D {

	@Description()
	@In
	@Unit ()
	public LinkedHashMap<String,ArrayList<double[]>> myVariables; // consider the opportunity to save varibale as float instead of double

	@Description()
	@In
	@Unit ()
	public double[] mySpatialCoordinate;
	
	@Description()
	@In
	@Unit ()
	public double[] myDualSpatialCoordinate;

	@Description()
	@In
	@Unit ()
	public String fileName;

	@Description("Brief descritpion of the problem")
	@In
	@Unit ()
	public String briefDescritpion;

	@Description("Boolean variable to print output file only at the end of the simulation")
	@In
	@Unit ()
	public boolean doProcess;

	int NLVL;
	int dualNLVL;
	int NREC;
	// human readable date will be converted in unix format, the format will be an input and it has to be consistent with that used in OMS
	DateFormat dateFormat;
	Date date = null;
	long unixTime;
	double[] myTempVariable; 
	Iterator it;


	// Create the file.
	String filename;
	NetcdfFileWriter dataFile;

	@Execute
	public void writeNetCDF() {


		if(doProcess == false) {
			
			
			final int NLVL = mySpatialCoordinate.length;
			final int dualNLVL = myDualSpatialCoordinate.length;
			final int NREC = myVariables.keySet().size();
			// human readable date will be converted in unix format, the format will be an input and it has to be consistent with that used in OMS
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date date = null;
			long unixTime;
			double[] myTempVariable; 
			//Iterator it;


			// Create the file.
			//String filename = fileName;
			NetcdfFileWriter dataFile = null;

			try {
				// Create new netcdf-3 file with the given filename
				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName);
				// add a general attribute describing the problem and containing other relevant information for the user
				dataFile.addGroupAttribute(null, new Attribute("Description of the problem",briefDescritpion));

				//add dimensions  where time dimension is unlimit
				// in 1D case dimension are time and the depth
				Dimension lvlDim = dataFile.addDimension(null, "depth", NLVL);
				Dimension dualLvlDim = dataFile.addDimension(null, "dualDepth", dualNLVL);
				@SuppressWarnings("unused")
				Dimension timeDim = dataFile.addUnlimitedDimension("time");

				// Define the coordinate variables.
				Variable depthVar = dataFile.addVariable(null, "depth", DataType.DOUBLE, "depth");
				Variable dualDepthVar = dataFile.addVariable(null, "dual depth", DataType.DOUBLE, "dualDepth");
				Variable timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");

				// Define units attributes for data variables.
				dataFile.addVariableAttribute(depthVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(depthVar, new Attribute("long_name", "Soil depth"));
				
				dataFile.addVariableAttribute(dualDepthVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(dualDepthVar, new Attribute("long_name", "Dual soil depth"));
				
				dataFile.addVariableAttribute(timeVar, new Attribute("units","unix convention"));

				// Define the netCDF variables for the psi and theta data.
				String dims = "time depth";
				String dualDims = "time dualDepth";


				Variable temperatureVar = dataFile.addVariable(null, "T", DataType.DOUBLE, dims);
				Variable iCVar = dataFile.addVariable(null, "TIC", DataType.DOUBLE, "depth");
				Variable internalEnergyVar = dataFile.addVariable(null, "internalEnergy", DataType.DOUBLE, dims);
				Variable diffusionFluxesVar = dataFile.addVariable(null, "diffusionFlux", DataType.DOUBLE, dualDims);
				Variable advectionFluxesVar = dataFile.addVariable(null, "advectionFlux", DataType.DOUBLE, dualDims);
				Variable pecletVar = dataFile.addVariable(null, "peclet", DataType.DOUBLE, dualDims);
				Variable errorVar = dataFile.addVariable(null, "error", DataType.DOUBLE, "time");
				Variable airTemperatureVar = dataFile.addVariable(null, "airTemperature", DataType.DOUBLE, "time");
				Variable shortWaveVar = dataFile.addVariable(null, "shortWave", DataType.DOUBLE, "time");
				Variable windVelocityVar = dataFile.addVariable(null, "windVelocity", DataType.DOUBLE, "time");
				Variable bottomBCVar = dataFile.addVariable(null, "bottomBC", DataType.DOUBLE, "time");

				// Define units attributes for data variables.
				dataFile.addVariableAttribute(temperatureVar, new Attribute("units", "K"));
				dataFile.addVariableAttribute(temperatureVar, new Attribute("long_name", "Temperature"));
				dataFile.addVariableAttribute(iCVar, new Attribute("units", "K"));
				dataFile.addVariableAttribute(iCVar, new Attribute("long_name", "Initial condition for temperature"));
				dataFile.addVariableAttribute(internalEnergyVar, new Attribute("units", "Joule"));
				dataFile.addVariableAttribute(internalEnergyVar, new Attribute("long_name", "Internal energy"));
				dataFile.addVariableAttribute(diffusionFluxesVar, new Attribute("units", "W/m^2"));
				dataFile.addVariableAttribute(diffusionFluxesVar, new Attribute("long_name", "Heat diffusion flux"));
				dataFile.addVariableAttribute(advectionFluxesVar, new Attribute("units", "W/m^2"));
				dataFile.addVariableAttribute(advectionFluxesVar, new Attribute("long_name", "Heat advection flux"));
				dataFile.addVariableAttribute(errorVar, new Attribute("units", "J"));
				dataFile.addVariableAttribute(errorVar, new Attribute("long_name", "energy error at each time step"));
				dataFile.addVariableAttribute(airTemperatureVar, new Attribute("units", "K"));
				dataFile.addVariableAttribute(airTemperatureVar, new Attribute("long_name", "air temperature"));
				dataFile.addVariableAttribute(shortWaveVar, new Attribute("units", "W/m^2"));
				dataFile.addVariableAttribute(shortWaveVar, new Attribute("long_name", "Short wave radiation at soil surface"));
				dataFile.addVariableAttribute(windVelocityVar, new Attribute("units", "m/s"));
				dataFile.addVariableAttribute(windVelocityVar, new Attribute("long_name", "wind velocity"));
				dataFile.addVariableAttribute(bottomBCVar, new Attribute("units", "K"));
				dataFile.addVariableAttribute(bottomBCVar, new Attribute("long_name", "bottom boundary condition value"));

				// These data are those created by bufferWriter class. If this wasn't an example program, we
				// would have some real data to write for example, model output.
				// times variable is filled later
				ArrayDouble.D1 depths = new ArrayDouble.D1(lvlDim.getLength());
				ArrayDouble.D1 dualDepths = new ArrayDouble.D1(dualLvlDim.getLength());

				ArrayDouble.D1 dataTemperatureIC = new ArrayDouble.D1(lvlDim.getLength());
				Array times = Array.factory(DataType.LONG, new int[] {NREC});

				int z;

				for (z = 0; z < lvlDim.getLength(); z++) {
					depths.set(z, mySpatialCoordinate[z]);
				}
				
				for (z = 0; z < dualLvlDim.getLength(); z++) {
					dualDepths.set(z, myDualSpatialCoordinate[z]);
				}

				// These data are those created by bufferWriter class. This will write our hydraulic head (psi) and
				// adimensional water content (theta) data
				ArrayDouble.D2 dataInternalEnergy = new ArrayDouble.D2(NREC, lvlDim.getLength());
				ArrayDouble.D2 dataTemperature = new ArrayDouble.D2(NREC, lvlDim.getLength());
				ArrayDouble.D2 dataDiffusionFluxes = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
				ArrayDouble.D2 dataAdvectionFluxes = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
				ArrayDouble.D2 dataPeclet = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
				ArrayDouble.D1 dataError =  new ArrayDouble.D1(NREC);
				ArrayDouble.D1 dataAirTemperature =  new ArrayDouble.D1(NREC);
				ArrayDouble.D1 dataShortWave =  new ArrayDouble.D1(NREC);
				ArrayDouble.D1 dataWindVelocity =  new ArrayDouble.D1(NREC);
				ArrayDouble.D1 dataBottomBC =  new ArrayDouble.D1(NREC);


				int i=0;
				it = myVariables.entrySet().iterator();
				while (it.hasNext()) {

					@SuppressWarnings("unchecked")
					Entry<String, ArrayList<double[]>> entry = (Entry<String, ArrayList<double[]>>) it.next();

					try {
						date = dateFormat.parse(entry.getKey());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					unixTime = (long) date.getTime()/1000;
					// think if there is a better way instead of using i
					times.setLong(i, unixTime);


					myTempVariable =  entry.getValue().get(0);
					for (int lvl = 0; lvl < NLVL; lvl++) {

						dataTemperature.set(i, lvl, myTempVariable[lvl]);

					}

					myTempVariable =  entry.getValue().get(1);
					for (int lvl = 0; lvl < NLVL; lvl++) {

						dataInternalEnergy.set(i, lvl, myTempVariable[lvl]);

					}
					
					myTempVariable =  entry.getValue().get(2);
					for (int lvl = 0; lvl < NLVL; lvl++) {

						dataTemperatureIC.set(lvl, myTempVariable[lvl]);

					}
					
					myTempVariable =  entry.getValue().get(3);
					for (int lvl = 0; lvl < dualNLVL; lvl++) {

						dataDiffusionFluxes.set(i,lvl, myTempVariable[lvl]);

					}

					myTempVariable =  entry.getValue().get(4);
					for (int lvl = 0; lvl < dualNLVL; lvl++) {

						dataAdvectionFluxes.set(i,lvl, myTempVariable[lvl]);

					}
					
					myTempVariable =  entry.getValue().get(5);
					for (int lvl = 0; lvl < dualNLVL; lvl++) {

						dataPeclet.set(i,lvl, myTempVariable[lvl]);

					}

					dataError.set(i, entry.getValue().get(6)[0]);

					dataAirTemperature.set(i, entry.getValue().get(7)[0]);

					dataShortWave.set(i, entry.getValue().get(8)[0]);
					
					dataWindVelocity.set(i, entry.getValue().get(9)[0]);
					
					dataBottomBC.set(i, entry.getValue().get(10)[0]);

					i++;
				}


				//Create the file. At this point the (empty) file will be written to disk
				dataFile.create();

				// A newly created Java integer array to be initialized to zeros.
				int[] origin = new int[2];

				dataFile.write(depthVar, depths);
				dataFile.write(dualDepthVar, dualDepths);
				dataFile.write(timeVar, origin, times);
				dataFile.write(temperatureVar, origin, dataTemperature);
				dataFile.write(internalEnergyVar, origin, dataInternalEnergy);
				dataFile.write(iCVar, origin, dataTemperatureIC);
				dataFile.write(diffusionFluxesVar, origin, dataDiffusionFluxes);
				dataFile.write(advectionFluxesVar, origin, dataAdvectionFluxes);
				dataFile.write(pecletVar, origin, dataPeclet);
				dataFile.write(errorVar, origin, dataError);
				dataFile.write(airTemperatureVar, origin, dataAirTemperature);
				dataFile.write(shortWaveVar, origin, dataShortWave);
				dataFile.write(windVelocityVar, origin, dataWindVelocity);
				dataFile.write(bottomBCVar, origin, dataBottomBC);

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

			System.out.println("*** SUCCESS writing energy equation 1D output file, " + fileName);


		}
		
		
	}

}
