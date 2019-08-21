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

package bufferWriter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import oms3.annotations.*;

@Description("Buffer for 1D Richards simulation.")
@Documentation("")
@Author(name = "Niccolo' Tubini, Riccardo Rigon", contact = "tubini.niccolo@gmail.com")
@Keywords("Hydrology, Richards, Infiltration")
//@Label(JGTConstants.HYDROGEOMORPHOLOGY)
//@Name("shortradbal")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class RichardsBuffer2D {
	
	@Description("Varible to store")
	@In 
	@Unit ("-")
	public ArrayList<double[]> inputVariable;
	
	@Description("Date at which the varible is computed")
	@In 
	@Unit ("YYYY-MM-DD HH:mm")
	public String inputDate;
	
	@Description("Spatial coordinate: is the position of the centroids ")
	@In 
	@Unit ("m")
	public Map<Integer, Double[]> inputSpatialCoordinate;
	
	@Description("Dual spatial coordinate: is the position of edges ")
	@In 
	@Unit ("m")
	public Map<Integer, Double[]> inputDualSpatialCoordinate;
	
	
	@Description()
	@Out
	@Unit ()
	public LinkedHashMap<String,ArrayList<double[]>> myVariable = new LinkedHashMap<String,ArrayList<double[]>>(); // consider the opportunity to save varibale as float instead of double
	
	@Description()
	@Out
	@Unit ()
	public double[] mySpatialCoordinateX;
	
	@Description()
	@Out
	@Unit ()
	public double[] mySpatialCoordinateZ;
	
	@Description()
	@Out
	@Unit ()
	public double[] myDualSpatialCoordinateX;
	
	@Description()
	@Out
	@Unit ()
	public double[] myDualSpatialCoordinateZ;
	
	@Description("")
	int step=0;
	
	ArrayList<double[]> tempVariable;
	
	
	
	@Execute
	public void solve() {
		//System.out.println("Buffer1D step:" + step);
		if(step==0){

			mySpatialCoordinateX = new double[inputSpatialCoordinate.size()+1];
			mySpatialCoordinateZ = new double[inputSpatialCoordinate.size()+1];
			
			myDualSpatialCoordinateX = new double[inputDualSpatialCoordinate.size()+1];
			myDualSpatialCoordinateZ = new double[inputDualSpatialCoordinate.size()+1];
			
			for(Integer i : inputSpatialCoordinate.keySet()) {
				mySpatialCoordinateX[i] = inputSpatialCoordinate.get(i)[0];
				mySpatialCoordinateZ[i] = inputSpatialCoordinate.get(i)[1];
			};
			
			for(Integer i : inputDualSpatialCoordinate.keySet()) {
				myDualSpatialCoordinateX[i] = inputDualSpatialCoordinate.get(i)[0];
				myDualSpatialCoordinateZ[i] = inputDualSpatialCoordinate.get(i)[1];
				
//				System.out.println(myDualSpatialCoordinateX[i] + "  " + myDualSpatialCoordinateZ[i]);
			};

//					myDualSpatialCoordinate = ???;

			tempVariable = new ArrayList<double[]>();
			//System.out.println(mySpatialCoordinate.toString());

		}
		
		
		// psiIC
		tempVariable.add(inputVariable.get(0).clone());
		
		// water suction values
		tempVariable.add(inputVariable.get(1).clone());
		
		// thetas
		tempVariable.add(inputVariable.get(2).clone());
		
		// saturation degree
		tempVariable.add(inputVariable.get(3).clone());
		
		// Darcy velocities
		tempVariable.add(inputVariable.get(4).clone());
//		for(int i=0; i<inputVariable.get(2).length; i++) {
//			System.out.println(inputVariable.get(2)[i]);
//		}
//		System.out.println("\\\\\\\\\\\\ \n\n");
//		// Darcy velocities x component
		tempVariable.add(inputVariable.get(5).clone());
//		for(int i=0; i<inputVariable.get(3).length; i++) {
//			System.out.println(inputVariable.get(3)[i]);
//		}
//		System.out.println("\\\\\\\\\\\\ \n\n");
		// Darcy velocities z component
		tempVariable.add(inputVariable.get(6).clone());
//		for(int i=0; i<inputVariable.get(4).length; i++) {
//			System.out.println(inputVariable.get(4)[i]);
//		}		
//		// Darcy velocities due to capillary gradient
//		tempVariable.add(inputVariable.get(4).clone());
//		
//		// Darcy velocities due to gravity gradient
//		tempVariable.add(inputVariable.get(5).clone());
//		
//		// pore velocities 
//		tempVariable.add(inputVariable.get(6).clone());
//		
//		// celerities
//		tempVariable.add(inputVariable.get(7).clone());
//		
//		// kinematic ratio
//		tempVariable.add(inputVariable.get(8).clone());
//		
//		// errorVolume
//		tempVariable.add(inputVariable.get(9).clone());
//		
//		// top boundary condition value
//		tempVariable.add(inputVariable.get(10).clone());
//		
//		// bottom boundary condition value
//		tempVariable.add(inputVariable.get(11).clone());
//		
//		// surface run-off
//		tempVariable.add(inputVariable.get(12).clone());

		myVariable.put(inputDate,(ArrayList<double[]>) tempVariable.clone());
		//System.out.println(myVariable.size() +"       "+ myVariable.keySet());
		//System.out.println(myVariable.toString());
		tempVariable.clear();
		step++;
		
	}
	

}
