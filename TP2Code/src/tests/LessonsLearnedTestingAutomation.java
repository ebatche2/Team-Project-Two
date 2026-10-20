package tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import entityClasses.LessonsLearned;
import entityClasses.LessonsLearned.Experience;
import entityClasses.LessonsLearnedList;

/*******
 * <p> Title: LessonsLearnedTestingAutomation Class </p>
 *
 * <p> Description: Semi-automated tests for the LessonsLearned and LessonsLearnedList classes,
 * in the same style as the PasswordEvaluator tests.  Each test prints "***Success***" or
 * "***Failure***" with the requirement it checks, and the totals are printed at the end.  The
 * run passed when it prints "Number of tests failed: 0".  The requirement names (C-1 to C-4,
 * CU-1 to CU-5, V-2, TC, DB) are explained in the LessonsLearned class. </p>
 *
 * <p> The tests use a separate in-memory database, so the real FoundationDatabase is never
 * changed.  The tables are first made exactly the way our Database class makes them, with one
 * lesson and one experience entry already in them, to check that lessons added from the Role1
 * page are still there after the new columns are added. </p>
 *
 * <p> Copyright: Rohan Arora, Ronald Yalung, Elijah Batchelor, Hung Ngo © 2026 </p>
 *
 * @author Rohan Arora
 *
 * @author Ronald Yalung
 *
 * @author Elijah Batchelor
 *
 * @author Hung Ngo
 *
 * @version 1.00	2026-10-19 Initial version for TP2
 */
public class LessonsLearnedTestingAutomation {

	static int numPassed = 0;
	static int numFailed = 0;

	static final String DB_URL = "jdbc:h2:mem:lessonsLearnedTest;DB_CLOSE_DELAY=-1";
	static final String ALICE = "alice";
	static final String BOB = "bob";
	static final String CURATOR = "carla";
	static final String GOOD_BODY = "Record with Zoom to the cloud and keep each video under 100 MB.";

	/*****
	 * <p> Method: main </p>
	 *
	 * <p> Description: Runs every test in order and prints the totals. </p>
	 *
	 * @param args are not used
	 *
	 * @throws Exception if the test database can't be opened
	 */
	public static void main(String[] args) throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection(DB_URL, "sa", "");

		// DB: make the tables the way Database.createTables does, and add a lesson and an
		// experience entry the way the Role1 page does
		try (Statement s = conn.createStatement()) {
			s.execute("CREATE TABLE lessonsDB (id INT AUTO_INCREMENT PRIMARY KEY, contributor VARCHAR(255), "
					+ "title VARCHAR(255), description VARCHAR(4000), lessonsLocked BOOL DEFAULT FALSE, "
					+ "titleLocked BOOL DEFAULT FALSE, descriptionLocked BOOL DEFAULT FALSE, "
					+ "curatorNotes VARCHAR(4000))");
			s.execute("ALTER TABLE lessonsDB ADD COLUMN IF NOT EXISTS lessonLocked BOOL DEFAULT FALSE");
			s.execute("CREATE TABLE experienceDB (id INT AUTO_INCREMENT PRIMARY KEY, lessonID INT, "
					+ "contributor VARCHAR(255), whatWasDone VARCHAR(4000), howItWasDone VARCHAR(4000), "
					+ "timeTaken VARCHAR(255), teamEffort VARCHAR(4000))");
			s.execute("INSERT INTO lessonsDB (contributor, title, description) VALUES "
					+ "('dana', 'Old Lesson From Role1', 'This lesson was added before TP2.')");
			s.execute("INSERT INTO experienceDB (lessonID, contributor, whatWasDone, howItWasDone, "
					+ "timeTaken, teamEffort) VALUES (1, 'dana', 'Used it', 'Same steps', '1 hour', 'Dana 1h')");
		}

		validationTests();
		LessonsLearnedList list = new LessonsLearnedList(conn);

		// ---- DB: lessons from the Role1 page are kept
		LessonsLearned old = list.read(1);
		check("DB-1", "DB: a lesson added before TP2 is still there", old != null
				&& old.getTitle().equals("Old Lesson From Role1") && old.getContributor().equals("dana"));
		check("DB-2", "DB: its experience entry is still there", old != null
				&& old.getExperiences().size() == 1
				&& old.getExperiences().get(0).getTimeTaken().equals("1 hour"));

