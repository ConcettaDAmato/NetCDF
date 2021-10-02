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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
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

public class WriteNetCDFRichards2D {

	@Description()
	@In
	@Unit ()
	public String timeUnits = "Minutes since 01/01/1970 00:00:00 UTC";
	
	@Description("Time zone used to convert dates in UNIX time")
	@In
	@Unit()
	public String timeZone = "UTC";


	@Description()
	@In
	@Unit ()
	public LinkedHashMap<String,ArrayList<ArrayList<Double>>> variables; // consider the opportunity to save varibale as float instead of double

	@Description()
	@In
	@Unit ()
	public List<Double[]> spatialCoordinate;

	@Description()
	@In
	@Unit ()
	public List<Double[]> dualSpatialCoordinate;
	
	@Description("Initial condition for water suction.")
	@In
	@Unit ()
	public List<Double> psiIC;
	
	@Description("Temperature.")
	@In
	@Unit ()
	public List<Double> temperature;

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
	public String interfaceHydraulicConductivityModel = " ";

	@Description("Boolean variable to print output file only at the end of the simulation")
	@In
	@Unit ()
	public boolean doProcess;
	
	@Description("Maximum allowed file size")
	@In
	@Unit ("MB")
	public double fileSizeMax = 10000;

	@Description("Name of the variables to save")
	@In
	@Unit ()
	public String [] outVariables = new String[]{""};

	private ArrayList<Double> tempVariable; 
	private Iterator it;

	private DateFormat dateFormat;
	private Date date = null;
	private String filename;
	private NetcdfFileWriter dataFile;
	private String fileNameToSave;
	private int NREC;
	private int[] origin;
	private int[] time_origin;
	private int i;
	private int origin_counter;
	private int fileNumber = 0;
	private double fileSizeMB;
	private Dimension xDim;
	private Dimension zDim;
	private Dimension xDualDim;
	private Dimension zDualDim;
	private Dimension timeDim;
	private Array times;
	private String dims;
	private String dualDims;
	private List<String> outVariablesList;

	private Variable timeVar;
	private Variable zVar;
	private Variable xVar;
	private Variable zDualVar;
	private Variable xDualVar;
	private Variable psiVar;
	private Variable temperatureVar;
	private Variable icVar;
	private Variable thetaVar;
	private Variable waterVolumeVar;
	private Variable saturationDegreeVar;
	private Variable darcyVelocitiesVar;
	private Variable darcyVelocitiesXVar;
	private Variable darcyVelocitiesZVar;
	private Variable errorVar;
	
	
	private ArrayDouble.D1 z;
	private ArrayDouble.D1 x;
	private ArrayDouble.D1 xDual;
	private ArrayDouble.D1 zDual;
	private ArrayDouble.D1 dataPsiIC;
	private ArrayDouble.D1 dataTemperature;
	private ArrayDouble.D1 dataError;

	private ArrayDouble.D2 dataPsi;
	private ArrayDouble.D2 dataTheta;
	private ArrayDouble.D2 dataWaterVolume;
	private ArrayDouble.D2 dataSaturationDegree;
	private ArrayDouble.D2 dataDarcyVelocities;
	private ArrayDouble.D2 dataDarcyVelocitiesX;
	private ArrayDouble.D2 dataDarcyVelocitiesZ;


	private int step = 0;
	private int stepCreation = 0;

