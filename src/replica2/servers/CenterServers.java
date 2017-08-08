package replica2.servers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import classManagement.Record;
import classManagement.StudentRecord;
import classManagement.TeacherRecord;
import frontEnd.FrontEnd;
import frontEnd.SequenceIdGenerator;
import replica1.utilities.EventLogger;
import staticData.Ports;
import CORBAClassManagement.CORBAClassManagementPOA;
import FrontEndToReplicaManager.FrontEndToReplicaManager;
import FrontEndToReplicaManager.FrontEndToReplicaManagerHelper;
import FrontEndToReplicaManager.FrontEndToReplicaManagerPOA;

public class CenterServers extends FrontEndToReplicaManagerPOA implements Runnable {

	public String serverName = "";
	public int serverPort = 0;
	public int UDPPort = 0;
	public HashRecord record;
	public String managerID = "";
	private EventLogger logger = null;
	private ORB orb;
	DatagramSocket socket = null;


	public ORB getOrb() {
		return orb;
	}

	public void setOrb(ORB orb) {
		this.orb = orb;
	}

	public CenterServers(String serverName, int serverPort, int UDPPort) {
		super();
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.UDPPort = UDPPort;
		String DBFileName = "DB_RM2" + serverName;
		
		int leaderPort = FrontEnd.leaderPort;
		String leader = "";
		if (leaderPort == Ports.RM1UDPPort) {
			leader = "RM1";
		} else if (leaderPort == Ports.RM2UDPPort) {
			leader = "RM2";
		} else if (leaderPort == Ports.RM3UDPPort) {
			leader = "RM3";
		}
		if (!leader.equals("RM2")) {
			String LeaderFileName = "DB_" + leader + serverName;
			try {
				FileUtils.copyFile(new File(LeaderFileName), new File(DBFileName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
			}
		}
		try {
			socket = new DatagramSocket(this.UDPPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		record = new HashRecord(DBFileName);
		this.logger = new EventLogger("RM2_"+this.serverName);
	}
	@Override
	  public void finalize() {
	    System.out.println(serverName + "In finalize");
	    socket.close();
	  }

	
	public static void main(String[] args) {
		// Starting all servers---
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
			CenterServers MTLServer = new CenterServers("MTL", 8890, Ports.RM2MTL);
			MTLServer.setOrb(orb);
			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(MTLServer);
			FrontEndToReplicaManager href = FrontEndToReplicaManagerHelper.narrow(ref);
			// bind the Object Reference in Naming
			String name = "MTLServerRM2";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);
			System.out.println(MTLServer.serverName + " server2 is started..");
			(new Thread(MTLServer)).start();

			CenterServers LVLServer = new CenterServers("LVL", 8891, Ports.RM2LVL);
			LVLServer.setOrb(orb);

			ref = rootpoa.servant_to_reference(LVLServer);
			href = FrontEndToReplicaManagerHelper.narrow(ref);

			name = "LVLServerRM2";
			path = ncRef.to_name(name);
			ncRef.rebind(path, href);
			System.out.println(LVLServer.serverName + " server2 is started..");
			(new Thread(LVLServer)).start();

			CenterServers DDOServer = new CenterServers("DDO", 8892, Ports.RM2DDO);
			DDOServer.setOrb(orb);
			ref = rootpoa.servant_to_reference(DDOServer);
			href = FrontEndToReplicaManagerHelper.narrow(ref);
			name = "DDOServerRM2";
			path = ncRef.to_name(name);
			ncRef.rebind(path, href);
			System.out.println(DDOServer.serverName + " server2 is started..");
			(new Thread(DDOServer)).start();

			orb.run();
		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}

	public boolean createTRecord(String managerID, String recordID, String firstName, String lastName, String address,
			String phone, String specialization, String location) {
		//String teacherID = SequenceIdGenerator.getInstance().getID("TR");
		TeacherRecord teacherRecord = new TeacherRecord(managerID, recordID, firstName, lastName, address, phone,
				specialization, location);
		// EventLogger logger = new EventLogger(this.serverName);

		if (record.addRecord(teacherRecord)) {
			logger.setMessage(managerID + ": Teacher record has been Created ~ " + teacherRecord.toString());
			return true;
		} else {
			logger.setMessage(
					managerID + ": Failed: Teacher record has not been created ~ " + teacherRecord.toString());
			return false;
		}
	}

	public boolean createSRecord(String managerID, String recordID, String firstName, String lastName,
			String courseRegisterd, boolean status, String statusDate) {
		//String studentID = SequenceIdGenerator.getInstance().getID("SR");
		StudentRecord studentRecord = new StudentRecord(managerID, recordID, firstName, lastName, courseRegisterd,
				status, statusDate);
		// EventLogger logger = new EventLogger(this.serverName);
		// TODO: add who in log message

		if (record.addRecord(studentRecord)) {
			logger.setMessage(managerID + ": Student record has been Created ~ " + studentRecord.toString());
			return true;
		} else {
			logger.setMessage(
					managerID + ": Failed - Student record has not been created ~ " + studentRecord.toString());
			return false;
		}
	}

	public String getRecordCounts() {
		// EventLogger logger = new EventLogger(this.serverName);
		logger.setMessage(managerID + ": is getting record count.");

		String result = this.serverName + " : " + record.getRecordCount();
		if (this.serverName.equals("MTL")) {
			result += " LVL : " + UDPClient(Ports.RM2LVL, null) + " DDO : " + UDPClient(Ports.RM2DDO, null);
		} else if (this.serverName.equals("LVL")) {
			result += " MTL : " + UDPClient(Ports.RM2MTL, null) + " DDO : " + UDPClient(Ports.RM2DDO, null);
		} else if (this.serverName.equals("DDO")) {
			result += " LVL : " + UDPClient(Ports.RM2LVL, null) + " MTL : " + UDPClient(Ports.RM2MTL, null);
		}

		return result;
	}

	public boolean editRecord(String managerID, String recordID, String fieldName, String newValue) {
		// EventLogger logger = new EventLogger(this.serverName);

		if (record.editRecord(recordID, fieldName, newValue)) {
			logger.setMessage(managerID + ": Editing record" + ": RecordID : " + recordID + " has changed '" + fieldName
					+ "' to '" + newValue + "'");
			return true;
		} else {
			logger.setMessage(
					managerID + ": Failed - Editing record: RecordID : " + recordID + " was unable to change");
			return false;
		}
	}

	public boolean deleteRecord(String managerID, String recordID) {
		// System.out.println("Delete record server start");
		if (record.deleteRecord(recordID)) {
			// System.out.println("Delete record server");
			logger.setMessage(managerID + ": Deleting record" + ": RecordID : " + recordID + " has deleted ");
			return true;
		} else {
			logger.setMessage(
					managerID + ": Failed - Deleting record: RecordID : " + recordID + " was unable to delete");
			return false;
		}
	}

	public boolean transferRecord(String managerID, String recordID, String remoteCenterServerName) {
		Record existingRecord = record.getRecordByID(recordID);
		if (existingRecord == null) {
			return false;
		} else {
			// checks if remoteCenterServernName and managerID are in the same
			// center
			if (remoteCenterServerName.equalsIgnoreCase(managerID.substring(0, 3)))
				return false;

			// forward record to the requested server by UDP
			if (remoteCenterServerName.equalsIgnoreCase("MTL")) {
				if (this.UDPClient(Ports.RM2MTL, existingRecord).startsWith("true")) {
					// System.out.println("transfer is running in transfer
					// record");
					return this.deleteRecord(managerID, recordID);
				} else
					return false;

			}
			if (remoteCenterServerName.equalsIgnoreCase("LVL")) {
				if (this.UDPClient(Ports.RM2LVL, existingRecord).startsWith("true"))
					return this.deleteRecord(managerID, recordID);
				else
					return false;
			}
			if (remoteCenterServerName.equalsIgnoreCase("DDO")) {
				if (this.UDPClient(Ports.RM2DDO, existingRecord).startsWith("true"))
					return this.deleteRecord(managerID, recordID);
				else
					return false;
			}
			return false;
		}
	}

	public boolean addTransferedRecord(Record record) {
		if (this.record.addRecord(record)) {
			logger.setMessage("Transfered Record: " + record.recordID + " is added .");
			return true;
		} else {
			logger.setMessage("Transfered Record:" + record.recordID + " could not be saved.");
			return false;
		}
	}

	public void run() {
		System.out.println("UDP Connection for : " + this.serverName + " is listening on port: " + this.UDPPort);
		try {
			DatagramPacket reply = null;
			byte[] buffer = new byte[65536];
			while (true) {

				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);

				byte[] requestByteArray = request.getData();
				// System.out.println(Arrays.toString(requestByteArray));
				if (requestByteArray[0] == 102) {
					// run count method
					// System.out.println("count");
					String count = "" + record.getRecordCount();
					reply = new DatagramPacket(count.getBytes(), count.getBytes().length, request.getAddress(),
							request.getPort());
					socket.send(reply);

				} else {
					// run transfer
					// System.out.println("in runtransfer");
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestByteArray);
					ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
					Record recordToInsert = (Record) objectInputStream.readObject();
					// System.out.println(recordToInsert.toString());
					String insertStatus = null;
					if (this.addTransferedRecord(recordToInsert)) {
						insertStatus = "true";
						reply = new DatagramPacket(insertStatus.getBytes(), insertStatus.getBytes().length,
								request.getAddress(), request.getPort());
						socket.send(reply);

					} else {
						insertStatus = "false";
						reply = new DatagramPacket(insertStatus.getBytes(), insertStatus.getBytes().length,
								request.getAddress(), request.getPort());
						socket.send(reply);

					}

				}

			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}
		finally{
			if(socket!=null)
				socket.close();
		}
	}

	/**
	 * To send request to other servers by UDP/IP for number of records
	 * 
	 * @param int
	 *            port
	 * @return String result
	 */
	public String UDPClient(int port, Record recordToSend) {
		System.out.println("Service requested at " + port);
		String result = null;
		DatagramSocket socket = null;
		byte[] args = new byte[1];

		try {
			InetAddress ahost = InetAddress.getByName("localhost");
			socket = new DatagramSocket();
			if (recordToSend == null) {
				// System.out.println("call for count");
				args[0] = 102;
				String count = "" + record.getRecordCount();

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(args);
				outputStream.write(this.serverName.getBytes());
				// System.out.println("steam : " + outputStream.toString());
				DatagramPacket request = new DatagramPacket(outputStream.toByteArray(),
						outputStream.toByteArray().length, ahost, port);
				// System.out.println(request.getData().toString());
				socket.send(request);
				// ----waiting for reply from other sever
				System.out.println("waiting....");

				byte[] buffer = new byte[65536];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				socket.receive(reply);
				// System.out.println("reply received..");
				byte[] countRecieved = reply.getData();
				result = new String(countRecieved);
			} else {
				// System.out.println("Transfer record");
				// args[0] = 101;
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
				// byteStream.write(args);
				objectStream.writeObject(recordToSend);
				byte[] buffer = byteStream.toByteArray();
				// System.out.println(objectStream.toString());

				DatagramPacket request = new DatagramPacket(byteStream.toByteArray(), byteStream.toByteArray().length,
						ahost, port);
				// System.out.println(request.getData().toString());
				socket.send(request);

				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				socket.receive(reply);
				byte[] resultRecieved = reply.getData();
				result = new String(resultRecieved);
				// System.out.println("transferring UDP" + result);

			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(socket!=null)
				socket.close();
		}
		return result;

	}

	
}
