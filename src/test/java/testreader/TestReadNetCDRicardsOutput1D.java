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


import static org.junit.Assert.assertTrue;
import monodimensionalProblemTimeDependent.ReadNetCDFRichardsOutput1D;
import org.junit.Test;


/**
 * Test the {@link ReadNetCDRicardsOutput} module.
 * 
 * @author Niccolo' Tubini
 */
public class TestReadNetCDRicardsOutput1D {

	@Test
	public void Test() throws Exception {
		
		ReadNetCDFRichardsOutput1D read = new ReadNetCDFRichardsOutput1D();
		
		read.richardsOutputFilename = "resources/input/Richards1DOutput.nc";
		read.read();
		
		assertTrue("Error: runOff is empty", read.runOff.length > 0);
		assertTrue("Error: psi is empty", read.psi.length > 0 & read.psi[0].length>0);
		assertTrue("Error: theta is empty", read.theta.length > 0 & read.theta[0].length>0);

	}

}
	

