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

@Description("This class writes a NetCDF with Richards' equation outputs. Before writing, outputs are stored in a buffer writer"
		+ " and as simulation is ended they are written in a NetCDF file.")
@Documentation("")
@Author(name = "Niccolo' Tubini, Francesco Serafin, Riccardo Rigon", contact = "tubini.niccolo@gmail.com")
@Keywords("Hydrology, Richards, Infiltration")
//@Label(JGTConstants.HYDROGEOMORPHOLOGY)
//@Name("shortradbal")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class WriteNetCDFRichards2D {

	@Description()
	@In
	@Unit ()
	public LinkedHashMap<String,ArrayList<double[]>> myVariables; // consider the opportunity to save varibale as float instead of double

	@Description()
	@In
	@Unit ()
	public double[] mySpatialCoordinateX;

	@Description()
	@In
	@Unit ()
	public double[] mySpatialCoordinateZ;

	@Description()
	@In
	@Unit ()
	public double[] myDualSpatialCoordinateX;
	
	@Description()
	@In
	@Unit ()
	public double[] myDualSpatialCoordinateZ;

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

//	int NLVL;
//	int dualNLVL;
//	int NREC;
	// human readable date will be converted in unix format, the format will be an input and it has to be consistent with that used in OMS
//	DateFormat dateFormat;
//	Date date = null;
//	long unixTime;
	double[] myTempVariable; 
	Iterator it;


	// Create the file.
	String filename;
	NetcdfFileWriter dataFile;

	@Execute
	public void writeNetCDF() {


		if(doProcess == false) {
			
			final int NX = mySpatialCoordinateX.length-1;
			final int NZ = mySpatialCoordinateZ.length-1;

			final int dualNX = myDualSpatialCoordinateX.length-1;
			final int dualNZ = myDualSpatialCoordinateZ.length-1;
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
				Dimension zDim = dataFile.addDimension(null, "z", NZ);
				Dimension xDim = dataFile.addDimension(null, "x", NX);
				Dimension xDualDim = dataFile.addDimension(null, "xDual", dualNX);
				Dimension zDualDim = dataFile.addDimension(null, "zDual", dualNZ);
				Dimension timeDim = dataFile.addUnlimitedDimension("time");

				// Define the coordinate variables.
				Variable zVar = dataFile.addVariable(null, "z", DataType.DOUBLE, "z");
				Variable xVar = dataFile.addVariable(null, "x", DataType.DOUBLE, "x");
				Variable xDualVar = dataFile.addVariable(null, "dual x", DataType.DOUBLE, "xDual");
				Variable zDualVar = dataFile.addVariable(null, "dual z", DataType.DOUBLE, "zDual");
				Variable timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");

				// Define units attributes for data variables.
				dataFile.addVariableAttribute(zVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(zVar, new Attribute("long_name", "z coordinate"));
				dataFile.addVariableAttribute(xVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(xVar, new Attribute("long_name", "x coordinate"));
				dataFile.addVariableAttribute(xDualVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(xDualVar, new Attribute("long_name", "xDual"));
				dataFile.addVariableAttribute(zDualVar, new Attribute("long_name", "zDual"));
				dataFile.addVariableAttribute(timeVar, new Attribute("units","unix convention"));

				// Define the netCDF variables for the psi and theta data.
				String dims = "time x";
				String dualDims = "time xDual";


				Variable psiVar = dataFile.addVariable(null, "psi", DataType.DOUBLE, dims);
				Variable iCVar = dataFile.addVariable(null, "psiIC", DataType.DOUBLE, "x");
				Variable thetaVar = dataFile.addVariable(null, "theta", DataType.DOUBLE, dims);
				Variable saturationDegreeVar = dataFile.addVariable(null, "saturationDegree", DataType.DOUBLE, dims);
				Variable darcyVelocitiesVar = dataFile.addVariable(null, "darcyVelocities", DataType.DOUBLE, dualDims);
				Variable darcyVelocitiesXVar = dataFile.addVariable(null, "darcyVelocitiesX", DataType.DOUBLE, dualDims);
				Variable darcyVelocitiesZVar = dataFile.addVariable(null, "darcyVelocitiesZ", DataType.DOUBLE, dualDims);
//				Variable darcyVelocitiesCapillaryVar = dataFile.addVariable(null, "darcyVelocitiesCapillary", DataType.DOUBLE, dualDims);
//				Variable darcyVelocitiesGravityVar = dataFile.addVariable(null, "darcyVelocitiesGravity", DataType.DOUBLE, dualDims);
//				Variable poreVelocitiesVar = dataFile.addVariable(null, "poreVelocities", DataType.DOUBLE, dualDims);
//				Variable celerityVar = dataFile.addVariable(null, "celerities", DataType.DOUBLE, dualDims);
//				Variable kinematicRatioVar = dataFile.addVariable(null, "kinematicRatio", DataType.DOUBLE, dualDims);
//				Variable errorVar = dataFile.addVariable(null, "error", DataType.DOUBLE, "time");
//				Variable topBCVar = dataFile.addVariable(null, "topBC", DataType.DOUBLE, "time");
//				Variable bottomBCVar = dataFile.addVariable(null, "bottomBC", DataType.DOUBLE, "time");
//				Variable runOffVar = dataFile.addVariable(null, "runOff", DataType.DOUBLE, "time");

				// Define units attributes for data variables.
				dataFile.addVariableAttribute(psiVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(psiVar, new Attribute("long_name", "Water suction"));
				dataFile.addVariableAttribute(iCVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(iCVar, new Attribute("long_name", "Initial condition for water suction"));
				dataFile.addVariableAttribute(thetaVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(thetaVar, new Attribute("long_name", "Adimensional water content"));
				dataFile.addVariableAttribute(saturationDegreeVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(saturationDegreeVar, new Attribute("long_name", "Saturation degree"));
				dataFile.addVariableAttribute(darcyVelocitiesVar, new Attribute("units", "m/s"));
				dataFile.addVariableAttribute(darcyVelocitiesVar, new Attribute("long_name", "Darcy velocities"));
				dataFile.addVariableAttribute(darcyVelocitiesXVar, new Attribute("units", "m/s"));
				dataFile.addVariableAttribute(darcyVelocitiesXVar, new Attribute("long_name", "Darcy velocities component on x"));
				dataFile.addVariableAttribute(darcyVelocitiesZVar, new Attribute("units", "m/s"));
				dataFile.addVariableAttribute(darcyVelocitiesZVar, new Attribute("long_name", "Darcy velocities component on z"));
//				dataFile.addVariableAttribute(darcyVelocitiesCapillaryVar, new Attribute("units", "m/s"));
//				dataFile.addVariableAttribute(darcyVelocitiesCapillaryVar, new Attribute("long_name", "Darcy velocities due to the gradient of capillary forces "));
//				dataFile.addVariableAttribute(darcyVelocitiesGravityVar, new Attribute("units", "m/s"));
//				dataFile.addVariableAttribute(darcyVelocitiesGravityVar, new Attribute("long_name", "Darcy velocities due to the gradient of gravity"));
//				dataFile.addVariableAttribute(poreVelocitiesVar, new Attribute("units", "m/s"));
//				dataFile.addVariableAttribute(poreVelocitiesVar, new Attribute("long_name", "Pore velocities, ratio between the Darcy velocities and porosity"));
//				dataFile.addVariableAttribute(celerityVar, new Attribute("units", "m/s"));
//				dataFile.addVariableAttribute(celerityVar, new Attribute("long_name", "Celerity of the pressure wave (Rasmussen et al. 2000"));
//				dataFile.addVariableAttribute(kinematicRatioVar, new Attribute("units", "-"));
//				dataFile.addVariableAttribute(kinematicRatioVar, new Attribute("long_name", "Kinematic ratio (Rasmussen et al. 2000)"));
//				dataFile.addVariableAttribute(errorVar, new Attribute("units", "m"));
//				dataFile.addVariableAttribute(errorVar, new Attribute("long_name", "volume error at each time step"));
//				dataFile.addVariableAttribute(topBCVar, new Attribute("units", "mm"));
//				dataFile.addVariableAttribute(topBCVar, new Attribute("long_name", "rainfall heights"));
//				dataFile.addVariableAttribute(bottomBCVar, new Attribute("units", "m"));
//				dataFile.addVariableAttribute(bottomBCVar, new Attribute("long_name", "water suction"));
//				dataFile.addVariableAttribute(runOffVar, new Attribute("units", "m/s"));
//				dataFile.addVariableAttribute(runOffVar, new Attribute("long_name", "run off"));

				// These data are those created by bufferWriter class. If this wasn't an example program, we
				// would have some real data to write for example, model output.
				// times variable is filled later
				ArrayDouble.D1 z = new ArrayDouble.D1(zDim.getLength());
				ArrayDouble.D1 x = new ArrayDouble.D1(xDim.getLength());
				ArrayDouble.D1 dualX = new ArrayDouble.D1(xDualDim.getLength());
				ArrayDouble.D1 dualZ = new ArrayDouble.D1(zDualDim.getLength());
				ArrayDouble.D1 dataPsiIC = new ArrayDouble.D1(xDim.getLength());

//				ArrayDouble.D1 dataPsiIC = new ArrayDouble.D1(lvlDim.getLength());
				Array times = Array.factory(DataType.LONG, new int[] {NREC});
				
				for (int i = 0; i < zDim.getLength(); i++) {
					z.set(i, (float) mySpatialCoordinateZ[i+1]);
					x.set(i, (float) mySpatialCoordinateX[i+1]);
				}


				for (int i = 0; i < zDualDim.getLength(); i++) {
					dualX.set(i, (float) myDualSpatialCoordinateX[i+1]);
					dualZ.set(i, (float) myDualSpatialCoordinateZ[i+1]);
				}

				// These data are those created by bufferWriter class. This will write our hydraulic head (psi) and
				// adimensional water content (theta) data
				ArrayDouble.D2 dataTheta = new ArrayDouble.D2(NREC, xDim.getLength());
				ArrayDouble.D2 dataSaturationDegree = new ArrayDouble.D2(NREC, xDim.getLength());
				ArrayDouble.D2 dataPsi = new ArrayDouble.D2(NREC, xDim.getLength());
				ArrayDouble.D2 dataDarcyVelocities = new ArrayDouble.D2(NREC, xDualDim.getLength());
				ArrayDouble.D2 dataDarcyVelocitiesX = new ArrayDouble.D2(NREC, xDualDim.getLength());
				ArrayDouble.D2 dataDarcyVelocitiesZ = new ArrayDouble.D2(NREC, xDualDim.getLength());
//				ArrayDouble.D2 dataDarcyVelocitiesCapillary = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
//				ArrayDouble.D2 dataDarcyVelocitiesGravity = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
//				ArrayDouble.D2 dataPoreVelocities = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
//				ArrayDouble.D2 dataCelerity = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
//				ArrayDouble.D2 dataKinematicRatio = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
//				ArrayDouble.D1 dataError =  new ArrayDouble.D1(NREC);
//				ArrayDouble.D1 dataTopBC =  new ArrayDouble.D1(NREC);
//				ArrayDouble.D1 dataBottomBC =  new ArrayDouble.D1(NREC);
//				ArrayDouble.D1 dataRunOff =  new ArrayDouble.D1(NREC);

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
					for (int n = 0; n < myTempVariable.length-1; n++) {

						dataPsiIC.set(n, myTempVariable[n+1]);

					}
					
					myTempVariable =  entry.getValue().get(1);
					for (int n = 0; n < myTempVariable.length-1; n++) {

						dataPsi.set(i, n, myTempVariable[n+1]);

					}

					myTempVariable =  entry.getValue().get(2);
					for (int n = 0; n < myTempVariable.length-1; n++) {

						dataTheta.set(i, n, myTempVariable[n+1]);

					}
					
					myTempVariable =  entry.getValue().get(3);
					for (int n = 0; n < myTempVariable.length-1; n++) {

						dataSaturationDegree.set(i, n, myTempVariable[n+1]);

					}

					myTempVariable =  entry.getValue().get(4);
					for (int n = 0; n < myTempVariable.length-1; n++) {

						dataDarcyVelocities.set(i, n, myTempVariable[n+1]);

					}

					myTempVariable =  entry.getValue().get(5);
					for (int n = 0; n < myTempVariable.length-1; n++) {

						dataDarcyVelocitiesX.set(i, n, myTempVariable[n+1]);

					}

					myTempVariable =  entry.getValue().get(6);
					for (int n = 0; n < myTempVariable.length-1; n++) {

						dataDarcyVelocitiesZ.set(i, n, myTempVariable[n+1]);

					}
//
//					myTempVariable =  entry.getValue().get(7);
//					for (int lvl = 0; lvl < dualNLVL; lvl++) {
//
//						dataDarcyVelocitiesGravity.set(i,lvl, myTempVariable[lvl]);
//
//					}
//
//					myTempVariable =  entry.getValue().get(8);
//					for (int lvl = 0; lvl < dualNLVL; lvl++) {
//
//						dataPoreVelocities.set(i,lvl, myTempVariable[lvl]);
//
//					}
//
//					myTempVariable =  entry.getValue().get(9);
//					for (int lvl = 0; lvl < dualNLVL; lvl++) {
//
//						dataCelerity.set(i,lvl, myTempVariable[lvl]);
//
//					}
//
//					myTempVariable =  entry.getValue().get(10);
//					for (int lvl = 0; lvl < dualNLVL; lvl++) {
//
//						dataKinematicRatio.set(i,lvl, myTempVariable[lvl]);
//
//					}
//
//					dataError.set(i, entry.getValue().get(11)[0]);
//
//					dataTopBC.set(i, entry.getValue().get(12)[0]);
//
//					dataBottomBC.set(i, entry.getValue().get(13)[0]);
//
//					dataRunOff.set(i, entry.getValue().get(14)[0]);

					i++;
				}


				//Create the file. At this point the (empty) file will be written to disk
				dataFile.create();

				// A newly created Java integer array to be initialized to zeros.
				int[] origin = new int[2];

				dataFile.write(xVar, x);
				dataFile.write(zVar, z);
				dataFile.write(xDualVar, dualX);
				dataFile.write(zDualVar, dualZ);
				dataFile.write(timeVar, origin, times);
				dataFile.write(psiVar, origin, dataPsi);
				dataFile.write(thetaVar, origin, dataTheta);
				dataFile.write(saturationDegreeVar, origin, dataSaturationDegree);
				dataFile.write(iCVar, origin, dataPsiIC);
				dataFile.write(darcyVelocitiesVar, origin, dataDarcyVelocities);
				dataFile.write(darcyVelocitiesXVar, origin, dataDarcyVelocitiesX);
				dataFile.write(darcyVelocitiesZVar, origin, dataDarcyVelocitiesZ);
//				dataFile.write(darcyVelocitiesCapillaryVar, origin, dataDarcyVelocitiesCapillary);
//				dataFile.write(darcyVelocitiesGravityVar, origin, dataDarcyVelocitiesGravity);
//				dataFile.write(poreVelocitiesVar, origin, dataPoreVelocities);
//				dataFile.write(celerityVar, origin, dataCelerity);
//				dataFile.write(kinematicRatioVar, origin, dataKinematicRatio);
//				dataFile.write(errorVar, origin, dataError);
//				dataFile.write(topBCVar, origin, dataTopBC);
//				dataFile.write(bottomBCVar, origin, dataBottomBC);
//				dataFile.write(runOffVar, origin, dataRunOff);

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

			System.out.println("*** SUCCESS writing Richards' 1D output file, " + fileName);


		}


	}

}