		// ---- C-1: create
		LessonsLearned a = new LessonsLearned(ALICE, "TP1 Video Question", GOOD_BODY, "Methods");
		a.setCuratorNotes("a contributor must not be able to set this");
		checkMessage("CR-1", "C-1: a valid lesson is saved", a.create(list), "");
		check("CR-2", "C-1: an id and creation date were filled in",
				a.getId() > 0 && a.getDateCreated().length() > 0);
		check("CR-3", "C-1: a contributor can't set curator notes",
				list.read(a.getId()).getCuratorNotes().equals(""));
		checkMessage("CR-4", "C-1: a title already used (different case) is refused",
				list.addLesson(BOB, "tp1 video question", GOOD_BODY, "Methods"),
				LessonsLearnedList.DUPLICATE_TITLE_MESSAGE);
		checkMessage("CR-5", "C-1, TC-3: an invalid lesson is not saved",
				list.addLesson(BOB, "abc", GOOD_BODY, "Methods"),
				"Title too short. Minimum 4 characters. Current: 3");
		checkMessage("CR-6", "C-1: addLesson works like Database.addLesson",
				list.addLesson(ALICE, "Javadoc Tags", GOOD_BODY, "Javadoc"), "");
		list.addLesson(BOB, "Bob Lesson", "Bob's description is long enough.", "Classes");

		// ---- C-2, C-4: read
		LessonsLearned r = LessonsLearned.read(list, a.getId());
		check("RD-1", "C-1: every field is read back", r != null && r.getContributor().equals(ALICE)
				&& r.getTitle().equals("TP1 Video Question") && r.getDescription().equals(GOOD_BODY)
				&& r.getCategory().equals("Methods"));
		check("RD-2", "TC-18: a missing id returns null", list.read(9999) == null);
		checkMessage("RD-3", "TC-18: a missing id gives the not-found message", list.checkExists(9999),
				LessonsLearnedList.notFoundMessage(9999));
		List<String> mine = list.getLessonList(ALICE);
		check("RD-4", "C-2, DB: getLessonList matches the Role1 combo box format",
				mine.equals(Arrays.asList("<Select a Lesson>", a.getId() + ": TP1 Video Question",
						(a.getId() + 1) + ": Javadoc Tags")));
		check("RD-5", "C-4: another contributor's lesson can't be read",
				list.readForContributor(a.getId(), BOB) == null);
		List<String> all = list.getAllLessonList();
		check("RD-6", "DB: getAllLessonList matches the Role2 combo box format",
				all.size() == 5 && all.get(1).equals("1: Old Lesson From Role1 (dana)"));
		list.loadByContributor("nobody");
		checkMessage("RD-7", "C-2, TC-16: an empty list gives the empty message", list.getEmptyMessage(),
				LessonsLearnedList.EMPTY_MESSAGE);
		check("RD-8", "C-1: doesLessonTitleExist finds a title in any case",
				list.doesLessonTitleExist("JAVADOC TAGS", -1) && !list.doesLessonTitleExist("Javadoc Tags", a.getId() + 1));

		// ---- V-2: keyword search
		check("SR-1", "V-2: one keyword finds a lesson", list.search("ZOOM") == 2);
		check("SR-2", "V-2: every keyword must match", list.search("zoom javadoc") == 1);
		check("SR-3", "V-2: a category is searched too", list.search("classes") == 1);
		check("SR-4", "V-2: % is searched for as typed", list.search("%") == 0);
		check("SR-5", "V-2: blank keywords list every lesson", list.search("  ") == 4);

