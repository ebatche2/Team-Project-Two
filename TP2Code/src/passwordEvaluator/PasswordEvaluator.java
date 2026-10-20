package passwordEvaluator;

/*******
 * <p> Title: PasswordEvaluator Class. </p>
 *
 * <p> Description: A standalone password validator derived from the Directed Graph (Finite State
 * Machine) design used in the PasswordEvaluationTestbed-F26 application's passwordPopUpWindow.Model
 * class. This class contains only the validation logic - it has no dependency on any GUI widgets -
 * so it can be called directly from the account creation and account update pages of this
 * application, or from an automated test suite, without needing to instantiate a JavaFX Stage.
 *
 * In addition to the requirements demonstrated in the original testbed (at least one upper case
 * letter, one lower case letter, one numeric digit, one special character, and a minimum length
 * of 8 characters), this class adds a maximum length requirement. Without a maximum length, a
 * malicious or careless user could submit an extremely long string to see whether doing so
 * crashes the application - one of the standard first steps an attacker takes when probing a new
 * system for weaknesses. </p>
 *
 * <p> Copyright: Elijah Batchelor © 2026 </p>
 *
 * @author Elijah Batchelor
 *
 * @version 1.00 2026-09-11 Adapted from passwordPopUpWindow.Model (Lynn Robert Carter, © 2025)
 * 		for use in the TP1 Foundations-SU26 application; added the maximum-length requirement.
 */
public class PasswordEvaluator {
	
	/** Default constructor, not used. **/
	private PasswordEvaluator() {}

	/** The fewest characters a password may contain. **/
	public static final int MIN_PASSWORD_LENGTH = 8;

	/** The most characters a password may contain. Input longer than this is rejected before any
	* character-by-character evaluation takes place.
	**/
	public static final int MAX_PASSWORD_LENGTH = 32;

	/** The set of characters that count as a "special character" for these requirements. **/
	private static final String SPECIAL_CHARACTERS = "~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/";

	/*-********************************************************************************************

	Result attributes. These are set every time evaluatePassword(...) is called, so a GUI (or a
	test) can inspect exactly which requirements were satisfied by the most recently evaluated
	password.

	 */
	
	/** A boolean that stores if we found an upper case character**/
	public static boolean foundUpperCase = false;
	
	/** A boolean that stores if we found a lower case character**/
	public static boolean foundLowerCase = false;
	
	/** A boolean that stores if we found a numeric digit**/
	public static boolean foundNumericDigit = false;
	
	/** A boolean that stores if we found a special character**/
	public static boolean foundSpecialChar = false;
	
	/** A boolean that stores if the password meets minimum character count**/
	public static boolean foundLongEnough = false;
	
	/** A boolean that stores if the password is over the maximum character count**/
	public static boolean foundNotTooLong = false;

	/** The index of the character that caused evaluation to stop, if any. -1 means no error. **/
	public static int passwordIndexofError = -1;

	/**********
	 * <p> Method: evaluatePassword(String input) </p>
	 *
	 * <p> Description: Evaluates the input String against the password requirements: at least one
	 * upper case letter, one lower case letter, one numeric digit, one special character, a
	 * minimum length of MIN_PASSWORD_LENGTH, and no more than MAX_PASSWORD_LENGTH characters.
	 *
	 * @param input the candidate password to be evaluated
	 *
	 * @return an empty String if the password satisfies every requirement, otherwise a String
	 * 		describing every requirement that was not satisfied
	 */
	public static String evaluatePassword(String input) {

		// Reset the requirement flags so results from a previous call cannot leak into this one
		foundUpperCase = false;
		foundLowerCase = false;
		foundNumericDigit = false;
		foundSpecialChar = false;
		foundLongEnough = false;
		foundNotTooLong = false;
		passwordIndexofError = -1;

		if (input == null || input.length() <= 0) {
			return "*** Error *** The password is empty!";
		}

		// Reject input that is too long before doing anything else with it. This guards against
		// the classic "drop a huge string into the field" crash-probing attack.
		if (input.length() > MAX_PASSWORD_LENGTH) {
			passwordIndexofError = MAX_PASSWORD_LENGTH;
			return "*** Error *** The password must not exceed " + MAX_PASSWORD_LENGTH +
					" characters.";
		}
		foundNotTooLong = true;

		// Walk the input one character at a time - a mechanical transformation of the same
		// Directed Graph design used by the original testbed - checking which category of
		// character each one belongs to.
		for (int i = 0; i < input.length(); i++) {
			char currentChar = input.charAt(i);

			if (currentChar >= 'A' && currentChar <= 'Z') {
				foundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				foundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				foundNumericDigit = true;
			} else if (SPECIAL_CHARACTERS.indexOf(currentChar) >= 0) {
				foundSpecialChar = true;
			} else {
				passwordIndexofError = i;
				return "*** Error *** An invalid character has been found!";
			}

			if (i >= MIN_PASSWORD_LENGTH - 1) {
				foundLongEnough = true;
			}
		}

		// Build a message that lists every requirement that was NOT satisfied
		String errMessage = "";
		if (!foundUpperCase) errMessage += "Upper case; ";
		if (!foundLowerCase) errMessage += "Lower case; ";
		if (!foundNumericDigit) errMessage += "Numeric digit; ";
		if (!foundSpecialChar) errMessage += "Special character; ";
		if (!foundLongEnough) errMessage += "At least " + MIN_PASSWORD_LENGTH + " characters; ";

		if (errMessage.equals(""))
			return "";

		passwordIndexofError = input.length();
		return errMessage + "conditions were not satisfied";
	}
}