# anac2019
the going-to-Macau group diplomacy agent - top secret!

## Prerequisites:
* Java 8
* python 2.7
* maven 3 (might not be required if only working in Intellij or Eclipse as you can import a maven project)

## installing the tournament server:
pip install parlance

## running a tournament:
you will need to either edit the file ParlanceRunner.java, or pass parameters to the program (in intellij do this by going to "Run->edit configurations")

run the TournamentRunner.main() and voilla.

## running the tournament with our agent using maven:
run the following from the project root dir:

* mvn initialize
* mvn clean install -pl ourAgent
* mvn clean install
