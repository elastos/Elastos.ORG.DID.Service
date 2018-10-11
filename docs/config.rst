Set up client
########################################

.. toctree::
  :maxdepth: 3

.. walk-through:

Walk Through
=================================
The code is written in java . make sure you add jdk in your environment.

1. download the code 

* git clone https://github.com/elastos/Elastos.ORG.DID.Service.git


2. modify application.properties 

* `node.didPrefix` change it to your own local sidechain node .

* `did.address` change it to your own fee collection address . 


3. install project 

* mvn install -Dmaven.test.skip

4. start your project

* java -jar did.api-*.jar

