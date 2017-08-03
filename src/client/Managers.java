package client;

public class Managers {
	private static String[] managerID={"MTL0001","MTL0002","MTL0003","DDO1001","DDO1002","LVL2001","LVL2002"};
	public static boolean verifyManager(String id)
	{
		for(int i=0;i<managerID.length;i++)
		{
			if(managerID[i].equalsIgnoreCase(id))
				return true;
		}
		return false;
	}
}
