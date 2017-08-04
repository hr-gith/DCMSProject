package replicaManagement;

import replica1.servers.CenterServers;

public abstract class ReplicaManager {
	public static int id= 1;
	public boolean leaderStatus;
	

	public void start(){
		CenterServers.main(null);
	};



	
}
