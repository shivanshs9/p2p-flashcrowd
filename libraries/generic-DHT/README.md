# peersim-generic-DHT
Generic DHT anonymous protocol simulator build on the PeerSim framework.

Main class: peersim.Simulator
Arguments: example.cfg
Class Path: add contents of /lib


# Instructions from original project

---------------------------------
Generic DHT for the Peersim Simulator
---------------------------------

The jar files of peersim must be downloaded separately.
An example exmaple.cfg configuration file can be found in the 
main directory.

---------------------------------
Makefile
---------------------------------

To run the Makefile, modify the PEERSIM_JARS variable to point to 
your peersim installation, or copy the peersim jar files to the lib
directory. 

To compile the sources, invoke:

  make

To compile the API documentation, invoke:

  make doc

To run the code, invoke:

  make run

To run all the previous command in this order, invoke:

  make all
