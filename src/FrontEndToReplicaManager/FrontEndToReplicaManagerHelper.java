package FrontEndToReplicaManager;


/**
* FrontEndToReplicaManager/FrontEndToReplicaManagerHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FrontEndToReplicaManager.idl
* Friday, August 4, 2017 3:09:21 o'clock PM EDT
*/

abstract public class FrontEndToReplicaManagerHelper
{
  private static String  _id = "IDL:FrontEndToReplicaManager/FrontEndToReplicaManager:1.0";

  public static void insert (org.omg.CORBA.Any a, FrontEndToReplicaManager that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static FrontEndToReplicaManager extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (FrontEndToReplicaManagerHelper.id (), "FrontEndToReplicaManager");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static FrontEndToReplicaManager read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_FrontEndToReplicaManagerStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, FrontEndToReplicaManager value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static FrontEndToReplicaManager narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof FrontEndToReplicaManager)
      return (FrontEndToReplicaManager)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _FrontEndToReplicaManagerStub stub = new _FrontEndToReplicaManagerStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static FrontEndToReplicaManager unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof FrontEndToReplicaManager)
      return (FrontEndToReplicaManager)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _FrontEndToReplicaManagerStub stub = new _FrontEndToReplicaManagerStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
