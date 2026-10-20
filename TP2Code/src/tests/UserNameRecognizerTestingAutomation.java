package tests;

import userNameRecognizerTestbed.UserNameRecognizer;

/*******
 * <p> Title: UserNameRecognizerTestingAutomation Class. </p>
 * 
 * <p> Description: A Java demonstration for semi-automated tests of UserNameRecognizer, following
 * 		the same pattern as PasswordEvaluationTestingAutomation.java from the course examples. </p>
 * 
 * <p> Copyright: Elijah Batchelor © 2026 </p>
 * 
 * @author Elijah Batchelor
 * 
 * @version 1.00	2026-09-18	Initial test suite for TP1, covering the 14 cases documented in
 * 		TP1 Test Cases.pdf.
 */
public class UserNameRecognizerTestingAutomation {
	
	/** Default constructor, not used. **/
	private UserNameRecognizerTestingAutomation() {}

	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests
	
	/**
	 * The main method that runs all tests
	 * 
	 * @param args takes command line arguments
	 * **/
	public static void main(String[] args) {
		System.out.println("______________________________________");
		System.out.println("\nUserNameRecognizer Testing Automation");

		/************** Start of the test cases **************/

		performTestCase(1, "abcd", true);
		performTestCase(2, "abc", false);
		performTestCase(3, "", false);
		performTestCase(4, "1abcd", false);
		performTestCase(5, "abcdefghijklmnop", true);		// exactly 16 chars
		performTestCase(6, "abcdefghijklmnopq", false);		// 17 chars, too long
		performTestCase(7, "ab.cd", true);
		performTestCase(8, "ab..cd", false);
		performTestCase(9, "ab_cd", true);
		performTestCase(10, "ab-cd", true);
		performTestCase(11, "ab&cd", true);
		performTestCase(12, "abcd.", false);
		performTestCase(13, "ab cd", false);
		performTestCase(14, "Abcd1", true);

		/************** End of the test cases **************/

		System.out.println("____________________________________________________________________________");
		System.out.println();
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	private static void performTestCase(int testCase, String inputText, boolean expectedPass) {

		System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + inputText + "\"");
		System.out.println("______________");

		String resultText = UserNameRecognizer.checkForValidUserName(inputText);

		System.out.println();

		if (resultText != "" && resultText.length() > 0) {
			if (expectedPass) {
				System.out.println("***Failure*** The username <" + inputText + "> is invalid." +
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			} else {
				System.out.println("***Success*** The username <" + inputText + "> is invalid." +
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			}
		} else {
			if (expectedPass) {
				System.out.println("***Success*** The username <" + inputText +
						"> is valid, so this is a pass!");
				numPassed++;
			} else {
				System.out.println("***Failure*** The username <" + inputText +
						"> was judged as valid" +
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
	}
}