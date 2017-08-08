package replicaManagement;

import java.io.Serializable;

import classManagement.*;

public class Request implements Serializable{
	public final static int LOGIN_REQUEST = 0;
	public final static int CREATE_TEACHER_REQUEST = 1;
	public final static int CREATE_STUDENT_REQUEST = 2;
	public final static int EDIT_REQUEST = 3;
	public final static int TRANSFER_REQUEST = 4;
	public final static int GET_COUNT_REQUEST = 5;
	public final static int LOGOUT_REQUEST = 6;
	public final static int  RELIABLE_UDP= 7;

	//TODO: DESTINATION FOR TRANSFER
	public int typeOfRequest;
	public String managerID;
	//record attributes
	public String recordID;
	public String firstName;
	public String lastName;
	//teacher attributes
	public String address;
	public String phone;
	public String specialization;
	public String location;
	//student attributes
	public String courseRegistered;
	public boolean status;
	public String statusDate;
	//input for edit method
	public String fieldName;
	public String newValue;
	//input for transfer record
	public String destinationServer;

	public Request() {
	}
	public String toString(){
		String returnString="Request Type: "+typeOfRequest+" ManagerID :"+managerID;
		
		return returnString;
		
	}
	public Request(int typeOfRequest, String managerID) {
		this.typeOfRequest = typeOfRequest;
		this.managerID = managerID;
	}
}