		// ---- C-1, C-2: contributor update
		checkMessage("UD-1", "C-1, DB: updateLessonTitle saves a new title",
				list.updateLessonTitle(a.getId(), ALICE, "TP1 Video Upload"), "");
		checkMessage("UD-2", "C-1, DB: updateLessonDescription saves a new description",
				list.updateLessonDescription(a.getId(), ALICE, "Use Zoom cloud recording for big videos."), "");
		LessonsLearned edit = list.readForContributor(a.getId(), ALICE);
		edit.setCategory("Bug Issues");
		checkMessage("UD-3", "C-1: the owner can change the category", edit.update(list, ALICE), "");
		LessonsLearned after = list.read(a.getId());
		check("UD-4", "C-1: all the new values are saved", after.getTitle().equals("TP1 Video Upload")
				&& after.getDescription().equals("Use Zoom cloud recording for big videos.")
				&& after.getCategory().equals("Bug Issues"));
		check("UD-5", "TC-21: id, owner, and creation date didn't change", after.getId() == a.getId()
				&& after.getContributor().equals(ALICE) && after.getDateCreated().equals(a.getDateCreated()));
		checkMessage("UD-6", "C-4: another contributor can't change it",
				list.updateLessonTitle(a.getId(), BOB, "Bob Was Here"), LessonsLearnedList.NOT_OWNER_MESSAGE);
		checkMessage("UD-7", "TC-20: an invalid description is refused",
				list.updateLessonDescription(a.getId(), ALICE, "too short"),
				"Text entry too short. Minimum 15 characters. Current: 9");
		check("UD-8", "TC-20: the refused change saved nothing",
				list.read(a.getId()).getDescription().equals("Use Zoom cloud recording for big videos."));
		checkMessage("UD-9", "TC-22: changing a missing lesson gives not-found",
				list.updateLessonTitle(9999, ALICE, "Some Title"), LessonsLearnedList.notFoundMessage(9999));

		// ---- C-3: experience information
		checkMessage("EX-1", "C-3, DB: addExperience works like Database.addExperience",
				list.addExperience(a.getId(), ALICE, "Recorded the demo", "Zoom cloud", "45 minutes", "Ana 1h"), "");
		check("EX-2", "C-3, DB: getExperienceList matches the Role1 list format",
				list.getExperienceList(a.getId(), ALICE).equals(Arrays.asList(
						"What: Recorded the demo | How: Zoom cloud | Time: 45 minutes | Effort: Ana 1h")));
		checkMessage("EX-3", "C-4: another contributor can't add experience",
				list.addExperience(a.getId(), BOB, "Tried it", "Same way", "1 hour", "Bob 1h"),
				LessonsLearnedList.NOT_OWNER_MESSAGE);
		checkMessage("EX-4", "C-3: all four fields are required",
				list.addExperience(a.getId(), ALICE, "Tried it", "Same way", "", "Ana 1h"),
				"Time taken is empty. Please fill in all four experience fields.");
		Experience e1 = list.readExperiences(a.getId()).get(0);
		e1.setTimeTaken("1 hour");
		checkMessage("EX-5", "C-3: the owner can change an entry", list.updateExperience(e1, ALICE), "");
		check("EX-6", "C-3: the change is saved",
				list.readExperiences(a.getId()).get(0).getTimeTaken().equals("1 hour"));
		Experience e2 = new Experience(ALICE, "Recorded again", "Cloud recording", "30 minutes", "Ana 1h");
		a.addExperience(list, e2);
		checkMessage("EX-7", "C-3: the owner can delete an entry", list.deleteExperience(e2.getId(), ALICE), "");
		check("EX-8", "C-3: the deleted entry is gone", list.readExperiences(a.getId()).size() == 1);

