package replica1;

import CORBAClassManagement.CORBAClassManagement;
import CORBAClassManagement.CORBAClassManagementHelper;
import replica1.servers.CenterServers;
import replicaManagement.ReplicaManager;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class ReplicaManager1 extends ReplicaManager {

	public ReplicaManager1() {
		this.leaderStatus = false;
	}

	public void run() {

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

}