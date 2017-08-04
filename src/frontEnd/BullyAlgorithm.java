package frontEnd;

import java.util.HashMap;

import staticData.Ports;

public class BullyAlgorithm {


		public String Election(HashMap<Integer, String> replica_id ){
			
			int leader;
              
			int replicaid1=Integer.parseInt(replica_id.get(Ports.RM1UDPPort));
			if(replica_id.get(Ports.RM1UDPPort)==null){
				replica_id.put(Ports.RM1UDPPort, "1000");
			}
				
			int replicaid2=Integer.parseInt(replica_id.get(Ports.RM2UDPPort));
			if(replica_id.get(Ports.RM2UDPPort)==null){
				replica_id.put(Ports.RM2UDPPort, "1001");
			}
			int replicaid3=Integer.parseInt(replica_id.get(Ports.RM3UDPPort));
			if(replica_id.get(Ports.RM3UDPPort)==null){
				replica_id.put(Ports.RM3UDPPort, "1003");
			}
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
				
				return Integer.toString(leader);
			
		}
}

