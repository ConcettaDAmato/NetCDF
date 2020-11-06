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

package it.geoframe.blogpsot.monodimensionalproblemtimedependent;

import java.io.IOException;
import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Initialize;
import oms3.annotations.Keywords;
import oms3.annotations.License;
import oms3.annotations.Out;
import oms3.annotations.Unit;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

@Description("This class reads a NetCDF containing 1D grid data. "
		+ "The input file is created with EnergyMeshGen.ipynb (Jupyter Notebook)")
@Documentation("")
@Author(name = "Niccolo' Tubini, Riccardo Rigon", contact = "tubini.niccolo@gmail.com")
@Keywords("Hydrology, energy")
// @Label(JGTConstants.HYDROGEOMORPHOLOGY)
// @Name("shortradbal")
// @Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

/*
 * FIXME: da riscrivere in accordo al notebook
 */
public class ReadNetCDFEnergyGrid1D {

	@Description("File name of NetCDF containing grid data")
	@In
	public String energyGridFilename;

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
	@Unit("K")
	public double[] temperatureIC;

	@Description("Distance between consecutive controids, is used to compute gradients")
	@Out
	@Unit("m")
	public double[] spaceDelta;

	@Description("Length of each control volume")
	@Out
	@Unit("m")
	public double[] deltaZ;

	@Description("Percentage of clay.")
	@Out
	@Unit("-")
	public double[] clayFraction;

	@Description("Percentage of sand")
	@Out
	@Unit("-")
	public double[] sandFraction;

	@Description("Thermal conductivity of soil grain")
	@Out
	@Unit("W/ m K")
	public double[] grainThermalConductivity;

	@Description("Density of grain soil")
	@Out
	@Unit("kg/m3")
	public double[] grainDensity;

	@Description("Parameter 1")
	@Out
	@Unit(" ")
	public double[] par1;

	@Description("Parameter 2")
	@Out
	@Unit(" ")
	public double[] par2;

	@Description("Parameter 3")
	@Out
	@Unit(" ")
	public double[] par3;

	@Description("Parameter 4")
	@Out
	@Unit(" ")
	public double[] par4;

	@Description("Parameter 5")
	@Out
	@Unit(" ")
	public double[] par5;

	@Description("Parameter 6")
	@Out
	@Unit(" ")
	public double[] par6;

	int[] size;

	int step = 0;

