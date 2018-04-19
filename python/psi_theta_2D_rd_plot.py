# -*- coding: utf-8 -*-
"""
Created on Tue Apr 17 14:52:22 2018

@author: Niccolo` Tubini

This code was obtained modifing the code pres_temp_4D_rd.py 
by Jeff Whitaker <jeffrey.s.whitaker@noaa.gov> 20070202
https://www.unidata.ucar.edu/software/netcdf/examples/programs/

This is an example which reads some 2D hydraulic head and
adimensional water content. 
The data file read by this program is produced by
the java class WriteMain.

This code read the file hydraulic_2D.nc, prints variables' attributes, size and
other usefull stuff, and plots data
"""

# the Scientific Python netCDF 3 interface
# http://dirac.cnrs-orleans.fr/ScientificPython/
#from Scientific.IO.NetCDF import NetCDFFile as Dataset
# the 'classic' version of the netCDF4 python interface
# http://code.google.com/p/netcdf4-python/
#from netCDF4_classic import Dataset
from netCDF4 import Dataset

# plotting
import matplotlib.pyplot as plt

# to convert unix time to human readable date
import time
import datetime


##########
##########

# open netCDF file for reading.
ncfile = Dataset('hydraulic_2D.nc','r') 
print('\n ***FILE INFO:\n')
print(ncfile)
# other usefull commands:
#print (ncfile.dimensions['time'])
#print (ncfile.file_format)
#print (ncfile.dimensions.keys())
#print (ncfile.variables.keys())
#print (ncfile.variables['psi'])


depths = ncfile.variables['depth']
print('\n ***DEPTHS INFO:\n')
print(depths)
time = ncfile.variables['time']
print('\n ***TIME INFO:\n')
print(time)

psi = ncfile.variables['psi']
print('\n ***PSI INFO:\n')
print(psi)

theta = ncfile.variables['theta']
print('\n ***THETA INFO:\n')
print(theta)

print ('*** SUCCESS reading')

print('\n\n*** SOME PLOTS')

# enter the time index you want to plot
timeIndex = 1;
value = datetime.datetime.fromtimestamp(time[timeIndex])

plt.plot(psi[timeIndex],depths,'b')
# convert time value in a human readable date to title the plot
plt.title(value.strftime('%Y-%m-%d %H:%M'))
# use variable attributes to label axis
plt.xlabel(ncfile.variables['psi'].long_name + '  [' +ncfile.variables['psi'].units +']' )
plt.ylabel(ncfile.variables['depth'].long_name + '  [' +ncfile.variables['depth'].units +']' )
plt.grid()
plt.show()

plt.plot(theta[timeIndex],depths,'r')
# convert time value in a human readable date to title the plot
plt.title(value.strftime('%Y-%m-%d %H:%M'))
# use variable attributes to label axis
plt.xlabel(ncfile.variables['theta'].long_name + '  [' +ncfile.variables['theta'].units +']' )
plt.ylabel(ncfile.variables['depth'].long_name + '  [' +ncfile.variables['depth'].units +']' )
plt.grid()
plt.show()
    



#ncfile.close()