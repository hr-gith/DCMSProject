package replica1;

import FrontEndToReplicaManager.FrontEndToReplicaManager;
import FrontEndToReplicaManager.FrontEndToReplicaManagerHelper;
import classManagement.Record;
import classManagement.TeacherRecord;
import replica1.servers.CenterServers;
import replicaManagement.ReplicaManager;
import replicaManagement.Request;
import staticData.Ports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class ReplicaManager1 extends ReplicaManager {
	int UDPPort;
	String serverName = null;
	static FrontEndToReplicaManager callServer;

	public ReplicaManager1() {
		this.leaderStatus = false;
	}

	public void run() {// for receiving requests
		DatagramSocket socket = null;
		try {
			String result = "";
			socket = new DatagramSocket(this.UDPPort);
			DatagramPacket reply = null;
			byte[] buffer = new byte[65536];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				byte[] requestByteArray = request.getData();
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestByteArray);
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				Request reqReceived = (Request) objectInputStream.readObject();
				if (request.getPort() == Ports.FEUDPPort) {
					// TODO: multicast to all servers
					String result2 = null;

					InetAddress ahost = InetAddress.getByName("localhost");
					DatagramPacket request2 = new DatagramPacket(requestByteArray, requestByteArray.length, ahost,
							Ports.RM2UDPPort);

					socket.send(request2);
					DatagramPacket reply2 = new DatagramPacket(buffer, buffer.length);
					socket.receive(reply2);
					byte[] resultRecieved = reply.getData();
					String output = new String(resultRecieved);
					if (output.startsWith("true")) {
						DatagramPacket request3 = new DatagramPacket(requestByteArray, requestByteArray.length, ahost,
								Ports.RM3UDPPort);
						socket.send(request3);
						DatagramPacket reply3 = new DatagramPacket(buffer, buffer.length);
						socket.receive(reply3);
						byte[] resultRecieved2 = reply.getData();
						result = new String(resultRecieved2);
					}

				} // received on its own
				if (result.startsWith("true")) {
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
					String name = serverName + "Server"; // "rmi://localhost:" +
															// serverPort + "/"
															// + serverName;
					callServer = FrontEndToReplicaManagerHelper.narrow(ncRef.resolve_str(name));
					if (reqReceived.typeOfRequest == 1) {
						// TCreate teacher
						boolean createTrecordSuccess = callServer.createTRecord(reqReceived.managerID,
								reqReceived.recordID, reqReceived.firstName, reqReceived.lastName, reqReceived.address,
								reqReceived.phone, reqReceived.specialization, reqReceived.location);
						if (createTrecordSuccess) {
							// logger.setMessage("Teacher Record has been
							// created successfully.");
							System.out.println("Teacher is added successfully.");
							result = "true";
						} else {
							// logger.setMessage("Failed: Teacher Record has not
							// been created.");
							System.out.println("Error: Teacher is not added.");
							result = "false";
						}
					} else if (reqReceived.typeOfRequest == 2) {
						boolean createSrecordSucess = callServer.createSRecord(reqReceived.managerID,
								reqReceived.recordID, reqReceived.firstName, reqReceived.lastName,
								reqReceived.courseRegistered, reqReceived.status, reqReceived.statusDate);
						if (createSrecordSucess) {
							// logger.setMessage("Student Record has been
							// created successfully.");
							System.out.println("Student is added successfully.");
							result = "true";
						} else {
							// logger.setMessage("Failed: Student Record has not
							// been created.");
							System.out.println("Error: Student is not added.");
							result = "false";
						}
					} else if (reqReceived.typeOfRequest == 3) {
						if (callServer.editRecord(reqReceived.managerID, reqReceived.recordID, reqReceived.fieldName,
								reqReceived.newValue)) {
							// logger.setMessage("Records edited" + " Record
							// field -'" + fieldName + "' Record Value - '"
							// + newValue + "'");
							System.out.println("Record is successfully edited.");
							result = "true";
						} else {
							// logger.setMessage("Failed: Unable to edit record
							// " + recordIDEdit);
							System.out.println("Record is not existed or new value is not valid");
							result = "false";
						}
					} else if (reqReceived.typeOfRequest == 4) {
						if (callServer.transferRecord(reqReceived.managerID, reqReceived.recordID,
								reqReceived.location)) {
							// logger.setMessage(
							// "Record ID: " + recordTransfer + " has been moved
							// to location " + transferLoc);
							System.out.println("Transfer successfull of Record:" + reqReceived.recordID + " to location"
									+ reqReceived.location);
							result = "true";
						} else {
							// logger.setMessage("Transfer of Record " +
							// recordTransfer + " has been failed.");
							System.out.println("Transfer unsuccessfull of Record:" + reqReceived.recordID);
							result = "false";
						}
					} else if (reqReceived.typeOfRequest == 5) {
						// logger.setMessage("Requested for count on all
						// servers");
						String recordInfo = callServer.getRecordCounts();
						// logger.setMessage("Server response: (Total record
						// number: " + recordInfo + " )");
						System.out.println("Records are: " + recordInfo);
						result = "true" + recordInfo;
					} else if (reqReceived.typeOfRequest == 6) {
						callServer.logout();
						// logger.setMessage("Manager : " + managerID + " has
						// Logged out..");
						System.out.println("User logged out");
						result = "true";
					}
				}
				// send reply back
				reply = new DatagramPacket(result.getBytes(), result.getBytes().length, request.getAddress(),
						request.getPort());
				socket.send(reply);

			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
