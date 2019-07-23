package testreader;
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

import monodimensionalProblemTimeDependent.ReadNetCDFEnergyGrid1D;


import org.junit.Test;


/**
 * Test the {@link ReadNetCDFEnergyGrid1D} module.
 * 
 * @author Niccolo' Tubini
 */
public class TestReadNetCDFEnergyGrid1D {

	@Test
	public void Test() throws Exception {
		
		ReadNetCDFEnergyGrid1D read = new ReadNetCDFEnergyGrid1D();
		
		read.energyGridFilename = "resources/input/EnergyMesh_test.nc";
		read.read();

	}

}
	

