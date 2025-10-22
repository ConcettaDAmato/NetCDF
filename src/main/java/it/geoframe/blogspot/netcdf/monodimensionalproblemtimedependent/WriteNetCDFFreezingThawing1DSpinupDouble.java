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

package it.geoframe.blogspot.netcdf.monodimensionalproblemtimedependent;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import java.util.Map.Entry;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
//import oms3.annotations.Finalize;
import oms3.annotations.In;
//import oms3.annotations.Initialize;
import oms3.annotations.Keywords;
import oms3.annotations.License;
//import oms3.annotations.Out;
import oms3.annotations.Unit;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
//import ucar.ma2.ArrayFloat;
//import ucar.ma2.ArrayInt;
//import ucar.ma2.ArrayInt.D1;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

@Description("This class writes a NetCDF with the spin-up for the freezing thawing problem 1D. Before writing, outputs are stored in a buffer writer"
		+ " and as simulation is ended they are written in a NetCDF file.")
@Documentation("")
@Author(name = "Niccolo' Tubini", contact = "tubini.niccolo@gmail.com")
@Keywords("")
//@Label(JGTConstants.HYDROGEOMORPHOLOGY)
//@Name("shortradbal")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")


public class WriteNetCDFFreezingThawing1DSpinupDouble {

	@Description("Reference time string")
	@In
	@Unit ()
	public String timeUnits = "Minutes since 01/01/1970 00:00:00 UTC";

	@Description("Time zone used to convert dates in UNIX time")
	@In
	@Unit()
	public String timeZone = "UTC";
	
	@Description("Varibles to save in the output file. This comes from the buffer component.")
	@In
	@Unit ()
	public LinkedHashMap<String,ArrayList<double[]>> myVariables;

	@Description("Spatial cooridinate describing the 1D domain")
	@In
	@Unit ()
	public double[] mySpatialCoordinate;
	
	@Description("Dimension of each control volume.")
	@In
	@Unit ()
	public double[] myControlVolume;

	@Description("Numeber of time step every which the ouptut is written to the disk.")
	@In
	public int writeFrequency = 1;

	@Description("Name of the outupt file. It includes also the path.")
	@In
	@Unit ()
	public String fileName;

	@Description("Brief descritpion of the problem")
	@In
	@Unit ()
	public String briefDescritpion = " ";
	@In
	public String topBC = " ";
	@In
	public String bottomBC = " ";
	@In
	public String pathTopBC = " ";
	@In
	public String pathBottomBC = " ";
	@In
	public String pathGrid = " ";
	@In
	public String timeDelta = " ";
	@In
	public String sfccModel = " ";
	@In
	public String soilThermalConductivityModel = " ";
	@In
	public String interfaceThermalConductivityModel = " ";
	@In
	public String stateEquationModel = " ";


	@Description("Boolean variable to print output file only at the end of the simulation")
	@In
	@Unit ()
	public boolean doProcess;
	
	@Description("Maximum allowed file size")
	@In
	@Unit ()
	public double fileSizeMax;


	double[] tempVariable;
	Iterator it;
	DateFormat dateFormat;
	Date date = null;
	String fileNameToSave;
	NetcdfFileWriter dataFile;
	int KMAX;
	int NREC;
	int[] origin;
	int[] time_origin;
	int origin_counter;
	int i;
	int fileNumber = 0;
	double fileSizeMB;
	Dimension kDim;
	Dimension timeDim;
	D1 z;
	Array times;
	String dims;
	
	Variable timeVar;
	Variable zVar;
	// time and space
	Variable temperatureVar;

	// time
//	Variable errorEnergyVar;
//	Variable heatFluxTopVar;
//	Variable heatFluxBottomVar;

	ArrayDouble.D2 dataTemperature;

//	ArrayDouble.D1 dataErrorEnergy;
//	ArrayDouble.D1 dataHeatFluxTop;
//	ArrayDouble.D1 dataHeatFluxBottom;

	int step = 0;
	int stepCreation = 0;

