package replica3.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Verifier {
	public static boolean verifyString(String string) {
		boolean args = false;
		if (string.matches(".*\\d.*")) {
			args = false;
		} else
			args = true;
		return args;
	}

	public static boolean verifynumber(String Number) {
		boolean args = false;
		if (Number.matches(".*[a-zA-Z]+.*") || Number.length() < 6) {

			args = false;
		} else
			args = true;
		return args;
	}

	public static boolean verifyCity(String city) {
		boolean args = false;
		if (!(city.toUpperCase().equals("MTL") || city.toUpperCase().equals("DDO")
				|| city.toUpperCase().equals("LVL"))) {
			args = false;
		} else
			args = true;
		return args;
	}

	public static boolean VerifyDate(String dateToValidate) throws java.text.ParseException {
		if (dateToValidate == null) {
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		sdf.setLenient(false);
		try {
			// if not valid, it will throw ParseException
			Date date = sdf.parse(dateToValidate);
			//System.out.println(date);

		} catch (Exception e) {

			// e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean VerifyStatus(String status) {
		if (status.trim().toLowerCase().equals("active")) {
			return true;
		} else if (status.trim().toLowerCase().equals("inactive")) {
			return true;
		} else
			return false;
	}

	public static boolean VerifyRecordId(String record) {
		if (record.length() != 7)
			return false;
		if ((record.startsWith("TR")) || record.startsWith("SR"))
			return true;
		else
			return false;
	}
}
