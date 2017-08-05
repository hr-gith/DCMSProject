package frontEnd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import classManagement.Record;
import classManagement.TeacherRecord;
import replica1.ReplicaManager1;
import replica1.utilities.EventLogger;
import replica2.ReplicaManager2;
import replica3.ReplicaManager3;
import replicaManagement.*;
import staticData.Ports;
import CORBAClassManagement.*;

public class FrontEnd extends CORBAClassManagementPOA implements Runnable {

	public int UDPPort;
	public static int leaderPort = Ports.RM1UDPPort;
	boolean alive1 = false;
	boolean alive2 = false;
	boolean alive3 = false;
	HashMap<Integer, String> replica_info = new HashMap<Integer, String>();
	private EventLogger logger = null;
	private ORB orb;

	public ORB getOrb() {
		return orb;
	}

	public void setOrb(ORB orb) {
		this.orb = orb;
	}

	public FrontEnd() {
		this.UDPPort = Ports.FEUDPPort;
		this.logger = new EventLogger("FrontEnd");
	}

	public boolean createTRecord(String managerID, String firstName, String lastName, String address, String phone,
			String specialization, String location) {
		Request req = new Request();
		req.typeOfRequest = Request.CREATE_TEACHER_REQUEST;
		req.managerID = managerID;
		req.recordID = SequenceIdGenerator.getID("TR");
		req.firstName = firstName;
		req.lastName = lastName;
		req.address = address;
		req.phone = phone;
		req.specialization = specialization;
		req.location = location;
		logger.setMessage(managerID + ": Teacher record " + req.recordID + " has been forwarded to RM-Leader");
		String result = UDPClient(req);
		if (result.startsWith("true")) {
			logger.setMessage(managerID + ": Teacher record " + req.recordID + " has been sucessfull.");
			return true;
		} else
			return false;
	}

	public String getRecordCounts() {
		Request req = new Request();
		req.typeOfRequest = Request.GET_COUNT_REQUEST;
		String result = UDPClient(req);

		return result;
	}

	public boolean editRecord(String managerID, String recordID, String fieldName, String newValue) {
		Request req = new Request();
		req.typeOfRequest = Request.EDIT_REQUEST;
		req.managerID = managerID;
		req.recordID = recordID;
		req.fieldName = fieldName;
		req.newValue = newValue;
		String result = UDPClient(req);
		logger.setMessage(
				managerID + ": Editing request of record " + req.recordID + " has been forwarded to RM-Leader");
		if (result.startsWith("true")) {
			logger.setMessage(managerID + ": Record " + req.recordID + " has been sucessfully edited.");
			return true;

		} else
			return false;
	}

	public boolean createSRecord(String managerID, String firstName, String lastName, String coursesRegistered,
			boolean status, String statusDate) {
		Request req = new Request();
		req.typeOfRequest = Request.CREATE_STUDENT_REQUEST;
		req.managerID = managerID;
		req.recordID = SequenceIdGenerator.getID("SR");
		req.firstName = firstName;
		req.lastName = lastName;
		req.courseRegistered = coursesRegistered;
		req.status = status;
		String result = UDPClient(req);
		logger.setMessage(managerID + ": Student record " + req.recordID + " has been forwarded to RM-Leader");
		if (result.startsWith("true")) {
			logger.setMessage(managerID + ": Student record " + req.recordID + " has been sucessfull.");
			return true;

		} else
			return false;
	}

	public boolean transferRecord(String managerID, String recordID, String remoteCenterServerName) {
		Request req = new Request();
		req.typeOfRequest = Request.TRANSFER_REQUEST;
		req.managerID = managerID;
		req.recordID = recordID;
		req.remoteCenterServerName = remoteCenterServerName;
		String result = UDPClient(req);
		if (result.startsWith("true"))
			return true;
		else
			return false;
	}

