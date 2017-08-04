package frontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import classManagement.Record;
import classManagement.TeacherRecord;
import replica1.ReplicaManager1;
import replicaManagement.*;
import CORBAClassManagement.*;

public class FrontEnd extends CORBAClassManagementPOA implements Runnable {

	public int UDPPort = 9000;
	public static String leaderInfo;

	private ORB orb;

	public ORB getOrb() {
		return orb;
	}

	public void setOrb(ORB orb) {
		this.orb = orb;
	}

	public FrontEnd(int udpPort) {
		this.UDPPort = udpPort;
	}

	public boolean login(String managerID) {
		Request req = new Request();
		req.managerID = managerID;
		req.typeOfRequest = Request.LOGIN_REQUEST;
		FIFOQueue.getInstance().push(req);

		/// ?????????????????how should we get response
		return false;
	}

	public void logout() {
		Request req = new Request();
		req.typeOfRequest = Request.LOGIN_REQUEST;
		FIFOQueue.getInstance().push(req);
	}

	public boolean createTRecord(String managerID, String firstName, String lastName, String address, String phone,
			String specialization, String location) {
		Request req = new Request();
		req.typeOfRequest = Request.CREATE_TEACHER_REQUEST;
		req.managerID = managerID;
		req.firstName = firstName;
		req.lastName = lastName;
		req.address = address;
		req.phone = phone;
		req.specialization = specialization;
		req.location = location;
		FIFOQueue.getInstance().push(req);
		return false;
	}

	public String getRecordCounts() {
		Request req = new Request();
		req.typeOfRequest = Request.GET_COUNT_REQUEST;
		FIFOQueue.getInstance().push(req);

		return null;
	}

	public boolean editRecord(String managerID, String recordID, String fieldName, String newValue) {
		Request req = new Request();
		req.typeOfRequest = Request.EDIT_REQUEST;
		req.managerID = managerID;
		req.recordID = recordID;
		req.fieldName = fieldName;
		req.newValue = newValue;
		FIFOQueue.getInstance().push(req);

		return false;
	}

	public boolean createSRecord(String managerID, String firstName, String lastName, String coursesRegistered,
			boolean status, String statusDate) {
		Request req = new Request();
		req.typeOfRequest = Request.CREATE_STUDENT_REQUEST;
		req.managerID = managerID;
		req.firstName = firstName;
		req.lastName = lastName;
		req.courseRegistered = coursesRegistered;
		req.status = status;
		FIFOQueue.getInstance().push(req);
		return false;
	}

	public boolean transferRecord(String managerID, String recordID, String remoteCenterServerName) {
		Request req = new Request();
		req.typeOfRequest = Request.TRANSFER_REQUEST;
		req.managerID = managerID;
		req.recordID = recordID;
		req.remoteCenterServerName = remoteCenterServerName;
		FIFOQueue.getInstance().push(req);

		return false;
	}

	public void run() {
		boolean RM = false;
		boolean RM2 = false;
		boolean Rm3 = false;
		System.out.println("I am in run of Frontend!!!!");
		// To start the RM1
		ReplicaManager1 RM1 = new ReplicaManager1();
		// RM1.start(null);
		//RM1.HearBeat();

		// To start the RM2

		// To start the RM1

		// UDP to listen to RM's Heartbeat

		DatagramSocket datagramSocket = null;
		String heartBeat ="  ";
		HashMap<Integer, String> replica_info = new HashMap<Integer, String>();
		try {

			datagramSocket = new DatagramSocket(9000);
			byte[] bufferReceive = new byte[50];
			byte[] bufferSend = new byte[50];
			while (true) {
				System.out.println("I am in run of Frontend");
				DatagramPacket receivedPacket = new DatagramPacket(bufferReceive, bufferReceive.length);
				datagramSocket.setSoTimeout(1000);
				while(true){
					try{
						datagramSocket.receive(receivedPacket);		
					    heartBeat = new String(receivedPacket.getData());
						System.out.println("Received the heartbeat" + heartBeat);
						String[] splitted = heartBeat.split("!");
						System.out.println("Splitted String is " + splitted[1]);

						System.out.println("Port number at server run , fetched from UDP requuest ");
						int portfetched = receivedPacket.getPort();
						System.out.println("Port received" + portfetched);
						
						String portnum = splitted[1].trim();
						
						replica_info.put(3666, splitted[1].trim());
						replica_info.put(3667, "2");
						replica_info.put(3668, "3");
						
						System.out.println(replica_info.get(9000));
						System.out.println(replica_info.get(9002));
						System.out.println(replica_info.get(9003));

						// replica_info.put(9004, Integer.valueOf("4"));

						
						// bufferSend =
						// Integer.toString(recordcount(receivedPacket.getPort())).getBytes();

						DatagramPacket sendPackets = new DatagramPacket(bufferSend, bufferSend.length,
								receivedPacket.getAddress(), receivedPacket.getPort());
						// datagramSocket.send(sendPackets);


					}
					catch(SocketTimeoutException e){
						System.out.println("Timed out");
						if (heartBeat == " ") {
							if (this.UDPPort == 3666) {
								BullyAlgorithm obj = new BullyAlgorithm();
								Integer information = Integer.parseInt(obj.Election(replica_info));
								getKeyFromValue(replica_info, obj.Election(replica_info));
								// this.UDPPort =
								// Integer.parseInt(replica_info.get(information).trim());
								int keyValue= getKeyFromValue(replica_info, obj.Election(replica_info));
								System.out.println("New Leader port is" + replica_info.get(information) + this.UDPPort + information);
								if(keyValue==3666)
								this.leaderInfo = "3666";
								else if(keyValue==3667){
									this.leaderInfo = "3666";
								}
								else{
									this.leaderInfo="3667";
								}
							} 
							
							else {
								if (replica_info.get(3667) == null)
									RM1.start(null);
								else if (replica_info.get(3668) == null) {
									// RM2.start(null);
								}

								// else
								// Rm3.start(null);

							}
						}

						
					}
				}
				
				
	}
		} catch (SocketException ex) {
			System.out.println("Socket " + ex.getMessage());
		} catch (IOException e) {

			System.out.println("IO :" + e.getMessage());

		}

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

	public static void main(String[] args) {
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);
			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// get the root naming context
			// NameService invokes the name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// Create object reference of "MTL Server" and bind it to the
			// registry(name service)
			FrontEnd frontEnd = new FrontEnd(9000);
			frontEnd.setOrb(orb);
			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(frontEnd);
			CORBAClassManagement href = CORBAClassManagementHelper.narrow(ref);
			// bind the Object Reference in Naming
			String name = "FrontEnd";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);
			System.out.println("Front End is started..");
			(new Thread(frontEnd)).start();
			orb.run();

		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}

}
