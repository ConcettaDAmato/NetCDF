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
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.ArrayDouble.D3;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

@Description("This class writes a NetCDF with Richards' equation outputs. Before writing, outputs are stored in a buffer writer"
		+ " and as simulation is ended they are written in a NetCDF file.")
@Documentation("")
@Author(name = "Niccolo' Tubini, Riccardo Rigon", contact = "tubini.niccolo@gmail.com")
@Keywords("Hydrology, Richards, Infiltration")
//@Label(JGTConstants.HYDROGEOMORPHOLOGY)
//@Name("shortradbal")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class WriteNetCDFRichards2D2 {

	@Description()
	@In
	@Unit ()
	public String timeUnits = "Seconds since 01/01/1970 00:00:00 UTC";

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

	@In
	public int writeFrequency = 1;

	@Description()
	@In
	@Unit ()
	public String fileName;

	@Description("Brief descritpion of the problem")
	@In
	@Unit ()
	public String briefDescritpion;

	@In
	public String boundaryCondition = " ";
	@In
	public String pathBC = " ";
	@In
	public String pathGrid = " ";
	@In
	public String timeDelta = " ";
	@In
	public String picardIterationNumber = "1";
	@In
	public String swrcModel = " ";
	@In
	public String soilHydraulicConductivityModel = " ";
	@In
	public String interfaceThermalConductivityModel = " ";

	@Description("Boolean variable to print output file only at the end of the simulation")
	@In
	@Unit ()
	public boolean doProcess;

	@Description("Name of the variables to save")
	@In
	@Unit ()
	public String [] outVariables = new String[]{""};

	double[] tempVariable; 
	Iterator it;

	DateFormat dateFormat;
	Date date = null;
	String filename;
	NetcdfFileWriter dataFile;
	int KMAX;
	int DUALKMAX;
	int NREC;
	int[] origin;
	int[] dual_origin;
	int[] time_origin;
	int i;
	Dimension zDim;
	Dimension xDim;
	Dimension zDualDim;	
	Dimension xDualDim;
	Dimension timeDim;
	Array times;
	String dims;
	String dualDims;
	List<String> outVariablesList;

	Variable timeVar;
	Variable zVar;
	Variable xVar;
	Variable zDualVar;
	Variable xDualVar;
	Variable psiVar;
	Variable icVar;
	Variable thetaVar;
	Variable saturationDegreeVar;
	Variable darcyVelocitiesVar;
	Variable darcyVelocitiesXVar;
	Variable darcyVelocitiesZVar;
	//	Variable darcyVelocitiesCapillaryVar;
	//	Variable darcyVelocitiesGravityVar;
	//	Variable poreVelocitiesVar;
	//	Variable celerityVar;
	//	Variable kinematicRatioVar;
	Variable errorVar;
	//	Variable topBCVar;
	//	Variable bottomBCVar;
	//	Variable runOffVar;

	ArrayDouble.D1 dataPsiIC;
	ArrayDouble.D1 dataError;
