package replica2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import com.sun.xml.internal.ws.wsdl.writer.document.Port;

import FrontEndToReplicaManager.FrontEndToReplicaManager;
import FrontEndToReplicaManager.FrontEndToReplicaManagerHelper;
import replica2.servers.CenterServers;
import replica2.utilities.EventLogger;
import replicaManagement.ReplicaID;
import replicaManagement.ReplicaManager;
import replicaManagement.Request;
import staticData.Ports;

public class ReplicaManager2  implements Runnable {
	//public static int id = 2;
	static int id ;
	public boolean leaderStatus;
	public int UDPPort;
	String serverName = null;
	static FrontEndToReplicaManager callServer;
	private EventLogger logger = null;
	DatagramSocket socket = null;
	DatagramSocket socket2 = null;
	DatagramSocket socket3 = null;

	public ReplicaManager2() {
		this.UDPPort = Ports.RM2UDPPort;
		this.logger = new EventLogger("RM2Log");
		this.leaderStatus = false;

		id = ReplicaID.Id;
		ReplicaID.Id++;
		try {
			socket = new DatagramSocket(this.UDPPort);
			socket2 = new DatagramSocket(Ports.RM2UDPPort2);
			socket3 = new DatagramSocket(Ports.RM2UDPPort3);

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public static void main(String arg[]) {
		ReplicaManager2 RM2 = new ReplicaManager2();
		new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				replica2.servers.CenterServers.main(null);
			}
		}).start();

