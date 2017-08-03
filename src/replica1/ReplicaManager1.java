package replica1;

import CORBAClassManagement.CORBAClassManagement;
import CORBAClassManagement.CORBAClassManagementHelper;
import classManagement.Record;
import classManagement.TeacherRecord;
import replica1.servers.CenterServers;
import replicaManagement.ReplicaManager;
import replicaManagement.Request;
import staticData.Ports;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class ReplicaManager1 extends ReplicaManager {
	int UDPPort;
	String serverName = null;
	static CORBAClassManagement callServer;

	public ReplicaManager1() {
		this.leaderStatus = false;
	}

	@Override
	public void start(String arg[]) {
		// Starting all servers---
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(arg, null);
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
			CenterServers MTLServer = new CenterServers("MTL", 8890, 9991);
			MTLServer.setOrb(orb);
			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(MTLServer);
			CORBAClassManagement href = CORBAClassManagementHelper.narrow(ref);
			// bind the Object Reference in Naming
			String name = "MTLServer";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);
			System.out.println(MTLServer.serverName + " server is started..");
			(new Thread(MTLServer)).start();

			CenterServers LVLServer = new CenterServers("LVL", 8891, 9992);
			LVLServer.setOrb(orb);

			ref = rootpoa.servant_to_reference(LVLServer);
			href = CORBAClassManagementHelper.narrow(ref);

			name = "LVLServer";
			path = ncRef.to_name(name);
			ncRef.rebind(path, href);
			System.out.println(LVLServer.serverName + " server is started..");
			(new Thread(LVLServer)).start();

			CenterServers DDOServer = new CenterServers("DDO", 8892, 9993);
			DDOServer.setOrb(orb);
			ref = rootpoa.servant_to_reference(DDOServer);
			href = CORBAClassManagementHelper.narrow(ref);
			name = "DDOServer";
			path = ncRef.to_name(name);
			ncRef.rebind(path, href);
			System.out.println(DDOServer.serverName + " server is started..");
			(new Thread(DDOServer)).start();

			orb.run();
		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}

	public void run() {// for receiving requests
		DatagramSocket socket = null;
		try {
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
				} else {
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
					callServer = CORBAClassManagementHelper.narrow(ncRef.resolve_str(name));
					if (reqReceived.typeOfRequest == 1) {
						// TCreate teacher
						boolean createTrecordSuccess = callServer.createTRecord(reqReceived.managerID,
								reqReceived.recordID, reqReceived.firstName, reqReceived.lastName, reqReceived.address,
								reqReceived.phone, reqReceived.specialization, reqReceived.location);
						if (createTrecordSuccess) {
							// logger.setMessage("Teacher Record has been
							// created successfully.");
							System.out.println("Teacher is added successfully.");
						} else {
							// logger.setMessage("Failed: Teacher Record has not
							// been created.");
							System.out.println("Error: Teacher is not added.");
						}
					} else if (reqReceived.typeOfRequest == 2) {

					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}