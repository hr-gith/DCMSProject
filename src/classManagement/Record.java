package classManagement;

import java.io.Serializable;
/**
 * this class is responsible for providing the layout for Student And Teacher record
 * @author hamideh
 *
 */
public abstract class Record implements Serializable{

	public String recordID;
	public String firstName;
	public String lastName;
	public String managerID;
	public Record(String managerID,String recordID, String firstName, String lastName) {
		super();
		this.managerID=managerID;
		this.recordID = recordID;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}

	
	public String getRecordID() {
		return recordID;
	}

	public void setRecordID(String recordID) {
		this.recordID = recordID;
	}
	/**
	 * This method is overridden from Object class to provide string for the data its is having.
	 */
	public String toString() {
		return "Record ID : "+recordID+" FirstName : " + firstName + " LastName : " + lastName;
	}
}
