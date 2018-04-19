# -*- coding: utf-8 -*-
"""
Created on Tue Apr 17 14:52:22 2018

@author: Niccolo
"""

# the Scientific Python netCDF 3 interface
# http://dirac.cnrs-orleans.fr/ScientificPython/
#from Scientific.IO.NetCDF import NetCDFFile as Dataset
# the 'classic' version of the netCDF4 python interface
# http://code.google.com/p/netcdf4-python/
#from netCDF4_classic import Dataset
from netCDF4 import Dataset
from numpy import arange, dtype # array module from http://numpy.scipy.org
from numpy.testing import assert_array_equal, assert_array_almost_equal


from mpl_toolkits.mplot3d import axes3d
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
import matplotlib.pyplot as plt
import numpy as np
import time
import matplotlib.animation as manimation
"""
This is an example which reads some 4D pressure and
temperatures. The data file read by this program is produced by
the companion program pres_temp_4D_wr.py.

This example demonstrates the netCDF Python API.
It will work either with the Scientific Python NetCDF version 3 interface
(http://dirac.cnrs-orleans.fr/ScientificPython/)
of the 'classic' version of the netCDF4 interface. 
(http://netcdf4-python.googlecode.com/svn/trunk/docs/netCDF4_classic-module.html)
To switch from one to another, just comment/uncomment the appropriate
import statements at the beginning of this file.

Jeff Whitaker <jeffrey.s.whitaker@noaa.gov> 20070202
"""
nrecs = 20; nlevs = 2; nlats = 6; nlons = 12
# open netCDF file for reading.
ncfile = Dataset('hydraulic_2D.nc','r') 
# latitudes and longitudes of grid
lats_check = 25.0 + 5.0*arange(nlats,dtype='float32')
lons_check = -125.0 + 5.0*arange(nlons,dtype='float32')
# output data.
press_check = 900. + arange(nlevs*nlats*nlons,dtype='float32') # 1d array
press_check.shape = (nlevs,nlats,nlons) # reshape to 2d array
temp_check = 9. + arange(nlevs*nlats*nlons,dtype='float32') # 1d array
temp_check.shape = (nlevs,nlats,nlons) # reshape to 2d array
# get latitude, longitude coordinate variable data.
# check to see it is what is expected.
lats = ncfile.variables['latitude']
lons = ncfile.variables['longitude']
depths = ncfile.variables['depth']
time = ncfile.variables['time']

# get pressure, temperature data a record at a time,
# checking to see that the data matches what we expect.
# close the file.
press = ncfile.variables['psi']
temp = ncfile.variables['theta']


print ('*** SUCCESS reading example file pres_temp_4D.nc')

#plt.plot([0,1,2,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20],depths)
plt.plot(press[1],depths)

#lon, lat = np.meshgrid(lons[:], lats[:])
#surf = ax.plot_surface(lat, lon, press[1], cmap=cm.coolwarm,
#                       linewidth=0, antialiased=False)
#
#wframe = None
#tstart = time.time()
#
#fig = plt.figure()
#ax = fig.add_subplot(111, projection='3d')
#    
#for nrec in range(nrecs):
#    
#    oldcol = wframe
#    
#    Z = press[nrec,1]
#    wframe = ax.plot_wireframe(lon, lat, Z, rstride=2, cstride=2)
#
#    # Remove old line collection before drawing
#    #if oldcol is not None:
#    #    ax.collections.remove(oldcol)
#
#    #plt.pause(0.001)
#
#    #print('Pressure: %f' % (100 / (time.time() - tstart)))
#    
    
#ncfile.close()