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

package bufferWriter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import oms3.annotations.*;

@Description("Buffer for 1D energy equation simulation.")
@Documentation("")
@Author(name = "Niccolo' Tubini, Riccardo Rigon", contact = "tubini.niccolo@gmail.com")
@Keywords("Hydrology, Energy equation")
//@Label(JGTConstants.HYDROGEOMORPHOLOGY)
//@Name("shortradbal")
//@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")

public class EnergyBuffer1D {
	
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
	public double[] inputSpatialCoordinate;
	
	@Description("Dual spatial coordinate: is the position of volumes' interfaces ")
	@In 
	@Unit ("m")
	public double[] inputDualSpatialCoordinate;
	
	
	@Description()
	@Out
	@Unit ()
	public LinkedHashMap<String,ArrayList<double[]>> myVariable = new LinkedHashMap<String,ArrayList<double[]>>(); // consider the opportunity to save varibale as float instead of double
	
	@Description()
	@Out
	@Unit ()
	public double[] mySpatialCoordinate;
	
	@Description()
	@Out
	@Unit ()
	public double[] myDualSpatialCoordinate;
	
	@Description("")
	int step=0;
	
	ArrayList<double[]> tempVariable;
	
	
	
	@Execute
	public void solve() {
		//System.out.println("Buffer1D step:" + step);
		if(step==0){
			
		mySpatialCoordinate = inputSpatialCoordinate;
		myDualSpatialCoordinate = inputDualSpatialCoordinate;
		
		tempVariable = new ArrayList<double[]>();
		//System.out.println(mySpatialCoordinate.toString());
		
		}
		
		// temperature values
		tempVariable.add(inputVariable.get(0).clone());
		
		// internal energy values
		tempVariable.add(inputVariable.get(1).clone());
		
		// temperature initial condition
		tempVariable.add(inputVariable.get(2).clone());
		
		// energy fluxes
		tempVariable.add(inputVariable.get(3).clone());
		
		// diffusion fluxes
		tempVariable.add(inputVariable.get(4).clone());
		
		// advection fluxes
		tempVariable.add(inputVariable.get(5).clone());
		
		// Peclet number
		tempVariable.add(inputVariable.get(6).clone());
		
		// error energy
		tempVariable.add(inputVariable.get(7).clone());
		
		// air temperature
		tempVariable.add(inputVariable.get(8).clone());

		// short wave radiation
		tempVariable.add(inputVariable.get(9).clone());
		
		// wind velocity
		tempVariable.add(inputVariable.get(10).clone());
		
		// bottom boundary condition for temperature
		tempVariable.add(inputVariable.get(11).clone());
		
		myVariable.put(inputDate,(ArrayList<double[]>) tempVariable.clone());
		//System.out.println(myVariable.size() +"       "+ myVariable.keySet());
		//System.out.println(myVariable.toString());
		tempVariable.clear();
		step++;
		
	}
	

}
