Introduction
============

The HOPtim tool is composed of two parts :
- Java Back End
- Python GUI using the Django framework

The Java Back End runs an embedded mini HTTP server, using jersey-simple-server. This gives access to the Back End features through a RESTful API :
src/main/java/com/dassault_systemes/infra/hoptim/rest/SimpleServer.java

The Python GUI accesses the Back End through a RESTful client :
webapp/hoptimgui/home/client.py

The dependencies, packaging and deployment is handled by Maven both for the Java & Python parts, using various Maven plugins.
All the Maven configuration is contained inside the standard pom.xml file.

Requirements for Java Back End
==============================
- JDK (tested with version 1.7.0_79)
- Maven (tested with version 3.3.9)

Install JDK 7 on Windows :
http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html

Install Maven on Windows :
https://maven.apache.org/guides/getting-started/windows-prerequisites.html

Requirements for Python GUI
===========================
- python (tested with version 3.5)
- pip (tested with version 8.1.2)
- pip2pi (tested with version 0.6.8)

Install Python on Windows :
https://www.python.org/downloads/windows/

Install pip on Windows (the package manager for Python) :
https://pip.pypa.io/en/latest/installing/

Install pip2pi with command line (once pip is installed) :
$ pip install pip2pi

Compilation and deployment using Maven
======================================

You can generate the package for both Java Back End & Python GUI and send to versionning system, all in one command :
$ mvn package integration-test -Dmaven.test.skip=true

Running the Java Back End in localhost (for development)
========================================================

You can run this commande line :
$ java -jar target/hoptim-{version}.jar

The REST server is then running on port 7777 :
http://localhost:7777/api/hello

Running the Python GUI in localhost (for development)
=====================================================

You can run this commande line :
$ python3.5 webapp/hoptimgui/manage.py runserver 0.0.0.0:8070

The GUI is then running on port 8070 :
http://localhost:8070/home

Creating static packages for Python
===================================

The creation of static packages is done inside the pom.xml file. Still for reference, here is the method used to create the static packages for deployment.

What is pip2pi ?
Basically, it builds a PyPI-compatible package repository from pip requirements.
This allows ditribution python packages as static file so that they can be installed on a server without the need to use pip with internet access to PyPi package index.

The requirements for this application are inside the file :
python-requirements.txt

To generate the static packages for application, and store them inside target folder :

$ pip2tgz target/python-packages/ -r python-requirements.txt
$ dir2pi --no-symlink target/python-packages

(the no-symlink is mandatory in running dir2pi on Windows)


Licenses
========

Chart.js :
The MIT License (MIT)
Copyright (c) 2013-2016 Nick Downie

SB Admin 2 :
The MIT License (MIT)
Copyright (c) 2013-2016 Blackrock Digital LLC.

Bootstrap :
The MIT License (MIT)
Copyright (c) 2011-2016 Twitter, Inc.

Jersey :
(Double licence)
CDDL (Common Development and Distribution License)
GPL version 2 (General Public License)

Jackson :
Apache Software License 2.0

Opta Planner :
Apache Software License 2.0

Gson :
Apache Software License 2.0
