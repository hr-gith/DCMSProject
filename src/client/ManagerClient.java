package client;

import java.text.ParseException;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import CORBAClassManagement.CORBAClassManagement;
import CORBAClassManagement.CORBAClassManagementHelper;
import replica1.utilities.EventLogger;
import replica1.utilities.Verifier;

/**
 * operates all the methods related to the client side such as: Display the menu
 * Get request from user Send request to the server Display the reply to the
 * user
 * 
 * @author hamideh
 * 
 */
public class ManagerClient {

	static CORBAClassManagement callServer;

	public static void main(String args[]) throws ParseException {
		boolean valid = false;
		boolean validInput = false;
		String newLine = "";
		String firstName = "";
		String lastName = "";
		String address = "";
		String phone = "";
		String specialization = "";
		String location;
		String coursesRegistered;
		boolean status = false;
		String statusDate;
		String managerID = "";
		int optionChoice = 0;
		String serverName = null;
		int serverPort = 0;

		CORBAClassManagement callServer = null;
		// ---starting server based on manager id

		while (true) {
			System.out.print("Enter Manager ID:");
			Scanner managerSc = new Scanner(System.in);
			managerID = managerSc.nextLine();
			System.out.println();
			if (!Managers.verifyManager(managerID)) {
				valid = false;
				System.out.println("Error: Manager ID is invalid.");
			} else {
				valid = true;
				EventLogger logger = new EventLogger(managerID);

				logger.setMessage("Manager : " + managerID
						+ " has Logged in..");
				if (managerID.toUpperCase().startsWith("MTL")) {
					serverName = "MTL";
					serverPort = 8890;
				} else if (managerID.toUpperCase().startsWith("LVL")) {
					serverName = "LVL";
					serverPort = 8891;
				}
				if (managerID.toUpperCase().startsWith("DDO")) {
					serverName = "DDO";
					serverPort = 8892;
				}

				try {
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
					String name = "FrontEnd"; 
					callServer = CORBAClassManagementHelper.narrow(ncRef
							.resolve_str(name));
					
					while (valid) {
						listOptions();
						Scanner options = new Scanner(System.in);
						String optionChoiceStr = options.nextLine();
						optionChoice = Integer.parseInt(optionChoiceStr);
						switch (optionChoice) {
						case 1:// creating a teacher record
							Scanner trecordScanner = new Scanner(System.in);
							System.out
									.println("----Creating a Teacher Record----");
							do {
								System.out.println("Please enter firstname:");
								firstName = trecordScanner.nextLine();
							} while (!Verifier.verifyString(firstName));
							do {
								System.out.println("Please enter lastName:");
								lastName = trecordScanner.nextLine();
							} while (!Verifier.verifyString(lastName));
							do {
								System.out.println("Please enter address:");
								address = trecordScanner.nextLine();
							} while (!Verifier.verifyString(address));
							do {
								System.out.println("Please enter phone:");
								phone = trecordScanner.nextLine();
							} while (!Verifier.verifynumber(phone));
							do {
								System.out
										.println("Please enter specialisation:");
								specialization = trecordScanner.nextLine();
							} while (!Verifier.verifyString(specialization));
							do {
								System.out.println("Please enter location:");
								location = trecordScanner.nextLine();
							} while (!Verifier.verifyCity(location));

							logger.setMessage("Creating TeacherRecord = FirstName : "
									+ firstName
									+ " LastName : "
									+ lastName
									+ " Address : "
									+ address
									+ " Phone : "
									+ phone
									+ " Specialization : "
									+ specialization
									+ " Location : "
									+ location);
							boolean createTrecordSuccess = callServer
									.createTRecord(managerID, firstName,
											lastName, address, phone,
											specialization, location);
							if (createTrecordSuccess) {
								logger.setMessage("Teacher Record has been created successfully.");
								System.out
										.println("Teacher is added successfully.");
							} else {
								logger.setMessage("Failed: Teacher Record has not been created.");
								System.out
										.println("Error: Teacher is not added.");
							}
							break;
						case 2:// Create Student Record
							Scanner srecordScanner = new Scanner(System.in);
							System.out
									.println("----Creating a Student Record----");
							do {
								System.out.println("Please enter firstname:");
								firstName = srecordScanner.nextLine();
							} while (!Verifier.verifyString(firstName));
							do {
								System.out.println("Please enter lastName:");
								lastName = srecordScanner.nextLine();
							} while (!Verifier.verifyString(lastName));
							do {// ---work needeed---
								System.out
										.println("Please enter Course registered:");
								coursesRegistered = srecordScanner.nextLine();
							} while (!Verifier.verifyString(coursesRegistered));
							do {
								System.out
										.println("Please enter status (active/inactive):");
								newLine = srecordScanner.nextLine();
								if (Verifier.VerifyStatus(newLine)) {
									status = true;

								} else {
									status = false;
								}
							} while (!Verifier.VerifyStatus(newLine));
							do {
								System.out
										.println("Please enter statusdate in DD/MM/YYYY:");
								statusDate = srecordScanner.nextLine();
							} while (!Verifier.VerifyDate(statusDate));
							logger.setMessage("Creating StudentRecord = FirstName : "
									+ firstName
									+ " LastName : "
									+ lastName
									+ " Course Registered : "
									+ coursesRegistered
									+ " Status : "
									+ status
									+ " StatusDate : " + statusDate);
							boolean createSrecordSucess = callServer
									.createSRecord(managerID, firstName,
											lastName, coursesRegistered,
											status, statusDate);
							if (createSrecordSucess) {
								logger.setMessage("Student Record has been created successfully.");
								System.out
										.println("Student is added successfully.");
							} else {
								logger.setMessage("Failed: Student Record has not been created.");
								System.out
										.println("Error: Student is not added.");
							}
							break;
						case 3:// Get Record Counts
							logger.setMessage("Requested for count on all servers");
							String recordInfo = callServer.getRecordCounts();
							logger.setMessage("Server response: (Total record number: "
									+ recordInfo + " )");
							System.out.println("Records are: " + recordInfo);
							break;
						case 4:// Edit records
							boolean editLoop = true;
							String recordIDEdit = "";
							String fieldName = "";
							String newValue = "";
							Scanner recordsc = new Scanner(System.in);
							while (editLoop) {

								System.out.println("Enter the Record ID:");
								recordIDEdit = recordsc.nextLine();
								// check for valid id
								System.out
										.println("Choose the field you want to edit");

								if (recordIDEdit.startsWith("TR")) {
									listOptionsforTeacher();
									String ch = recordsc.nextLine();
									int choice = Integer.parseInt(ch);
									switch (choice) {
									case 1:
										fieldName = "address";
										editLoop = false;
										break;
									case 2:
										fieldName = "phone";
										editLoop = false;
										break;
									case 3:
										fieldName = "location";
										editLoop = false;
										break;
									case 4:
										fieldName = "specialisation";
										editLoop = false;
										break;

									default:
										editLoop = false;
										break;
									}
								} else if (recordIDEdit.startsWith("SR")) {
									listOptionsforStudent();
									String ch = recordsc.nextLine();
									int choice = Integer.parseInt(ch);

									switch (choice) {
									case 1:
										fieldName = "courseregistered";
										editLoop = false;
										break;
									case 2:
										fieldName = "status";
										editLoop = false;
										break;
									case 3:
										fieldName = "statusdate";
										editLoop = false;
										break;
									default:
										break;
									}
								}
								System.out.println("Enter New value:");
								newValue = recordsc.nextLine();
								System.out.println("Edit Field : " + fieldName
										+ " Edited value: " + newValue);
								if (InputsValid(fieldName, newValue)) {
									logger.setMessage("Editing record "
											+ recordIDEdit
											+ ": Record field -'" + fieldName
											+ "' Record Value - '" + newValue
											+ "'");
									if (callServer.editRecord(managerID,
											recordIDEdit, fieldName, newValue)) {
										logger.setMessage("Records edited"
												+ " Record field -'"
												+ fieldName
												+ "' Record Value - '"
												+ newValue + "'");
										System.out
												.println("Record is successfully edited.");
									} else {
										logger.setMessage("Failed: Unable to edit record "
												+ recordIDEdit);
										System.out
												.println("Record is not existed or new value is not valid");
									}
								} else {
									logger.setMessage("Failed: Inputs not valid "
											+ recordIDEdit);
									System.out.println("Input is not valid");

								}
							}

							break;
						case 5:// transfer Record
							String recordTransfer = null;
							String transferLoc = null;
							Scanner transferSc = new Scanner(System.in);
							do {
								System.out
										.println("Enter Record ID you want to transfer : ");

								recordTransfer = transferSc.nextLine();
							} while (!Verifier.VerifyRecordId(recordTransfer));
							do {
								System.out
										.println("Enter Location you want to transfer record:");
								transferLoc = transferSc.nextLine();
							} while (!Verifier.verifyCity(transferLoc)
									|| transferLoc == serverName);

							if (callServer.transferRecord(managerID,
									recordTransfer, transferLoc)) {
								logger.setMessage("Record ID: "
										+ recordTransfer
										+ " has been moved to location "
										+ transferLoc);
								System.out
										.println("Transfer successfull of Record:"
												+ recordTransfer
												+ " to location" + transferLoc);
							} else {
								logger.setMessage("Transfer of Record "
										+ recordTransfer + " has been failed.");
								System.out
										.println("Transfer unsuccessfull of Record:"
												+ recordTransfer);
							}
							break;
						case 6:// exit application
							System.out
									.println("Are you sure you want exit application? Y/N");
							Scanner exitSc = new Scanner(System.in);
							String exitCgStr = exitSc.nextLine();
							if (exitCgStr.toUpperCase().trim().equals("Y")) {
								logger.setMessage("Manager : " + managerID
										+ " has Logged out..");
								System.out.println("User logged out");
								valid = false;
								// System.exit(0);
							}
							break;
						}
					}

				} catch (Exception e) {
					System.out.println("ERROR : " + e);
					e.printStackTrace(System.out);
				}
			}
		}

	}

