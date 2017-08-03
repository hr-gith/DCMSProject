package classManagement;
/**
 * It is a layout for information of a teacher
 * inherited from Record class
 * @author hamideh
 *
 */
public class TeacherRecord extends Record {

	public String address;
	public String phone;
	public String specialization;
	public String location;

	public TeacherRecord(String managerID, String recordID, String firstName, String lastName, String address,
			String phone, String specialization, String location) {
		super(managerID, recordID, firstName, lastName);
		this.address = address;
		this.phone = phone;
		this.specialization = specialization;
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	@Override
	public String toString() {

		return "Teacher Record - " + super.toString() + " Address : " + address + " Phone : " + phone
				+ " Specialization : " + specialization + " Location : " + location + "\n";
	}
}
