/**
 * This package contains the classes to read and write NetCDF files for 1D problems.
 * 
 * Richards-1D
 * 	- ReadNetCDFRichardsGrid1D.java to read the input data for the simulation
 *  - ReadNetCDFRichardsOutput1D.java to read the output file, used within the JUnit Test
 *  - WriteNetCDFRichards1DDouble.java to save the output in double precision
 *  - WriteNetCDFRichards1DFloat.java to save the output in float precision
 *  
 * FreeThaw-1D
 * 	- ReadNetCDFFreezingThawingGrid1D.java to read the input data for the simulation
 *  - ReadNetCDFFreezingThawingOutput1D.java to read the output file, used within the JUnit Test
 *  - WriteNetCDFFreezingThawing1DDouble.java to save the output in double precision
 *  - WriteNetCDFFreezingThawing1DFloat.java to save the output in float precision
 *  - WriteNetCDFFreezingThawing1DSpinupDouble.java to save the output in double precision mainly for spin-up simulation
 * 
 * @author Niccolo` Tubini, Concetta D'Amato
 *
 */
package it.geoframe.blogpsot.netcdf.monodimensionalproblemtimedependent;