//	ArrayDouble.D1 dataTopBC;
//	ArrayDouble.D1 dataBottomBC;
//	ArrayDouble.D1 dataRunOff;

	ArrayDouble.D2 dataPsi;
	ArrayDouble.D2 dataTheta;
	ArrayDouble.D2 dataSaturationDegree;
	ArrayDouble.D2 dataDarcyVelocities;
	ArrayDouble.D2 dataDarcyVelocitiesX;
	ArrayDouble.D2 dataDarcyVelocitiesZ;
	//	D3 dataDarcyVelocitiesCapillary;
	//	D3 dataDarcyVelocitiesGravity;
	//	D3 dataPoreVelocities;
	//	D3 dataCelerity;
	//	D3 dataKinematicRatio;


	int step = 0;


	@Execute
	public void writeNetCDF() throws IOException {

		/*
		 * Create a new file
		 */
		if(step == 0) {

			outVariablesList = Arrays.asList(outVariables);

			final int NX = mySpatialCoordinateX.length-1;
			final int NZ = mySpatialCoordinateZ.length-1;

			final int dualNX = myDualSpatialCoordinateX.length-1;
			final int dualNZ = myDualSpatialCoordinateZ.length-1;
			final int NREC = myVariables.keySet().size();
			// human readable date will be converted in unix format, the format will be an input and it has to be consistent with that used in OMS
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			date = null;
			
			origin = new int[]{0, 0};
			dual_origin = new int[]{0, 0};
			time_origin = new int[]{0};

			dataFile = null;

			try {
				// Create new netcdf-3 file with the given filename
				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName);
				// add a general attribute describing the problem and containing other relevant information for the user
				dataFile.addGroupAttribute(null, new Attribute("Description of the problem",briefDescritpion));
				dataFile.addGroupAttribute(null, new Attribute("Boundary condition",boundaryCondition));
				dataFile.addGroupAttribute(null, new Attribute("Path of boundary condition file",pathBC));
				dataFile.addGroupAttribute(null, new Attribute("Path of the mesh",pathGrid));			
				dataFile.addGroupAttribute(null, new Attribute("Integration time step",timeDelta));
				dataFile.addGroupAttribute(null, new Attribute("Number of Picard iteration",picardIterationNumber));
				dataFile.addGroupAttribute(null, new Attribute("Swrc model",swrcModel));
				dataFile.addGroupAttribute(null, new Attribute("Soil hydraulic conductivity model",soilHydraulicConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("Interface thermal conductivity model",interfaceThermalConductivityModel));

				//add dimensions  where time dimension is unlimit
				// in 1D case dimension are time and the depth
				zDim = dataFile.addDimension(null, "z", NZ);
				xDim = dataFile.addDimension(null, "x", NX);
				xDualDim = dataFile.addDimension(null, "xDual", dualNX);
				zDualDim = dataFile.addDimension(null, "zDual", dualNZ);
				timeDim = dataFile.addUnlimitedDimension("time");

				// Define the coordinate variables.
				zVar = dataFile.addVariable(null, "z", DataType.DOUBLE, "z");
				xVar = dataFile.addVariable(null, "x", DataType.DOUBLE, "x");
				xDualVar = dataFile.addVariable(null, "xDual", DataType.DOUBLE, "xDual");
				zDualVar = dataFile.addVariable(null, "zDual", DataType.DOUBLE, "zDual");
				timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");

				// Define units attributes for data variables.
				dataFile.addVariableAttribute(zVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(zVar, new Attribute("long_name", "z coordinate, positive upward"));
				dataFile.addVariableAttribute(xVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(xVar, new Attribute("long_name", "x coordinate"));
				dataFile.addVariableAttribute(xDualVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(xDualVar, new Attribute("long_name", "x coordinate of the middle point of the edges"));
				dataFile.addVariableAttribute(zDualVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(zDualVar, new Attribute("long_name", "z coordinate of the middle point of the edges"));
				dataFile.addVariableAttribute(timeVar, new Attribute("units", timeUnits));

				// Define the netCDF variables for the psi and theta data.
				dims = "time x";
				dualDims = "time xDual";


				psiVar = dataFile.addVariable(null, "psi", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(psiVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(psiVar, new Attribute("long_name", "Water suction"));

				icVar = dataFile.addVariable(null, "psi_ic", DataType.DOUBLE, "x z");
				dataFile.addVariableAttribute(icVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(icVar, new Attribute("long_name", "Initial condition for water suction"));

				thetaVar = dataFile.addVariable(null, "theta", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(thetaVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(thetaVar, new Attribute("long_name", "Adimensional water content"));

				saturationDegreeVar = dataFile.addVariable(null, "saturation_degree", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(saturationDegreeVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(saturationDegreeVar, new Attribute("long_name", "Saturation degree"));

				if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("all")) {
					darcyVelocitiesVar = dataFile.addVariable(null, "darcy_velocity", DataType.DOUBLE, dualDims);
					dataFile.addVariableAttribute(darcyVelocitiesVar, new Attribute("units", "m/s"));
					dataFile.addVariableAttribute(darcyVelocitiesVar, new Attribute("long_name", "Darcy velocity"));
				}

				if (outVariablesList.contains("darcy_velocity_components") || outVariablesList.contains("all")) {
					darcyVelocitiesXVar = dataFile.addVariable(null, "darcy_velocity_x", DataType.DOUBLE, dualDims);
					dataFile.addVariableAttribute(darcyVelocitiesXVar, new Attribute("units", "m/s"));
					dataFile.addVariableAttribute(darcyVelocitiesXVar, new Attribute("long_name", "Darcy velocity component on x"));

					darcyVelocitiesZVar = dataFile.addVariable(null, "darcy_velocity_z", DataType.DOUBLE, dualDims);
					dataFile.addVariableAttribute(darcyVelocitiesZVar, new Attribute("units", "m/s"));
					dataFile.addVariableAttribute(darcyVelocitiesZVar, new Attribute("long_name", "Darcy velocity component on z"));

				}

				//				Variable darcyVelocitiesCapillaryVar = dataFile.addVariable(null, "darcyVelocitiesCapillary", DataType.DOUBLE, dualDims);
				//				Variable darcyVelocitiesGravityVar = dataFile.addVariable(null, "darcyVelocitiesGravity", DataType.DOUBLE, dualDims);
				//				Variable poreVelocitiesVar = dataFile.addVariable(null, "poreVelocities", DataType.DOUBLE, dualDims);
				//				Variable celerityVar = dataFile.addVariable(null, "celerities", DataType.DOUBLE, dualDims);
				//				Variable kinematicRatioVar = dataFile.addVariable(null, "kinematicRatio", DataType.DOUBLE, dualDims);
				//				Variable topBCVar = dataFile.addVariable(null, "topBC", DataType.DOUBLE, "time");
				//				Variable bottomBCVar = dataFile.addVariable(null, "bottomBC", DataType.DOUBLE, "time");
				//				Variable runOffVar = dataFile.addVariable(null, "runOff", DataType.DOUBLE, "time");

				Variable errorVar = dataFile.addVariable(null, "error", DataType.DOUBLE, "time");
				dataFile.addVariableAttribute(errorVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(errorVar, new Attribute("long_name", "Volume error at each time step"));

				// Define units attributes for data variables.


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
				ArrayDouble.D1 xDual = new ArrayDouble.D1(xDualDim.getLength());
				ArrayDouble.D1 zDual = new ArrayDouble.D1(zDualDim.getLength());

				//				ArrayDouble.D1 dataPsiIC = new ArrayDouble.D1(lvlDim.getLength());
				//				Array times = Array.factory(DataType.LONG, new int[] {NREC});

				for (int i = 0; i < zDim.getLength(); i++) {
					z.set(i, (float) mySpatialCoordinateZ[i+1]);
					x.set(i, (float) mySpatialCoordinateX[i+1]);
				}


				for (int i = 0; i < zDualDim.getLength(); i++) {
					xDual.set(i, (float) myDualSpatialCoordinateX[i+1]);
					zDual.set(i, (float) myDualSpatialCoordinateZ[i+1]);
				}

				//Create the file. At this point the (empty) file will be written to disk
				dataFile.create();
				dataFile.write(xVar, x);
				dataFile.write(zVar, z);
				dataFile.write(zDualVar, zDual);

				System.out.println("\n\t***Created NetCDF " + fileName +"\n\n");
			} catch (InvalidRangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (dataFile != null)
					try {
						dataFile.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
			}

		}

		/*
		 * Write data
		 */

		if( step%writeFrequency==0 || doProcess == false) {

			try {

				dataFile = NetcdfFileWriter.openExisting(fileName);

				// number of time record that will be saved
				NREC = myVariables.keySet().size();

				times = Array.factory(DataType.INT, new int[] {NREC});

				dataPsi = new ArrayDouble.D2(NREC, dataFile.findVariable("psi").getShape()[1]);
				dataPsiIC = new ArrayDouble.D1( dataFile.findVariable("psi_ic").getShape()[0]);	
				dataTheta = new ArrayDouble.D2(NREC, dataFile.findVariable("theta").getShape()[1]);
				dataSaturationDegree = new ArrayDouble.D2(NREC, dataFile.findVariable("saturation_degree").getShape()[1]);
				if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("all")) {
					dataDarcyVelocities = new ArrayDouble.D2(NREC, dataFile.findVariable("darcy_velocity").getShape()[1]);
				}
				if (outVariablesList.contains("darcy_velocity_components") || outVariablesList.contains("all")) {
					dataDarcyVelocitiesX = new ArrayDouble.D2(NREC, dataFile.findVariable("darcy_velocity_x").getShape()[1]);				
					dataDarcyVelocitiesX = new ArrayDouble.D2(NREC, dataFile.findVariable("darcy_velocity_z").getShape()[1]);
				}	

				dataError =  new ArrayDouble.D1(NREC);
				/*
				 * FIXME: complete the remaining variables
				 */
				//				ArrayDouble.D2 dataDarcyVelocitiesCapillary = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
				//				ArrayDouble.D2 dataDarcyVelocitiesGravity = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
				//				ArrayDouble.D2 dataPoreVelocities = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
				//				ArrayDouble.D2 dataCelerity = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
				//				ArrayDouble.D2 dataKinematicRatio = new ArrayDouble.D2(NREC, dualLvlDim.getLength());
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

					// think if there is a better way instead of using i
					times.setLong(i, (long) date.getTime()/1000);

					if(step==0) {
						tempVariable =  entry.getValue().get(0);
						for (int n = 0; n < tempVariable.length-1; n++) {

							dataPsiIC.set(n, tempVariable[n+1]);
//							dataPsiIC.set(n,  -9999);


						}
					}


					tempVariable =  entry.getValue().get(1);
					for (int n = 0; n < tempVariable.length-1; n++) {

						dataPsi.set(i, n, tempVariable[n+1]);

					}

					tempVariable =  entry.getValue().get(2);
					for (int n = 0; n < tempVariable.length-1; n++) {

						dataTheta.set(i, n, tempVariable[n+1]);

					}

					tempVariable =  entry.getValue().get(3);
					for (int n = 0; n < tempVariable.length-1; n++) {

						dataSaturationDegree.set(i, n, tempVariable[n+1]);

					}

					if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("all")) {
						tempVariable =  entry.getValue().get(4);
						for (int n = 0; n < tempVariable.length-1; n++) {

							dataDarcyVelocities.set(i, n, tempVariable[n+1]);

						}
					}

					if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("all")) {
						tempVariable =  entry.getValue().get(5);
						for (int n = 0; n < tempVariable.length-1; n++) {

							dataDarcyVelocitiesX.set(i, n, tempVariable[n+1]);

						}


						tempVariable =  entry.getValue().get(6);
						for (int n = 0; n < tempVariable.length-1; n++) {

							dataDarcyVelocitiesZ.set(i, n, tempVariable[n+1]);

						}
					}

					dataError.set(i, entry.getValue().get(7)[0]);


					/*
					 * FIXME: complete the remaining variables.
					 */
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
					//
					//					dataTopBC.set(i, entry.getValue().get(12)[0]);
					//
					//					dataBottomBC.set(i, entry.getValue().get(13)[0]);
					//
					//					dataRunOff.set(i, entry.getValue().get(14)[0]);

					i++;
				}


				// A newly created Java integer array to be initialized to zeros.
				origin[0] = dataFile.findVariable("psi").getShape()[0];
//				dual_origin[0] = dataFile.findVariable("darcy_velocity").getShape()[0];
				time_origin[0] = dataFile.findVariable("time").getShape()[0];


				dataFile.write(dataFile.findVariable("time"), time_origin, times);
				dataFile.write(dataFile.findVariable("psi"), origin, dataPsi);
				if(step==0) {
					dataFile.write(dataFile.findVariable("psi_ic"), origin, dataPsiIC);
				}
				dataFile.write(dataFile.findVariable("theta"), origin, dataTheta);
				dataFile.write(dataFile.findVariable("saturation_degree"), origin, dataSaturationDegree);
				if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("all")) {
					dataFile.write(dataFile.findVariable("darcy_velocity"), origin, dataDarcyVelocities);
				}
				if (outVariablesList.contains("darcy_velocity_components") || outVariablesList.contains("all")) {
					dataFile.write(dataFile.findVariable("darcy_velocity_x"), origin, dataDarcyVelocitiesX);
					dataFile.write(dataFile.findVariable("darcy_velocity_z"), origin, dataDarcyVelocitiesZ);
				}
				dataFile.write(dataFile.findVariable("error"), time_origin, dataError);

				/*
				 * FIXME: complete the remaining variables.
				 */
				//				dataFile.write(darcyVelocitiesVar, origin, dataDarcyVelocities);
				//				dataFile.write(darcyVelocitiesXVar, origin, dataDarcyVelocitiesX);
				//				dataFile.write(darcyVelocitiesZVar, origin, dataDarcyVelocitiesZ);
				//				dataFile.write(darcyVelocitiesCapillaryVar, origin, dataDarcyVelocitiesCapillary);
				//				dataFile.write(darcyVelocitiesGravityVar, origin, dataDarcyVelocitiesGravity);
				//				dataFile.write(poreVelocitiesVar, origin, dataPoreVelocities);
				//				dataFile.write(celerityVar, origin, dataCelerity);
				//				dataFile.write(kinematicRatioVar, origin, dataKinematicRatio);
				//				dataFile.write(errorVar, origin, dataError);
				//				dataFile.write(topBCVar, origin, dataTopBC);
				//				dataFile.write(bottomBCVar, origin, dataBottomBC);
				//				dataFile.write(runOffVar, origin, dataRunOff);

				System.out.println("\t*** " + myVariables.keySet().toArray()[i-1].toString() +", writing output file \n");
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


		}

		step++;
	}

}

