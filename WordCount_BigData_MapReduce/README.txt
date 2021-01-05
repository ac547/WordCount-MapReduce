
	$ scp states.tar.gz to NameNode
	$ sudo tar xvf states.tar.gz

Create the folder in HDFS (Not necessary but helpful)

	$ hdfs dfs -mkdir -p HDFS
	$ hdfs dfs -ls HDFS/
	$ hdfs dfs -put 'states' HDFS
	$ hdfs dfs -ls HDFS/states

Run Programs

	$ hadoop jar 'Prog_assignment1.jar' HDFS/states HDFS/result 
	$ hadoop jar 'Prog_assignment2.jar' HDFS/states HDFS/result 

Check Output

	$ hdfs dfs -cat HDFS/result/part-r-00000 



