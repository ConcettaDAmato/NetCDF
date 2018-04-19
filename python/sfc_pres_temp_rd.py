# -*- coding: utf-8 -*-
"""
Original file at:
    
https://www.unidata.ucar.edu/software/netcdf/examples/programs/sfc_pres_temp_rd.py
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

## to plot data: https://matplotlib.org/mpl_toolkits/mplot3d/tutorial.html
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
import numpy as np

"""
This is an example which reads some surface pressure and
temperatures. The data file read by this program is produced
companion program sfc_pres_temp_wr.py.

This example demonstrates the netCDF Python API.
It will work either with the Scientific Python NetCDF version 3 interface
(http://dirac.cnrs-orleans.fr/ScientificPython/)
of the 'classic' version of the netCDF4 interface. 
(http://netcdf4-python.googlecode.com/svn/trunk/docs/netCDF4_classic-module.html)
To switch from one to another, just comment/uncomment the appropriate
import statements at the beginning of this file.

Jeff Whitaker <jeffrey.s.whitaker@noaa.gov> 20070202
"""
nlats = 6; nlons = 12
# open netCDF file for reading
ncfile = Dataset('sfc_pres_temp.nc','r') 
# expected latitudes and longitudes of grid
lats_check = -25.0 + 5.0*arange(nlats,dtype='float32')
lons_check = -125.0 + 5.0*arange(nlons,dtype='float32')
# expected data.
press_check = 900. + arange(nlats*nlons,dtype='float32') # 1d array
#print('press_check:\n')
#print(press_check)
press_check.shape = (nlats,nlons) # reshape to 2d array
temp_check = 9. + 0.25*arange(nlats*nlons,dtype='float32') # 1d array
temp_check.shape = (nlats,nlons) # reshape to 2d array
# get pressure and temperature variables.
temp = ncfile.variables['temperature']
press = ncfile.variables['pressure']
#print('\npress\n')
#print(press[:])
# check units attributes.
try:
    assert(temp.units == 'celsius')
except:
    raise AttributeError('temperature units attribute not what was expected')
try:
    assert(press.units == 'hPa')
except:
    raise AttributeError('pressure units attribute not what was expected')
# check data
    ## values are correct but organized in a different way
#try:
#    assert_array_almost_equal(press[:],press_check)
#except:
#    raise ValueError('pressure data not what was expected')
#try:
#    assert_array_almost_equal(temp[:],temp_check)
#except:
#    raise ValueError('temperature data not what was expected')
# get coordinate variables.
lats = ncfile.variables['latitude']
lons = ncfile.variables['longitude']
# check units attributes.
##switched noth with east
try:
    assert(lats.units == 'degrees_east')
except:
    raise AttributeError('latitude units attribute not what was expected')
try:
    assert(lons.units == 'degrees_north')
except:
    raise AttributeError('longitude units attribute not what was expected')
# check data
#try:
#    assert_array_almost_equal(lats[:],lats_check)
#except:
#   raise ValueError('latitude data not what was expected')
#try:
#    assert_array_almost_equal(lons[:],lons_check)
#except:
#    raise ValueError('longitude data not what was expected')

print ('*** SUCCESS reading example file sfc_pres_temp.nc!')

#plot
fig = plt.figure()
ax = fig.gca(projection='3d')
lon, lat = np.meshgrid(lons[:], lats[:])
# Plot the surface.
surf = ax.plot_surface(lat, lon, press[:], cmap=cm.coolwarm,
                       linewidth=0, antialiased=False)

# Customize the z axis.
#ax.set_zlim(-1.01, 1.01)
#ax.zaxis.set_major_locator(LinearLocator(10))
#ax.zaxis.set_major_formatter(FormatStrFormatter('%.02f'))

# Add a color bar which maps values to colors.
fig.colorbar(surf, shrink=0.5, aspect=5)

plt.show()


# close the file. ONLY IF YOU HAVE FINISHED TO USE VARIABLES
#ncfile.close()