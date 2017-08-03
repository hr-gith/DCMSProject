package frontEnd;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import CORBAClassManagement.*;

public class FrontEnd extends CORBAClassManagementPOA implements Runnable{

	public int UDPPort = 0;

	private ORB orb;

	public ORB getOrb() {
		return orb;
	}

	public void setOrb(ORB orb) {
		this.orb = orb;
	}
	public FrontEnd(int udpPort){
		this.UDPPort = udpPort;
	}
	
	public boolean login(String managerID) {
		// TODO Auto-generated method stub
		return false;
	}

	public void logout() {
		// TODO Auto-generated method stub
		
	}

	public boolean createTRecord(String managerID, String firstName,
			String lastName, String address, String phone,
			String specialization, String location) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getRecordCounts() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean editRecord(String managerID, String recordID,
			String fieldName, String newValue) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean createSRecord(String managerID, String firstName,
			String lastName, String coursesRegistered, boolean status,
			String statusDate) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean transferRecord(String managerID, String recordID,
			String remoteCenterServerName) {
		// TODO Auto-generated method stub
		return false;
	}

	public void run() {
		// TODO Auto-generated method stub
		
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
			FrontEnd frontEnd = new FrontEnd(9999);
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