	@Execute
	public void writeNetCDF() throws IOException {

		/*
		 * Create a new file
		 */
		if(stepCreation == 0) {
			
			origin_counter = 0;
//			System.out.println("WriterNetCDF step:" + step);
			KMAX = mySpatialCoordinate.length;
			NREC = myVariables.keySet().size();

			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			date = null;

			origin = new int[]{0, 0};
			time_origin = new int[]{0};

			dataFile = null;
			
//			if(fileNumber==0) {
//				fileName = fileName;
//				fileNameOriginal = fileName;
//			} else {
//				fileName = fileNameOriginal.substring(0,fileNameOriginal.length()-3) + '_' + String.valueOf(fileNumber) + fileNameOriginal.substring(fileNameOriginal.length()-3,fileNameOriginal.length());
//			}
//			fileNameToSave = fileName.substring(0,fileName.length()-3) + '_' + String.valueOf(fileNumber) + fileName.substring(fileName.length()-3,fileName.length());
			fileNameToSave = fileName.substring(0,fileName.length()-3) + '_' + String.format("%04d", fileNumber) + fileName.substring(fileName.length()-3,fileName.length());

			try {
				// Create new netcdf-3 file with the given filename
				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileNameToSave);
				// add a general attribute describing the problem and containing other relevant information for the user
				dataFile.addGroupAttribute(null, new Attribute("Description of the problem",briefDescritpion));
				dataFile.addGroupAttribute(null, new Attribute("Top boundary condition",topBC));
				dataFile.addGroupAttribute(null, new Attribute("Bottom boundary condition",bottomBC));
				dataFile.addGroupAttribute(null, new Attribute("path top boundary condition",pathTopBC));
				dataFile.addGroupAttribute(null, new Attribute("path bottom boundary condition",pathBottomBC));
				dataFile.addGroupAttribute(null, new Attribute("path grid",pathGrid));				
				dataFile.addGroupAttribute(null, new Attribute("time delta",timeDelta));
				dataFile.addGroupAttribute(null, new Attribute("sfcc model",sfccModel));
				dataFile.addGroupAttribute(null, new Attribute("soil thermal conductivity model",soilThermalConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("interface thermal conductivity model",interfaceThermalConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("state equation",stateEquationModel));


				//add dimensions  where time dimension is unlimit
				// the spatial dimension is defined using just the indexes 
				kDim = dataFile.addDimension(null, "z", KMAX);
				timeDim = dataFile.addUnlimitedDimension("time");

				// Define the coordinate variables.
				zVar = dataFile.addVariable(null, "z", DataType.DOUBLE, "z");
				timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");

				// Define units attributes for data variables.
				// Define units attributes for data variables.
				dataFile.addVariableAttribute(timeVar, new Attribute("units", timeUnits));
				dataFile.addVariableAttribute(timeVar, new Attribute("long_name", "Time"));
				dataFile.addVariableAttribute(zVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(zVar, new Attribute("long_name", "Vertical coordinate"));

				// Define the netCDF variables for the psi and theta data.
				String dims = "time z";

				temperatureVar = dataFile.addVariable(null, "T", DataType.DOUBLE, dims);
								
//				errorEnergyVar = dataFile.addVariable(null, "error_energy", DataType.DOUBLE, "time");
//				heatFluxTopVar = dataFile.addVariable(null, "heat_flux_top", DataType.DOUBLE, "time");
//				heatFluxBottomVar = dataFile.addVariable(null, "heat_flux_bottom", DataType.DOUBLE, "time");

				/*
				 *  Define units attributes for data variables.
				 */
				dataFile.addVariableAttribute(temperatureVar, new Attribute("units", "K"));
				dataFile.addVariableAttribute(temperatureVar, new Attribute("long_name", "Temperature"));
				
//				dataFile.addVariableAttribute(errorEnergyVar, new Attribute("units", "J"));
//				dataFile.addVariableAttribute(errorEnergyVar, new Attribute("long_name", "energy error at each time step"));
//				dataFile.addVariableAttribute(heatFluxTopVar, new Attribute("units", "W/m2"));
//				dataFile.addVariableAttribute(heatFluxTopVar, new Attribute("long_name", "heat flux at the top"));
//				dataFile.addVariableAttribute(heatFluxBottomVar, new Attribute("units", "W/m2"));
//				dataFile.addVariableAttribute(heatFluxBottomVar, new Attribute("long_name", "heat flux at the bottom"));

				z = new ArrayDouble.D1(KMAX);

				for (int k = 0; k < KMAX; k++) {
					z.set(k, mySpatialCoordinate[k]);
				}


				//Create the file. At this point the (empty) file will be written to disk
				dataFile.create();
				dataFile.write(zVar, z);
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
				NREC = myVariables.keySet().size();
				
				times = Array.factory(DataType.INT, new int[] {NREC});
				
				dataTemperature = new ArrayDouble.D2(NREC, KMAX);
								
//				dataErrorEnergy = new ArrayDouble.D1(NREC);
//				dataHeatFluxTop = new ArrayDouble.D1(NREC);
//				dataHeatFluxBottom = new ArrayDouble.D1(NREC);


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
					
					times.setLong(i, (long) date.getTime()/(60*1000));

					tempVariable =  entry.getValue().get(0);
					for (int k = 0; k < KMAX; k++) {

						dataTemperature.set(i, k, tempVariable[k]);

					}

										
//					dataErrorEnergy.set(i, entry.getValue().get(4)[0]);
//
//					dataHeatFluxTop.set(i, entry.getValue().get(5)[0]);
//					
//					dataHeatFluxBottom.set(i, entry.getValue().get(6)[0]);

					i++;
				}


				// A newly created Java integer array to be initialized to zeros.
//				origin[0] = dataFile.findVariable("T").getShape()[0];
//				time_origin[0] = dataFile.findVariable("time").getShape()[0];
				origin[0] = origin_counter;
				time_origin[0] = origin_counter;
				
//				dataFile.write(kIndexVar, kIndex);
				dataFile.write(dataFile.findVariable("time"), time_origin, times);
				dataFile.write(dataFile.findVariable("T"), origin, dataTemperature);

//				dataFile.write(dataFile.findVariable("error_energy"), origin, dataErrorEnergy);
//				dataFile.write(dataFile.findVariable("heat_flux_top"), origin, dataHeatFluxTop);
//				dataFile.write(dataFile.findVariable("heat_flux_bottom"), origin, dataHeatFluxBottom);
				
				origin_counter = origin_counter + NREC;
				
				fileSizeMB = 1*KMAX*8*origin_counter/1000000;
//				System.out.println("\t\tfileSizeMB: " + fileSizeMB);
				stepCreation ++;
				if(fileSizeMB>fileSizeMax) {
					stepCreation = 0;
					fileNumber++;
					NREC = 0;
				}
						
				if(!myVariables.isEmpty()) {
					System.out.println("\t\t*** " + myVariables.keySet().toArray()[i-1].toString() +", writing output file: " + fileNameToSave + "\n");
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
