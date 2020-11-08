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

package it.geoframe.blogpsot.netcdf.monodimensionalproblemtimedependent;

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
import oms3.annotations.Out;
import oms3.annotations.Unit;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayInt.D1;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

@Description("This class writes a NetCDF with Excess ice problem outputs. Before writing, outputs are stored in a buffer writer"
		+ " and as simulation is ended they are written in a NetCDF file.")
@Documentation("")
@Author(name = "Niccolo' Tubini", contact = "tubini.niccolo@gmail.com")
@Keywords("Excess ice")
//@Label(JGTConstants.HYDROGEOMORPHOLOGY)
//@Name("shortradbal")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")


public class WriteNetCDFExcessIce1D {

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
	public double[] mySpatialCoordinate;

	@In
	public int writeFrequency = 1;

	//	@Description()
	//	@In
	//	@Unit ()
	//	public double[] myDualSpatialCoordinate;

	@Description()
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
	public String pathInWaterVolume = " ";
	@In
	public String pathInWaterTemperature = " ";
	@In
	public String pathInErosionDeposition = " ";
	@In
	public String pathGrid = " ";
	@In
	public String timeDelta = " ";
	@In
	public String sfccModel = " ";
	@In
	public String soilThermalConductivityModel = " ";
	@In
	public String thermalConductivityModel = " ";
	@In
	public String interfaceThermalConductivityModel = " ";
	@In
	public String stateEquationModel = " ";


	@Description("Boolean variable to print output file only at the end of the simulation")
	@In
	@Unit ()
	public boolean doProcess;


	double[] tempVariable;
	Iterator it;
	DateFormat dateFormat;
	Date date = null;
	String filename;
	NetcdfFileWriter dataFile;
	int KMAX;
	int NREC;
	int[] origin;
	int[] time_origin;
	int i;
	Dimension kDim;
	Dimension timeDim;
	D1 kIndex;
	Array times;
	String dims;
	
	Variable timeVar;
	Variable kIndexVar;
	Variable zVar;
	Variable temperatureVar;
	Variable waterContentVar;
	Variable iceContentVar;
	Variable excessWaterVolumeVar;
	Variable excessIceVolumeVar;
	Variable errorEnergyVar;
	Variable errorVolumeVar;
	Variable waterFromNirvanaVar;
	Variable waterToNirvanaVar;
	Variable energyFromNirvanaVar;
	Variable energyToNirvanaVar;
	Variable surfaceElevationVar;
	Variable KMAXVar;
	Variable heatFluxTopVar;
	Variable heatFluxBottomVar;

	Variable tStarVar;

	ArrayDouble.D2 dataZ;
	ArrayDouble.D2 dataTemperature;
	ArrayDouble.D2 dataWaterContent;
	ArrayDouble.D2 dataIceContent;
	ArrayDouble.D2 dataExcessWaterVolume;
	ArrayDouble.D2 dataExcessIceVolume;

	ArrayDouble.D2 dataTStar;

	ArrayDouble.D1 dataErrorEnergy;
	ArrayDouble.D1 dataErrorVolume;
	ArrayDouble.D1 dataWaterFromNirvana;
	ArrayDouble.D1 dataWaterToNirvana;
	ArrayDouble.D1 dataEnergyFromNirvana;
	ArrayDouble.D1 dataEnergyToNirvana;
	ArrayDouble.D1 dataSurfaceElevation;
	ArrayDouble.D1 dataKMAX;
	ArrayDouble.D1 dataHeatFluxTop;
	ArrayDouble.D1 dataHeatFluxBottom;
	
	int step = 0;

