package tests;

import java.sql.SQLException;
import database.Database;
import entityClasses.LessonsLearned;
import entityClasses.LessonsLearnedList;

/*******
 * <p> Title: LessonDatabaseTestingAutomation Class </p>
 *
 * <p> Description: Automated tests TC-12 to TC-24 from Test Cases.pdf for creating, reading, updating
 * and deleting lessons in the database, including persistence across a reconnect.  The tests use the
 * LessonsLearned and LessonsLearnedList classes in the entityClasses package. </p>
 *
 * <p> Copyright: Elijah Batchelor © 2026 </p>
 *
 * @author Elijah Batchelor
 */
public class LessonDatabaseTestingAutomation {
	
	/** Default constructor is not used. */
    public LessonDatabaseTestingAutomation() {}

    static int numPassed = 0;
    static int numFailed = 0;

    static final String AUTHOR_A = "testAuthorA";    // fake users that exist only for these tests
    static final String AUTHOR_B = "testAuthorB";
    static final String GOOD_BODY = "Record with Zoom to the cloud and keep each video under 100 MB.";

    static final String CATEGORY_ERROR =
            "Error: Please enter valid category. (Bug Issues, Javadoc, Methods, Classes). Entered: ";

    static Database db;
    static LessonsLearnedList list;

    private static String notFound(int id) {
        return "Lesson not found. No lesson with id " + id + " exists. Select a lesson from your list.";
    }

    // Closes the connection and opens a fresh one, which simulates restarting the program
    private static void reconnect() throws SQLException {
        db.closeConnection();
        db = new Database();
        db.connectToDatabase();
        list = new LessonsLearnedList(db);
    }

    // Saves a new lesson for an author and returns the result message ("" means it was saved)
    private static String createLesson(String title, String body, String category, String author) {
        return list.create(new LessonsLearned(author, title, body, category));
    }

    // Loads one author's lessons into the list and returns how many there are
    private static int lessonsBy(String author) {
        return list.loadByContributor(author);
    }

    // Changes the title, body and category of one of AUTHOR_A's lessons
    private static String updateLesson(int id, String title, String body, String category) {
        LessonsLearned lesson = list.readForContributor(id, AUTHOR_A);
        if (lesson == null) {                    // not found: send the id so the not-found message is returned
            lesson = new LessonsLearned(AUTHOR_A, title, body, category);
            lesson.setId(id);
        }
        lesson.setTitle(title);
        lesson.setDescription(body);
        lesson.setCategory(category);
        return list.update(lesson, AUTHOR_A);
    }
    
