package tests;

import entityClasses.LessonsLearned;

/*******
 * <p> Title: LessonValidatorTestingAutomation Class </p>
 *
 * <p> Description: Automated tests TC-1 to TC-11 from Test Cases.pdf for the lesson title, body and
 * category checks. Each test passes only when the exact expected message is returned.  The
 * checks are the static check methods of the LessonsLearned class. </p>
 *
 * <p> Copyright: Elijah Batchelor © 2026 </p>
 *
 * @author Elijah Batchelor
 */
public class LessonValidatorTestingAutomation {

	static int numPassed = 0;
	static int numFailed = 0;

	private static final String CATEGORY_ERROR = "Error: Please enter valid category. (Bug Issues, Javadoc, Methods, Classes). Entered: ";

	public static void main(String[] args) {

		// Title (US-6)
		performTestCase("TC-1a", "title", "100 MB Video Max", true, "");
		performTestCase("TC-1b", "title", "Bugs", true, "");
		performTestCase("TC-1c", "title", "a".repeat(50), true, "");
		performTestCase("TC-2a", "title", "", false, "Title is empty. Enter a title between 4 and 50 characters.");
		performTestCase("TC-2b", "title", "      ", false, "Title is empty. Enter a title between 4 and 50 characters.");
		performTestCase("TC-3",  "title", "abc", false, "Title too short. Minimum 4 characters. Current: 3");
		performTestCase("TC-4",  "title", "a".repeat(51), false, "Title too long. Maximum 50. Current: 51");
		performTestCase("TC-5a", "title", "!!!!!", false, "Title must contain at least one letter. Entered: !!!!!");
		performTestCase("TC-5b", "title", "(43324)", false, "Title must contain at least one letter. Entered: (43324)");

		// Body (US-7)
		performTestCase("TC-6a", "body", "Record with Zoom to the cloud and keep each video under 100 MB.", true, "");
		performTestCase("TC-6b", "body", "a".repeat(15), true, "");
		performTestCase("TC-6c", "body", "a".repeat(500), true, "");
		performTestCase("TC-7a", "body", "a".repeat(14), false, "Text entry too short. Minimum 15 characters. Current: 14");
		performTestCase("TC-7b", "body", "", false, "Text entry too short. Minimum 15 characters. Current: 0");
		performTestCase("TC-8",  "body", "a".repeat(501), false, "Text entry too long. Maximum 500. Current: 501");

		// Category (US-8)
		performTestCase("TC-9a", "category", "Bug Issues", true, "");
		performTestCase("TC-9b", "category", "Javadoc", true, "");
		performTestCase("TC-9c", "category", "Methods", true, "");
		performTestCase("TC-9d", "category", "Classes", true, "");
		performTestCase("TC-10", "category", "Bugz", false, CATEGORY_ERROR + "Bugz");
		performTestCase("TC-11", "category", "", false, CATEGORY_ERROR + "(nothing)");
		
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	private static void performTestCase(String testCase, String field, String input, boolean expectedPass,
			String expectedMessage) {

		System.out.println("____________________________________________________________");
		System.out.println("Test case: " + testCase);
		System.out.println("Input: \"" + input + "\"");

		// Step 1: run the checker for the field being tested
		String resultText = "";
		if (field.equals("title")) {
			resultText = LessonsLearned.checkTitle(input);
		} else if (field.equals("body")) {
			resultText = LessonsLearned.checkDescription(input);
		} else if (field.equals("category")) {
			resultText = LessonsLearned.checkCategory(input);
		} else {
			System.out.println("***Failure*** Unknown field: " + field);
			numFailed++;
			return; // stop here; there is nothing to judge
		}

		// Step 2: judge the result
		if (resultText.length() > 0) { // the checker returned an error
			if (expectedPass) {
				System.out.println("***Failure*** The " + field + " was rejected, but it was supposed to be valid.");
				System.out.println("Error message: " + resultText);
				numFailed++;
			} else if (resultText.equals(expectedMessage)) {
				System.out.println("***Success*** The " + field + " was rejected with the expected message.");
				System.out.println("Error message: " + resultText);
				numPassed++;
			} else {
				System.out.println("***Failure*** The " + field + " was rejected, but with the wrong message.");
				System.out.println("Expected: " + expectedMessage);
				System.out.println("Actual:   " + resultText);
				numFailed++;
			}
		} else { // the checker returned ""
			if (expectedPass) {
				System.out.println("***Success*** The " + field + " is valid, as expected.");
				numPassed++;
			} else {
				System.out.println("***Failure*** The " + field + " was accepted, but it was supposed to be rejected.");
				numFailed++;
			}
		}
	}
}
