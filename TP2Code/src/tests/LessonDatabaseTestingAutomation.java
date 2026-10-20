package tests;

import java.sql.SQLException;
import database.Database;
import entityClasses.Lesson;
import entityClasses.LessonList;

/*******
 * <p> Title: LessonDatabaseTestingAutomation Class </p>
 *
 * <p> Description: Automated tests TC-12 to TC-24 from Test Cases.pdf for creating, reading, updating
 * and deleting lessons in the database, including persistence across a reconnect. </p>
 *
 * <p> Copyright: Elijah Batchelor © 2026 </p>
 *
 * @author Elijah Batchelor
 */
public class LessonDatabaseTestingAutomation {

    static int numPassed = 0;
    static int numFailed = 0;

    static final int AUTHOR_A = 9001;    // fake users that exist only for these tests
    static final int AUTHOR_B = 9002;
    static final String GOOD_BODY = "Record with Zoom to the cloud and keep each video under 100 MB.";
    
    static final String CATEGORY_ERROR =
            "Error: Please enter valid category. (Bug Issues, Javadoc, Methods, Classes). Entered: ";

    private static String notFound(int id) {
        return "Lesson not found. No lesson with id " + id + " exists. Select a lesson from your list.";
    }

    // Closes the connection and opens a fresh one, which simulates restarting the program
    private static Database reconnect(Database old) throws SQLException {
        old.closeConnection();
        Database fresh = new Database();
        fresh.connectToDatabase();
        return fresh;
    }
    
