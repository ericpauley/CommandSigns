This file is part of CommandSigns.

    CommandSigns is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CommandSigns is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CommandSigns.  If not, see <http://www.gnu.org/licenses/>.
    
Building CommandSigns
--------------------------------------
All libraries required to build CommandSigns are included in the /lib folder. Just
add them to your build path and you're good to go. Note that for releases be sure
you don't export the /lib folder into the jar.

Alternatively, you can use the ant build-file, build.xml. While this file is mainly
for use by the automated build server, running it locally will export CommandSigns.jar
to the /bin directory.
Automatic Builds
---------------------------------
Automatic builds can be found at:
http://mysticrealms.net:8080/job/CommandSigns/