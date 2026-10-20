package tests;

import passwordEvaluator.PasswordEvaluator;

/*******
 * <p> Title: PasswordEvaluatorTestingAutomation Class. </p>
 * 
 * <p> Description: A Java demonstration for semi_automated tests of PasswordEvaluator, following
 * 		the same pattern as PasswordEvaluatorTestingAutomation.java from the course examples. </p>
 * 
 * <p> Copyright: Elijah Batchelor © 2026 </p>
 * 
 * @version 1.00 2026-09-18 Initial test suite for TP1, covering the 12 cases documented in
 * 		TP1 Test Cases.pdf, including the new maximum-length requirement.
 */

public class PasswordEvaluatorTestingAutomated {
	
	/** Default constructor, not used. **/
	private PasswordEvaluatorTestingAutomated() {}
	
	static int numPassed = 0;
	static int numFailed = 0;
	
	/**
	 * The main method that runs all tests
	 * 
	 * @param args takes command line arguments
	 * **/
	public static void main(String[] args) {
		System.out.println("--------------------------------------");
		System.out.println("\nPasswordEvaluator Testing Automation");
		
		/************** Start of the test cases **************/
		
		performTestCase(1, "Aa!15678", true);
		performTestCase(2, "A!", false);
		performTestCase(3, "", false);
		performTestCase(4, "alllowercase1!", false);
		performTestCase(5, "ALLUPPERCASE1!", false);
		performTestCase(6, "NoDigitsHere", false);
		performTestCase(7, "NoSpecialChar1", false);
		performTestCase(8, "Short1!", false);				// 7 chars, one under minimum
		performTestCase(9, "Aa1!aaaa", true);					// exactly 8 chars
		performTestCase(10, "Aa1!" + "a".repeat(29), false);	// 33 chars, one over minimum
		performTestCase(11, "Aa1!" + "a".repeat(28), true); // exactly 32 chars
		performTestCase(12, "Aa1!goodpq\u00e9", false);			// contains an invalid character
		
		/*************** End of the test cases ***************/
		
		System.out.println("----------------------------------------------------------------------------");
		System.out.println();
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}
	
	private static void performTestCase(int testCase, String inputText, boolean expectedPass) {
		System.out.println("----------------------------------------------------------------------------\n\nTest case: " + testCase);
		System.out.println("Input \"" + inputText + "\" (length " + inputText.length() + ")");
		System.out.println("-------------");
		
		String resultText = PasswordEvaluator.evaluatePassword(inputText);
		
		System.out.println();
		
		if (resultText != "" && resultText.length() > 0) {
			if (expectedPass) {
				System.out.println("***Failure*** The password <" + inputText + "> is invalid." +
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			} else {
				System.out.println("***Success*** The password <" + inputText + "> is invalid." +
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " +resultText);
				numPassed++;
			}
		} else {
			if (expectedPass) {
				System.out.println("***Success*** the password <" + inputText + 
						"> is valid, so this is a pass!");
				numPassed++;
			} else {
				System.out.println("***Failure*** The password <" + inputText + 
						"\n was judged as valid" + 
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
	}
}