	public static void listOptionsforTeacher() {
		System.out.println("1. Address:");
		System.out.println("2. Phone:");
		System.out.println("3. Specialisation:");
		System.out.println("4. Location:");

	}

	public static boolean InputsValid(String fieldName, String newValue)
			throws ParseException {
		if ((fieldName.equals("phone")) && (Verifier.verifynumber(newValue)))
			return true;
		else if ((fieldName.equals("location"))
				&& (Verifier.verifyCity(newValue)))
			return true;
		else if ((fieldName.equals("statusdate"))
				&& (Verifier.VerifyDate(newValue)))
			return true;
		else if (((fieldName.equals("status")) && (Verifier
				.VerifyStatus(newValue)))) {
			return true;

		} else if (((fieldName.equals("specialisation")) && (Verifier
				.verifyString(newValue)))) {
			return true;
		} else if (Verifier.verifyString(newValue)) {
			if ((fieldName.equals("address"))
					|| (fieldName.equals("courseregistered")))
				return true;
		}
		return false;
	}

	public static void listOptionsforStudent() {
		System.out.println("1. Course Registered");
		System.out.println("2. Status");
		System.out.println("3. Status Date");
	}

	public static void listOptions() {
		System.out.println("\n\n****Welcome to DCMS****\n");
		System.out.println("Please Select one of the Option(1-5)");
		System.out.println("1. Create a Teacher Record");
		System.out.println("2. Create a Student Record");
		System.out.println("3. Get Record Counts");
		System.out.println("4. Edit Record");
		System.out.println("5. Transfer Record");
		System.out.println("6. Log out");
	}
}
