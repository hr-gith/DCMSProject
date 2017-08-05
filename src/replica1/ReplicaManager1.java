package replica1;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FrontEndToReplicaManager.FrontEndToReplicaManager;
import FrontEndToReplicaManager.FrontEndToReplicaManagerHelper;
import replica1.servers.CenterServers;
import replicaManagement.Request;
import staticData.Ports;

public class ReplicaManager1 implements Runnable {
	public static int id = 1;
	public boolean leaderStatus;
	public int UDPPort;
	String serverName = null;
	static FrontEndToReplicaManager callServer;

	public ReplicaManager1() {
		this.UDPPort = Ports.RM1UDPPort;
		this.leaderStatus = false;
	}

	public static void main(String arg[]) {
		ReplicaManager1 rm1 = new ReplicaManager1();
		CenterServers.main(null);
		rm1.HearBeat();
		(new Thread(rm1)).start();
		System.out.println("RM 1 is started..");
	}

	public void run() {// for receiving requests
		DatagramSocket socket = null;
		try {
			String result = "", result2 = "", result3 = "";
			socket = new DatagramSocket(this.UDPPort);
			DatagramPacket reply = null;
			byte[] buffer = new byte[65536];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer,
						buffer.length);
				socket.receive(request);
				byte[] requestByteArray = request.getData();
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
						requestByteArray);
				ObjectInputStream objectInputStream = new ObjectInputStream(
						byteArrayInputStream);
				Request reqReceived = (Request) objectInputStream.readObject();
				if (request.getPort() == Ports.FEUDPPort) {
					// TODO: multicast to all servers

					InetAddress ahost = InetAddress.getByName("localhost");
					DatagramPacket request2 = new DatagramPacket(
							requestByteArray, requestByteArray.length, ahost,
							Ports.RM2UDPPort);
					socket.setSoTimeout(4000);
					try {
						socket.send(request2);
						DatagramPacket reply2 = new DatagramPacket(buffer,
								buffer.length);
						socket.receive(reply2);
						byte[] resultRecieved2 = reply.getData();
						result2 = new String(resultRecieved2);
					} catch (SocketTimeoutException e) {
						
					}

					DatagramPacket request3 = new DatagramPacket(
							requestByteArray, requestByteArray.length, ahost,
							Ports.RM3UDPPort);
					socket.setSoTimeout(4000);
					try {
						socket.send(request3);
						DatagramPacket reply3 = new DatagramPacket(buffer,
								buffer.length);
						socket.receive(reply3);
						byte[] resultRecieved3 = reply.getData();
						result3 = new String(resultRecieved3);
					} catch (SocketTimeoutException e) {
						
					}

				} // received on its own
					serverName = reqReceived.managerID.substring(0, 3);
					System.out.println("Server Name is" + serverName);
					// create and initialize the ORB
					String nullString[] = null;
					ORB orb = ORB.init(nullString, null);
					// get the root naming context
					org.omg.CORBA.Object objRef = orb
							.resolve_initial_references("NameService");
					// Use NamingContextExt instead of NamingContext. This is
					// part of the Interoperable naming Service.
					NamingContextExt ncRef = NamingContextExtHelper
							.narrow(objRef);
					// resolve the Object Reference in Naming
					String name = serverName + "Server";

					callServer = FrontEndToReplicaManagerHelper.narrow(ncRef
							.resolve_str(name));
					if (reqReceived.typeOfRequest == 1) {
						// TCreate teacher
						boolean createTrecordSuccess = callServer
								.createTRecord(reqReceived.managerID,
										reqReceived.recordID,
										reqReceived.firstName,
										reqReceived.lastName,
										reqReceived.address, reqReceived.phone,
										reqReceived.specialization,
										reqReceived.location);
						if (createTrecordSuccess) {
							// logger.setMessage("Teacher Record has been
							// created successfully.");
							System.out
									.println("RM:Teacher is added successfully.");
							result = "true";
						} else {
							// logger.setMessage("Failed: Teacher Record has not
							// been created.");
							System.out
									.println("RM:Error: Teacher is not added.");
							result = "false";
						}
					} else if (reqReceived.typeOfRequest == 2) {
						boolean createSrecordSucess = callServer.createSRecord(
								reqReceived.managerID, reqReceived.recordID,
								reqReceived.firstName, reqReceived.lastName,
								reqReceived.courseRegistered,
								reqReceived.status, reqReceived.statusDate);
						if (createSrecordSucess) {
							// logger.setMessage("Student Record has been
							// created successfully.");
							System.out
									.println("RM:Student is added successfully.");
							result = "true";
						} else {
							// logger.setMessage("Failed: Student Record has not
							// been created.");
							System.out
									.println("RM:Error: Student is not added.");
							result = "false";
						}
					} else if (reqReceived.typeOfRequest == 3) {
						if (callServer.editRecord(reqReceived.managerID,
								reqReceived.recordID, reqReceived.fieldName,
								reqReceived.newValue)) {
							// logger.setMessage("Records edited" + " Record
							// field -'" + fieldName + "' Record Value - '"
							// + newValue + "'");
							System.out
									.println("RM:Record is successfully edited.");
							result = "true";
						} else {
							// logger.setMessage("Failed: Unable to edit record
							// " + recordIDEdit);
							System.out
									.println("RM:Record is not existed or new value is not valid");
							result = "false";
						}
					} else if (reqReceived.typeOfRequest == 4) {
						if (callServer.transferRecord(reqReceived.managerID,
								reqReceived.recordID, reqReceived.location)) {
							// logger.setMessage(
							// "Record ID: " + recordTransfer + " has been moved
							// to location " + transferLoc);
							System.out
									.println("RM:Transfer successfull of Record:"
											+ reqReceived.recordID
											+ " to location"
											+ reqReceived.location);
							result = "true";
						} else {
							// logger.setMessage("Transfer of Record " +
							// recordTransfer + " has been failed.");
							System.out
									.println("RM:Transfer unsuccessfull of Record:"
											+ reqReceived.recordID);
							result = "false";
						}
					} else if (reqReceived.typeOfRequest == 5) {
						// logger.setMessage("Requested for count on all
						// servers");
						String recordInfo = callServer.getRecordCounts();
						// logger.setMessage("Server response: (Total record
						// number: " + recordInfo + " )");
						System.out.println("RM:Records are: " + recordInfo);
						result = "true" + recordInfo;
					}
				
				// send reply back
				reply = new DatagramPacket(result.getBytes(),
						result.getBytes().length, request.getAddress(),
						request.getPort());
				socket.send(reply);
			}
		} catch (Exception e) {
		}
	}

	// new Thread(new Runnable(){}).start();
	public void HearBeat() {
		// UDP to send the hearbeat to the frontEnd
		new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						System.out.println("I am in timer");

						DatagramSocket datagramSocket;
						try {
							datagramSocket = new DatagramSocket();
							System.out.println("I am in try");
							// ReplicaManager1 dummy = new ReplicaManager1();

							// String message = "I Am Alive!" + dummy.id +
							// "!3666";
							// id++;
							String message = "I Am Alive!" + id + "!"
									+ Ports.RM1UDPPort;

							InetAddress address = InetAddress.getLocalHost();
							byte[] bufferSend = message.getBytes();

							DatagramPacket sendRequestpacket = new DatagramPacket(
									bufferSend, bufferSend.length, address,
									Ports.FEUDPPort);
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
				timer.scheduleAtFixedRate(task, 30000, 10000);
			}

		}).start();

	}

}
