/*
 * GNU GPL v3 License
 *
 * Copyright 2016 Marialaura Bancheri
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

@Description("This class reads a NetCDF containing 1D grid data and Lysimeter data. The input file is created with RichardsMeshGen.ipynb (Jupyter Notebook)")
@Documentation("")
@Author(name = "Niccolo' Tubini, Concetta D'Amato, Riccardo Rigon", contact = "tubini.niccolo@gmail.com")
@Keywords("Hydrology, Richards, Infiltration")
// @Label(JGTConstants.HYDROGEOMORPHOLOGY)
// @Name("shortradbal")
// @Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class ReadNetCDFLisimetro {

	@Description("File name of NetCDF containing grid data")
	@In
	public String richardsGridFilename;

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

	@Description("Initial condition for water suction")
	@Out
	@Unit("m")
	public double[] psiIC;

	@Description("Distance between consecutive controids, is used to compute gradients")
	@Out
	@Unit("m")
	public double[] spaceDelta;

	@Description("Length of each control volume")
	@Out
	@Unit("m")
	public double[] deltaZ;

	@Description("Adimensional water content at saturation")
	@Out
	@Unit("-")
	public double[] thetaS;

	@Description("Adimensional residual water content")
	@Out
	@Unit("-")
	public double[] thetaR;

	@Description("Hydraulic conductivity at saturation")
	@Out
	@Unit("m/s")
	public double[] Ks;

	@Description("Aquitard compressibility")
	@Out
	@Unit("1/Pa")
	public double[] alphaSS;

	@Description("Water compressibility")
	@Out
	@Unit("1/Pa")
	public double[] betaSS;

	@Description("First SWRC parameter")
	@Out
	@Unit(" ")
	public double[] par1SWRC;

	@Description("Second SWRC parameter")
	@Out
	@Unit(" ")
	public double[] par2SWRC;

	@Description("Third SWRC parameter")
	@Out
	@Unit(" ")
	public double[] par3SWRC;

	@Description("Fouth SWRC parameter")
	@Out
	@Unit(" ")
	public double[] par4SWRC;

	@Description("Fifth SWRC parameter")
	@Out
	@Unit(" ")
	public double[] par5SWRC;

	@Description("Critical value of psi, where moisture capacity is null'")
	@Out
	@Unit("m")
	public double[] par6SWRC;

	@Description("Critical value of psi, where moisture capacity is null'")
	@Out
	@Unit("m")
	public double[] par7SWRC;

	@Description("Critical value of psi, where moisture capacity is null'")
	@Out
	@Unit("m")
	public double[] par8SWRC;
	
	@Description("Wilting point")
	@Out
	@Unit("-")
	public double[] thetaWp;
	
	@Description("Field capacity")
	@Out
	@Unit("-")
	public double[] thetaFc;

	@Description("Coefficient to model ET: -et(theta-thetaR). Casulli notes")
	@Out
	@Unit("1/s")
	public double[] et;

	int[] size;

	int step = 0;

	@Execute
	public void read() throws IOException {

		if (step == 0) {

			// Open the file. The ReadOnly parameter tells netCDF we want
			// read-only access to the file.
			NetcdfFile dataFile = null;
			String filename = richardsGridFilename;
			// Open the file.
			try {

				dataFile = NetcdfFile.open(filename, null);

				// Retrieve the variables named "___"
				Variable dataEta = dataFile.findVariable("eta");
				Variable dataEtaDual = dataFile.findVariable("etaDual");
				Variable dataZ = dataFile.findVariable("z");
				Variable dataZDual = dataFile.findVariable("zDual");
				Variable dataPsiIC = dataFile.findVariable("psiIC");
				Variable dataSpaceDelta = dataFile.findVariable("spaceDelta");
				Variable dataDeltaZ = dataFile.findVariable("deltaZ");
				Variable dataThetaS = dataFile.findVariable("thetaS");
				Variable dataThetaR = dataFile.findVariable("thetaR");
				Variable dataKs = dataFile.findVariable("Ks");
				Variable dataAlphaSS = dataFile.findVariable("alphaSpecificStorage");
				Variable dataBetaSS = dataFile.findVariable("betaSpecificStorage");
				Variable dataPar1SWRC = dataFile.findVariable("par1SWRC");
				Variable dataPar2SWRC = dataFile.findVariable("par2SWRC");
				Variable dataPar3SWRC = dataFile.findVariable("par3SWRC");
				Variable dataPar4SWRC = dataFile.findVariable("par4SWRC");
				Variable dataPar5SWRC = dataFile.findVariable("par5SWRC");
				Variable dataPar6SWRC = dataFile.findVariable("par6SWRC");
				Variable dataPar7SWRC = dataFile.findVariable("par7SWRC");
				Variable dataPar8SWRC = dataFile.findVariable("par8SWRC");
				Variable dataThetaWp = dataFile.findVariable("thetaWp");
				Variable dataThetaFc = dataFile.findVariable("thetaFc");
				Variable dataEt = dataFile.findVariable("et");

				// if (dataEta == null) {
				// System.out.println("Cant find Variable data");
				// return;
				// }

				size = dataEta.getShape();

				eta = new double[size[0]];
				etaDual = new double[size[0]];
				z = new double[size[0]];
				zDual = new double[size[0]];
				psiIC = new double[size[0]];
				spaceDelta = new double[size[0]];
				et = new double[size[0]];

				ArrayDouble.D1 dataArrayEta;
				ArrayDouble.D1 dataArrayEtaDual;
				ArrayDouble.D1 dataArrayZ;
				ArrayDouble.D1 dataArrayZDual;
				ArrayDouble.D1 dataArrayPsiIC;
				ArrayDouble.D1 dataArraySpaceDelta;
				ArrayDouble.D1 dataArrayEt;

				dataArrayEta = (ArrayDouble.D1) dataEta.read(null, size);
				dataArrayEtaDual = (ArrayDouble.D1) dataEtaDual.read(null, size);
				dataArrayZ = (ArrayDouble.D1) dataZ.read(null, size);
				dataArrayZDual = (ArrayDouble.D1) dataZDual.read(null, size);
				dataArrayPsiIC = (ArrayDouble.D1) dataPsiIC.read(null, size);
				dataArraySpaceDelta = (ArrayDouble.D1) dataSpaceDelta.read(null, size);
				dataArrayEt = (ArrayDouble.D1) dataEt.read(null, size);

				for (int i = 0; i < size[0]; i++) {

					eta[i] = dataArrayEta.get(i);
					etaDual[i] = dataArrayEtaDual.get(i);
					z[i] = dataArrayZ.get(i);
					zDual[i] = dataArrayZDual.get(i);
					psiIC[i] = dataArrayPsiIC.get(i);
					spaceDelta[i] = dataArraySpaceDelta.get(i);
					et[i] = dataArrayEt.get(i);

				}

				//////////////////////////
				//////////////////////////
				//////////////////////////

				size = dataDeltaZ.getShape();

				deltaZ = new double[size[0]];
				thetaS = new double[size[0]];
				thetaR = new double[size[0]];
				Ks = new double[size[0]];
				alphaSS = new double[size[0]];
				betaSS = new double[size[0]];
				par1SWRC = new double[size[0]];
				par2SWRC = new double[size[0]];
				par3SWRC = new double[size[0]];
				par4SWRC = new double[size[0]];
				par5SWRC = new double[size[0]];
				par6SWRC = new double[size[0]];
				par7SWRC = new double[size[0]];
				par8SWRC = new double[size[0]];
				thetaWp = new double[size[0]];
				thetaFc = new double[size[0]];
				
				ArrayDouble.D1 dataArrayDeltaZ;
				ArrayDouble.D1 dataArrayThetaS;
				ArrayDouble.D1 dataArrayThetaR;
				ArrayDouble.D1 dataArrayKs;
				ArrayDouble.D1 dataArrayAlphaSS;
				ArrayDouble.D1 dataArrayBetaSS;
				ArrayDouble.D1 dataArrayPar1SWRC;
				ArrayDouble.D1 dataArrayPar2SWRC;
				ArrayDouble.D1 dataArrayPar3SWRC;
				ArrayDouble.D1 dataArrayPar4SWRC;
				ArrayDouble.D1 dataArrayPar5SWRC;
				ArrayDouble.D1 dataArrayPar6SWRC;
				ArrayDouble.D1 dataArrayPar7SWRC;
				ArrayDouble.D1 dataArrayPar8SWRC;
				ArrayDouble.D1 dataArrayThetaWp;
				ArrayDouble.D1 dataArrayThetaFc;
				
				dataArrayDeltaZ = (ArrayDouble.D1) dataDeltaZ.read(null, size);
				dataArrayThetaS = (ArrayDouble.D1) dataThetaS.read(null, size);
				dataArrayThetaR = (ArrayDouble.D1) dataThetaR.read(null, size);
				dataArrayKs = (ArrayDouble.D1) dataKs.read(null, size);
				dataArrayAlphaSS = (ArrayDouble.D1) dataAlphaSS.read(null, size);
				dataArrayBetaSS = (ArrayDouble.D1) dataBetaSS.read(null, size);
				dataArrayPar1SWRC = (ArrayDouble.D1) dataPar1SWRC.read(null, size);
				dataArrayPar2SWRC = (ArrayDouble.D1) dataPar2SWRC.read(null, size);
				dataArrayPar3SWRC = (ArrayDouble.D1) dataPar3SWRC.read(null, size);
				dataArrayPar4SWRC = (ArrayDouble.D1) dataPar4SWRC.read(null, size);
				dataArrayPar5SWRC = (ArrayDouble.D1) dataPar5SWRC.read(null, size);
				dataArrayPar6SWRC = (ArrayDouble.D1) dataPar6SWRC.read(null, size);
				dataArrayPar7SWRC = (ArrayDouble.D1) dataPar7SWRC.read(null, size);
				dataArrayPar8SWRC = (ArrayDouble.D1) dataPar8SWRC.read(null, size);
				dataArrayThetaWp = (ArrayDouble.D1) dataThetaWp.read(null, size);
				dataArrayThetaFc = (ArrayDouble.D1) dataThetaFc.read(null, size);
				
				for (int i = 0; i < size[0]; i++) {

					deltaZ[i] = dataArrayDeltaZ.get(i);
					thetaS[i] = dataArrayThetaS.get(i);
					thetaR[i] = dataArrayThetaR.get(i);
					Ks[i] = dataArrayKs.get(i);
					alphaSS[i] = dataArrayAlphaSS.get(i);
					betaSS[i] = dataArrayBetaSS.get(i);
					par1SWRC[i] = dataArrayPar1SWRC.get(i);
					par2SWRC[i] = dataArrayPar2SWRC.get(i);
					par3SWRC[i] = dataArrayPar3SWRC.get(i);
					par4SWRC[i] = dataArrayPar4SWRC.get(i);
					par5SWRC[i] = dataArrayPar5SWRC.get(i);
					par6SWRC[i] = dataArrayPar6SWRC.get(i);
					par7SWRC[i] = dataArrayPar7SWRC.get(i);
					par8SWRC[i] = dataArrayPar8SWRC.get(i);
					thetaWp[i] = dataArrayThetaWp.get(i);
					thetaFc[i] = dataArrayThetaFc.get(i);
				}
				// Control values
				
				/*System.out.println(dataPar1SWRC + "\n" + par1SWRC[0] + "\n\n" + dataPar2SWRC + "\n" + par2SWRC[0]
						+ "\n\n" + dataPar3SWRC + "\n" + par3SWRC[0] + "\n\n" + dataPar4SWRC + "\n" + par4SWRC[0]
						+ "\n\n" + dataPar5SWRC + "\n" + par5SWRC[0] + "\n\n" + dataPar6SWRC + "\n" + par6SWRC[0]
						+ "\n\n" + dataPar7SWRC + "\n" + par7SWRC[0] + "\n\n" + dataPar8SWRC + "\n" + par8SWRC[0]
						+ "\n\n" + dataThetaS + "\n" + thetaS[0] + "\n\n" + dataThetaR + "\n" + thetaR[0] 
						+ "\n\n"+ dataThetaWp + "\n" +thetaWp[0] +"\n\\n"+  dataThetaFc + "\n" +thetaFc[0] 
						+"\n\\n"+ dataKs + "\n" + Ks[0]
						+ "\n\n" + dataBetaSS + "\n" + betaSS[0] + "\n\n" + dataAlphaSS + "\n" + alphaSS[0]);
				*/
				/*
				 * System.out.println("Check IC values:\n\n"); for (int i = 0; i < size[0]; i++)
				 * {
				 * 
				 * System.out.println("	"+psiIC[i]);
				 * 
				 * }
				 */

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

			System.out.println("*** SUCCESS reading example file " + richardsGridFilename);

		}
		step++;

	}
}