		// ---- C-1, C-2, CU-5: locks
		checkMessage("LK-1", "CU-5, DB: updateLessonLock locks the title",
				list.updateLessonLock(a.getId(), "Title", true), "");
		LessonsLearned locked = list.readForContributor(a.getId(), ALICE);
		check("LK-2", "C-2: the contributor sees which fields are locked",
				locked.isFieldLocked("Title") && !locked.isFieldLocked("Description"));
		checkMessage("LK-3", "C-1: a locked title can't change",
				list.updateLessonTitle(a.getId(), ALICE, "New Locked Title"),
				LessonsLearnedList.fieldLockedMessage("Title"));
		checkMessage("LK-4", "C-1: an unlocked field can still change",
				list.updateLessonDescription(a.getId(), ALICE, "Use cloud recording and check the size."), "");
		checkMessage("LK-5", "C-2, DB: a lesson with a locked field can't be deleted",
				list.deleteLesson(a.getId(), ALICE), LessonsLearnedList.DELETE_LOCKED_MESSAGE);
		list.updateLessonLock(a.getId(), "Title", false);
		checkMessage("LK-9", "CU-5: a curator locks the category",
				list.updateLessonLock(a.getId(), "Category", true), "");
		LessonsLearned catEdit = list.readForContributor(a.getId(), ALICE);
		catEdit.setCategory("Classes");
		checkMessage("LK-10", "C-1: a locked category can't change", list.update(catEdit, ALICE),
				LessonsLearnedList.fieldLockedMessage("Category"));
		checkMessage("LK-11", "C-2: a lesson with a locked category can't be deleted",
				list.deleteLesson(a.getId(), ALICE), LessonsLearnedList.DELETE_LOCKED_MESSAGE);
		list.updateLessonLock(a.getId(), "Category", false);
		list.updateLessonLock(a.getId(), "Title", true);
		list.updateLessonLock(a.getId(), "Lesson", true);
		checkMessage("LK-6", "C-1: nothing in a locked lesson can change",
				list.updateLessonDescription(a.getId(), ALICE, "Trying to change a locked lesson."),
				LessonsLearnedList.LESSON_LOCKED_MESSAGE);
		check("LK-7", "C-2: a locked lesson locks every field",
				list.read(a.getId()).isFieldLocked("Description") && list.read(a.getId()).isFieldLocked("Category"));
		checkMessage("LK-8", "CU-5: an unknown lock name is refused", list.updateLessonLock(a.getId(), "Body", true),
				"Unknown lock field. Use Lesson, Title, Description, or Category. Entered: Body");
		check("EX-9", "C-3: a locked lesson still takes new experience",
				list.addExperience(a.getId(), ALICE, "Used it again", "Same way", "20 minutes", "Ana 0.5h").equals(""));
		list.updateLessonLock(a.getId(), "Lesson", false);
		list.updateLessonLock(a.getId(), "Title", false);

		// ---- C-1, C-4, CU-1 to CU-4: curator information
		checkMessage("CU-1", "C-1: a curator adds private notes", list.setCuratorNotes(a.getId(), "Good"), "");
		checkMessage("CU-2", "CU-1: a curator adds search terms",
				list.setSearchTerms(a.getId(), Arrays.asList("screencast", "mp4")), "");
		check("CU-3", "CU-1, V-2: search looks in the curator's terms", list.search("mp4") == 1);
		list.setCuratorStatistics(a.getId(), "2 entries, about 40 minutes each.", false);
		LessonsLearned view = list.readForContributor(a.getId(), ALICE);
		check("CU-4", "C-1: the contributor can't see notes, terms, or hidden statistics",
				view.getCuratorNotes().equals("") && view.getSearchTerms().isEmpty()
				&& view.getCuratorStatistics().equals(""));
		list.setCuratorStatistics(a.getId(), "2 entries, about 40 minutes each.", true);
		check("CU-5", "CU-4: statistics made visible are shown to the contributor",
				list.readForContributor(a.getId(), ALICE).getCuratorStatistics().startsWith("2 entries"));
		LessonsLearned curated = list.read(a.getId());
		curated.setDescription("Record in the Zoom cloud; local files are often over 100 MB.");
		checkMessage("CU-6", "C-1: a curator can edit the lesson", list.curatorUpdate(curated, CURATOR), "");
		LessonsLearned seen = list.readForContributor(a.getId(), ALICE);
		check("CU-7", "C-4: the contributor still sees the lesson after the curator's edit", seen != null
				&& seen.getDescription().startsWith("Record in the Zoom cloud;")
				&& seen.getLastEditedBy().equals(CURATOR));

		// ---- Saved data is still there after reconnecting (TC-14)
		conn.close();
		conn = DriverManager.getConnection(DB_URL, "sa", "");
		list = new LessonsLearnedList(conn);
		LessonsLearned persisted = list.read(a.getId());
		check("PS-1", "TC-14: every change is still there after reconnecting", persisted != null
				&& persisted.getTitle().equals("TP1 Video Upload") && persisted.getCuratorNotes().equals("Good")
				&& persisted.getExperiences().size() == 2);

		// ---- C-1, C-2: delete
		checkMessage("DL-1", "C-4: another contributor can't delete", list.deleteLesson(a.getId(), BOB),
				LessonsLearnedList.NOT_OWNER_MESSAGE);
		list.loadByContributor(ALICE);
		checkMessage("DL-2", "C-1: the owner deletes an unlocked lesson", persisted.delete(list, ALICE), "");
		check("DL-3", "TC-23: the lesson is gone", list.read(a.getId()) == null);
		check("DL-4", "C-3: its experience entries are gone too", list.readExperiences(a.getId()).isEmpty());
		check("DL-5", "C-2: it is gone from the list", list.findById(a.getId()) == null);
		checkMessage("DL-6", "TC-24: deleting a missing lesson gives not-found", list.deleteLesson(9999, ALICE),
				LessonsLearnedList.notFoundMessage(9999));
		list.updateLessonLock(1, "Lesson", true);
		checkMessage("DL-7", "C-1: a curator can delete even a locked lesson", list.curatorDelete(1), "");

