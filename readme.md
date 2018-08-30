# Coreference Annotation of Large Datasets (Coalda)

This is code for my diploma thesis "Analysis and Visualization of Coreference Features" published in 2010. Since then, there has not been any further work on this.


## Content

The folder "src" contains the Code of the Coalda software.

The folder "doc" contains the documentation of the code as created automatically by javadoc.

The folder "manual" contains an installation manual and a user guide for the software.

The folder "db" contains files for the creation of the database that is used by Coalda.

The folder "exampledata" contains some example data that can be imported to the database and used for visualization with Coalda. This data is a short excerpt of "Oliver Twist" written by Charles Dickens.

## Running Coalda

The software needs MatLab with the toolbox `somtoolbox` and a specific SOM Server written for it. This is not part of this repository as it was not my work in the thesis. If you are interested, contact me and we can sort it out.

For more information see the installation and user manual.

### Adjust coalda.properties in the root directory
Settings in coalda.properties (*-ed properties are required for correct functioning) : 
textFile, wordFile, markableFile, linkFile, featureFile : Only needed for importing corpus or feature information to the database. Give path to files.
dbCreateScript : Only needed for importing corpus or feature information to the database. Give path to file.
featureDefinitionsFile : Used for displaying the names of the features used for producing the data. Will throw an error if the file is not found, but is otherwise not important.
dbLocation* : Location of the database with the feature vectors and corpus information. Format should be "jdbc:postgresql://<db server name>/<db name>". Always required. 
dbUser* : Database user name. Always required.
dbPassword* : Database password. Always required.
matlabServer* : Computer where the matlab server is running. Always required.

### To compile (from root directory):
Coalda runs with Java 1.6
 ```
javac -classpath lib/prefuse.jar:lib/postgresql-8.1-414.jdbc3.jar:lib/de.unistuttgart.ais.sukre.refinery_100202.jar:lib/miglayout-3.7.jar:.: coalda/CoaldaVis.java
```

### To run:
```
java -classpath lib/prefuse.jar:lib/postgresql-8.1-414.jdbc3.jar:lib/de.unistuttgart.ais.sukre.refinery_100202.jar:lib/miglayout-3.7.jar:lib/simpleorm-dataset.jar:lib/simple-log.jar:lib/simpleorm-sessionJdbc.jar:.: coalda.CoaldaVis
```
To load a specific calculation ID just write it behind the command, for example to load calculation ID 93 try
```
java -classpath lib/prefuse.jar:lib/postgresql-8.1-414.jdbc3.jar:lib/de.unistuttgart.ais.sukre.refinery_100202.jar:lib/miglayout-3.7.jar:lib/simpleorm-dataset.jar:lib/simple-log.jar:lib/simpleorm-sessionJdbc.jar:.: coalda.CoaldaVis 93
```

## Licence and references

(c) Wiltrud Kessler

This code is made available under a [BSD licence 2.0](https://opensource.org/licenses/BSD-3-Clause).


Please reference this work for more information and cite it, if you use the software:
Wiltrud Kessler (2010)
Analysis and Visualization of Coreference Features.
Diplomarbeit, Universität Stuttgart & Universidad Politécnica de Madrid.