    public static void main(String[] args) throws Exception {
        Database db = new Database();
        db.connectToDatabase();
        cleanUp(db);                         // start with no leftover test lessons

        String result = db.createLesson("TP1 Video Question", GOOD_BODY, "Methods", AUTHOR_A);
        checkMessage("TC-12", "createLesson accepts a valid lesson", result, "");

        LessonList list = db.getLessonsByAuthor(AUTHOR_A);
        check("TC-12", "exactly one lesson was saved", list.size() == 1);

        Lesson saved = list.get(0);
        check("TC-12", "title was saved", saved.getTitle().equals("TP1 Video Question"));
        check("TC-12", "body was saved", saved.getBody().equals(GOOD_BODY));
        check("TC-12", "category was saved", saved.getCategory().equals("Methods"));
        check("TC-12", "author id was saved", saved.getAuthorId() == AUTHOR_A);
        check("TC-12", "an id was assigned", saved.getId() > 0);
        check("TC-12", "creation date was filled in", saved.getDateCreated().length() > 0);
        
        
        
        // TC-13: invalid lessons are not saved
        cleanUp(db);
        {
            String r1 = db.createLesson("", GOOD_BODY, "Methods", AUTHOR_A);
            checkMessage("TC-13", "empty title is rejected", r1, "Title is empty. Enter a title between 4 and 50 characters.");
            String r2 = db.createLesson("TP1 Video Question", GOOD_BODY, "Bugz", AUTHOR_A);
            checkMessage("TC-13", "invalid category is rejected", r2, CATEGORY_ERROR + "Bugz");
            check("TC-13", "no lesson was saved", db.getLessonsByAuthor(AUTHOR_A).size() == 0);
        }

        // TC-14: a lesson is still there after restarting
        cleanUp(db);
        {
            db.createLesson("TP1 Manage Invitations", GOOD_BODY, "Classes", AUTHOR_A);
            db = reconnect(db);
            LessonList persisted = db.getLessonsByAuthor(AUTHOR_A);
            check("TC-14", "lesson is still there after reconnecting", persisted.size() == 1);
            check("TC-14", "its title is unchanged",
                    persisted.size() == 1 && persisted.get(0).getTitle().equals("TP1 Manage Invitations"));
        }

        // TC-15: the list shows only my lessons
        cleanUp(db);
        {
            for (int i = 1; i <= 3; i++) {
                db.createLesson("Lesson A " + i, GOOD_BODY, "Methods", AUTHOR_A);
            }
            for (int i = 1; i <= 2; i++) {
                db.createLesson("Lesson B " + i, GOOD_BODY, "Methods", AUTHOR_B);
            }
            LessonList mine = db.getLessonsByAuthor(AUTHOR_A);
            check("TC-15", "user A sees exactly 3 lessons", mine.size() == 3);
            boolean allMine = true;
            for (int i = 0; i < mine.size(); i++) {
                if (mine.get(i).getAuthorId() != AUTHOR_A) {
                    allMine = false;
                }
            }
            check("TC-15", "none of user B's lessons are in the list", allMine);
        }

        // TC-16: the lesson list class with 0, 1 and 1,000 lessons (no database needed)
        {
            LessonList none = new LessonList();
            check("TC-16", "empty list has size 0", none.size() == 0 && none.isEmpty());
            checkMessage("TC-16", "empty list gives the 'no lessons' message",
                    none.getEmptyMessage(), "You have not created any lessons yet.");

            LessonList one = new LessonList();
            one.add(new Lesson(1, "Lesson 1", GOOD_BODY, "Methods", AUTHOR_A, "2026-10-03"));
            check("TC-16", "list with one lesson has size 1", one.size() == 1);
            checkMessage("TC-16", "one-lesson list gives no empty message", one.getEmptyMessage(), "");

            LessonList many = new LessonList();
            for (int i = 1; i <= 1000; i++) {
                many.add(new Lesson(i, "Lesson " + i, GOOD_BODY, "Methods", AUTHOR_A, "2026-10-03"));
            }
            check("TC-16", "list with 1,000 lessons has size 1000", many.size() == 1000);
            check("TC-16", "first and last lessons are in place",
                    many.get(0).getId() == 1 && many.get(999).getId() == 1000);
        }

        // TC-17: read one lesson that exists
        cleanUp(db);
        {
            db.createLesson("HW2 Clarification", GOOD_BODY, "Javadoc", AUTHOR_A);
            int id = db.getLessonsByAuthor(AUTHOR_A).get(0).getId();
            Lesson found = db.getLesson(id);
            check("TC-17", "the lesson was found", found != null);
            check("TC-17", "all fields match what was saved", found != null
                    && found.getTitle().equals("HW2 Clarification")
                    && found.getBody().equals(GOOD_BODY)
                    && found.getCategory().equals("Javadoc")
                    && found.getAuthorId() == AUTHOR_A
                    && found.getDateCreated().length() > 0);
        }

        // TC-18: read one lesson that does not exist
        {
            checkMessage("TC-18", "id 9999 gives the not-found message", db.checkLessonExists(9999), notFound(9999));
            check("TC-18", "getLesson returns nothing for id 9999", db.getLesson(9999) == null);
        }

        // TC-19: update with valid values
        cleanUp(db);
        {
            db.createLesson("Old Title", GOOD_BODY, "Methods", AUTHOR_A);
            int id = db.getLessonsByAuthor(AUTHOR_A).get(0).getId();
            String r = db.updateLesson(id, "New Title", "A new body that is long enough.", "Classes");
            checkMessage("TC-19", "valid update is accepted", r, "");
            db = reconnect(db);
            Lesson changed = db.getLesson(id);
            check("TC-19", "new values are stored after reconnecting", changed != null
                    && changed.getTitle().equals("New Title")
                    && changed.getBody().equals("A new body that is long enough.")
                    && changed.getCategory().equals("Classes"));
        }

        // TC-20: invalid updates leave the lesson unchanged
        cleanUp(db);
        {
            db.createLesson("Keep This Title", GOOD_BODY, "Methods", AUTHOR_A);
            int id = db.getLessonsByAuthor(AUTHOR_A).get(0).getId();
            checkMessage("TC-20", "empty title is rejected",
                    db.updateLesson(id, "", GOOD_BODY, "Methods"),
                    "Title is empty. Enter a title between 4 and 50 characters.");
            checkMessage("TC-20", "14-character body is rejected",
                    db.updateLesson(id, "Keep This Title", "a".repeat(14), "Methods"),
                    "Text entry too short. Minimum 15 characters. Current: 14");
            checkMessage("TC-20", "invalid category is rejected",
                    db.updateLesson(id, "Keep This Title", GOOD_BODY, "Bugz"),
                    CATEGORY_ERROR + "Bugz");
            Lesson same = db.getLesson(id);
            check("TC-20", "the lesson still has its old values", same != null
                    && same.getTitle().equals("Keep This Title")
                    && same.getBody().equals(GOOD_BODY)
                    && same.getCategory().equals("Methods"));
        }

        // TC-21: id, author id and creation date do not change in an update
        cleanUp(db);
        {
            db.createLesson("Fixed Fields", GOOD_BODY, "Methods", AUTHOR_A);
            int id = db.getLessonsByAuthor(AUTHOR_A).get(0).getId();
            Lesson before = db.getLesson(id);
            db.updateLesson(id, "Fixed Fields Edited", GOOD_BODY, "Javadoc");
            Lesson after = db.getLesson(id);
            check("TC-21", "id, author id and date are identical after the update",
                    before != null && after != null
                    && before.getId() == after.getId()
                    && before.getAuthorId() == after.getAuthorId()
                    && before.getDateCreated().equals(after.getDateCreated()));
        }

        // TC-22: update a lesson that does not exist
        cleanUp(db);
        {
            checkMessage("TC-22", "id 9999 gives the not-found message",
                    db.updateLesson(9999, "Some Title", GOOD_BODY, "Methods"), notFound(9999));
            check("TC-22", "nothing was created", db.getLessonsByAuthor(AUTHOR_A).size() == 0);
        }

        // TC-23: delete a lesson that exists
        cleanUp(db);
        {
            db.createLesson("Delete Me", GOOD_BODY, "Methods", AUTHOR_A);
            db.createLesson("Keep Me", GOOD_BODY, "Methods", AUTHOR_A);
            int deleteId = db.getLessonsByAuthor(AUTHOR_A).get(0).getId();
            checkMessage("TC-23", "delete is accepted", db.deleteLesson(deleteId), "");
            db = reconnect(db);
            LessonList left = db.getLessonsByAuthor(AUTHOR_A);
            check("TC-23", "the deleted lesson is gone after reconnecting", db.getLesson(deleteId) == null);
            check("TC-23", "the other lesson is untouched",
                    left.size() == 1 && left.get(0).getTitle().equals("Keep Me"));
        }

        // TC-24: delete a lesson that does not exist
        cleanUp(db);
        {
            db.createLesson("Still Here", GOOD_BODY, "Methods", AUTHOR_A);
            checkMessage("TC-24", "id 9999 gives the not-found message", db.deleteLesson(9999), notFound(9999));
            check("TC-24", "other lessons are not affected", db.getLessonsByAuthor(AUTHOR_A).size() == 1);
        }
        
        cleanUp(db);                         // leave nothing behind
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
    private static void cleanUp(Database db) throws SQLException {
        int[] testAuthors = {AUTHOR_A, AUTHOR_B};
        for (int a = 0; a < testAuthors.length; a++) {
            LessonList list = db.getLessonsByAuthor(testAuthors[a]);
            for (int i = 0; i < list.size(); i++) {
                db.deleteLesson(list.get(i).getId());
            }
        }
    }
}