	@Execute
	public void writeNetCDF() throws IOException {

		final int NX = spatialCoordinate.size()-1;

		final int dualNX = dualSpatialCoordinate.size()-1;
		
		/*
		 * Create a new file
		 */
		if(stepCreation == 0) {

			origin_counter = 0;
			outVariablesList = Arrays.asList(outVariables);


			final int NREC = variables.keySet().size();
			// human readable date will be converted in unix format, the format will be an input and it has to be consistent with that used in OMS
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			date = null;
			
			origin = new int[]{0, 0};
			time_origin = new int[]{0};

			dataFile = null;
			
			fileNameToSave = fileName.substring(0,fileName.length()-3) + '_' + String.format("%04d", fileNumber) + fileName.substring(fileName.length()-3,fileName.length());

			try {
				// Create new netcdf-3 file with the given filename
				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileNameToSave);
				// add a general attribute describing the problem and containing other relevant information for the user
				dataFile.addGroupAttribute(null, new Attribute("Description of the problem",briefDescritpion));
				dataFile.addGroupAttribute(null, new Attribute("Boundary condition",boundaryCondition));
				dataFile.addGroupAttribute(null, new Attribute("Path of boundary condition file",pathBC));
				dataFile.addGroupAttribute(null, new Attribute("Path of the mesh",pathGrid));			
				dataFile.addGroupAttribute(null, new Attribute("Integration time step",timeDelta));
				dataFile.addGroupAttribute(null, new Attribute("Number of Picard iteration",picardIterationNumber));
				dataFile.addGroupAttribute(null, new Attribute("Swrc model",swrcModel));
				dataFile.addGroupAttribute(null, new Attribute("Soil hydraulic conductivity model",soilHydraulicConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("Interface thermal conductivity model",interfaceHydraulicConductivityModel));

				//add dimensions  where time dimension is unlimited		
				zDim = dataFile.addDimension(null, "z", NX);
				xDim = dataFile.addDimension(null, "x", NX);
				xDualDim = dataFile.addDimension(null, "xDual", dualNX);
				zDualDim = dataFile.addDimension(null, "zDual", dualNX);
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
				dataFile.addVariableAttribute(timeVar, new Attribute("long_name", "Time."));


				// Define the netCDF variables for the psi and theta data.
				dims = "time x";
				dualDims = "time xDual";


				psiVar = dataFile.addVariable(null, "psi", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(psiVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(psiVar, new Attribute("long_name", "Water suction"));

				icVar = dataFile.addVariable(null, "psiIC", DataType.DOUBLE, "x");
				dataFile.addVariableAttribute(icVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(icVar, new Attribute("long_name", "Initial condition for water suction"));

				temperatureVar = dataFile.addVariable(null, "T", DataType.DOUBLE, "x");
				dataFile.addVariableAttribute(temperatureVar, new Attribute("units", "K"));
				dataFile.addVariableAttribute(temperatureVar, new Attribute("long_name", "Temperature"));
				
				thetaVar = dataFile.addVariable(null, "theta", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(thetaVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(thetaVar, new Attribute("long_name", "Adimensional water content"));
				
				waterVolumeVar = dataFile.addVariable(null, "waterVolume", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(thetaVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(thetaVar, new Attribute("long_name", "Water volume"));

				saturationDegreeVar = dataFile.addVariable(null, "saturationDegree", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(saturationDegreeVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(saturationDegreeVar, new Attribute("long_name", "Saturation degree"));

				if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("all")) {
					darcyVelocitiesVar = dataFile.addVariable(null, "darcyVelocity", DataType.DOUBLE, dualDims);
					dataFile.addVariableAttribute(darcyVelocitiesVar, new Attribute("units", "m/s"));
					dataFile.addVariableAttribute(darcyVelocitiesVar, new Attribute("long_name", "Darcy velocity"));
				}

				if (outVariablesList.contains("darcy_velocity_components") || outVariablesList.contains("all")) {
					darcyVelocitiesXVar = dataFile.addVariable(null, "darcyVelocityX", DataType.DOUBLE, dualDims);
					dataFile.addVariableAttribute(darcyVelocitiesXVar, new Attribute("units", "m/s"));
					dataFile.addVariableAttribute(darcyVelocitiesXVar, new Attribute("long_name", "Darcy velocity component on x"));

					darcyVelocitiesZVar = dataFile.addVariable(null, "darcyVelocityZ", DataType.DOUBLE, dualDims);
					dataFile.addVariableAttribute(darcyVelocitiesZVar, new Attribute("units", "m/s"));
					dataFile.addVariableAttribute(darcyVelocitiesZVar, new Attribute("long_name", "Darcy velocity component on z"));

				}

				Variable errorVar = dataFile.addVariable(null, "errorVolume", DataType.DOUBLE, "time");
				dataFile.addVariableAttribute(errorVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(errorVar, new Attribute("long_name", "Volume error at each time step"));


				z = new ArrayDouble.D1(NX);
				x = new ArrayDouble.D1(NX);
				xDual = new ArrayDouble.D1(dualNX);
				zDual = new ArrayDouble.D1(dualNX);

				dataPsiIC = new ArrayDouble.D1(NX);
				dataTemperature = new ArrayDouble.D1(NX);
				
				for (int i = 1; i <= NX; i++) {
					
					z.set(i-1, spatialCoordinate.get(i)[1]);
					x.set(i-1, spatialCoordinate.get(i)[0]);
					dataPsiIC.set(i-1, psiIC.get(i));
					dataTemperature.set(i-1, temperature.get(i));

				}


				for (int i = 1; i <= dualNX; i++) {
					
					xDual.set(i-1, dualSpatialCoordinate.get(i)[0]);
					zDual.set(i-1, dualSpatialCoordinate.get(i)[1]);
					
				}

				//Create the file. At this point the (empty) file will be written to disk
				dataFile.create();
				dataFile.write(xVar, x);
				dataFile.write(zVar, z);
				dataFile.write(xDualVar, xDual);
				dataFile.write(zDualVar, zDual); 
				dataFile.write(icVar, dataPsiIC);
				dataFile.write(temperatureVar, dataTemperature);
				stepCreation = 1;
				System.out.println("\n\t***Created NetCDF " + fileNameToSave +"\n\n");
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

				dataFile = NetcdfFileWriter.openExisting(fileNameToSave);

				// number of time record that will be saved
				NREC = variables.keySet().size();

				times = Array.factory(DataType.INT, new int[] {NREC});

				dataPsi = new ArrayDouble.D2(NREC, NX);
				dataTheta = new ArrayDouble.D2(NREC, NX);
				dataWaterVolume = new ArrayDouble.D2(NREC, NX);
				dataSaturationDegree = new ArrayDouble.D2(NREC, NX);
				
				if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("darcy velocity") || outVariablesList.contains("all")) {
					dataDarcyVelocities = new ArrayDouble.D2(NREC, NX);
				}
				if (outVariablesList.contains("darcy_velocity_components") || outVariablesList.contains("darcy velocity components") || outVariablesList.contains("all")) {
					dataDarcyVelocitiesX = new ArrayDouble.D2(NREC, NX);				
					dataDarcyVelocitiesX = new ArrayDouble.D2(NREC, NX);
				}	

				dataError =  new ArrayDouble.D1(NREC);

				int i=0;
				it = variables.entrySet().iterator();
				while (it.hasNext()) {

					@SuppressWarnings("unchecked")
					Entry<String, ArrayList<ArrayList<Double>>> entry = (Entry<String, ArrayList<ArrayList<Double>>>) it.next();

					try {
						
						date = dateFormat.parse(entry.getKey());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					times.setLong(i, (long) date.getTime()/(60*1000));

					
					tempVariable =  entry.getValue().get(0);
					for (int n = 1; n < tempVariable.size(); n++) {
						
							dataPsi.set(i, n-1, tempVariable.get(n));

						}

					tempVariable = entry.getValue().get(1);
					for (int n = 1; n < tempVariable.size(); n++) {

						dataTheta.set(i, n-1, tempVariable.get(n));

					}

					tempVariable = entry.getValue().get(2);
					for (int n = 1; n < tempVariable.size(); n++) {

						dataWaterVolume.set(i, n-1, tempVariable.get(n));

					}

					tempVariable =  entry.getValue().get(3);
					for (int n = 1; n < tempVariable.size(); n++) {

						dataSaturationDegree.set(i, n-1, tempVariable.get(n));

					}

					if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("darcy velocity") || outVariablesList.contains("all")) {
						tempVariable =  entry.getValue().get(4);
						for (int n = 1; n < tempVariable.size(); n++) {

							dataDarcyVelocities.set(i, n-1, tempVariable.get(n));

						}
					}

					if (outVariablesList.contains("darcy_velocity_components") || outVariablesList.contains("darcy velocity components") || outVariablesList.contains("all")) {
						tempVariable =  entry.getValue().get(5);
						for (int n = 1; n < tempVariable.size(); n++) {

							dataDarcyVelocitiesX.set(i, n-1, tempVariable.get(n));

						}


						tempVariable =  entry.getValue().get(6);
						for (int n = 1; n < tempVariable.size(); n++) {

							dataDarcyVelocitiesZ.set(i, n-1, tempVariable.get(n));

						}
					}

					dataError.set(i, entry.getValue().get(7).get(0));


					i++;
				}


				// A newly created Java integer array to be initialized to zeros.
				origin[0] = origin_counter; //dataFile.findVariable("psi").getShape()[0];
				time_origin[0] = origin_counter; //dataFile.findVariable("time").getShape()[0];


				dataFile.write(dataFile.findVariable("time"), time_origin, times);
				dataFile.write(dataFile.findVariable("psi"), origin, dataPsi);
				dataFile.write(dataFile.findVariable("theta"), origin, dataTheta);
				dataFile.write(dataFile.findVariable("waterVolume"), origin, dataWaterVolume);
				dataFile.write(dataFile.findVariable("saturationDegree"), origin, dataSaturationDegree);
				if (outVariablesList.contains("darcy_velocity") || outVariablesList.contains("darcy velocity") || outVariablesList.contains("all")) {
					dataFile.write(dataFile.findVariable("darcyVelocity"), origin, dataDarcyVelocities);
				}
				if (outVariablesList.contains("darcy_velocity_components") || outVariablesList.contains("darcy velocity components") || outVariablesList.contains("all")) {
					dataFile.write(dataFile.findVariable("darcyVelocityX"), origin, dataDarcyVelocitiesX);
					dataFile.write(dataFile.findVariable("darcyVelocityZ"), origin, dataDarcyVelocitiesZ);
				}
				dataFile.write(dataFile.findVariable("errorVolume"), time_origin, dataError);

				origin_counter = origin_counter + NREC;
				
				fileSizeMB = (2*NX + 2*dualNX)*4 + (2*NX + 3*NX*origin_counter)*8;
				if(outVariablesList.contains("darcy_velocity") || outVariablesList.contains("darcy velocity")) {
					fileSizeMB += dualNX*8*origin_counter;
				}
				if(outVariablesList.contains("darcy_velocity_components") || outVariablesList.contains("darcy velocity components") ) {
					fileSizeMB += 2*dualNX*8*origin_counter;
				}
				if(outVariablesList.contains("all")){
					fileSizeMB += dualNX*8*origin_counter+2*dualNX*8*origin_counter;
				}
				stepCreation ++;
				if(fileSizeMB/1000000>fileSizeMax) {
					
					stepCreation = 0;
					fileNumber++;
					NREC = 0;
					
				}
					
				if(!variables.isEmpty()) {
					
					System.out.println("\t\t*** " + variables.keySet().toArray()[i-1].toString() +", writing output file: " + fileNameToSave + "\n");
				
				}

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