		conn.close();
		System.out.println("____________________________________________________________");
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}


	/*****
	 * <p> Method: void validationTests() </p>
	 *
	 * <p> Description: Tests the check methods.  VA-1 to VA-9 repeat TC-1 to TC-11 from our
	 * Test Cases. </p>
	 */
	private static void validationTests() {
		checkMessage("VA-1", "TC-1: a 4-character title is valid", LessonsLearned.checkTitle("Bugs"), "");
		checkMessage("VA-2", "TC-2: a blank title is refused", LessonsLearned.checkTitle("    "),
				"Title is empty. Enter a title between 4 and 50 characters.");
		checkMessage("VA-3", "TC-4: a 51-character title is refused", LessonsLearned.checkTitle("a".repeat(51)),
				"Title too long. Maximum 50. Current: 51");
		checkMessage("VA-4", "TC-5: a title needs a letter", LessonsLearned.checkTitle("(43324)"),
				"Title must contain at least one letter. Entered: (43324)");
		checkMessage("VA-5", "TC-6: a 500-character description is valid",
				LessonsLearned.checkDescription("a".repeat(500)), "");
		checkMessage("VA-6", "TC-7: a 14-character description is refused",
				LessonsLearned.checkDescription("a".repeat(14)),
				"Text entry too short. Minimum 15 characters. Current: 14");
		checkMessage("VA-7", "TC-8: a 501-character description is refused",
				LessonsLearned.checkDescription("a".repeat(501)), "Text entry too long. Maximum 500. Current: 501");
		checkMessage("VA-8", "TC-9: Bug Issues is a valid category", LessonsLearned.checkCategory("Bug Issues"), "");
		checkMessage("VA-9", "TC-11: an empty category is refused", LessonsLearned.checkCategory(""),
				"Error: Please enter valid category. (Bug Issues, Javadoc, Methods, Classes). Entered: (nothing)");
		checkMessage("VA-10", "C-1: a lesson with no contributor is refused",
				new LessonsLearned("", "Good Title", GOOD_BODY, "Methods").validate(),
				"Every lesson must have a contributor. No userName was given.");
		checkMessage("VA-11", "C-3: a complete experience entry is valid",
				new Experience(ALICE, "Recorded it", "Cloud recording", "30 minutes", "Ana 1h").validate(), "");
		checkMessage("VA-12", "C-3, DB: time taken can't be longer than its column",
				new Experience(ALICE, "Recorded it", "Cloud recording", "a".repeat(256), "Ana 1h").validate(),
				"Time taken too long. Maximum 255. Current: 256");
	}

	/*****
	 * <p> Method: void check(String testCase, String what, boolean passed) </p>
	 *
	 * <p> Description: Prints one result and counts it. </p>
	 *
	 * @param testCase is the test's name, e.g. "CR-1"
	 *
	 * @param what says what the test checks
	 *
	 * @param passed is true when the test passed
	 */
	private static void check(String testCase, String what, boolean passed) {
		if (passed) {
			System.out.println("***Success*** " + testCase + ": " + what);
			numPassed++;
		} else {
			System.out.println("***Failure*** " + testCase + ": " + what);
			numFailed++;
		}
	}

	/*****
	 * <p> Method: void checkMessage(String testCase, String what, String actual, String expected) </p>
	 *
	 * <p> Description: Compares the message a method returned with the expected one, and
	 * prints both when they are different. </p>
	 *
	 * @param testCase is the test's name
	 *
	 * @param what says what the test checks
	 *
	 * @param actual is the message the method returned
	 *
	 * @param expected is the message it should return
	 */
	private static void checkMessage(String testCase, String what, String actual, String expected) {
		check(testCase, what, actual.equals(expected));
		if (!actual.equals(expected)) {
			System.out.println("   Expected: " + expected);
			System.out.println("   Actual:   " + actual);
		}
	}
}