	public String UDPClient(Request req) {
		String result = "";
		DatagramSocket socket = null;
		logger.setMessage("Transfer of request started to RM-Leader : " + this.leaderPort);
		try {
			synchronized (req) {
				while (leaderPort == 0) {
					System.out.println("leader port is 0");
				}
				;

				// serialize the message object
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutput oo = new ObjectOutputStream(bos);
				oo.writeObject(req);
				oo.close();
				System.out.println(
						"111Record " + req.recordID + " has been forwarded to RM-Leader with port" + this.leaderPort+"front end port"+this.UDPPort);
				
				socket = new DatagramSocket(this.UDPPort);
				InetAddress host = InetAddress.getByName("localhost");
				byte[] serializedMsg = bos.toByteArray();
				System.out.println(
						"Record " + req.recordID + " has been forwarded to RM-Leader with port" + this.leaderPort);
				DatagramPacket request = new DatagramPacket(serializedMsg, serializedMsg.length, host, this.leaderPort);
				socket.send(request);

				// send reply back to client
				byte[] buffer = new byte[1000];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				socket.receive(reply);
				result = new String(reply.getData());
			}
		} catch (SocketException s) {
			System.out.println("Socket: " + s.getMessage());
		} catch (Exception e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
		return result;

	}

	public void checkIfAlive() {

		System.out.println("I am in check if alive");
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				System.out.println("I am run of alive");
				if (alive1 == false && leaderPort == Ports.RM1UDPPort) {
					System.out.println("RM1 1 has failed, calling elction algorithm");
					BullyAlgorithm obj = new BullyAlgorithm();
					Integer information = Integer.parseInt(obj.Election(replica_info));
					getKeyFromValue(replica_info, obj.Election(replica_info));
					int keyValue = getKeyFromValue(replica_info, obj.Election(replica_info));
					System.out.println("New Leader port is" + replica_info.get(information) + UDPPort + information);
					leaderPort = keyValue;

				} else {
					System.out.println("RM1 has failed and starting it now");
					if (alive1 == false)
						new Thread(new Runnable() {

							public void run() {

								ReplicaManager1.main(null);
							}

						}).start();

				}
				if (alive2 == false && leaderPort == Ports.RM2UDPPort) {
					BullyAlgorithm obj = new BullyAlgorithm();
					Integer information = Integer.parseInt(obj.Election(replica_info));
					getKeyFromValue(replica_info, obj.Election(replica_info));
					int keyValue = getKeyFromValue(replica_info, obj.Election(replica_info));
					System.out.println("New Leader port is" + replica_info.get(information) + UDPPort + information);
					leaderPort = keyValue;
				}

				else {
					if (alive2 == false)

						new Thread(new Runnable() {

							public void run() {
								ReplicaManager2.main(null);
							}

						}).start();

				}
				if (alive3 == false && leaderPort == Ports.RM3UDPPort) {
					BullyAlgorithm obj = new BullyAlgorithm();
					Integer information = Integer.parseInt(obj.Election(replica_info));
					getKeyFromValue(replica_info, obj.Election(replica_info));
					// this.UDPPort =
					// Integer.parseInt(replica_info.get(information).trim());
					int keyValue = getKeyFromValue(replica_info, obj.Election(replica_info));
					System.out.println("New Leader port is" + replica_info.get(information) + UDPPort + information);
					leaderPort = keyValue;
				}

				else {
					if (alive3 == false)

						new Thread(new Runnable() {

							public void run() {
								ReplicaManager3.main(null);
							}

						}).start();

				}

			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, 30000, 40000);

	}

	public void run() {
		boolean RM = false;
		boolean RM2 = false;
		boolean Rm3 = false;
		System.out.println("I am in run of Frontend!!!!");

		// To start the RM1
		new Thread(new Runnable() {
			public void run() {

				ReplicaManager1.main(null);
				alive1 = true;
			}

		}).start();

		// To start the RM2
		new Thread(new Runnable() {
			public void run() {
				ReplicaManager2.main(null);
				alive2 = true;
			}

		}).start();

		// To start the RM3
		new Thread(new Runnable() {
			public void run() {
				ReplicaManager3.main(null);
				alive3 = true;
			}

		}).start();
		checkIfAlive();
		// UDP to listen to RM's Heartbeat

		DatagramSocket datagramSocket = null;

		String heartBeat = "  ";

		try {

			datagramSocket = new DatagramSocket(Ports.FEUDPPortHearbeat);
			byte[] bufferReceive = new byte[50];
			byte[] bufferSend = new byte[50];
			while (true) {
				System.out.println("I am in run of Frontend");
				DatagramPacket receivedPacket = new DatagramPacket(bufferReceive, bufferReceive.length);
				// datagramSocket.setSoTimeout(1000);
				while (true) {
					try {
						/*
						 * RM = false; RM2 = false; Rm3 = false;
						 */
						datagramSocket.receive(receivedPacket);
						heartBeat = new String(receivedPacket.getData());
						System.out.println("Received the heartbeat" + heartBeat);
						String[] splitted = heartBeat.split("!");
						System.out.println("Splitted String is " + splitted[1]);

						System.out.println("Port number at server run , fetched from UDP requuest ");
						int portFetched = Integer.parseInt(splitted[2].trim());
						System.out.println("Port received" + portFetched);

						if (portFetched == Ports.RM1UDPPortHearbeat) {
							replica_info.put(Ports.RM1UDPPortHearbeat, splitted[1].trim());
							if (splitted[1].trim() != null) {
								alive1 = true;
							}
						}

						else if (portFetched == Ports.RM2UDPPortHearbeat) {
							replica_info.put(Ports.RM2UDPPortHearbeat, splitted[1].trim());
							if (splitted[1].trim() != null) {
								alive2 = true;
							}
						} else if (portFetched == Ports.RM3UDPPortHearbeat) {
							replica_info.put(Ports.RM3UDPPortHearbeat, splitted[1].trim());
							if (splitted[1].trim() != null) {
								alive3 = true;
							}
						}

						System.out.println(replica_info.get(Ports.RM1UDPPortHearbeat));
						System.out.println(replica_info.get(Ports.RM2UDPPortHearbeat));
						System.out.println(replica_info.get(Ports.RM3UDPPortHearbeat));

						/*
						 * DatagramPacket sendPackets = new
						 * DatagramPacket(bufferSend, bufferSend.length,
						 * receivedPacket.getAddress(),
						 * receivedPacket.getPort());
						 */

					}

					catch (SocketTimeoutException e) {

					}
				}
			}
		}

		catch (SocketException ex) {
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
			FrontEnd frontEnd = new FrontEnd();
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