	@Execute
	public void read() throws IOException {

		if (step == 0) {

			// Open the file. The ReadOnly parameter tells netCDF we want
			// read-only access to the file.
			NetcdfFile dataFile = null;
			String filename = energyGridFilename;
			// Open the file.
			try {

				dataFile = NetcdfFile.open(filename, null);

				// Retrieve the variables named "___"
				Variable dataEta = dataFile.findVariable("eta");
				Variable dataEtaDual = dataFile.findVariable("etaDual");
				Variable dataZ = dataFile.findVariable("z");
				Variable dataZDual = dataFile.findVariable("zDual");
				Variable dataTemperatureIC = dataFile.findVariable("temperatureIC");
				Variable dataSpaceDelta = dataFile.findVariable("spaceDelta");
				Variable dataDeltaZ = dataFile.findVariable("deltaZ");
				Variable dataClayFraction = dataFile.findVariable("clayFraction");
				Variable dataSandFraction = dataFile.findVariable("sandFraction");
				Variable dataGrainThermalConductivity = dataFile.findVariable("grainThermalConductivity");
				Variable dataGrainDensity = dataFile.findVariable("grainDensity");
				Variable dataPar1 = dataFile.findVariable("par1");
				Variable dataPar2 = dataFile.findVariable("par2");
				Variable dataPar3 = dataFile.findVariable("par3");
				Variable dataPar4 = dataFile.findVariable("par4");
				Variable dataPar5 = dataFile.findVariable("par5");
				Variable dataPar6 = dataFile.findVariable("par6");

				// if (dataEta == null) {
				// System.out.println("Cant find Variable data");
				// return;
				// }

				size = dataEta.getShape();

				eta = new double[size[0]];
				etaDual = new double[size[0]];
				z = new double[size[0]];
				zDual = new double[size[0]];
				temperatureIC = new double[size[0]];
				spaceDelta = new double[size[0]];

				ArrayDouble.D1 dataArrayEta;
				ArrayDouble.D1 dataArrayEtaDual;
				ArrayDouble.D1 dataArrayZ;
				ArrayDouble.D1 dataArrayZDual;
				ArrayDouble.D1 dataArrayTemperatureIC;
				ArrayDouble.D1 dataArraySpaceDelta;

				dataArrayEta = (ArrayDouble.D1) dataEta.read(null, size);
				dataArrayEtaDual = (ArrayDouble.D1) dataEtaDual.read(null, size);
				dataArrayZ = (ArrayDouble.D1) dataZ.read(null, size);
				dataArrayZDual = (ArrayDouble.D1) dataZDual.read(null, size);
				dataArrayTemperatureIC = (ArrayDouble.D1) dataTemperatureIC.read(null, size);
				dataArraySpaceDelta = (ArrayDouble.D1) dataSpaceDelta.read(null, size);

				for (int i = 0; i < size[0]; i++) {

					eta[i] = dataArrayEta.get(i);
					etaDual[i] = dataArrayEtaDual.get(i);
					z[i] = dataArrayZ.get(i);
					zDual[i] = dataArrayZDual.get(i);
					temperatureIC[i] = dataArrayTemperatureIC.get(i);
					spaceDelta[i] = dataArraySpaceDelta.get(i);

				}

				//////////////////////////
				//////////////////////////
				//////////////////////////

				size = dataDeltaZ.getShape();

				deltaZ = new double[size[0]];
				clayFraction = new double[size[0]];
				sandFraction = new double[size[0]];
				grainThermalConductivity = new double[size[0]];
				grainDensity = new double[size[0]];
				par1 = new double[size[0]];
				par2 = new double[size[0]];
				par3 = new double[size[0]];
				par4 = new double[size[0]];
				par5 = new double[size[0]];
				par6 = new double[size[0]];

				ArrayDouble.D1 dataArrayDeltaZ;
				ArrayDouble.D1 dataArrayClayFraction;
				ArrayDouble.D1 dataArraySandFraction;
				ArrayDouble.D1 dataArrayGrainThermalConductivity;
				ArrayDouble.D1 dataArrayGrainDensity;
				ArrayDouble.D1 dataArrayPar1;
				ArrayDouble.D1 dataArrayPar2;
				ArrayDouble.D1 dataArrayPar3;
				ArrayDouble.D1 dataArrayPar4;
				ArrayDouble.D1 dataArrayPar5;
				ArrayDouble.D1 dataArrayPar6;

				dataArrayDeltaZ = (ArrayDouble.D1) dataDeltaZ.read(null, size);
				dataArrayClayFraction = (ArrayDouble.D1) dataClayFraction.read(null, size);
				dataArraySandFraction = (ArrayDouble.D1) dataSandFraction.read(null, size);
				dataArrayGrainThermalConductivity = (ArrayDouble.D1) dataGrainThermalConductivity.read(null, size);
				dataArrayGrainDensity = (ArrayDouble.D1) dataGrainDensity.read(null, size);
				dataArrayPar1 = (ArrayDouble.D1) dataPar1.read(null, size);
				dataArrayPar2 = (ArrayDouble.D1) dataPar2.read(null, size);
				dataArrayPar3 = (ArrayDouble.D1) dataPar3.read(null, size);
				dataArrayPar4 = (ArrayDouble.D1) dataPar4.read(null, size);
				dataArrayPar5 = (ArrayDouble.D1) dataPar5.read(null, size);
				dataArrayPar6 = (ArrayDouble.D1) dataPar6.read(null, size);

				for (int i = 0; i < size[0]; i++) {

					deltaZ[i] = dataArrayDeltaZ.get(i);
					clayFraction[i] = dataArrayClayFraction.get(i);
					sandFraction[i] = dataArraySandFraction.get(i);
					grainThermalConductivity[i] = dataArrayGrainThermalConductivity.get(i);
					grainDensity[i] = dataArrayGrainDensity.get(i);
					par1[i] = dataArrayPar1.get(i);
					par2[i] = dataArrayPar2.get(i);
					par3[i] = dataArrayPar3.get(i);
					par4[i] = dataArrayPar4.get(i);
					par5[i] = dataArrayPar5.get(i);
					par6[i] = dataArrayPar6.get(i);


				}
				// Control values
//				
//				System.out.println(dataPar1 + "\n" + par1[0] + "\n\n" + dataPar2 + "\n" + par2[0]
//						+ "\n\n" + dataPar3 + "\n" + par3[0] + "\n\n" + dataPar4 + "\n" + par4[0]
//						+ "\n\n" + dataPar5 + "\n" + par5[0] + "\n\n" + dataPar6 + "\n" + par6[0]
//						+ "\n\n" + dataClayFraction + "\n" + clayFraction[0] + "\n\n" + dataSandFraction + "\n" + sandFraction[0] + "\n\n"
//						+ dataGrainThermalConductivity + "\n" + grainThermalConductivity[0]
//
//						+ "\n\n" + dataGrainDensity + "\n" + grainDensity[0] );
//				
//				
//				  System.out.println("Check IC values:\n\n"); for (int i = 0; i < size[0]+1; i++)
//				  {
//				  
//				  System.out.println("	"+temperatureIC[i]);
//				 
//				  }
				

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

			System.out.println("*** SUCCESS reading example file " + energyGridFilename);

		}
		step++;

	}
}