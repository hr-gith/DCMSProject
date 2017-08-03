package CORBAClassManagement;


/**
* CORBAClassManagement/CORBAClassManagementOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CORBAClassManagement.idl
* Thursday, August 3, 2017 7:30:16 PM EDT
*/

public interface CORBAClassManagementOperations 
{
  boolean login (String managerID);
  void logout ();
  boolean createTRecord (String managerID, String recordID, String firstName, String lastName, String address, String phone, String specialization, String location);
  String getRecordCounts ();
  boolean editRecord (String managerID, String recordID, String fieldName, String newValue);
  boolean createSRecord (String managerID, String recordID, String firstName, String lastName, String coursesRegistered, boolean status, String statusDate);
  boolean transferRecord (String managerID, String recordID, String remoteCenterServerName);
} // interface CORBAClassManagementOperations