	/**
	 * Runs every test case in this class and prints the number passed and failed.
	 *
	 * @param args not used
	 * @throws Exception if a database error stops the tests
	 */
    public static void main(String[] args) throws Exception {
        db = new Database();
        db.connectToDatabase();
        list = new LessonsLearnedList(db);
        cleanUp();                         // start with no leftover test lessons

        String result = createLesson("TP1 Video Question", GOOD_BODY, "Methods", AUTHOR_A);
        checkMessage("TC-12", "create accepts a valid lesson", result, "");

        check("TC-12", "exactly one lesson was saved", lessonsBy(AUTHOR_A) == 1);

        LessonsLearned saved = list.get(0);
        check("TC-12", "title was saved", saved.getTitle().equals("TP1 Video Question"));
        check("TC-12", "body was saved", saved.getDescription().equals(GOOD_BODY));
        check("TC-12", "category was saved", saved.getCategory().equals("Methods"));
        check("TC-12", "author was saved", saved.getContributor().equals(AUTHOR_A));
        check("TC-12", "an id was assigned", saved.getId() > 0);
        check("TC-12", "creation date was filled in", saved.getDateCreated().length() > 0);

        // TC-13: invalid lessons are not saved
        cleanUp();
        {
            String r1 = createLesson("", GOOD_BODY, "Methods", AUTHOR_A);
            checkMessage("TC-13", "empty title is rejected", r1, "Title is empty. Enter a title between 4 and 50 characters.");
            String r2 = createLesson("TP1 Video Question", GOOD_BODY, "Bugz", AUTHOR_A);
            checkMessage("TC-13", "invalid category is rejected", r2, CATEGORY_ERROR + "Bugz");
            check("TC-13", "no lesson was saved", lessonsBy(AUTHOR_A) == 0);
        }

        // TC-14: a lesson is still there after restarting
        cleanUp();
        {
            createLesson("TP1 Manage Invitations", GOOD_BODY, "Classes", AUTHOR_A);
            reconnect();
            check("TC-14", "lesson is still there after reconnecting", lessonsBy(AUTHOR_A) == 1);
            check("TC-14", "its title is unchanged",
                    list.size() == 1 && list.get(0).getTitle().equals("TP1 Manage Invitations"));
        }

        // TC-15: the list shows only my lessons
        cleanUp();
        {
            for (int i = 1; i <= 3; i++) {
                createLesson("Lesson A " + i, GOOD_BODY, "Methods", AUTHOR_A);
            }
            for (int i = 1; i <= 2; i++) {
                createLesson("Lesson B " + i, GOOD_BODY, "Methods", AUTHOR_B);
            }
            check("TC-15", "user A sees exactly 3 lessons", lessonsBy(AUTHOR_A) == 3);
            boolean allMine = true;
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).getContributor().equals(AUTHOR_A)) {
                    allMine = false;
                }
            }
            check("TC-15", "none of user B's lessons are in the list", allMine);
        }

        // TC-16: the lesson list with 0, 1 and 1,000 lessons
        cleanUp();
        {
            lessonsBy(AUTHOR_A);
            check("TC-16", "empty list has size 0", list.size() == 0 && list.isEmpty());
            checkMessage("TC-16", "empty list gives the 'no lessons' message",
                    list.getEmptyMessage(), "You have not created any lessons yet.");

            createLesson("Lesson 1", GOOD_BODY, "Methods", AUTHOR_A);
            check("TC-16", "list with one lesson has size 1", lessonsBy(AUTHOR_A) == 1);
            checkMessage("TC-16", "one-lesson list gives no empty message", list.getEmptyMessage(), "");

            for (int i = 2; i <= 1000; i++) {
                createLesson("Lesson " + i, GOOD_BODY, "Methods", AUTHOR_A);
            }
            check("TC-16", "list with 1,000 lessons has size 1000", lessonsBy(AUTHOR_A) == 1000);
            check("TC-16", "first and last lessons are in place",
                    list.get(0).getTitle().equals("Lesson 1") && list.get(999).getTitle().equals("Lesson 1000"));
        }

        // TC-17: read one lesson that exists
        cleanUp();
        {
            createLesson("HW2 Clarification", GOOD_BODY, "Javadoc", AUTHOR_A);
            lessonsBy(AUTHOR_A);
            int id = list.get(0).getId();
            LessonsLearned found = list.read(id);
            check("TC-17", "the lesson was found", found != null);
            check("TC-17", "all fields match what was saved", found != null
                    && found.getTitle().equals("HW2 Clarification")
                    && found.getDescription().equals(GOOD_BODY)
                    && found.getCategory().equals("Javadoc")
                    && found.getContributor().equals(AUTHOR_A)
                    && found.getDateCreated().length() > 0);
        }

        // TC-18: read one lesson that does not exist
        {
            checkMessage("TC-18", "id 9999 gives the not-found message", list.checkExists(9999), notFound(9999));
            check("TC-18", "read returns nothing for id 9999", list.read(9999) == null);
        }

        // TC-19: update with valid values
        cleanUp();
        {
            createLesson("Old Title", GOOD_BODY, "Methods", AUTHOR_A);
            lessonsBy(AUTHOR_A);
            int id = list.get(0).getId();
            String r = updateLesson(id, "New Title", "A new body that is long enough.", "Classes");
            checkMessage("TC-19", "valid update is accepted", r, "");
            reconnect();
            LessonsLearned changed = list.read(id);
            check("TC-19", "new values are stored after reconnecting", changed != null
                    && changed.getTitle().equals("New Title")
                    && changed.getDescription().equals("A new body that is long enough.")
                    && changed.getCategory().equals("Classes"));
        }

        // TC-20: invalid updates leave the lesson unchanged
        cleanUp();
        {
            createLesson("Keep This Title", GOOD_BODY, "Methods", AUTHOR_A);
            lessonsBy(AUTHOR_A);
            int id = list.get(0).getId();
            checkMessage("TC-20", "empty title is rejected",
                    updateLesson(id, "", GOOD_BODY, "Methods"),
                    "Title is empty. Enter a title between 4 and 50 characters.");
            checkMessage("TC-20", "14-character body is rejected",
                    updateLesson(id, "Keep This Title", "a".repeat(14), "Methods"),
                    "Text entry too short. Minimum 15 characters. Current: 14");
            checkMessage("TC-20", "invalid category is rejected",
                    updateLesson(id, "Keep This Title", GOOD_BODY, "Bugz"),
                    CATEGORY_ERROR + "Bugz");
            LessonsLearned same = list.read(id);
            check("TC-20", "the lesson still has its old values", same != null
                    && same.getTitle().equals("Keep This Title")
                    && same.getDescription().equals(GOOD_BODY)
                    && same.getCategory().equals("Methods"));
        }

        // TC-21: id, author and creation date do not change in an update
        cleanUp();
        {
            createLesson("Fixed Fields", GOOD_BODY, "Methods", AUTHOR_A);
            lessonsBy(AUTHOR_A);
            int id = list.get(0).getId();
            LessonsLearned before = list.read(id);
            updateLesson(id, "Fixed Fields Edited", GOOD_BODY, "Javadoc");
            LessonsLearned after = list.read(id);
            check("TC-21", "id, author and date are identical after the update",
                    before != null && after != null
                    && before.getId() == after.getId()
                    && before.getContributor().equals(after.getContributor())
                    && before.getDateCreated().equals(after.getDateCreated()));
        }

        // TC-22: update a lesson that does not exist
        cleanUp();
        {
            checkMessage("TC-22", "id 9999 gives the not-found message",
                    updateLesson(9999, "Some Title", GOOD_BODY, "Methods"), notFound(9999));
            check("TC-22", "nothing was created", lessonsBy(AUTHOR_A) == 0);
        }

        // TC-23: delete a lesson that exists
        cleanUp();
        {
            createLesson("Delete Me", GOOD_BODY, "Methods", AUTHOR_A);
            createLesson("Keep Me", GOOD_BODY, "Methods", AUTHOR_A);
            lessonsBy(AUTHOR_A);
            int deleteId = list.get(0).getId();
            checkMessage("TC-23", "delete is accepted", list.delete(deleteId, AUTHOR_A), "");
            reconnect();
            check("TC-23", "the deleted lesson is gone after reconnecting", list.read(deleteId) == null);
            check("TC-23", "the other lesson is untouched",
                    lessonsBy(AUTHOR_A) == 1 && list.get(0).getTitle().equals("Keep Me"));
        }

        // TC-24: delete a lesson that does not exist
        cleanUp();
        {
            createLesson("Still Here", GOOD_BODY, "Methods", AUTHOR_A);
            checkMessage("TC-24", "id 9999 gives the not-found message", list.delete(9999, AUTHOR_A), notFound(9999));
            check("TC-24", "other lessons are not affected", lessonsBy(AUTHOR_A) == 1);
        }

        cleanUp();                         // leave nothing behind
        db.closeConnection();
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    private static void check(String testCase, String what, boolean passed) {
        if (passed) {
            System.out.println("***Success*** " + testCase + ": " + what);
            numPassed++;
        } else {
            System.out.println("***Failure*** " + testCase + ": " + what);
            numFailed++;
        }
    }

    private static void checkMessage(String testCase, String what, String actual, String expected) {
        check(testCase, what, actual.equals(expected));
        if (!actual.equals(expected)) {
            System.out.println("   Expected: " + expected);
            System.out.println("   Actual:   " + actual);
        }
    }

    // Deletes every lesson belonging to the two fake test authors
    private static void cleanUp() {
        String[] testAuthors = {AUTHOR_A, AUTHOR_B};
        for (int a = 0; a < testAuthors.length; a++) {
            lessonsBy(testAuthors[a]);
            int[] ids = new int[list.size()];
            for (int i = 0; i < ids.length; i++) ids[i] = list.get(i).getId();
            for (int i = 0; i < ids.length; i++) list.curatorDelete(ids[i]);
        }
    }
}