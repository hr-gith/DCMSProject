package replicaManagement;

import org.omg.CORBA.ORB;

import replica1.servers.CenterServers;

public abstract class ReplicaManager {
	public String id;
	public boolean leaderStatus;
	
	public void start(){
		CenterServers.main(null);
	};
	
}
