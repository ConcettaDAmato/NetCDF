/*
// * GNU GPL v3 License
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
import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D1;
//import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

@Description("This class reads a NetCDF containing 1D grid data.")
@Documentation("")
@Author(name = "Niccolo' Tubini", contact = "tubini.niccolo@gmail.com")
@Keywords("Soil heat conduction, phase change, frozen soil, GEOframe, Permafrostnet")
@Label("GEOframe.NETCDF")
@Name("readFreezingThawing1Dgrid")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class ReadNetCDFFreezingThawing1D {

	@Description("File name of NetCDF containing grid data")
	@In
	public String gridFilename;

	@Description("Number of control volume")
	@Out
	@Unit("-")
	public int KMAX;
		
	@Description("eta coordinate of volume centroids: zero is at soil surface and and positive upward")
	@Out
	@Unit("m")
	public double[] eta;

	@Description("eta coordinate of volume interfaces: zero is at soil surface and and positive upward.")
	@Out
	@Unit("m")
	public double[] etaDual;

	@Description("z coordinate  of volume centroids: zero is at the bottom of the column and and positive upward")
	@Out
	@Unit("m")
	public double[] z;

	@Description("z coordinate of volume interfaces: zero is at soil surface and and positive upward.")
	@Out
	@Unit("m")
	public double[] zDual;

	@Description("Initial condition for temperature")
	@Out
	@Unit("m")
	public double[] temperatureIC;

	@Description("Distance between consecutive controids, is used to compute gradients")
	@Out
	@Unit("m")
	public double[] spaceDelta;

	@Description("Dimension of the control volume. It is used to integrate enthalpy function")
	@Out
	@Unit("m")
	public double[] controlVolumeDimension;

	@Description("Control volume label identifying the rheology model")
	@Out
	@Unit("-")
	public int[] rheologyID;

	@Description("Control volume label identifying the set of paremeters describing the soil")
	@Out
	@Unit("-")
	public int[] parameterID;
	
	@Description("Soil particles density")
	@Out
	@Unit("kg m-3")
	public double[] soilParticlesDensity;
	
	@Description("Soil particles thermal conductivity")
	@Out
	@Unit("W m-2 K-1")
	public double[] soilParticlesThermalConductivity;
	
	@Description("Soil particles specific heat capacity")
	@Out
	@Unit("J kg-1 m-3")
	public double[] soilParticlesSpecificHeatCapacity;
	
	@Description("Adimensional water content at saturation")
	@Out
	@Unit("-")
	public double[] thetaS;
	
	@Description("Adimensional residual water content")
	@Out
	@Unit("-")
	public double[] thetaR;
	
	@Description("Melting temperature")
	@Out
	@Unit("K")
	public double[] meltingTemperature;
	
	@Description("SFCC parameter 1. This depends on the SFCC used, look at the documentation.")
	@Out
	@Unit("-")
	public double[] par1;
	
	@Description("SFCC parameter 2. This depends on the SFCC used, look at the documentation.")
	@Out
	@Unit("-")
	public double[] par2;
	
	@Description("SFCC parameter 3. This depends on the SFCC used, look at the documentation.")
	@Out
	@Unit("-")
	public double[] par3;
	
	@Description("SFCC parameter 4. This depends on the SFCC used, look at the documentation.")
	@Out
	@Unit("-")
	public double[] par4;

	
	private int[] size;
	private int[] size1;
	private int[] sizeParameter;
//	private int[] sizeCellSize;
	private int step = 0;

	@Execute
	/**
	 * Read the computational grid.
	 **/
	public void read() throws IOException {

		if (step == 0) {

			// Open the file. The ReadOnly parameter tells netCDF we want
			// read-only access to the file.
			NetcdfFile dataFile = null;
			String filename = gridFilename;
			// Open the file.
			try {

				dataFile = NetcdfFile.open(filename, null);

				// Retrieve the variables named "___"
				Variable dataKMAX = dataFile.findVariable("KMAX");
				Variable dataEta = dataFile.findVariable("eta");
				Variable dataEtaDual = dataFile.findVariable("etaDual");
				Variable dataZ = dataFile.findVariable("z");
				Variable dataZDual = dataFile.findVariable("zDual");
				Variable dataIC = dataFile.findVariable("ic");
				Variable dataSpaceDelta = dataFile.findVariable("spaceDelta");
				Variable dataVolumeSoil = dataFile.findVariable("volumeSoil");
				Variable dataRheologyID = dataFile.findVariable("rheologyID");
				Variable dataParameterID = dataFile.findVariable("parameterID");
				
				Variable dataSoilParticlesDensity = dataFile.findVariable("soilParticlesDensity");
				Variable dataThermalConductivitySoilParticles = dataFile.findVariable("thermalConductivitySoilParticles");
				Variable dataSpecificThermalCapacitySoilParticles = dataFile.findVariable("specificThermalCapacitySoilParticles");
				Variable dataThetaS = dataFile.findVariable("thetaS");
				Variable dataThetaR = dataFile.findVariable("thetaR");
				Variable dataMeltingTemperature = dataFile.findVariable("meltingTemperature");
				Variable dataPar1 = dataFile.findVariable("par1");
				Variable dataPar2 = dataFile.findVariable("par2");
				Variable dataPar3 = dataFile.findVariable("par3");
				Variable dataPar4 = dataFile.findVariable("par4");


				size = dataEta.getShape();
				size1 = dataEtaDual.getShape();
				sizeParameter = dataPar1.getShape();

				KMAX = 0;
				eta = new double[size[0]];
				z = new double[size[0]];
				controlVolumeDimension = new double[size[0]];
				temperatureIC = new double[size[0]];
				rheologyID = new int[size[0]];
				parameterID = new int[size[0]];
				
				etaDual = new double[size1[0]];
				zDual = new double[size1[0]];
				spaceDelta = new double[size1[0]];
					
				
				soilParticlesDensity = new double[sizeParameter[0]];
				soilParticlesThermalConductivity = new double[sizeParameter[0]];
				soilParticlesSpecificHeatCapacity = new double[sizeParameter[0]];
				thetaS  = new double[sizeParameter[0]];
				thetaR = new double[sizeParameter[0]];
				meltingTemperature = new double[sizeParameter[0]];
				par1 = new double[sizeParameter[0]];
				par2 = new double[sizeParameter[0]];
				par3 = new double[sizeParameter[0]];
				par4 = new double[sizeParameter[0]];
				
				ArrayDouble.D1 dataArrayEta;
				ArrayDouble.D1 dataArrayEtaDual;
				ArrayDouble.D1 dataArrayZ;
				ArrayDouble.D1 dataArrayZDual;
				ArrayDouble.D1 dataArraySpaceDelta;
				ArrayDouble.D1 dataArrayVolumeSoil;
				ArrayDouble.D1 dataArrayTemperatureIC;
				ArrayDouble.D1 dataArrayRheologyID;
				ArrayDouble.D1 dataArrayParameterID;
				ArrayDouble.D1 dataArraySoilParticlesDensity;
				ArrayDouble.D1 dataArrayThermalConductivitySoilParticles;
				ArrayDouble.D1 dataArraySpecificThermalCapacitySoilParticles;
				ArrayDouble.D1 dataArrayThetaS;
				ArrayDouble.D1 dataArrayThetaR;
				ArrayDouble.D1 dataArrayMeltingTemperature;
				ArrayDouble.D1 dataArrayPar1;
				ArrayDouble.D1 dataArrayPar2;
				ArrayDouble.D1 dataArrayPar3;
				ArrayDouble.D1 dataArrayPar4;

				
				
				dataArrayEta = (ArrayDouble.D1) dataEta.read(null, size);
				dataArrayZ = (ArrayDouble.D1) dataZ.read(null, size);
				dataArrayVolumeSoil = (ArrayDouble.D1) dataVolumeSoil.read(null, size);
				dataArrayTemperatureIC = (ArrayDouble.D1) dataIC.read(null, size);
				dataArrayRheologyID = (D1) dataRheologyID.read(null, size);
				dataArrayParameterID = (D1) dataParameterID.read(null, size);

				dataArrayEtaDual = (ArrayDouble.D1) dataEtaDual.read(null, size1);
				dataArrayZDual = (ArrayDouble.D1) dataZDual.read(null, size1);
				dataArraySpaceDelta = (ArrayDouble.D1) dataSpaceDelta.read(null, size1);
								
				dataArraySoilParticlesDensity = (ArrayDouble.D1) dataSoilParticlesDensity.read(null, sizeParameter);
				dataArrayThermalConductivitySoilParticles = (ArrayDouble.D1) dataThermalConductivitySoilParticles.read(null, sizeParameter);
				dataArraySpecificThermalCapacitySoilParticles = (ArrayDouble.D1) dataSpecificThermalCapacitySoilParticles.read(null, sizeParameter);
				dataArrayThetaS = (ArrayDouble.D1) dataThetaS.read(null, sizeParameter);
				dataArrayThetaR = (ArrayDouble.D1) dataThetaR.read(null, sizeParameter);
				dataArrayMeltingTemperature = (ArrayDouble.D1) dataMeltingTemperature.read(null, sizeParameter);	
				dataArrayPar1 = (ArrayDouble.D1) dataPar1.read(null, sizeParameter);
				dataArrayPar2 = (ArrayDouble.D1) dataPar2.read(null, sizeParameter);	
				dataArrayPar3 = (ArrayDouble.D1) dataPar3.read(null, sizeParameter);	
				dataArrayPar4 = (ArrayDouble.D1) dataPar4.read(null, sizeParameter);				

				KMAX = dataKMAX.readScalarInt();
				for (int i = 0; i < size[0]; i++) {

					eta[i] = dataArrayEta.get(i);
					z[i] = dataArrayZ.get(i);
					temperatureIC[i] = dataArrayTemperatureIC.get(i);
					controlVolumeDimension[i] = dataArrayVolumeSoil.get(i);
					rheologyID[i] = (int) dataArrayRheologyID.get(i);
					parameterID[i] = (int) dataArrayParameterID.get(i);

				}

			
				for (int i = 0; i < size1[0]; i++) {
					
					etaDual[i] = dataArrayEtaDual.get(i);
					zDual[i] = dataArrayZDual.get(i);
					spaceDelta[i] = dataArraySpaceDelta.get(i);

				}

				for (int i = 0; i < sizeParameter[0]; i++) {
					
					soilParticlesDensity[i] = dataArraySoilParticlesDensity.get(i);
					soilParticlesThermalConductivity[i] = dataArrayThermalConductivitySoilParticles.get(i);
					soilParticlesSpecificHeatCapacity[i] = dataArraySpecificThermalCapacitySoilParticles.get(i);
					thetaS[i] = dataArrayThetaS.get(i);
					thetaR[i] = dataArrayThetaR.get(i);
					meltingTemperature[i] = dataArrayMeltingTemperature.get(i);
					par1[i] = dataArrayPar1.get(i);
					par2[i] = dataArrayPar2.get(i);
					par3[i] = dataArrayPar3.get(i);
					par4[i] = dataArrayPar4.get(i);
					
				}
				

			} catch (InvalidRangeException e) {
				e.printStackTrace();

			} finally {
				if (dataFile != null)
					try {
						dataFile.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
			}

			System.out.println("*** SUCCESS reading file " + gridFilename);

		}
		step++;

	}
}