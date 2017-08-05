package replica1.servers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import classManagement.Record;
import classManagement.StudentRecord;
import classManagement.TeacherRecord;

public class HashRecord {
	private static HashRecord cutomerRecordObject = null;
	
	private HashMap<String, ArrayList<Record>> customerInfoTable = new HashMap<String, ArrayList<Record>>();
	private String DBFileName;

	public HashRecord(String fileName) {
		DBFileName = fileName;
		readFromFile();
	}

	public synchronized void saveToFile() {
		try {
			//File fileDB = new File(DBFileName);
			FileOutputStream fos = new FileOutputStream("DBFileName");
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(customerInfoTable);
			oos.flush();
			oos.close();
			fos.close();
		} catch (Exception e) {
			System.out.println("File not found: " + e.getStackTrace());
		}
		
	}

	public void readFromFile() {
		File fileDB = new File(DBFileName);
		if (fileDB.exists() && !fileDB.isDirectory()) {
			if (fileDB.isFile() && fileDB.canRead()) {
				try {
					FileInputStream fis = new FileInputStream(fileDB);
					ObjectInputStream ois = new ObjectInputStream(fis);

					customerInfoTable = (HashMap<String, ArrayList<Record>>) ois
							.readObject();

					ois.close();
					fis.close();
				} catch (Exception e) {
					System.out.println(e.getStackTrace());
				}
			} else
				System.out.println("File not found ");
		}
	}

	public boolean deleteRecord(String recordID) {
		// System.out.println("in record delete start "+recordID);
		Record record = this.getRecordByID(recordID);
		String keyValue = record.getLastName().charAt(0) + "".toUpperCase();
		ArrayList<Record> list = customerInfoTable.get(keyValue);
		// System.out.println("in record delete.. "+recordID);
		synchronized (record) {
			if (list.remove(record)) {
				saveToFile();
				return true;
			}
		}
		return false;
	}

	public Record getRecordByID(String recordID) {
		// System.out.println("Record id is :" + recordID);
		for (ArrayList<Record> listOfRecords : customerInfoTable.values()) {
			for (Record rec : listOfRecords) {
				if (rec.recordID.equals(recordID)) {
					// System.out.println("Record name" + rec.firstName);
					return rec;
				}
			}
		}
		return null;
	}

	public static HashRecord getCustomerInfoObject(String fileName) {
		if (cutomerRecordObject == null)
			cutomerRecordObject = new HashRecord(fileName);

		return cutomerRecordObject;
	}

	public boolean addRecord(Record record) {
		String keyValue = record.getLastName().charAt(0) + "".toUpperCase();
		if (customerInfoTable.get(keyValue) == null) {
			customerInfoTable.put(keyValue, new ArrayList<Record>());
		}
		// .println("--Record Added---");
		ArrayList<Record> list = customerInfoTable.get(keyValue);
		synchronized (record) {
			if (list.add(record)) {
				saveToFile();
				return true;
			}
		}
		return false;
	}

	public Map<String, ArrayList<Record>> getRecord() {
		return customerInfoTable;
	}

	public boolean editRecord(String recordID, String fieldName, String newValue) {
		fieldName = fieldName.trim().toLowerCase();

		Record recFound = this.getRecordByID(recordID);
		if (recFound == null) {
			// System.out.println("Record not found" + recordID);
			return false;
		} else {
			// System.out.println("Record Found" + recFound.getLastName());
			synchronized (recFound) {
				if (recFound != null) {
					if (recordID.startsWith("TR")) {
						// Teacher record
						if (fieldName.equals("address")) {
							((TeacherRecord) recFound).setAddress(newValue);
						} else if (fieldName.equals("phone")) {
							((TeacherRecord) recFound).setPhone(newValue);
						} else if (fieldName.equals("location")) {
							((TeacherRecord) recFound).setLocation(newValue);
						} else
							return false;
					} else if (recordID.startsWith("SR")) {
						// Student record
						if (fieldName.equals("courseregistered")) {
							((StudentRecord) recFound).setCourseRegistered(newValue);
						} else if (fieldName.equals("status")) {
							boolean status = false;
							if (newValue.toLowerCase().equals("active"))
								status = true;
							else if (newValue.toLowerCase().equals("inactive"))
								status = false;
							else
								return false;
							((StudentRecord) recFound).status = status;
						} else if (fieldName.equals("statusdate")) {
							((StudentRecord) recFound).setStatusDate(newValue);
						} else
							return false;
					}
				}
			}
			saveToFile();
			return true;

		}
	}

	public int getRecordCount() {
		int count = 0;
		for (ArrayList<Record> list : customerInfoTable.values()) {
			count = count + list.size();
		}
		return count;
	}
}
