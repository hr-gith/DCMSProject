package frontEnd;

import java.lang.reflect.GenericArrayType;

public class SequenceIdGenerator {

	private static int id = 0;
	private static SequenceIdGenerator instance = null;

	public static SequenceIdGenerator getInstance() {
		if (instance == null)
			instance = new SequenceIdGenerator();
		return instance;

	}

	private SequenceIdGenerator() {
	}

	public static String getID(String type) {
		String correctedID = null;
		++id;
		correctedID = String.format("%05d", id);
		//System.out.println("less than 10");
		if (type.equals("TR"))
			return "TR" + correctedID;
		else if (type.equals("SR"))
			return "SR" + correctedID;
		return "Incorrect Type";
	}
}
