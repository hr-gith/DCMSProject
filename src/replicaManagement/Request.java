package replicaManagement;

import classManagement.*;
public class Request {
	private String id;
	private Record record;
	private boolean isGetCount;
	private String response;
	
	public Request(){
	}

	public Request(String id, Record record) {
		super();
		this.id = id;
		this.record = record;
		if (record != null)
			this.isGetCount = false;
		else
			this.isGetCount = true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	public boolean isGetCount() {
		return isGetCount;
	}

	public void setGetCount(boolean isGetCount) {
		this.isGetCount = isGetCount;
	}
}