		new Thread(new Runnable() {

			public void run() {
				System.out.println("I am in thread of HB1");
				HearBeat();
			}

		}).start();
		System.out.println("RM2 is started..");
		(new Thread(RM2)).start();
	}

	public void run() {// for receiving requests

		try {

			DatagramPacket reply = null;
			byte[] buffer = new byte[65536];
			byte[] buffer2 = new byte[65536];
			byte[] buffer3 = new byte[65536];
			while (true) {
				String result = "", result2 = "", result3 = "";
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				logger.setMessage("Request received at RM2 from port: " + request.getPort());
				byte[] requestByteArray = request.getData();
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestByteArray);
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				Request reqReceived = (Request) objectInputStream.readObject();
				logger.setMessage("Request is" + reqReceived.toString());
				/* */// received on its own
				serverName = reqReceived.managerID.substring(0, 3);
				System.out.println("Server Name is" + serverName);
				// create and initialize the ORB
				String nullString[] = null;
				ORB orb = ORB.init(nullString, null);
				// get the root naming context
				org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
				// Use NamingContextExt instead of NamingContext. This is
				// part of the Interoperable naming Service.
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
				// resolve the Object Reference in Naming
				String name = serverName + "ServerRM2";

				callServer = FrontEndToReplicaManagerHelper.narrow(ncRef.resolve_str(name));
				if (reqReceived.typeOfRequest == 1) {
					// TCreate teacher
					boolean createTrecordSuccess = callServer.createTRecord(reqReceived.managerID, reqReceived.recordID,
							reqReceived.firstName, reqReceived.lastName, reqReceived.address, reqReceived.phone,
							reqReceived.specialization, reqReceived.location);
					if (createTrecordSuccess) {
						logger.setMessage(
								"Teacher Record :" + reqReceived.recordID + " has been created successfully.");
						System.out.println("RM:Teacher is added successfully.");
						result = "true";
					} else {
						logger.setMessage("Failed: Teacher Record has not been created.");
						System.out.println("RM:Error: Teacher is not added.");
						result = "false";
					}
				} else if (reqReceived.typeOfRequest == 2) {
					boolean createSrecordSucess = callServer.createSRecord(reqReceived.managerID, reqReceived.recordID,
							reqReceived.firstName, reqReceived.lastName, reqReceived.courseRegistered,
							reqReceived.status, reqReceived.statusDate);
					if (createSrecordSucess) {
						logger.setMessage(
								"Student Record :" + reqReceived.recordID + " has been created successfully.");
						System.out.println("RM:Student is added successfully.");
						result = "true";
					} else {
						// logger.setMessage("Failed: Student Record has not
						// been created.");
						System.out.println("RM:Error: Student is not added.");
						result = "false";
					}
				} else if (reqReceived.typeOfRequest == 3) {
					if (callServer.editRecord(reqReceived.managerID, reqReceived.recordID, reqReceived.fieldName,
							reqReceived.newValue)) {
						logger.setMessage("Records edited" + " Record field -'" + reqReceived.fieldName
								+ "' Record Value - '" + reqReceived.newValue + "'");
						System.out.println("RM:Record is successfully edited.");
						result = "true";
					} else {
						logger.setMessage("Failed: Unable to edit record" + reqReceived.recordID);
						System.out.println("RM:Record is not existed or new value is not valid");
						result = "false";
					}
				} else if (reqReceived.typeOfRequest == 4) {
					if (callServer.transferRecord(reqReceived.managerID, reqReceived.recordID,
							reqReceived.destinationServer)) {
						logger.setMessage("Record ID: " + reqReceived.recordID + " has been moved to location "
								+ reqReceived.destinationServer);
						System.out.println("RM:Transfer successfull of Record:" + reqReceived.recordID + " to location"
								+ reqReceived.destinationServer);
						result = "true";
					} else {
						logger.setMessage("Transfer of Record " + reqReceived.recordID + " has been failed.");
						System.out.println("RM:Transfer unsuccessfull of Record:" + reqReceived.recordID);
						result = "false";
					}
				} else if (reqReceived.typeOfRequest == 5) {
					logger.setMessage("Requested for count on all servers");
					String recordInfo = callServer.getRecordCounts();
					logger.setMessage("Server response: (Total record number: " + recordInfo + " )");
					System.out.println("RM2:Records are: " + recordInfo);
					result = "true";
				}else if(reqReceived.typeOfRequest==Request.RELIABLE_UDP){
					result="true";
				}
				// after executing in its own server
				// broadcasting to other RMs
				logger.setMessage("Result after executing on center servers is " + result);
				if (result.startsWith("true")) {
					logger.setMessage("Request has sucessfully executed on RM2 : " + serverName);
					if (request.getPort() == Ports.FEUDPPort
							&& reqReceived.typeOfRequest != Request.GET_COUNT_REQUEST) {

						// TODO: multicast to all servers
						logger.setMessage("Request executed sucessfully so braodcasting to other RMs");

						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutput oo = new ObjectOutputStream(bos);
						oo.writeObject(reqReceived);
						oo.close();
						byte[] serializedMsg = bos.toByteArray();
						InetAddress ahost = InetAddress.getLocalHost();
						DatagramPacket request2 = new DatagramPacket(serializedMsg, serializedMsg.length, ahost,
								Ports.RM1UDPPort);
						// TODO: create a new socket
						socket2.setSoTimeout(40000);
						try {
							socket2.send(request2);
							DatagramPacket reply2 = new DatagramPacket(buffer2, buffer2.length);
							socket2.receive(reply2);
							byte[] resultRecieved2 = reply2.getData();
							result2 = new String(resultRecieved2);
							logger.setMessage("Result received from RM1 after Broadcast" + result2);
							
						} catch (SocketTimeoutException e) {
							System.out.println("socket has timed out to send to replica 1");
						}

						DatagramPacket request3 = new DatagramPacket(serializedMsg, serializedMsg.length, ahost,
								Ports.RM3UDPPort);
						socket3.setSoTimeout(40000);
						try {
							socket3.send(request3);
							DatagramPacket reply3 = new DatagramPacket(buffer3, buffer3.length);
							socket3.receive(reply3);
							byte[] resultRecieved3 = reply3.getData();
							result3 = new String(resultRecieved3);
							logger.setMessage("Result received from RM3 after Broadcast" + result3);
							
						} catch (SocketTimeoutException e) {
							System.out.println("socket has timed out to send to replica 3");
						}

					}
				}
				// send reply back
				reply = new DatagramPacket(result.getBytes(), result.getBytes().length, request.getAddress(),
						request.getPort());
				socket.send(reply);
			
				// socket=null;

			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static void HearBeat() {
		// UDP to send the hearbeat to the frontEnd


		TimerTask task = new TimerTask() {

			
			
			@Override
			public void run() {
				System.out.println("I am in timer");

				DatagramSocket datagramSocket;
				try {
					datagramSocket = new DatagramSocket(Ports.RM2UDPPortHearbeat);
					System.out.println("I am in try");
					
					String message = "RM2 is Alive!" + id  + "!" + Ports.RM2UDPPortHearbeat;
					
					
					System.out.println("Replica id 2 " + id);
					InetAddress address = InetAddress.getLocalHost();
					byte[] bufferSend = message.getBytes();

					DatagramPacket sendRequestpacket = new DatagramPacket(bufferSend, bufferSend.length, address,
							Ports.FEUDPPortHearbeat);
					datagramSocket.send(sendRequestpacket);
					System.out.println("sent packet");

					if (datagramSocket != null)
						datagramSocket.close();

				} catch (Exception e) {
					System.err.println("ERROR: " + e);
					e.printStackTrace(System.out);
				}
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, 1, 30000);
}
}
