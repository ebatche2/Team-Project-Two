package entityClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import entityClasses.LessonsLearned.Experience;

/*******
 * <p> Title: LessonsLearnedList Class </p>
 *
 * <p> Description: This LessonsLearnedList class stores lessons learned in the database and
 * does all of the Create, Read, Update, and Delete (CRUD) work for lessons and their experience
 * entries.  It also keeps a list of the lessons it last loaded so a page can show them.  The TP2
 * assignment asks for a list of all the lessons in the system (loadAll) and for smaller lists
 * such as the lessons matching some keywords (search). </p>
 *
 * <p> It uses the same lessonsDB and experienceDB tables as our Database class, and it has
 * methods with the same names as the lesson methods the Role1 and Role2 home pages already
 * call (getLessonList, getAllLessonList, addLesson, doesLessonTitleExist, updateLessonTitle,
 * updateLessonDescription, deleteLesson, updateLessonLock, addExperience, getExperienceList).
 * That way those pages can switch from theDatabase to this class with very few changes.  The
 * difference is that these methods check the input first and return an error message the page
 * can show, instead of only true or false.  The requirement names (C-1, C-2, ...) are
 * explained in the LessonsLearned class. </p>
 *
 * <p> How this class supports the Contributor User Stories: </p>
 * <ul>
 *   <li>C-1: create() / addLesson() add a lesson, and update(), updateLessonTitle(),
 *       updateLessonDescription(), and delete() / deleteLesson() manage it.  Other roles use
 *       curatorUpdate(), curatorDelete(), and updateLessonLock().</li>
 *   <li>C-2: getLessonList() and loadByContributor() list the contributor's lessons, and
 *       readForContributor() shows one with its locks.  deleteLesson() refuses locked
 *       lessons.</li>
 *   <li>C-3: addExperience(), getExperienceList(), readExperiences(), updateExperience(), and
 *       deleteExperience() handle experience information.</li>
 *   <li>C-4: every contributor method checks the owner and leaves out the curator-only
 *       information, so contributors only see their own lessons.</li>
 * </ul>
 *
 * <p> CRUD operations: </p>
 * <ul>
 *   <li>Create: create, addLesson, addExperience</li>
 *   <li>Read: read, readForContributor, getLessonList, getAllLessonList, getExperienceList,
 *       readExperiences, checkExists, doesLessonTitleExist, loadAll, loadByContributor,
 *       search</li>
 *   <li>Update: update, updateLessonTitle, updateLessonDescription, updateExperience, and for
 *       curators curatorUpdate, updateLessonLock, setCuratorNotes, setSearchTerms,
 *       setCuratorStatistics</li>
 *   <li>Delete: delete, deleteLesson, deleteExperience, curatorDelete</li>
 * </ul>
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

public class LessonsLearnedList {

	/*-*******************************************************************************************
	 * Messages used by several methods.  They are public so the tests can compare with them.
	 */

	/** Shown when the contributor has no lessons (C-2). */
	public static final String EMPTY_MESSAGE = "You have not created any lessons yet.";
	/** Shown when a contributor tries to change someone else's lesson (C-4). */
	public static final String NOT_OWNER_MESSAGE = "You can only change lessons you created.";
	/** Shown when the whole lesson is locked (C-1, CU-5). */
	public static final String LESSON_LOCKED_MESSAGE =
			"This lesson is locked by a curator and cannot be changed.";
	/** Shown when a contributor tries to delete a lesson with any lock (C-2).  Same wording as
	 *  the Role1 page. */
	public static final String DELETE_LOCKED_MESSAGE = "This lesson is locked and can not be deleted.";
	/** Shown when another lesson already has the title.  Same wording as the Role1 page. */
	public static final String DUPLICATE_TITLE_MESSAGE = "A lesson with this title already exists.";
	/** Shown when an experience entry doesn't exist. */
	public static final String EXPERIENCE_NOT_FOUND_MESSAGE = "Experience entry not found.";

	/*****
	 * <p> Method: String notFoundMessage(int id) </p>
	 *
	 * <p> Description: The message used when no lesson has the given id.  The wording comes
	 * from TC-18, TC-22, and TC-24. </p>
	 *
	 * @param id is the id that was not found
	 *
	 * @return the not-found message
	 */
	public static String notFoundMessage(int id) {
		return "Lesson not found. No lesson with id " + id + " exists. Select a lesson from your list.";
	}

	/*****
	 * <p> Method: String fieldLockedMessage(String field) </p>
	 *
	 * <p> Description: The message used when an update tries to change a locked field
	 * (C-1). </p>
	 *
	 * @param field is "Title", "Description", or "Category"
	 *
	 * @return the locked-field message
	 */
	public static String fieldLockedMessage(String field) {
		return "The " + field.toLowerCase() + " is locked by a curator and cannot be changed.";
	}

	/** The lessonsDB columns every query reads, kept in one place so all queries match. */
	private static final String COLUMNS = "id, contributor, title, description, category, "
			+ "lessonLocked, titleLocked, descriptionLocked, categoryLocked, curatorNotes, dateCreated, lastEditedBy, "
			+ "searchTerms, curatorStatistics, statisticsVisibleToContributor";

	/*-*******************************************************************************************
	 * Attributes
	 */

	/** The database connection, shared with our Database class.  Source: DB.  Rationale: the
	 *  app already has one connection, so this class uses it instead of opening another. */
	private final Connection connection;

	/** The lessons last loaded.  Source: C-2 (the contributor's list), V-1 (all lessons), V-2
	 *  (search results).  Rationale: a page can show this list and pick from it without going
	 *  back to the database. */
	private final List<LessonsLearned> lessons = new ArrayList<LessonsLearned>();

	/** True when the lessons were loaded for a contributor.  Source: C-4.  Rationale: when a
	 *  lesson in the list is reloaded after a change, it has to stay without the curator
	 *  information. */
	private boolean contributorMode = false;


	/*****
	 * <p> Method: LessonsLearnedList(Database database) </p>
	 *
	 * <p> Description: Creates a list that uses the app's database.  connectToDatabase() must
	 * already have been called. </p>
	 *
	 * @param database is the connected Database
	 *
	 * @throws SQLException if the new lesson columns can't be added
	 */
	public LessonsLearnedList(Database database) throws SQLException {
		this(database.getConnection());
	}


	/*****
	 * <p> Method: LessonsLearnedList(Connection connection) </p>
	 *
	 * <p> Description: Creates a list that uses the given database connection.  The tests use
	 * this with a separate in-memory database so they never change real data. </p>
	 *
	 * @param connection is an open connection to an H2 database
	 *
	 * @throws SQLException if the new lesson columns can't be added
	 */
	public LessonsLearnedList(Connection connection) throws SQLException {
		if (connection == null) throw new SQLException("LessonsLearnedList needs a connected database.");
		this.connection = connection;
		createColumns();
	}


	/*****
	 * <p> Method: void createColumns() </p>
	 *
	 * <p> Description: Our Database class already creates lessonsDB and experienceDB.  This
	 * only adds the columns that are new for TP2, using ADD COLUMN IF NOT EXISTS, so lessons
	 * already saved from the Role1 page are kept.  The CREATE TABLE lines are only for the
	 * in-memory test database, where Database hasn't run. </p>
	 *
	 * @throws SQLException if a table or column can't be created
	 */
	private void createColumns() throws SQLException {
		try (Statement s = connection.createStatement()) {
			// The same tables Database.createTables makes (DB)
			s.execute("CREATE TABLE IF NOT EXISTS lessonsDB (id INT AUTO_INCREMENT PRIMARY KEY, "
					+ "contributor VARCHAR(255), title VARCHAR(255), description VARCHAR(4000), "
					+ "lessonLocked BOOL DEFAULT FALSE, titleLocked BOOL DEFAULT FALSE, "
					+ "descriptionLocked BOOL DEFAULT FALSE, curatorNotes VARCHAR(4000))");
			s.execute("CREATE TABLE IF NOT EXISTS experienceDB (id INT AUTO_INCREMENT PRIMARY KEY, "
					+ "lessonID INT, contributor VARCHAR(255), whatWasDone VARCHAR(4000), "
					+ "howItWasDone VARCHAR(4000), timeTaken VARCHAR(255), teamEffort VARCHAR(4000))");

			// Columns that are new for TP2
			String[] newColumns = {
				"category VARCHAR(50) DEFAULT ''",								// TC-9 to TC-11
				"categoryLocked BOOL DEFAULT FALSE",							// C-1, CU-5
				"dateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP",				// TC-12, TC-21
				"lastEditedBy VARCHAR(255) DEFAULT ''",							// C-4, CU-2
				"searchTerms VARCHAR(1000) DEFAULT ''",							// CU-1
				"curatorStatistics VARCHAR(4000) DEFAULT ''",					// CU-3
				"statisticsVisibleToContributor BOOL DEFAULT FALSE"				// CU-4
			};
			for (String c : newColumns) s.execute("ALTER TABLE lessonsDB ADD COLUMN IF NOT EXISTS " + c);
		}
	}


	/*-*******************************************************************************************
	 * CREATE
	 */

	/*****
	 * <p> Method: String create(LessonsLearned lesson) </p>
	 *
	 * <p> Description: Create (C-1).  Checks the lesson and saves it if it is valid and its
	 * title isn't already used.  If it is saved, its id and creation date are filled in and it
	 * is added to the list.  The locks and curator information always start empty, so a
	 * contributor can't set them. </p>
	 *
	 * @param lesson is the new lesson
	 *
	 * @return "" if the lesson was saved, else an error message
	 */
	public String create(LessonsLearned lesson) {
		if (lesson == null) return "No lesson was given.";

		// C-1: a contributor can't add the information other roles add
		for (String field : LessonsLearned.LOCKABLE_FIELDS) lesson.setLock(field, false);
		lesson.setCuratorNotes("");
		lesson.setLastEditedBy("");
		lesson.setSearchTerms(null);
		lesson.setCuratorStatistics("");
		lesson.setStatisticsVisibleToContributor(false);

		String error = lesson.validate();										// C-1: valid input only
		if (error.length() > 0) return error;
		if (doesLessonTitleExist(lesson.getTitle().trim(), -1)) return DUPLICATE_TITLE_MESSAGE;

		String query = "INSERT INTO lessonsDB (contributor, title, description, category) "
				+ "VALUES (?, ?, ?, ?)";
		try (PreparedStatement p = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			p.setString(1, lesson.getContributor().trim());
			p.setString(2, lesson.getTitle().trim());
			p.setString(3, lesson.getDescription().trim());
			p.setString(4, lesson.getCategory());
			p.executeUpdate();
			ResultSet keys = p.getGeneratedKeys();
			if (keys.next()) lesson.setId(keys.getInt(1));
		} catch (SQLException e) {
			return "Database error: " + e.getMessage();
		}

		// The database fills in the creation date, so read it back
		LessonsLearned saved = read(lesson.getId());
		if (saved != null) lesson.setDateCreated(saved.getDateCreated());
		lessons.add(lesson);
		return "";
	}


	/*****
	 * <p> Method: String addLesson(String contributor, String title, String description,
	 * String category) </p>
	 *
	 * <p> Description: Create (C-1).  The same job as Database.addLesson, plus the category
	 * and input checks, so the Role1 page's Add button can call it directly. </p>
	 *
	 * @param contributor is the userName of the contributor adding the lesson
	 *
	 * @param title is the title of the lesson
	 *
	 * @param description is the description of the lesson
	 *
	 * @param category is one of LessonsLearned.CATEGORIES
	 *
	 * @return "" if the lesson was saved, else an error message
	 */
	public String addLesson(String contributor, String title, String description, String category) {
		return create(new LessonsLearned(contributor, title, description, category));
	}


	/*****
	 * <p> Method: String addExperience(int lessonId, Experience experience) </p>
	 *
	 * <p> Description: Create for experience information (C-3).  Only the owner of the lesson
	 * may add experience to it (C-4).  A locked lesson still takes new experience entries,
	 * because the Role1 page leaves the Add Experience button on for locked lessons: a lock
	 * stops changes to what was written, but an entry is a new attempt at using the lesson. </p>
	 *
	 * @param lessonId is the id of the lesson
	 *
	 * @param experience is the new experience entry
	 *
	 * @return "" if the entry was saved, else an error message
	 */
	public String addExperience(int lessonId, Experience experience) {
		if (experience == null) return "No experience entry was given.";
		LessonsLearned lesson = read(lessonId);
		if (lesson == null) return notFoundMessage(lessonId);
		if (!lesson.getContributor().equals(experience.getContributor())) return NOT_OWNER_MESSAGE;
		String error = experience.validate();
		if (error.length() > 0) return error;

		String query = "INSERT INTO experienceDB (lessonID, contributor, whatWasDone, howItWasDone, "
				+ "timeTaken, teamEffort) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement p = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			p.setInt(1, lessonId);
			p.setString(2, experience.getContributor());
			p.setString(3, experience.getWhatWasDone().trim());
			p.setString(4, experience.getHowItWasDone().trim());
			p.setString(5, experience.getTimeTaken().trim());
			p.setString(6, experience.getTeamEffort().trim());
			p.executeUpdate();
			ResultSet keys = p.getGeneratedKeys();
			if (keys.next()) experience.setId(keys.getInt(1));
		} catch (SQLException e) {
			return "Database error: " + e.getMessage();
		}
		experience.setLessonId(lessonId);
		refreshInMemory(lessonId);
		return "";
	}


	/*****
	 * <p> Method: String addExperience(int lessonId, String contributor, String whatWasDone,
	 * String howItWasDone, String timeTaken, String teamEffort) </p>
	 *
	 * <p> Description: Create for experience information (C-3).  Same parameters as
	 * Database.addExperience, so the Role1 page can call it directly. </p>
	 *
	 * @param lessonId is the id of the lesson
	 *
	 * @param contributor is the userName of the contributor
	 *
	 * @param whatWasDone is what was done
	 *
	 * @param howItWasDone is how it was done
	 *
	 * @param timeTaken is how long it took
	 *
	 * @param teamEffort is the effort used by each team member
	 *
	 * @return "" if the entry was saved, else an error message
	 */
	public String addExperience(int lessonId, String contributor, String whatWasDone,
			String howItWasDone, String timeTaken, String teamEffort) {
		return addExperience(lessonId, new Experience(contributor, whatWasDone, howItWasDone,
				timeTaken, teamEffort));
	}


	/*-*******************************************************************************************
	 * READ
	 */

	/*****
	 * <p> Method: LessonsLearned read(int id) </p>
	 *
	 * <p> Description: Read.  Reads one whole lesson and its experience entries, including the
	 * curator information.  This is what the Role2 (curator) page needs, like
	 * Database.getLessonDetailsForCurator.  Contributor pages use readForContributor
	 * instead (C-4). </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @return the lesson, or null if there is no lesson with that id
	 */
	public LessonsLearned read(int id) {
		try (PreparedStatement p = connection.prepareStatement(
				"SELECT " + COLUMNS + " FROM lessonsDB WHERE id = ?")) {
			p.setInt(1, id);
			ResultSet rs = p.executeQuery();
			if (!rs.next()) return null;
			LessonsLearned lesson = rowToLesson(rs);
			lesson.setExperiences(readExperiences(id));
			return lesson;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	/*****
	 * <p> Method: LessonsLearned readForContributor(int id, String contributor) </p>
	 *
	 * <p> Description: Read (C-2, C-4).  Reads one lesson for its owner, like
	 * Database.getLessonDetails.  It returns null for someone else's lesson and leaves out the
	 * curator information. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param contributor is the userName of the contributor asking
	 *
	 * @return the lesson without curator information, or null if it isn't theirs or doesn't
	 * exist
	 */
	public LessonsLearned readForContributor(int id, String contributor) {
		LessonsLearned lesson = read(id);
		if (lesson == null || !lesson.getContributor().equals(contributor)) return null;	// C-4
		return lesson.contributorView();
	}


	/*****
	 * <p> Method: String checkExists(int id) </p>
	 *
	 * <p> Description: Read.  Checks an id before using it (TC-18). </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @return "" if the lesson exists, else the not-found message
	 */
	public String checkExists(int id) { return read(id) == null ? notFoundMessage(id) : ""; }


	/*****
	 * <p> Method: boolean doesLessonTitleExist(String title, int excludeId) </p>
	 *
	 * <p> Description: Read.  Same as Database.doesLessonTitleExist: true when a lesson other
	 * than excludeId already has this title, ignoring upper and lower case.  Pass -1 when
	 * adding a new lesson.  This keeps two lessons in the C-2 list from looking the same. </p>
	 *
	 * @param title is the title to look for
	 *
	 * @param excludeId is the id of the lesson being edited, or -1 for a new lesson
	 *
	 * @return true when another lesson already has the title
	 */
	public boolean doesLessonTitleExist(String title, int excludeId) {
		String query = "SELECT COUNT(*) FROM lessonsDB WHERE LOWER(title) = LOWER(?) AND id <> ?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setString(1, title);
			p.setInt(2, excludeId);
			ResultSet rs = p.executeQuery();
			return rs.next() && rs.getInt(1) > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	/*****
	 * <p> Method: List&lt;String&gt; getLessonList(String contributor) </p>
	 *
	 * <p> Description: Read (C-2, C-4).  Same as Database.getLessonList: "&lt;Select a
	 * Lesson&gt;" followed by "id: title" for each of this contributor's lessons, for the Role1
	 * combo box.  It also loads those lessons into this list. </p>
	 *
	 * @param contributor is the userName of the contributor
	 *
	 * @return the combo box entries
	 */
	public List<String> getLessonList(String contributor) {
		loadByContributor(contributor);
		return toDisplayList();
	}


	/*****
	 * <p> Method: List&lt;String&gt; getAllLessonList() </p>
	 *
	 * <p> Description: Read.  Same as Database.getAllLessonList: "&lt;Select a Lesson&gt;"
	 * followed by "id: title (contributor)" for every lesson, for the Role2 (curator) combo
	 * box.  It also loads every lesson into this list. </p>
	 *
	 * @return the combo box entries
	 */
	public List<String> getAllLessonList() {
		loadAll();
		List<String> display = new ArrayList<String>();
		display.add("<Select a Lesson>");
		for (LessonsLearned l : lessons) display.add(l + " (" + l.getContributor() + ")");
		return display;
	}


	/*****
	 * <p> Method: List&lt;Experience&gt; readExperiences(int lessonId) </p>
	 *
	 * <p> Description: Read for experience information (C-3).  Returns every entry for one
	 * lesson, oldest first. </p>
	 *
	 * @param lessonId is the id of the lesson
	 *
	 * @return the experience entries; empty if there are none
	 */
	public List<Experience> readExperiences(int lessonId) {
		List<Experience> list = new ArrayList<Experience>();
		String query = "SELECT id, lessonID, contributor, whatWasDone, howItWasDone, timeTaken, "
				+ "teamEffort FROM experienceDB WHERE lessonID = ? ORDER BY id";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, lessonId);
			ResultSet rs = p.executeQuery();
			while (rs.next()) list.add(rowToExperience(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}


	/*****
	 * <p> Method: List&lt;String&gt; getExperienceList(int lessonId, String contributor) </p>
	 *
	 * <p> Description: Read for experience information (C-3, C-4).  Same as
	 * Database.getExperienceList: one "What: ... | How: ... | Time: ... | Effort: ..." line for
	 * each entry this contributor added to the lesson, for the Role1 experience list. </p>
	 *
	 * @param lessonId is the id of the lesson
	 *
	 * @param contributor is the userName of the contributor
	 *
	 * @return the formatted experience entries
	 */
	public List<String> getExperienceList(int lessonId, String contributor) {
		List<String> list = new ArrayList<String>();
		for (Experience e : readExperiences(lessonId))
			if (e.getContributor().equals(contributor)) list.add(e.toString());
		return list;
	}


	/*****
	 * <p> Method: int loadAll() </p>
	 *
	 * <p> Description: Read.  Loads the list of every lesson in the system, oldest first.  The
	 * TP2 assignment asks for this list; curators use it now and viewers (V-1) will in TP3.
	 * Not for contributor pages (C-4). </p>
	 *
	 * @return the number of lessons loaded
	 */
	public int loadAll() { return load(false, "ORDER BY id"); }


	/*****
	 * <p> Method: int loadByContributor(String contributor) </p>
	 *
	 * <p> Description: Read (C-2, C-4).  Loads only this contributor's lessons, without the
	 * curator information. </p>
	 *
	 * @param contributor is the userName of the contributor
	 *
	 * @return the number of lessons loaded
	 */
	public int loadByContributor(String contributor) {
		return load(true, "WHERE contributor = ? ORDER BY id", contributor);
	}


	/*****
	 * <p> Method: int search(String keywords) </p>
	 *
	 * <p> Description: Read (V-2).  Loads the lessons that match every keyword.  Keywords are
	 * separated by spaces, and a lesson matches a keyword when its title, description,
	 * category, or curator search terms (CU-1) contain it, ignoring case.  Each extra keyword
	 * makes the list shorter.  Blank keywords load every lesson. </p>
	 *
	 * @param keywords is one or more words separated by spaces
	 *
	 * @return the number of lessons loaded
	 */
	public int search(String keywords) {
		String[] words = keywords == null ? new String[0] : keywords.trim().toLowerCase().split("\\s+");
		List<String> params = new ArrayList<String>();
		String where = "WHERE 1 = 1";
		for (String word : words) {
			if (word.length() == 0) continue;					// "".split gives one empty word
			// % and _ are wildcards in LIKE, so they are escaped to be searched for as typed
			String like = "%" + word.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
			where += " AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ? "
					+ "OR LOWER(category) LIKE ? OR LOWER(searchTerms) LIKE ?)";
			for (int i = 0; i < 4; i++) params.add(like);
		}
		return load(false, where + " ORDER BY id", params.toArray(new String[0]));
	}


	/*-*******************************************************************************************
	 * UPDATE
	 */

	/*****
	 * <p> Method: String update(LessonsLearned lesson, String requester) </p>
	 *
	 * <p> Description: Update (C-1, C-2).  Saves a contributor's changes to the title,
	 * description, and category.  Nothing is saved, and a message is returned, if the lesson
	 * doesn't exist, isn't the requester's, is locked, changes a locked field, isn't valid, or
	 * uses another lesson's title.  The id, owner, creation date, locks, and curator
	 * information never change here. </p>
	 *
	 * @param lesson holds the lesson's id and the new values
	 *
	 * @param requester is the userName of the contributor making the change
	 *
	 * @return "" if the changes were saved, else an error message
	 */
	public String update(LessonsLearned lesson, String requester) {
		if (lesson == null) return "No lesson was given.";
		LessonsLearned stored = read(lesson.getId());
		if (stored == null) return notFoundMessage(lesson.getId());
		if (!stored.getContributor().equals(requester)) return NOT_OWNER_MESSAGE;	// C-4
		if (stored.getLessonLocked()) return LESSON_LOCKED_MESSAGE;				// C-1, CU-5

		// C-1: a locked field may come back unchanged (the page shows it read only), but it may
		// not come back with a new value.  Database.updateLessonTitle has the same rule.
		if (stored.getTitleLocked() && !stored.getTitle().equals(lesson.getTitle()))
			return fieldLockedMessage("Title");
		if (stored.getDescriptionLocked() && !stored.getDescription().equals(lesson.getDescription()))
			return fieldLockedMessage("Description");
		if (stored.getCategoryLocked() && !stored.getCategory().equals(lesson.getCategory()))
			return fieldLockedMessage("Category");

		// Copy what a contributor can't change from the saved lesson before checking (C-1)
		lesson.setContributor(stored.getContributor());
		for (String field : LessonsLearned.LOCKABLE_FIELDS)
			lesson.setLock(field, field.equals("Lesson") ? stored.getLessonLocked()
					: stored.isFieldLocked(field));
		lesson.setCuratorNotes(stored.getCuratorNotes());
		lesson.setLastEditedBy(stored.getLastEditedBy());
		lesson.setSearchTerms(stored.getSearchTerms());
		lesson.setCuratorStatistics(stored.getCuratorStatistics());
		lesson.setStatisticsVisibleToContributor(stored.getStatisticsVisibleToContributor());
		lesson.setDateCreated(stored.getDateCreated());

		String error = lesson.validate();
		if (error.length() > 0) return error;
		if (doesLessonTitleExist(lesson.getTitle().trim(), lesson.getId())) return DUPLICATE_TITLE_MESSAGE;

		return writeCoreFields(lesson, null);
	}


	/*****
	 * <p> Method: String updateLessonTitle(int id, String contributor, String title) </p>
	 *
	 * <p> Description: Update (C-1).  Same as Database.updateLessonTitle, plus the title
	 * checks: changes only the title, and only if it isn't locked. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param contributor is the userName of the contributor
	 *
	 * @param title is the new title
	 *
	 * @return "" if the title was saved, else an error message
	 */
	public String updateLessonTitle(int id, String contributor, String title) {
		LessonsLearned lesson = readForContributor(id, contributor);
		if (lesson == null) return read(id) == null ? notFoundMessage(id) : NOT_OWNER_MESSAGE;
		lesson.setTitle(title == null ? "" : title.trim());
		return update(lesson, contributor);
	}


	/*****
	 * <p> Method: String updateLessonDescription(int id, String contributor, String description) </p>
	 *
	 * <p> Description: Update (C-1).  Same as Database.updateLessonDescription, plus the
	 * description checks: changes only the description, and only if it isn't locked. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param contributor is the userName of the contributor
	 *
	 * @param description is the new description
	 *
	 * @return "" if the description was saved, else an error message
	 */
	public String updateLessonDescription(int id, String contributor, String description) {
		LessonsLearned lesson = readForContributor(id, contributor);
		if (lesson == null) return read(id) == null ? notFoundMessage(id) : NOT_OWNER_MESSAGE;
		lesson.setDescription(description == null ? "" : description.trim());
		return update(lesson, contributor);
	}


	/*****
	 * <p> Method: String updateExperience(Experience experience, String requester) </p>
	 *
	 * <p> Description: Update for experience information (C-3).  Only the contributor who
	 * added the entry may change it. </p>
	 *
	 * @param experience holds the entry's id and the new values
	 *
	 * @param requester is the userName of the contributor making the change
	 *
	 * @return "" if the changes were saved, else an error message
	 */
	public String updateExperience(Experience experience, String requester) {
		if (experience == null) return "No experience entry was given.";
		Experience stored = readExperience(experience.getId());
		if (stored == null) return EXPERIENCE_NOT_FOUND_MESSAGE;
		if (!stored.getContributor().equals(requester)) return NOT_OWNER_MESSAGE;
		String error = experience.validate();
		if (error.length() > 0) return error;

		String query = "UPDATE experienceDB SET whatWasDone = ?, howItWasDone = ?, timeTaken = ?, "
				+ "teamEffort = ? WHERE id = ?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setString(1, experience.getWhatWasDone().trim());
			p.setString(2, experience.getHowItWasDone().trim());
			p.setString(3, experience.getTimeTaken().trim());
			p.setString(4, experience.getTeamEffort().trim());
			p.setInt(5, experience.getId());
			p.executeUpdate();
		} catch (SQLException e) {
			return "Database error: " + e.getMessage();
		}
		refreshInMemory(stored.getLessonId());
		return "";
	}


	/*-*******************************************************************************************
	 * DELETE
	 */

	/*****
	 * <p> Method: String delete(int id, String requester) </p>
	 *
	 * <p> Description: Delete (C-1, C-2).  Deletes the contributor's lesson and its experience
	 * entries.  It is refused if the lesson doesn't exist, isn't theirs, or has any lock: "When
	 * a lesson I created is locked or has a locked field, I am unable to delete that lesson." </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param requester is the userName of the contributor
	 *
	 * @return "" if the lesson was deleted, else an error message
	 */
	public String delete(int id, String requester) {
		LessonsLearned stored = read(id);
		if (stored == null) return notFoundMessage(id);
		if (!stored.getContributor().equals(requester)) return NOT_OWNER_MESSAGE;	// C-4
		if (!stored.isDeletable()) return DELETE_LOCKED_MESSAGE;					// C-2
		return deleteRows(id);
	}


	/*****
	 * <p> Method: String deleteLesson(int id, String contributor) </p>
	 *
	 * <p> Description: Delete (C-1, C-2).  Same as Database.deleteLesson, so the Role1 page's
	 * Delete button can call it directly. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param contributor is the userName of the contributor
	 *
	 * @return "" if the lesson was deleted, else an error message
	 */
	public String deleteLesson(int id, String contributor) { return delete(id, contributor); }


	/*****
	 * <p> Method: String deleteExperience(int experienceId, String requester) </p>
	 *
	 * <p> Description: Delete for experience information (C-3).  Only the contributor who
	 * added the entry may delete it. </p>
	 *
	 * @param experienceId is the id of the experience entry
	 *
	 * @param requester is the userName of the contributor
	 *
	 * @return "" if the entry was deleted, else an error message
	 */
	public String deleteExperience(int experienceId, String requester) {
		Experience stored = readExperience(experienceId);
		if (stored == null) return EXPERIENCE_NOT_FOUND_MESSAGE;
		if (!stored.getContributor().equals(requester)) return NOT_OWNER_MESSAGE;
		try (PreparedStatement p = connection.prepareStatement("DELETE FROM experienceDB WHERE id = ?")) {
			p.setInt(1, experienceId);
			p.executeUpdate();
		} catch (SQLException e) {
			return "Database error: " + e.getMessage();
		}
		refreshInMemory(stored.getLessonId());
		return "";
	}


	/*-*******************************************************************************************
	 * Curator operations.  C-1 says "Other roles may update or delete the information I have
	 * added" and "may lock one or more fields".  These skip the owner and lock checks because a
	 * curator works on everyone's lessons, so only the curator (Role2) page should call them.
	 */

	/*****
	 * <p> Method: String curatorUpdate(LessonsLearned lesson, String curator) </p>
	 *
	 * <p> Description: Update (C-1, C-4, CU-2).  Saves a curator's edits to the title,
	 * description, and category.  Locks don't stop the curator.  The owner isn't changed, so
	 * the contributor still sees the edited lesson, and the curator's userName is saved in
	 * lastEditedBy (C-4). </p>
	 *
	 * @param lesson holds the lesson's id and the new values
	 *
	 * @param curator is the userName of the curator
	 *
	 * @return "" if the changes were saved, else an error message
	 */
	public String curatorUpdate(LessonsLearned lesson, String curator) {
		if (lesson == null) return "No lesson was given.";
		LessonsLearned stored = read(lesson.getId());
		if (stored == null) return notFoundMessage(lesson.getId());
		lesson.setContributor(stored.getContributor());					// C-4: owner stays the same
		String error = lesson.validate();
		if (error.length() > 0) return error;
		if (doesLessonTitleExist(lesson.getTitle().trim(), lesson.getId())) return DUPLICATE_TITLE_MESSAGE;
		return writeCoreFields(lesson, curator == null ? "" : curator);
	}


	/*****
	 * <p> Method: String curatorDelete(int id) </p>
	 *
	 * <p> Description: Delete (C-1: "They may delete the lessons learned I have added").
	 * Deletes any lesson and its experience entries, even a locked one. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @return "" if the lesson was deleted, else an error message
	 */
	public String curatorDelete(int id) {
		if (read(id) == null) return notFoundMessage(id);
		return deleteRows(id);
	}


	/*****
	 * <p> Method: String updateLessonLock(int id, String field, boolean value) </p>
	 *
	 * <p> Description: Update (C-1, CU-5).  Same as Database.updateLessonLock: locks or
	 * unlocks the whole lesson, the title, or the description.  It can also lock the
	 * category, which is new in TP2. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param field is "Lesson", "Title", "Description", or "Category"
	 *
	 * @param value is true to lock, false to unlock
	 *
	 * @return "" if the lock was saved, else an error message
	 */
	public String updateLessonLock(int id, String field, boolean value) {
		// The column name comes from this list, never from the caller, so the query is safe
		String column;
		if ("Lesson".equals(field)) column = "lessonLocked";
		else if ("Title".equals(field)) column = "titleLocked";
		else if ("Description".equals(field)) column = "descriptionLocked";
		else if ("Category".equals(field)) column = "categoryLocked";
		else return "Unknown lock field. Use Lesson, Title, Description, or Category. Entered: " + field;
		if (read(id) == null) return notFoundMessage(id);
		return writeColumn(id, column, value);
	}


	/*****
	 * <p> Method: String setCuratorNotes(int id, String notes) </p>
	 *
	 * <p> Description: Update (C-1, CU-2).  Saves notes the contributor never sees. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param notes are the curator's notes
	 *
	 * @return "" if the notes were saved, else an error message
	 */
	public String setCuratorNotes(int id, String notes) {
		String error = LessonsLearned.checkLength("Curator notes", notes, LessonsLearned.LONG_TEXT_MAX);
		if (error.length() > 0) return error;
		if (read(id) == null) return notFoundMessage(id);
		return writeColumn(id, "curatorNotes", notes == null ? "" : notes.trim());
	}


	/*****
	 * <p> Method: String setSearchTerms(int id, List&lt;String&gt; terms) </p>
	 *
	 * <p> Description: Update (CU-1, for TP3).  Saves a curator's search terms, which search()
	 * also looks in. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param terms are the search terms
	 *
	 * @return "" if the terms were saved, else an error message
	 */
	public String setSearchTerms(int id, List<String> terms) {
		String joined = terms == null ? "" : String.join(",", terms);
		String error = LessonsLearned.checkLength("Search terms", joined, 1000);	// DB: VARCHAR(1000)
		if (error.length() > 0) return error;
		if (read(id) == null) return notFoundMessage(id);
		return writeColumn(id, "searchTerms", joined);
	}


	/*****
	 * <p> Method: String setCuratorStatistics(int id, String statistics, boolean visible) </p>
	 *
	 * <p> Description: Update (CU-3, CU-4, for TP3).  Saves the curator's time and effort
	 * statistics and whether the contributor may see them. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param statistics is the statistics text
	 *
	 * @param visible is true to show the statistics to the contributor
	 *
	 * @return "" if the statistics were saved, else an error message
	 */
	public String setCuratorStatistics(int id, String statistics, boolean visible) {
		String error = LessonsLearned.checkLength("Curator statistics", statistics,
				LessonsLearned.LONG_TEXT_MAX);
		if (error.length() > 0) return error;
		if (read(id) == null) return notFoundMessage(id);
		String result = writeColumn(id, "curatorStatistics", statistics == null ? "" : statistics.trim());
		if (result.length() > 0) return result;
		return writeColumn(id, "statisticsVisibleToContributor", visible);
	}


	/*-*******************************************************************************************
	 * Methods for the list of lessons last loaded
	 */

	/*****
	 * <p> Method: int size() </p>
	 *
	 * <p> Description: Returns how many lessons are in the list. </p>
	 *
	 * @return the number of lessons loaded
	 */
	public int size() { return lessons.size(); }

	/*****
	 * <p> Method: boolean isEmpty() </p>
	 *
	 * <p> Description: Tells whether the list is empty. </p>
	 *
	 * @return true when no lessons are loaded
	 */
	public boolean isEmpty() { return lessons.isEmpty(); }

	/*****
	 * <p> Method: LessonsLearned get(int index) </p>
	 *
	 * <p> Description: Returns the lesson at one position in the list (C-2). </p>
	 *
	 * @param index is a position from 0 to size() - 1
	 *
	 * @return the lesson at that position
	 */
	public LessonsLearned get(int index) { return lessons.get(index); }

	/*****
	 * <p> Method: LessonsLearned findById(int id) </p>
	 *
	 * <p> Description: Finds a lesson in the list by its id (C-2: "use that list to specify
	 * which lesson learned to examine"). </p>
	 *
	 * @param id is the id of a lesson
	 *
	 * @return the lesson, or null if it isn't in the list
	 */
	public LessonsLearned findById(int id) {
		for (LessonsLearned l : lessons) if (l.getId() == id) return l;
		return null;
	}

	/*****
	 * <p> Method: String getEmptyMessage() </p>
	 *
	 * <p> Description: C-2.  The message a page shows instead of an empty list (TC-16). </p>
	 *
	 * @return EMPTY_MESSAGE when the list is empty, else ""
	 */
	public String getEmptyMessage() { return lessons.isEmpty() ? EMPTY_MESSAGE : ""; }

	/*****
	 * <p> Method: List&lt;String&gt; toDisplayList() </p>
	 *
	 * <p> Description: C-2.  Returns the list the way the Role1 combo box shows it:
	 * "&lt;Select a Lesson&gt;" followed by "id: title" for each lesson. </p>
	 *
	 * @return the combo box entries
	 */
	public List<String> toDisplayList() {
		List<String> display = new ArrayList<String>();
		display.add("<Select a Lesson>");
		for (LessonsLearned l : lessons) display.add(l.toString());
		return display;
	}


	/*-*******************************************************************************************
	 * Private helper methods
	 */

	/*****
	 * <p> Method: int load(boolean forContributor, String clause, String... params) </p>
	 *
	 * <p> Description: Runs a SELECT and puts the lessons it finds in the list.  When
	 * forContributor is true, the curator information is left out (C-4). </p>
	 *
	 * @param forContributor is true when the list is for a contributor page
	 *
	 * @param clause is the WHERE and ORDER BY part of the query
	 *
	 * @param params are the values for the ? marks, in order
	 *
	 * @return the number of lessons loaded
	 */
	private int load(boolean forContributor, String clause, String... params) {
		lessons.clear();
		contributorMode = forContributor;
		try (PreparedStatement p = connection.prepareStatement(
				"SELECT " + COLUMNS + " FROM lessonsDB " + clause)) {
			for (int i = 0; i < params.length; i++) p.setString(i + 1, params[i]);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				LessonsLearned lesson = rowToLesson(rs);
				lesson.setExperiences(readExperiences(lesson.getId()));
				lessons.add(forContributor ? lesson.contributorView() : lesson);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lessons.size();
	}

	/*****
	 * <p> Method: String writeCoreFields(LessonsLearned lesson, String curator) </p>
	 *
	 * <p> Description: Saves the title, description, and category.  When curator isn't null
	 * it is also saved as lastEditedBy (C-4). </p>
	 *
	 * @param lesson holds the id and the new values
	 *
	 * @param curator is the curator's userName, or null for a contributor's change
	 *
	 * @return "" if the changes were saved, else an error message
	 */
	private String writeCoreFields(LessonsLearned lesson, String curator) {
		String query = "UPDATE lessonsDB SET title = ?, description = ?, category = ?"
				+ (curator == null ? "" : ", lastEditedBy = ?") + " WHERE id = ?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setString(1, lesson.getTitle().trim());
			p.setString(2, lesson.getDescription().trim());
			p.setString(3, lesson.getCategory());
			if (curator == null) p.setInt(4, lesson.getId());
			else {
				p.setString(4, curator);
				p.setInt(5, lesson.getId());
			}
			p.executeUpdate();
		} catch (SQLException e) {
			return "Database error: " + e.getMessage();
		}
		refreshInMemory(lesson.getId());
		return "";
	}

	/*****
	 * <p> Method: LessonsLearned rowToLesson(ResultSet rs) </p>
	 *
	 * <p> Description: Makes a LessonsLearned object from the current row.  Lessons added by
	 * the Role1 page before TP2 have no category or date, so those come back empty. </p>
	 *
	 * @param rs is a result set on the row to read
	 *
	 * @return the lesson in that row
	 *
	 * @throws SQLException if the row can't be read
	 */
	private LessonsLearned rowToLesson(ResultSet rs) throws SQLException {
		LessonsLearned l = new LessonsLearned(rs.getString("contributor"), rs.getString("title"),
				rs.getString("description"), rs.getString("category"));
		l.setId(rs.getInt("id"));
		l.setLock("Lesson", rs.getBoolean("lessonLocked"));
		l.setLock("Title", rs.getBoolean("titleLocked"));
		l.setLock("Description", rs.getBoolean("descriptionLocked"));
		l.setLock("Category", rs.getBoolean("categoryLocked"));
		l.setCuratorNotes(rs.getString("curatorNotes"));
		l.setDateCreated(rs.getTimestamp("dateCreated") == null ? "" : rs.getTimestamp("dateCreated").toString());
		l.setLastEditedBy(rs.getString("lastEditedBy"));
		List<String> terms = new ArrayList<String>();
		String saved = rs.getString("searchTerms");
		if (saved != null) for (String t : saved.split(",")) terms.add(t);
		l.setSearchTerms(terms);
		l.setCuratorStatistics(rs.getString("curatorStatistics"));
		l.setStatisticsVisibleToContributor(rs.getBoolean("statisticsVisibleToContributor"));
		return l;
	}

	/*****
	 * <p> Method: Experience rowToExperience(ResultSet rs) </p>
	 *
	 * <p> Description: Makes an Experience object from the current row. </p>
	 *
	 * @param rs is a result set on the row to read
	 *
	 * @return the experience entry in that row
	 *
	 * @throws SQLException if the row can't be read
	 */
	private Experience rowToExperience(ResultSet rs) throws SQLException {
		Experience e = new Experience(rs.getString("contributor"), rs.getString("whatWasDone"),
				rs.getString("howItWasDone"), rs.getString("timeTaken"), rs.getString("teamEffort"));
		e.setId(rs.getInt("id"));
		e.setLessonId(rs.getInt("lessonID"));
		return e;
	}

	/*****
	 * <p> Method: Experience readExperience(int experienceId) </p>
	 *
	 * <p> Description: Reads one experience entry by its id. </p>
	 *
	 * @param experienceId is the id of the entry
	 *
	 * @return the entry, or null if none has that id
	 */
	private Experience readExperience(int experienceId) {
		String query = "SELECT id, lessonID, contributor, whatWasDone, howItWasDone, timeTaken, "
				+ "teamEffort FROM experienceDB WHERE id = ?";
		try (PreparedStatement p = connection.prepareStatement(query)) {
			p.setInt(1, experienceId);
			ResultSet rs = p.executeQuery();
			return rs.next() ? rowToExperience(rs) : null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*****
	 * <p> Method: String deleteRows(int id) </p>
	 *
	 * <p> Description: Deletes a lesson and its experience entries (like Database.deleteLesson)
	 * and takes it out of the list. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @return "" if the lesson was deleted, else an error message
	 */
	private String deleteRows(int id) {
		try (PreparedStatement p = connection.prepareStatement("DELETE FROM lessonsDB WHERE id = ?");
			 PreparedStatement q = connection.prepareStatement("DELETE FROM experienceDB WHERE lessonID = ?")) {
			p.setInt(1, id);
			p.executeUpdate();
			q.setInt(1, id);									// C-3: the entries belong to the lesson
			q.executeUpdate();
		} catch (SQLException e) {
			return "Database error: " + e.getMessage();
		}
		for (int i = 0; i < lessons.size(); i++) {
			if (lessons.get(i).getId() == id) {
				lessons.remove(i);
				break;
			}
		}
		return "";
	}

	/*****
	 * <p> Method: String writeColumn(int id, String column, Object value) </p>
	 *
	 * <p> Description: Saves one column of a lesson and reloads it in the list.  Only column
	 * names written in this class are passed in. </p>
	 *
	 * @param id is the id of the lesson
	 *
	 * @param column is the column to change
	 *
	 * @param value is the new value
	 *
	 * @return "" if the value was saved, else an error message
	 */
	private String writeColumn(int id, String column, Object value) {
		try (PreparedStatement p = connection.prepareStatement(
				"UPDATE lessonsDB SET " + column + " = ? WHERE id = ?")) {
			p.setObject(1, value);
			p.setInt(2, id);
			p.executeUpdate();
		} catch (SQLException e) {
			return "Database error: " + e.getMessage();
		}
		refreshInMemory(id);
		return "";
	}

	/*****
	 * <p> Method: void refreshInMemory(int id) </p>
	 *
	 * <p> Description: Reloads one lesson in the list after it changes.  If the list was
	 * loaded for a contributor, the curator information stays hidden (C-4). </p>
	 *
	 * @param id is the id of the lesson
	 */
	private void refreshInMemory(int id) {
		for (int i = 0; i < lessons.size(); i++) {
			if (lessons.get(i).getId() == id) {
				LessonsLearned fresh = read(id);
				if (fresh == null) lessons.remove(i);
				else lessons.set(i, contributorMode ? fresh.contributorView() : fresh);
				return;
			}
		}
	}
}