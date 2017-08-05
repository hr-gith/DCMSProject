package replicaManagement;

import org.omg.CORBA.ORB;

public abstract class ReplicaManager {
	public static int id= 1;
	public boolean leaderStatus;
	
	public abstract  void start(String argp[]);
	
}
