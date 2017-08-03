package frontEnd;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import classManagement.Record;
import classManagement.TeacherRecord;
import replicaManagement.*;
import CORBAClassManagement.*;

public class FrontEnd extends CORBAClassManagementPOA implements Runnable{

	public int UDPPort = 0;
	public static String leaderInfo;

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
		Request req = new Request ();
		req.managerID = managerID;
		req.typeOfRequest = Request.LOGIN_REQUEST;
		FIFOQueue.getInstance().push(req);
		
		///?????????????????how should we get response
		return false;
	}

	public void logout() {
		Request req = new Request ();
		req.typeOfRequest = Request.LOGIN_REQUEST;
		FIFOQueue.getInstance().push(req);				
	}

	public boolean createTRecord(String managerID, String firstName,
			String lastName, String address, String phone,
			String specialization, String location) {
		Request req = new Request ();
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
		Request req = new Request ();
		req.typeOfRequest = Request.GET_COUNT_REQUEST;
		FIFOQueue.getInstance().push(req);

		return null;
	}

	public boolean editRecord(String managerID, String recordID,
			String fieldName, String newValue) {
		Request req = new Request ();
		req.typeOfRequest = Request.EDIT_REQUEST;
		req.managerID = managerID;
		req.recordID = recordID;
		req.fieldName = fieldName;
		req.newValue = newValue;
		FIFOQueue.getInstance().push(req);		
		
		return false;
	}

	public boolean createSRecord(String managerID, String firstName,
			String lastName, String coursesRegistered, boolean status,
			String statusDate) {
		Request req = new Request ();
		req.typeOfRequest = Request.CREATE_STUDENT_REQUEST;
		req.managerID = managerID;
		req.firstName = firstName;
		req.lastName = lastName;
		req.courseRegistered = coursesRegistered;
		req.status = status;
		FIFOQueue.getInstance().push(req);		
		return false;
	}

	public boolean transferRecord(String managerID, String recordID,
			String remoteCenterServerName) {
		Request req = new Request ();
		req.typeOfRequest = Request.TRANSFER_REQUEST;
		req.managerID = managerID;
		req.recordID = recordID;
		req.remoteCenterServerName = remoteCenterServerName;
		FIFOQueue.getInstance().push(req);		

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
