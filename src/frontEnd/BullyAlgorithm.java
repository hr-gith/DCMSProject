package frontEnd;

import java.util.HashMap;
import java.util.Map.Entry;

import replica1.ReplicaManager1;
import replica2.ReplicaManager2;
import replica3.ReplicaManager3;
import staticData.Ports;

public class BullyAlgorithm {


	public String Election(HashMap<Integer, String> replica_id ){
		System.out.println("In election Algorithm");
		int leader;
      
        if(replica_id.get(Ports.RM1UDPPort)==null){
			replica_id.put(Ports.RM1UDPPort, "0");
		}
		int replicaid1=Integer.parseInt(replica_id.get(Ports.RM1UDPPort).trim());
		
		
		if(replica_id.get(Ports.RM2UDPPort)==null){
			replica_id.put(Ports.RM2UDPPort, "0");
		}
		int replicaid2=Integer.parseInt(replica_id.get(Ports.RM2UDPPort).trim());
		
		if(replica_id.get(Ports.RM3UDPPort)==null){
			replica_id.put(Ports.RM3UDPPort, "0");
		}
		int replicaid3=Integer.parseInt(replica_id.get(Ports.RM3UDPPort).trim());
		System.out.println(replicaid1);
		System.out.println(replicaid2);
		System.out.println(replicaid3);
		//Leader
			
			
			if(Integer.compare(replicaid1, replicaid2) >0 && Integer.compare(replicaid1, replicaid3)>0){
				leader=replicaid1;
			}
			else if(Integer.compare(replicaid2, replicaid3)>0 && Integer.compare(replicaid2, replicaid1)>0){
				leader=replicaid2;
			
			}
			else{
				leader =replicaid3;
			}
			
			if(getKeyFromValue(replica_id,Integer.toString(leader))== Ports.RM1UDPPort){
				new Thread(new Runnable(){

					public void run() {
		
						ReplicaManager1.main(null);	
					}
						
				}).start();
			}
			else if(getKeyFromValue(replica_id,Integer.toString(leader))== Ports.RM2UDPPort){
				new Thread(new Runnable(){

					public void run() {
		
						ReplicaManager2.main(null);	
					}
						
				}).start();
			}
			
			else{
				new Thread(new Runnable(){

					public void run() {
		
						ReplicaManager3.main(null);	
					}
						
				}).start();
				
			}
			return Integer.toString(leader);
		
	}
	
	public Integer getKeyFromValue(HashMap<Integer, String> hashmap, String id) {

		int key = 0;
		for (Entry<Integer, String> entry : hashmap.entrySet()) {
			if (entry.getValue().equals(id)) {
				// System.out.println(entry.getKey());
				key = entry.getKey();
			}
		}
		return key;
	}
}

