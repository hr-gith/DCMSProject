package classManagement;
/**
 * It is a layout for information of a student
 * inherited from Record class
 * @author hamideh
 *
 */
public class StudentRecord extends Record {

	public String courseRegistered;
	public boolean status;
	public String statusDate;

	public StudentRecord(String managerID, String recordID, String firstName, String lastName, String courseRegisterd,
			boolean status, String statusDate) {
		super(managerID, recordID, firstName, lastName);
		this.courseRegistered = courseRegisterd;
		this.status = status;
		this.statusDate = statusDate;
	}

	public void setCourseRegistered(String courseRegistered) {
		this.courseRegistered = courseRegistered;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}

	@Override
	public String toString() {

		return "Student Record - " + super.toString() + " Course Registered : " + courseRegistered + " Status : "
				+ status + " StatusDate : " + statusDate + "\n";
	}

}
