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

@Description("This class reads a NetCDF containing FrozenSoil 1D output.")
@Documentation("")
@Author(name = "Niccolo' Tubini", contact = "tubini.niccolo@gmail.com")
@Keywords("Frozen Soil, phase change, heat equation, permafrost")
@License("General Public License Version 3 (GPLv3)")

public class ReadNetCDFFreezingThawingOutput1D {

	@Description("File name of NetCDF containing output")
	@In
	public String filename;

	@Description("Temperature")
	@Out
	@Unit("K")
	public double[][] temperature;

	@Description("Adimensional liquid water content.")
	@Out
	@Unit("-")
	public double[][] waterContent;

	@Description("Adimensional ice content")
	@Out
	@Unit("-")
	public double[][] iceContent;
	

	private int[] size;

	private int step = 0;

	@Execute
	public void read() throws IOException {

		if (step == 0) {

			// Open the file. The ReadOnly parameter tells netCDF we want
			// read-only access to the file.
			NetcdfFile dataFile = null;
			// Open the file.
			try {

				dataFile = NetcdfFile.open(filename, null);

				// Retrieve the variables named "___"
				Variable dataTemperature = dataFile.findVariable("T");
				Variable dataWaterContent = dataFile.findVariable("water_content");
				Variable dataIceContent = dataFile.findVariable("ice_content");



				size = dataTemperature.getShape();

				temperature = new double[size[0]][size[1]];
				waterContent = new double[size[0]][size[1]];
				iceContent = new double[size[0]][size[1]];
				
				ArrayDouble.D2 dataArrayTemperature = (ArrayDouble.D2) dataTemperature.read(null, size);
				ArrayDouble.D2 dataArrayWaterContent = (ArrayDouble.D2) dataWaterContent.read(null, size);
				ArrayDouble.D2 dataArrayIceContent = (ArrayDouble.D2) dataIceContent.read(null, size);

				for (int i = 0; i < size[0]; i++) {
					for (int j = 0; j < size[1]; j++) {
						temperature[i][j] = dataArrayTemperature.get(i,j);
						waterContent[i][j] = dataArrayWaterContent.get(i,j);
						iceContent[i][j] = dataArrayIceContent.get(i,j);
					}
					//System.out.println("\n");
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

			System.out.println("*** SUCCESS reading file " + filename);

		}
		step++;

	}
	

}