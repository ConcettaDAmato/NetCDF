/*
 * GNU GPL v3 License
 *
 * Copyright 2018 Niccolo` Tubini
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

package it.geoframe.blogpsot.netcdf.monodimensionalproblemtimedependent;

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
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.License;
import oms3.annotations.Unit;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.ArrayDouble.D1;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

@Description("This class writes a NetCDF with solute advection-dispersion equation outputs. Before writing, outputs are stored in a buffer writer"
		+ " and as simulation is ended they are written in a NetCDF file.")
@Documentation("")
@Author(name = "Concetta D'Amato, Niccolo' Tubini, Riccardo Rigon", contact = "concettadamato94@gmail.com")
@Keywords("Hydrology, solute transport equation, advection-dispersion")
//@Label(JGTConstants.HYDROGEOMORPHOLOGY)
//@Name("shortradbal")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")


public class WriteNetCDFSoluteAdvectionDispersion1D {

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
	public LinkedHashMap<String,ArrayList<double[]>> variables; // consider the opportunity to save varibale as float instead of double

	@Description()
	@In
	@Unit ()
	public double[] spatialCoordinate;

	@Description()
	@In
	@Unit ()
	public double[] dualSpatialCoordinate;

	@Description("Dimension of each control volume.")
	@In
	@Unit ()
	public double[] controlVolume;
	
	@Description("Water suction profile.")
	@In
	@Unit ()
	public double[] psi;
	
	@Description("Initial condition for concentration profile.")
	@In
	@Unit ()
	public double[] concentrationIC;
	
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
	public String topSoluteBC = " ";
	@In
	public String bottomSoluteBC = " ";
	
	public String topRichardsBC = " ";
	@In
	public String bottomRichardsBC = " ";
	@In
	public String pathSoluteTopBC = " ";
	@In
	public String pathSoluteBottomBC = " ";
	@In
	public String pathRichardsTopBC = " ";
	@In
	public String pathRichardsBottomBC = " ";
	@In
	public String pathGrid = " ";
	@In
	public String timeDelta = " ";
	@In
	public String swrcModel = " ";
	@In
	public String soilHydraulicConductivityModel = " ";
	@In
	public String interfaceHydraulicConductivityModel = " ";
	//@In
	//public String soilThermalConductivityModel = " ";
	@In
	public String interfaceDispersionCoefficientModel = " ";


	@Description("Boolean variable to print output file only at the end of the simulation")
	@In
	@Unit ()
	public boolean doProcess;
	
	@Description("Maximum allowed file size")
	@In
	@Unit ()
	public double fileSizeMax = 10000;
	
	@Description("Name of the variables to save")
	@In
	@Unit ()
	public String [] outVariables = new String[]{""};

	double[] tempVariable;
	Iterator it;
	DateFormat dateFormat;
	Date date = null;
	String fileNameToSave;
	String filename;
	NetcdfFileWriter dataFile;
	int KMAX;
	int DUALKMAX;
	int NREC;
	int[] origin;
	int[] dual_origin;
	int[] time_origin;
	int origin_counter;
	int i;
	int fileNumber = 0;
	double fileSizeMB;
	Dimension kDim;
	Dimension dualKDim;
	Dimension timeDim;
	D1 depth;
	D1 dualDepth;
	Array times;
	String dims;
	List<String> outVariablesList;

	Variable timeVar;
	Variable depthVar;
	Variable dualDepthVar;
	Variable psiICVar;
	Variable concentrationICVar;
	Variable psiVar;
	Variable concentrationVar; ////SIAMO ARRIVATI QUA 
	Variable thetaVar;
	Variable internalEnergyVar;
	Variable darcyFluxVar;
	Variable heatFluxVar;
	Variable errorHeatVar;
	Variable errorVolumeVar;
//	Variable topHeatBCVar;
//	Variable bottomHeatBCVar;
//	Variable topRichardsBCVar;
//	Variable bottomRichardsBCVar;
	Variable controlVolumeVar;

	ArrayDouble.D1 dataPsiIC;
	ArrayDouble.D1 dataTemperatureIC;
	ArrayDouble.D1 dataErrorHeat;
	ArrayDouble.D1 dataErrorVolume;
//	ArrayDouble.D1 dataTopHeatBC;
//	ArrayDouble.D1 dataBottomHeatBC;
//	ArrayDouble.D1 dataTopRichardsBC;
//	ArrayDouble.D1 dataBottomRichardsBC;
	ArrayDouble.D1 dataControlVolume;
	
	ArrayDouble.D2 dataTemperature;
	ArrayDouble.D2 dataPsi;
	ArrayDouble.D2 dataTheta;
	ArrayDouble.D2 dataDarcyFlux;
	ArrayDouble.D2 dataHeatFlux;


	int step = 0;
	int stepCreation = 0;

	@Execute
	public void writeNetCDF() throws IOException {


		/*
		 * Create a new file
		 */
		if(stepCreation == 0) {
			
			origin_counter = 0;
			outVariablesList = Arrays.asList(outVariables);
			
			//			System.out.println("WriterNetCDF step:" + step);
			KMAX = spatialCoordinate.length;
			DUALKMAX = dualSpatialCoordinate.length;
			//			NREC = myVariables.keySet().size();

			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			date = null;

			origin = new int[]{0, 0};
			dual_origin = new int[]{0, 0};
			time_origin = new int[]{0};

			dataFile = null;

			fileNameToSave = fileName.substring(0,fileName.length()-3) + '_' + String.format("%04d", fileNumber) + fileName.substring(fileName.length()-3,fileName.length());

			try {
				// Create new netcdf-3 file with the given filename
				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileNameToSave);
				// add a general attribute describing the problem and containing other relevant information for the user
				dataFile.addGroupAttribute(null, new Attribute("Description of the problem",briefDescritpion));
				dataFile.addGroupAttribute(null, new Attribute("Top boundary condition for heat equation",topHeatBC));
				dataFile.addGroupAttribute(null, new Attribute("Bottom boundary condition for heat equation",bottomHeatBC));
				dataFile.addGroupAttribute(null, new Attribute("Top boundary condition for Richards equation",topRichardsBC));
				dataFile.addGroupAttribute(null, new Attribute("Bottom boundary condition for Richards equation",bottomRichardsBC));
				dataFile.addGroupAttribute(null, new Attribute("path top boundary condition for heat equation",pathHeatTopBC));
				dataFile.addGroupAttribute(null, new Attribute("path bottom boundary condition for heat equation",pathHeatBottomBC));
				dataFile.addGroupAttribute(null, new Attribute("path top boundary condition for Richards equation",pathRichardsTopBC));
				dataFile.addGroupAttribute(null, new Attribute("path bottom boundary condition for Richards equation",pathRichardsBottomBC));
				dataFile.addGroupAttribute(null, new Attribute("path grid",pathGrid));			
				dataFile.addGroupAttribute(null, new Attribute("time delta",timeDelta));
				dataFile.addGroupAttribute(null, new Attribute("swrc model",swrcModel));
				dataFile.addGroupAttribute(null, new Attribute("soil hydraulic conductivity model",soilHydraulicConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("interface hydraulic conductivity model",interfaceHydraulicConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("soil thermal conductivity model",soilThermalConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("interface thermal conductivity model",interfaceThermalConductivityModel));

				//add dimensions  where time dimension is unlimit
				// the spatial dimension is defined using just the indexes 
				kDim = dataFile.addDimension(null, "depth", KMAX);
				dualKDim = dataFile.addDimension(null, "dualDepth", DUALKMAX);
				timeDim = dataFile.addUnlimitedDimension("time");

				// Define the coordinate variables.
				depthVar = dataFile.addVariable(null, "depth", DataType.DOUBLE, "depth");
				dualDepthVar = dataFile.addVariable(null, "dualDepth", DataType.DOUBLE, "dualDepth");
				timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");

				// Define units attributes for data variables.
				// Define units attributes for data variables.
				dataFile.addVariableAttribute(timeVar, new Attribute("units", timeUnits));
				dataFile.addVariableAttribute(timeVar, new Attribute("long_name", "Time."));

				dataFile.addVariableAttribute(depthVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(depthVar, new Attribute("long_name", "Soil depth."));

				dataFile.addVariableAttribute(dualDepthVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(dualDepthVar, new Attribute("long_name", "Dual soil depth."));

				// Define the netCDF variables and their attributes.
				String dims = "time depth";
				String dualDims = "time dualDepth";

				psiICVar = dataFile.addVariable(null, "psiIC", DataType.DOUBLE, "depth");
				dataFile.addVariableAttribute(psiICVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(psiICVar, new Attribute("long_name", "Initial condition for water suction."));
				
				temperatureICVar = dataFile.addVariable(null, "temperatureIC", DataType.DOUBLE, "depth");
				dataFile.addVariableAttribute(temperatureICVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(temperatureICVar, new Attribute("long_name", "Initial condition for temperature."));

				psiVar = dataFile.addVariable(null, "psi", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(psiVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(psiVar, new Attribute("long_name", "Water suction."));
				
				temperatureVar = dataFile.addVariable(null, "T", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(temperatureVar, new Attribute("units", "K"));
				dataFile.addVariableAttribute(temperatureVar, new Attribute("long_name", "Temperature."));
				
				thetaVar = dataFile.addVariable(null, "theta", DataType.DOUBLE, dims);
				dataFile.addVariableAttribute(thetaVar, new Attribute("units", " "));
				dataFile.addVariableAttribute(thetaVar, new Attribute("long_name", "theta for within soil and water depth."));

				darcyFluxVar = dataFile.addVariable(null, "darcyFlux", DataType.DOUBLE, dualDims);
				dataFile.addVariableAttribute(darcyFluxVar, new Attribute("units", "m s-1"));
				dataFile.addVariableAttribute(darcyFluxVar, new Attribute("long_name", "Darcy flux."));
				
				heatFluxVar = dataFile.addVariable(null, "heatFlux", DataType.DOUBLE, dualDims);
				dataFile.addVariableAttribute(heatFluxVar, new Attribute("units", "W m-2"));
				dataFile.addVariableAttribute(heatFluxVar, new Attribute("long_name", "Heat flux."));

				
				errorHeatVar = dataFile.addVariable(null, "errorHeat", DataType.DOUBLE, "time");
				dataFile.addVariableAttribute(errorHeatVar, new Attribute("units", "J"));
				dataFile.addVariableAttribute(errorHeatVar, new Attribute("long_name", "Internal energy error at each time step."));
				
				errorVolumeVar = dataFile.addVariable(null, "errorVolume", DataType.DOUBLE, "time");
				dataFile.addVariableAttribute(errorVolumeVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(errorVolumeVar, new Attribute("long_name", "Volume error at each time step."));
				
//				topHeatBCVar  = dataFile.addVariable(null, "topBC for heat", DataType.DOUBLE, "time");
//				dataFile.addVariableAttribute(topHeatBCVar, new Attribute("units", ""));                   //?????
//				dataFile.addVariableAttribute(topHeatBCVar, new Attribute("long_name", "")); //?????
//				
//				bottomHeatBCVar = dataFile.addVariable(null, "bottomHeatBC", DataType.DOUBLE, "time");
//				dataFile.addVariableAttribute(bottomHeatBCVar, new Attribute("units", ""));                 //?????
//				dataFile.addVariableAttribute(bottomHeatBCVar, new Attribute("long_name", "")); //?????
//				
//				topRichardsBCVar  = dataFile.addVariable(null, "topRichardsBC", DataType.DOUBLE, "time");
//				dataFile.addVariableAttribute(topRichardsBCVar, new Attribute("units", ""));                   //?????
//				dataFile.addVariableAttribute(topRichardsBCVar, new Attribute("long_name", "")); //?????
//				
//				bottomRichardsBCVar = dataFile.addVariable(null, "bottomRichardsBC", DataType.DOUBLE, "time");
//				dataFile.addVariableAttribute(bottomRichardsBCVar, new Attribute("units", ""));                 //?????
//				dataFile.addVariableAttribute(bottomRichardsBCVar, new Attribute("long_name", "")); //?????
								
				controlVolumeVar = dataFile.addVariable(null, "controlVolume", DataType.DOUBLE, "depth");
				dataFile.addVariableAttribute(controlVolumeVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(controlVolumeVar, new Attribute("long_name", "dimension of each control volumes"));


				depth = new ArrayDouble.D1(kDim.getLength());
				dualDepth = new ArrayDouble.D1(dualKDim.getLength());
				dataControlVolume = new ArrayDouble.D1(kDim.getLength());
				dataPsiIC = new ArrayDouble.D1(kDim.getLength());
				dataTemperatureIC = new ArrayDouble.D1(kDim.getLength());

				for (int k = 0; k < kDim.getLength(); k++) {
					depth.set(k, spatialCoordinate[k]);
					dataControlVolume.set(k, controlVolume[k]);
					dataPsiIC.set(k, psi[k]);
					dataTemperatureIC.set(k, temperatureIC[k]);	
				}
				

				for (int k = 0; k < dualKDim.getLength(); k++) {
					dualDepth.set(k, dualSpatialCoordinate[k]);
				}

				//Create the file. At this point the (empty) file will be written to disk
				dataFile.create();
				dataFile.write(depthVar, depth);
				dataFile.write(dualDepthVar, dualDepth);
				dataFile.write(controlVolumeVar, dataControlVolume);
				dataFile.write(psiICVar, dataPsiIC);
				dataFile.write(temperatureICVar, dataTemperatureIC);
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

				dataPsi = new ArrayDouble.D2(NREC, KMAX);
				dataTemperature = new ArrayDouble.D2(NREC, KMAX);
				dataTheta = new ArrayDouble.D2(NREC, KMAX);
				dataHeatFlux = new ArrayDouble.D2(NREC, KMAX);
				dataDarcyFlux = new ArrayDouble.D2(NREC, DUALKMAX);
				dataErrorHeat = new ArrayDouble.D1(NREC);
				dataErrorVolume = new ArrayDouble.D1(NREC);
//				dataTopHeatBC = new ArrayDouble.D1(NREC);
//				dataBottomHeatBC = new ArrayDouble.D1(NREC);
//				dataTopRichardsBC = new ArrayDouble.D1(NREC);
//				dataBottomRichardsBC = new ArrayDouble.D1(NREC);

				int i=0;
				it = variables.entrySet().iterator();
				while (it.hasNext()) {

					@SuppressWarnings("unchecked")
					Entry<String, ArrayList<double[]>> entry = (Entry<String, ArrayList<double[]>>) it.next();

					try {
						date = dateFormat.parse(entry.getKey());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					times.setLong(i, (long) date.getTime()/(60*1000));

					tempVariable =  entry.getValue().get(0);
					for (int k = 0; k < KMAX; k++) {

						dataPsi.set(i, k, tempVariable[k]);

					}

					tempVariable =  entry.getValue().get(1);
					for (int k = 0; k < KMAX; k++) {

						dataTemperature.set(i, k, tempVariable[k]);

					}


					tempVariable =  entry.getValue().get(2);
					for (int k = 0; k < KMAX; k++) {

						dataTheta.set(i, k, tempVariable[k]);

					}
					
					tempVariable =  entry.getValue().get(3);
					for (int k = 0; k < KMAX; k++) {

						dataHeatFlux.set(i, k, tempVariable[k]);

					}
					
					
					tempVariable =  entry.getValue().get(4);
					for (int k = 0; k < DUALKMAX; k++) {

						dataDarcyFlux.set(i, k, tempVariable[k]);

					}
					
					
					dataErrorHeat.set(i, entry.getValue().get(5)[0]);

					dataErrorVolume.set(i, entry.getValue().get(6)[0]);

//					dataBottomBC.set(i, entry.getValue().get(6)[0]);


					i++;
				}				

				// A newly created Java integer array to be initialized to zeros.
//				origin[0] = dataFile.findVariable("psi").getShape()[0];
//				dual_origin[0] = dataFile.findVariable("darcy_velocity").getShape()[0];
//				time_origin[0] = dataFile.findVariable("time").getShape()[0];

				origin[0] = origin_counter;
				time_origin[0] = origin_counter;
				
				//				dataFile.write(kIndexVar, kIndex);
				dataFile.write(dataFile.findVariable("time"), time_origin, times);
				dataFile.write(dataFile.findVariable("psi"), origin, dataPsi);
				dataFile.write(dataFile.findVariable("T"), origin, dataTemperature);
				dataFile.write(dataFile.findVariable("theta"), origin, dataTheta);
				dataFile.write(dataFile.findVariable("heatFlux"), origin, dataHeatFlux);
				dataFile.write(dataFile.findVariable("darcyFlux"), origin, dataDarcyFlux);

				dataFile.write(dataFile.findVariable("errorHeat"), time_origin, dataErrorHeat);
				dataFile.write(dataFile.findVariable("errorVolume"), time_origin, dataErrorVolume);
//				dataFile.write(dataFile.findVariable("topBC"), time_origin, dataTopBC);
//				dataFile.write(dataFile.findVariable("bottomBC"), time_origin, dataBottomBC);

				origin_counter = origin_counter + NREC;
				
				
				fileSizeMB = 1*KMAX*8*origin_counter/1000000;
//				System.out.println("\t\tfileSizeMB: " + fileSizeMB);
				stepCreation ++;
				if(fileSizeMB>fileSizeMax) {
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