	@Execute
	public void writeNetCDF() throws IOException {

		/*
		 * Create a new file
		 */
		if(step == 0) {
//			System.out.println("WriterNetCDF step:" + step);
			KMAX = mySpatialCoordinate.length;
			NREC = myVariables.keySet().size();

			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			date = null;

			origin = new int[]{0, 0};
			time_origin = new int[]{0};

			dataFile = null;


			try {
				// Create new netcdf-3 file with the given filename
				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName);
				// add a general attribute describing the problem and containing other relevant information for the user
				dataFile.addGroupAttribute(null, new Attribute("Description of the problem",briefDescritpion));
				dataFile.addGroupAttribute(null, new Attribute("Top boundary condition",topBC));
				dataFile.addGroupAttribute(null, new Attribute("Bottom boundary condition",bottomBC));
				dataFile.addGroupAttribute(null, new Attribute("path top boundary condition",pathTopBC));
				dataFile.addGroupAttribute(null, new Attribute("path bottom boundary condition",pathBottomBC));
				dataFile.addGroupAttribute(null, new Attribute("path grid",pathGrid));
				dataFile.addGroupAttribute(null, new Attribute("path in water volume",pathInWaterVolume));
				dataFile.addGroupAttribute(null, new Attribute("path in water temperature",pathInWaterTemperature));				
				dataFile.addGroupAttribute(null, new Attribute("path in erosion/deposition",pathInErosionDeposition));				
				dataFile.addGroupAttribute(null, new Attribute("time delta",timeDelta));
				dataFile.addGroupAttribute(null, new Attribute("sfcc model",sfccModel));
				dataFile.addGroupAttribute(null, new Attribute("soil thermal conductivity model",soilThermalConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("thermal conductivity model",thermalConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("interface thermal conductivity model",interfaceThermalConductivityModel));
				dataFile.addGroupAttribute(null, new Attribute("state equation",stateEquationModel));


				//add dimensions  where time dimension is unlimit
				// the spatial dimension is defined using just the indexes 
				kDim = dataFile.addDimension(null, "k", KMAX);
				timeDim = dataFile.addUnlimitedDimension("time");

				// Define the coordinate variables.
				kIndexVar = dataFile.addVariable(null, "k", DataType.INT, "k");
				timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");

				// Define units attributes for data variables.
				// Define units attributes for data variables.
				dataFile.addVariableAttribute(timeVar, new Attribute("units", timeUnits));
				dataFile.addVariableAttribute(timeVar, new Attribute("long_name", "Time"));
				dataFile.addVariableAttribute(kIndexVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(kIndexVar, new Attribute("long_name", "k index"));

				// Define the netCDF variables for the psi and theta data.
				String dims = "time k";

				zVar = dataFile.addVariable(null, "z", DataType.DOUBLE, dims);
				temperatureVar = dataFile.addVariable(null, "T", DataType.DOUBLE, dims);
				waterContentVar = dataFile.addVariable(null, "water_content", DataType.DOUBLE, dims);
				iceContentVar = dataFile.addVariable(null, "ice_content", DataType.DOUBLE, dims);
				excessWaterVolumeVar = dataFile.addVariable(null, "excess_water_volume", DataType.DOUBLE, dims);
				excessIceVolumeVar = dataFile.addVariable(null, "excess_ice_volume", DataType.DOUBLE, dims);
				
				tStarVar = dataFile.addVariable(null, "t_star", DataType.DOUBLE, dims);
				
				errorEnergyVar = dataFile.addVariable(null, "error_energy", DataType.DOUBLE, "time");
				errorVolumeVar = dataFile.addVariable(null, "error_volume", DataType.DOUBLE, "time");
				waterFromNirvanaVar = dataFile.addVariable(null, "water_from_nirvana", DataType.DOUBLE, "time");
				waterToNirvanaVar = dataFile.addVariable(null, "water_to_nirvana", DataType.DOUBLE, "time");
				energyFromNirvanaVar = dataFile.addVariable(null, "energy_from_nirvana", DataType.DOUBLE, "time");
				energyToNirvanaVar = dataFile.addVariable(null, "energy_to_nirvana", DataType.DOUBLE, "time");
				surfaceElevationVar = dataFile.addVariable(null, "surface_elevation", DataType.DOUBLE, "time");
				KMAXVar = dataFile.addVariable(null, "KMAX", DataType.INT, "time");
				heatFluxTopVar = dataFile.addVariable(null, "heat_flux_top", DataType.DOUBLE, "time");
				heatFluxBottomVar = dataFile.addVariable(null, "heat_flux_bottom", DataType.DOUBLE, "time");



				/*
				 *  Define units attributes for data variables.
				 */
				dataFile.addVariableAttribute(zVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(zVar, new Attribute("long_name", "Height from the bottom"));
				dataFile.addVariableAttribute(temperatureVar, new Attribute("units", "K"));
				dataFile.addVariableAttribute(temperatureVar, new Attribute("long_name", "Temperature"));
				dataFile.addVariableAttribute(waterContentVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(waterContentVar, new Attribute("long_name", "soil liquid water content"));
				dataFile.addVariableAttribute(iceContentVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(iceContentVar, new Attribute("long_name", "soil ice content"));
				dataFile.addVariableAttribute(excessWaterVolumeVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(excessWaterVolumeVar, new Attribute("long_name", "excess liquid water volume"));
				dataFile.addVariableAttribute(excessIceVolumeVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(excessIceVolumeVar, new Attribute("long_name", "excess ice volume"));
				dataFile.addVariableAttribute(errorEnergyVar, new Attribute("units", "J"));
				dataFile.addVariableAttribute(errorEnergyVar, new Attribute("long_name", "energy error at each time step"));
				dataFile.addVariableAttribute(errorVolumeVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(errorVolumeVar, new Attribute("long_name", "volume error at each time step"));
				dataFile.addVariableAttribute(waterFromNirvanaVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(waterFromNirvanaVar, new Attribute("long_name", "total volume of water from nirvana"));
				dataFile.addVariableAttribute(waterToNirvanaVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(waterToNirvanaVar, new Attribute("long_name", "total volume of water to nirvana"));
				dataFile.addVariableAttribute(energyFromNirvanaVar, new Attribute("units", "J"));
				dataFile.addVariableAttribute(energyFromNirvanaVar, new Attribute("long_name", "total energy from nirvana"));
				dataFile.addVariableAttribute(energyToNirvanaVar, new Attribute("units", "J"));
				dataFile.addVariableAttribute(energyToNirvanaVar, new Attribute("long_name", "total energy to nirvana"));
				dataFile.addVariableAttribute(surfaceElevationVar, new Attribute("units", "m"));
				dataFile.addVariableAttribute(surfaceElevationVar, new Attribute("long_name", "soil surface elevation measured from the bottom"));
				dataFile.addVariableAttribute(KMAXVar, new Attribute("units", "-"));
				dataFile.addVariableAttribute(KMAXVar, new Attribute("long_name", "index of control volumes where the solution is computed"));
				dataFile.addVariableAttribute(heatFluxTopVar, new Attribute("units", "W/m2"));
				dataFile.addVariableAttribute(heatFluxTopVar, new Attribute("long_name", "heat flux at the top"));
				dataFile.addVariableAttribute(heatFluxBottomVar, new Attribute("units", "W/m2"));
				dataFile.addVariableAttribute(heatFluxBottomVar, new Attribute("long_name", "heat flux at the bottom"));

				//			D1 kIndex = new ArrayInt.D1(KMAX);

				kIndex = new ArrayInt.D1(KMAX);

				for (int k = 0; k < KMAX; k++) {
					kIndex.set(k, k);
				}


				//Create the file. At this point the (empty) file will be written to disk
				dataFile.create();
				dataFile.write(kIndexVar, kIndex);

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

//			System.out.println("WriterNetCDF updating");

			//			final int KMAX = mySpatialCoordinate.length;
			////			final int dualNLVL = myDualSpatialCoordinate.length;
			//			final int NREC = myVariables.keySet().size();
			//			// human readable date will be converted in unix format, the format will be an input and it has to be consistent with that used in OMS
			//			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			//			Date date = null;
			//			long unixTime;
			//			double[] myTempVariable; 
			//			//Iterator it;
			//
			//
			//			// Create the file.
			//			//String filename = fileName;
			//			NetcdfFileWriter dataFile = null;
			//
			//			try {
			//				// Create new netcdf-3 file with the given filename
			//				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName);
			//				// add a general attribute describing the problem and containing other relevant information for the user
			//				dataFile.addGroupAttribute(null, new Attribute("Description of the problem",briefDescritpion));
			//				dataFile.addGroupAttribute(null, new Attribute("Top boundary condition",topBC));
			//				dataFile.addGroupAttribute(null, new Attribute("Bottom boundary condition",bottomBC));
			//				dataFile.addGroupAttribute(null, new Attribute("path top boundary condition",pathTopBC));
			//				dataFile.addGroupAttribute(null, new Attribute("path bottom boundary condition",pathBottomBC));
			//				dataFile.addGroupAttribute(null, new Attribute("path grid",pathGrid));
			//				dataFile.addGroupAttribute(null, new Attribute("path in water volume",pathInWaterVolume));
			//				dataFile.addGroupAttribute(null, new Attribute("path in water temperature",pathInWaterTemperature));				
			//				dataFile.addGroupAttribute(null, new Attribute("time delta",timeDelta));
			//				dataFile.addGroupAttribute(null, new Attribute("sfcc model",sfccModel));
			//				dataFile.addGroupAttribute(null, new Attribute("thermal conductivity model",thermalConductivityModel));
			//				dataFile.addGroupAttribute(null, new Attribute("state equation",stateEquationModel));
			//				
			//
			//
			//
			//
			//
			//				//add dimensions  where time dimension is unlimit
			//				// the spatial dimension is defined using just the indexes 
			//				Dimension kDim = dataFile.addDimension(null, "k", KMAX);
			//				Dimension timeDim = dataFile.addUnlimitedDimension("time");
			//
			//				// Define the coordinate variables.
			//				Variable kIndexVar = dataFile.addVariable(null, "k", DataType.INT, "k");
			//				Variable timeVar = dataFile.addVariable(null, "time", DataType.INT, "time");
			//
			//
			//				// Define units attributes for data variables.
			//				dataFile.addVariableAttribute(timeVar, new Attribute("units", timeUnits));
			//				dataFile.addVariableAttribute(timeVar, new Attribute("long_name", "Time"));
			//				dataFile.addVariableAttribute(kIndexVar, new Attribute("units", "-"));
			//				dataFile.addVariableAttribute(kIndexVar, new Attribute("long_name", "k index"));
			//
			//				// Define the netCDF variables for the psi and theta data.
			//				String dims = "time k";
			//
			//				Variable zVar = dataFile.addVariable(null, "z", DataType.DOUBLE, dims);
			//				Variable temperatureVar = dataFile.addVariable(null, "T", DataType.DOUBLE, dims);
			//				Variable waterContentVar = dataFile.addVariable(null, "water_content", DataType.DOUBLE, dims);
			//				Variable iceContentVar = dataFile.addVariable(null, "ice_content", DataType.DOUBLE, dims);
			//				Variable excessWaterVolumeVar = dataFile.addVariable(null, "excess_water_volume", DataType.DOUBLE, dims);
			//				Variable excessIceVolumeVar = dataFile.addVariable(null, "excess_ice_volume", DataType.DOUBLE, dims);
			//				Variable errorEnergyVar = dataFile.addVariable(null, "error_energy", DataType.DOUBLE, "time");
			//				Variable errorVolumeVar = dataFile.addVariable(null, "error_volume", DataType.DOUBLE, "time");
			//				Variable waterFromNirvanaVar = dataFile.addVariable(null, "water_from_nirvana", DataType.DOUBLE, "time");
			//				Variable waterToNirvanaVar = dataFile.addVariable(null, "water_to_nirvana", DataType.DOUBLE, "time");
			//				Variable energyFromNirvanaVar = dataFile.addVariable(null, "energy_from_nirvana", DataType.DOUBLE, "time");
			//				Variable energyToNirvanaVar = dataFile.addVariable(null, "energy_to_nirvana", DataType.DOUBLE, "time");
			//				Variable surfaceElevationVar = dataFile.addVariable(null, "surface_elevation", DataType.DOUBLE, "time");
			//
			//
			//
			//				/*
			//				 *  Define units attributes for data variables.
			//				 */
			//				dataFile.addVariableAttribute(zVar, new Attribute("units", "m"));
			//				dataFile.addVariableAttribute(zVar, new Attribute("long_name", "Height from the bottom"));
			//				dataFile.addVariableAttribute(temperatureVar, new Attribute("units", "K"));
			//				dataFile.addVariableAttribute(temperatureVar, new Attribute("long_name", "Temperature"));
			//				dataFile.addVariableAttribute(waterContentVar, new Attribute("units", "-"));
			//				dataFile.addVariableAttribute(waterContentVar, new Attribute("long_name", "soil liquid water content"));
			//				dataFile.addVariableAttribute(iceContentVar, new Attribute("units", "-"));
			//				dataFile.addVariableAttribute(iceContentVar, new Attribute("long_name", "soil ice content"));
			//				dataFile.addVariableAttribute(excessWaterVolumeVar, new Attribute("units", "m"));
			//				dataFile.addVariableAttribute(excessWaterVolumeVar, new Attribute("long_name", "excess liquid water volume"));
			//				dataFile.addVariableAttribute(excessIceVolumeVar, new Attribute("units", "m"));
			//				dataFile.addVariableAttribute(excessIceVolumeVar, new Attribute("long_name", "excess ice volume"));
			//				dataFile.addVariableAttribute(errorEnergyVar, new Attribute("units", "J"));
			//				dataFile.addVariableAttribute(errorEnergyVar, new Attribute("long_name", "energy error at each time step"));
			//				dataFile.addVariableAttribute(errorVolumeVar, new Attribute("units", "m"));
			//				dataFile.addVariableAttribute(errorVolumeVar, new Attribute("long_name", "volume error at each time step"));
			//				dataFile.addVariableAttribute(waterFromNirvanaVar, new Attribute("units", "m"));
			//				dataFile.addVariableAttribute(waterFromNirvanaVar, new Attribute("long_name", "total volume of water from nirvana"));
			//				dataFile.addVariableAttribute(waterToNirvanaVar, new Attribute("units", "m"));
			//				dataFile.addVariableAttribute(waterToNirvanaVar, new Attribute("long_name", "total volume of water to nirvana"));
			//				dataFile.addVariableAttribute(energyFromNirvanaVar, new Attribute("units", "J"));
			//				dataFile.addVariableAttribute(energyFromNirvanaVar, new Attribute("long_name", "total energy from nirvana"));
			//				dataFile.addVariableAttribute(energyToNirvanaVar, new Attribute("units", "J"));
			//				dataFile.addVariableAttribute(energyToNirvanaVar, new Attribute("long_name", "total energy to nirvana"));
			//				dataFile.addVariableAttribute(surfaceElevationVar, new Attribute("units", "m"));
			//				dataFile.addVariableAttribute(surfaceElevationVar, new Attribute("long_name", "soil surface elevation measured from the bottom"));
			//
			//				D1 kIndex = new ArrayInt.D1(KMAX);
			//				Array times = Array.factory(DataType.LONG, new int[] {NREC});
			//
			//
			//				for (int k = 0; k < KMAX; k++) {
			//					kIndex.set(k, k);
			//				}

			try {
				dataFile = NetcdfFileWriter.openExisting(fileName);
				
				// number of time record that will be saved
				NREC = myVariables.keySet().size();
				
				times = Array.factory(DataType.INT, new int[] {NREC});
				
				dataZ = new ArrayDouble.D2(NREC, dataFile.findVariable("z").getShape()[1]);
				dataTemperature = new ArrayDouble.D2(NREC, dataFile.findVariable("T").getShape()[1]);
				dataWaterContent = new ArrayDouble.D2(NREC, dataFile.findVariable("water_content").getShape()[1]);
				dataIceContent = new ArrayDouble.D2(NREC, dataFile.findVariable("ice_content").getShape()[1]);
				dataExcessWaterVolume = new ArrayDouble.D2(NREC, dataFile.findVariable("excess_water_volume").getShape()[1]);
				dataExcessIceVolume = new ArrayDouble.D2(NREC, dataFile.findVariable("excess_ice_volume").getShape()[1]);
				
				dataTStar = new ArrayDouble.D2(NREC, dataFile.findVariable("t_star").getShape()[1]);
				
				dataErrorEnergy = new ArrayDouble.D1(NREC);
				dataErrorVolume = new ArrayDouble.D1(NREC);
				dataWaterFromNirvana = new ArrayDouble.D1(NREC);
				dataWaterToNirvana = new ArrayDouble.D1(NREC);
				dataEnergyFromNirvana = new ArrayDouble.D1(NREC);
				dataEnergyToNirvana = new ArrayDouble.D1(NREC);
				dataSurfaceElevation = new ArrayDouble.D1(NREC);
				dataKMAX = new ArrayDouble.D1(NREC);
				dataHeatFluxTop = new ArrayDouble.D1(NREC);
				dataHeatFluxBottom = new ArrayDouble.D1(NREC);


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
					
					times.setLong(i, (long) date.getTime()/1000);


					tempVariable =  entry.getValue().get(0);
					for (int k = 0; k < KMAX; k++) {

						dataZ.set(i, k, tempVariable[k]);

					}


					tempVariable =  entry.getValue().get(1);
					for (int k = 0; k < KMAX; k++) {

						dataTemperature.set(i, k, tempVariable[k]);
						//						System.out.println("\t\tmyTempVariable[k]: "+dataTemperature.get(i,k));


					}

					tempVariable =  entry.getValue().get(2);
					for (int k = 0; k < KMAX; k++) {

						dataWaterContent.set(i, k, tempVariable[k]);

					}

					tempVariable =  entry.getValue().get(3);
					for (int k = 0; k < KMAX; k++) {

						dataIceContent.set(i, k, tempVariable[k]);

					}

					tempVariable =  entry.getValue().get(4);
					for (int k = 0; k < KMAX; k++) {

						dataExcessWaterVolume.set(i, k, tempVariable[k]);

					}

					tempVariable =  entry.getValue().get(5);
					for (int k = 0; k < KMAX; k++) {

						dataExcessIceVolume.set(i, k, tempVariable[k]);

					}
					
					
					tempVariable =  entry.getValue().get(16);
					for (int k = 0; k < KMAX; k++) {

						dataTStar.set(i, k, tempVariable[k]);

					}

					dataErrorEnergy.set(i, entry.getValue().get(6)[0]);

					dataErrorVolume.set(i, entry.getValue().get(7)[0]);

					dataWaterFromNirvana.set(i, entry.getValue().get(8)[0]);

					dataWaterToNirvana.set(i, entry.getValue().get(9)[0]);

					dataEnergyFromNirvana.set(i, entry.getValue().get(10)[0]);

					dataEnergyToNirvana.set(i, entry.getValue().get(11)[0]);

					dataSurfaceElevation.set(i, entry.getValue().get(12)[0]);
					
					dataKMAX.set(i, entry.getValue().get(13)[0]);
					
					dataHeatFluxTop.set(i, entry.getValue().get(14)[0]);
					
					dataHeatFluxBottom.set(i, entry.getValue().get(15)[0]);

					//					System.out.println("\t\tdataError.get(i): "+dataError.get(i));
					//
					//					dataTopBC.set(i, entry.getValue().get(10)[0]);
					//
					//					dataBottomBC.set(i, entry.getValue().get(11)[0]);
					//					
					//					dataRunOff.set(i, entry.getValue().get(12)[0]);

					i++;
				}


				// A newly created Java integer array to be initialized to zeros.
				origin[0] = dataFile.findVariable("z").getShape()[0];
				time_origin[0] = dataFile.findVariable("time").getShape()[0];
				
//				dataFile.write(kIndexVar, kIndex);
				dataFile.write(dataFile.findVariable("time"), time_origin, times);
				dataFile.write(dataFile.findVariable("z"), origin, dataZ);
				dataFile.write(dataFile.findVariable("T"), origin, dataTemperature);
				dataFile.write(dataFile.findVariable("water_content"), origin, dataWaterContent);
				dataFile.write(dataFile.findVariable("ice_content"), origin, dataIceContent);
				dataFile.write(dataFile.findVariable("excess_water_volume"), origin, dataExcessWaterVolume);
				dataFile.write(dataFile.findVariable("excess_ice_volume"), origin, dataExcessIceVolume);
				dataFile.write(dataFile.findVariable("t_star"), origin, dataTStar);

				dataFile.write(dataFile.findVariable("error_energy"), origin, dataErrorEnergy);
				dataFile.write(dataFile.findVariable("error_volume"), origin, dataErrorVolume);
				dataFile.write(dataFile.findVariable("energy_from_nirvana"), origin, dataEnergyFromNirvana);
				dataFile.write(dataFile.findVariable("energy_to_nirvana"), origin, dataEnergyToNirvana);
				dataFile.write(dataFile.findVariable("water_from_nirvana"), origin, dataWaterFromNirvana);
				dataFile.write(dataFile.findVariable("water_to_nirvana"), origin, dataWaterToNirvana);
				dataFile.write(dataFile.findVariable("surface_elevation"), origin, dataSurfaceElevation);
				dataFile.write(dataFile.findVariable("KMAX"), origin, dataKMAX);
				dataFile.write(dataFile.findVariable("heat_flux_top"), origin, dataHeatFluxTop);
				dataFile.write(dataFile.findVariable("heat_flux_bottom"), origin, dataHeatFluxBottom);

				
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
