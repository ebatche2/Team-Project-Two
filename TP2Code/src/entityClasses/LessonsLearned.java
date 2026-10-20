package entityClasses;

import java.util.ArrayList;
import java.util.List;

/*******
 * <p> Title: LessonsLearned Class </p>
 *
 * <p> Description: This LessonsLearned class represents one lesson learned in the system.  It
 * holds the same lesson information our Database class and the Role1 (contributor) and Role2
 * (curator) home pages already use: the contributor who added it, its title and description,
 * the locks a curator can set, the curator's notes, and the experience entries added to
 * it.  It also checks that each value is valid before it is saved.  The database work for each
 * Create, Read, Update, and Delete (CRUD) operation is done by the LessonsLearnedList class,
 * which stores lessons in the lessonsDB table and experience entries in the experienceDB
 * table. </p>
 *
 * <p> The user stories in "Lessons Learned System - Initial User Stories V1" are not numbered,
 * so we gave them the short names below.  These names are used in the comments of this class,
 * LessonsLearnedList, and the tests so you can see which code satisfies which requirement. </p>
 * <ul>
 *   <li>C-1: A contributor adds a lesson with core information and can see and manage it.
 *       Other roles may update, delete, or lock fields of it, and the contributor can't see or
 *       change what those roles add.</li>
 *   <li>C-2: A contributor sees a list of their lessons, picks one to look at, and sees which
 *       fields are locked.  A locked lesson, or one with a locked field, can't be deleted.</li>
 *   <li>C-3: A contributor adds experience information to a lesson: what was done, how it
 *       was done, how long it took, and the effort used by the team members.</li>
 *   <li>C-4: A contributor only sees their own content, including after a curator edits it,
 *       and never sees other contributors' lessons.</li>
 *   <li>EC-1: (Phase Three epic) AI writing, brainstorming, and search tools that help a
 *       contributor write a lesson.</li>
 *   <li>V-1, V-2, V-3: (Phase Three) viewers list all lessons, search by keyword, and open
 *       one lesson.</li>
 *   <li>CU-1 to CU-5: (Phase Three curator epics) AI search terms, AI editing help, merged
 *       time and effort statistics, showing or hiding those statistics, and locking.</li>
 *   <li>TC: our TP2 Test Cases document (TC-1 to TC-24).</li>
 *   <li>AIC: our AICoaching.pdf, which lists Title (priority 1) and Description (priority 2)
 *       as the input fields to coach.</li>
 *   <li>DB: the lessonsDB and experienceDB tables and lesson methods already in our Database
 *       class, which the Role1 and Role2 home pages use.</li>
 * </ul>
 *
 * <p> How this class supports the Contributor User Stories: </p>
 * <ul>
 *   <li>C-1: the title, description, and category attributes hold the core information,
 *       create() saves it, and validate() makes sure only valid values are saved.  The lock
 *       attributes and curatorNotes let curators work on the lesson, and contributorView()
 *       hides what they add.</li>
 *   <li>C-2: the contributor attribute links each lesson to its owner, isFieldLocked() tells
 *       the GUI which fields to make read only, and isDeletable() stops locked lessons from
 *       being deleted.</li>
 *   <li>C-3: the Experience class inside this class holds one experience entry, and
 *       addExperience() saves it.</li>
 *   <li>C-4: contributorView() removes the curator-only information, and lastEditedBy shows
 *       the contributor that a curator changed their lesson.</li>
 * </ul>
 *
 * <p> How this class is ready for AI coaching (EC-1, AIC): the check methods are public and
 * static, so a coaching tool can call checkTitle() and checkDescription() on text while the
 * contributor is still typing, before anything is saved.  AICoaching.pdf plans LanguageTool for
 * the title and Grammarly for the description; their suggestions only change the text the
 * contributor enters, so no new attributes are needed for them.  The same checks still run in
 * validate() when the lesson is saved, so coaching can never let invalid input through. </p>
 *
 * <p> CRUD operations: Create is create() and addExperience(); Read is read() and the getters;
 * Update is update() and the setters; Delete is delete().  Each one passes the database work
 * to LessonsLearnedList. </p>
 *
 * <p> Like the PasswordEvaluator and UserNameRecognizer classes, each check method returns an
 * empty String when the value is valid, or an error message the GUI can show. </p>
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

public class LessonsLearned {

	/*-*******************************************************************************************
	 * Validation limits and allowed values.  These are public so the GUI can show them to the
	 * user and the tests can use them.
	 */

	/** Minimum title length.  Source: TC-1, TC-3. */
	public static final int TITLE_MIN = 4;
	/** Maximum title length.  Source: TC-1, TC-4.  Rationale: titles must fit in the C-2 combo
	 *  box. */
	public static final int TITLE_MAX = 50;
	/** Minimum description length.  Source: TC-6, TC-7. */
	public static final int DESCRIPTION_MIN = 15;
	/** Maximum description length.  Source: TC-6, TC-8. */
	public static final int DESCRIPTION_MAX = 500;
	/** Maximum length of the longer text fields.  Source: DB (VARCHAR(4000) columns). */
	public static final int LONG_TEXT_MAX = 4000;
	/** Maximum length of the time-taken field.  Source: DB (timeTaken is VARCHAR(255)). */
	public static final int TIME_TAKEN_MAX = 255;

	/** Allowed categories.  Source: TC-9 to TC-11. */
	public static final String[] CATEGORIES = {"Bug Issues", "Javadoc", "Methods", "Classes"};

	/** The parts of a lesson that can be locked.  Source: DB (updateLessonLock and the Role2
	 *  page's check boxes for Lesson, Title, and Description), plus Category because C-1 says
	 *  curators "may lock one or more fields" and category is a field the contributor
	 *  enters.  CU-5. */
	public static final String[] LOCKABLE_FIELDS = {"Lesson", "Title", "Description", "Category"};

	/*-*******************************************************************************************
	 * Attributes.  Each comment gives the Source (requirement) and Rationale for the attribute.
	 */

	/** Database id; 0 until saved.  Source: DB (lessonsDB.id).  Rationale: C-2 picks a lesson
	 *  from the combo box by its id ("id: title"). */
	private int id;

	/** userName of the contributor who added the lesson.  Source: C-1, C-4; DB
	 *  (lessonsDB.contributor).  Rationale: the Role1 page only lists and changes the lessons
	 *  owned by the user who is logged in. */
	private String contributor;

	/** Title of the lesson.  Source: C-1 core information; DB (lessonsDB.title); TC-1 to TC-5.
	 *  Rationale: lessons are listed and picked by title (C-2). */
	private String title;

	/** Description of the lesson.  Source: C-1 core information; DB (lessonsDB.description);
	 *  TC-6 to TC-8. */
	private String description;

	/** Category of the lesson.  Source: TC-9 to TC-11.  Rationale: groups related lessons for
	 *  viewers (V-1) and curators (CU-3).  Not shown on the HW2 Role1 page yet; the TP2 GUI
	 *  (Task 4) adds it. */
	private String category;

	/** True when the whole lesson is locked.  Source: C-1, C-2, CU-5; DB (lessonLocked).
	 *  Rationale: a whole-lesson lock makes every field read only. */
	private boolean lessonLocked;

	/** True when the title is locked.  Source: C-1, CU-5; DB (titleLocked). */
	private boolean titleLocked;

	/** True when the description is locked.  Source: C-1, CU-5; DB (descriptionLocked). */
	private boolean descriptionLocked;

	/** True when the category is locked.  Source: C-1 ("lock one or more fields"), CU-5.
	 *  Rationale: category is new in TP2, so it needs its own lock like the other fields the
	 *  contributor enters. */
	private boolean categoryLocked;

	/** The curator's private notes.  Source: C-1 ("I cannot see or alter any additional
	 *  information those roles may add"); DB (curatorNotes).  Never shown to the contributor. */
	private String curatorNotes;

	/** When the lesson was created, set by the database.  Source: TC-12, TC-21.  Rationale: the
	 *  tests check it is filled in and never changes. */
	private String dateCreated;

	/** The experience entries for this lesson.  Source: C-3; DB (experienceDB). */
	private List<Experience> experiences;

	/** userName of the curator who last edited the lesson.  Source: C-4, CU-2.  Rationale: the
	 *  contributor still sees the lesson after a curator edits it (C-4), so the page can say who
	 *  changed it.  Stored now for TP3. */
	private String lastEditedBy;

	/** Search terms a curator adds, possibly with AI help.  Source: CU-1.  Rationale: lets a
	 *  keyword search (V-2) find lessons that don't use the searcher's words.  Stored now for
	 *  TP3; hidden from the contributor (C-1). */
	private List<String> searchTerms;

	/** Merged time and effort statistics from a curator.  Source: CU-3.  Stored now for TP3. */
	private String curatorStatistics;

	/** Whether the contributor may see curatorStatistics.  Source: CU-4.  Stored now for TP3. */
	private boolean statisticsVisibleToContributor;


	/*****
	 * <p> Method: LessonsLearned() </p>
	 *
	 * <p> Description: This default constructor creates an empty lesson that hasn't been
	 * saved, with nothing locked. </p>
	 */
	public LessonsLearned() {
		id = 0;
		contributor = "";
		title = "";
		description = "";
		category = "";
		curatorNotes = "";
		dateCreated = "";
		experiences = new ArrayList<Experience>();
		lastEditedBy = "";
		searchTerms = new ArrayList<String>();
		curatorStatistics = "";
	}


	/*****
	 * <p> Method: LessonsLearned(String contributor, String title, String description,
	 * String category) </p>
	 *
	 * <p> Description: This constructor creates a new lesson that hasn't been saved yet (C-1).
	 * </p>
	 *
	 * @param contributor is the userName of the contributor adding the lesson
	 *
	 * @param title is the title of the lesson
	 *
	 * @param description is the description of the lesson
	 *
	 * @param category is one of {@link #CATEGORIES}
	 */
	public LessonsLearned(String contributor, String title, String description, String category) {
		this();
		this.contributor = nz(contributor);
		this.title = nz(title);
		this.description = nz(description);
		this.category = nz(category);
	}


	/*****
	 * <p> Method: LessonsLearned(LessonsLearned other) </p>
	 *
	 * <p> Description: This constructor makes a copy of a lesson.  contributorView() uses it so
	 * that hiding the curator information never changes the original lesson. </p>
	 *
	 * @param other is the lesson to copy
	 */
	public LessonsLearned(LessonsLearned other) {
		this();
		id = other.id;
		contributor = other.contributor;
		title = other.title;
		description = other.description;
		category = other.category;
		lessonLocked = other.lessonLocked;
		titleLocked = other.titleLocked;
		descriptionLocked = other.descriptionLocked;
		categoryLocked = other.categoryLocked;
		curatorNotes = other.curatorNotes;
		dateCreated = other.dateCreated;
		experiences = new ArrayList<Experience>(other.experiences);
		lastEditedBy = other.lastEditedBy;
		searchTerms = new ArrayList<String>(other.searchTerms);
		curatorStatistics = other.curatorStatistics;
		statisticsVisibleToContributor = other.statisticsVisibleToContributor;
	}


	/*-*******************************************************************************************
	 * CRUD operations for one lesson.  The database work is done by LessonsLearnedList.
	 */

	/*****
	 * <p> Method: String create(LessonsLearnedList list) </p>
	 *
	 * <p> Description: Create (C-1).  Checks this lesson and saves it.  If it is saved, the id
	 * and creation date are filled in. </p>
	 *
	 * @param list is the LessonsLearnedList that stores lessons
	 *
	 * @return "" if the lesson was saved, else an error message
	 */
	public String create(LessonsLearnedList list) { return list.create(this); }


	/*****
	 * <p> Method: LessonsLearned read(LessonsLearnedList list, int id) </p>
	 *
	 * <p> Description: Read (C-2).  Reads one whole lesson and its experience entries.  Call
	 * contributorView() on the result before showing it to a contributor. </p>
	 *
	 * @param list is the LessonsLearnedList that stores lessons
	 *
	 * @param id is the id of the lesson
	 *
	 * @return the lesson, or null if there is no lesson with that id
	 */
	public static LessonsLearned read(LessonsLearnedList list, int id) { return list.read(id); }


	/*****
	 * <p> Method: String update(LessonsLearnedList list, String requester) </p>
	 *
	 * <p> Description: Update (C-1, C-2).  Saves the contributor's changes, unless the
	 * requester doesn't own the lesson or a changed field is locked. </p>
	 *
	 * @param list is the LessonsLearnedList that stores lessons
	 *
	 * @param requester is the userName of the contributor making the change
	 *
	 * @return "" if the changes were saved, else an error message
	 */
	public String update(LessonsLearnedList list, String requester) {
		return list.update(this, requester);
	}


	/*****
	 * <p> Method: String delete(LessonsLearnedList list, String requester) </p>
	 *
	 * <p> Description: Delete (C-1, C-2).  Deletes this lesson and its experience entries,
	 * unless the requester doesn't own it or anything is locked. </p>
	 *
	 * @param list is the LessonsLearnedList that stores lessons
	 *
	 * @param requester is the userName of the contributor making the change
	 *
	 * @return "" if the lesson was deleted, else an error message
	 */
	public String delete(LessonsLearnedList list, String requester) {
		return list.delete(id, requester);
	}


	/*****
	 * <p> Method: String addExperience(LessonsLearnedList list, Experience experience) </p>
	 *
	 * <p> Description: Create for experience information (C-3).  Adds one experience entry to
	 * this saved lesson. </p>
	 *
	 * @param list is the LessonsLearnedList that stores lessons
	 *
	 * @param experience is the new experience entry
	 *
	 * @return "" if the entry was saved, else an error message
	 */
	public String addExperience(LessonsLearnedList list, Experience experience) {
		String result = list.addExperience(id, experience);
		if (result.length() == 0) experiences.add(experience);
		return result;
	}


	/*-*******************************************************************************************
	 * Input validation.  Each check method returns "" when the value is valid, or an error
	 * message.
	 */

	/*****
	 * <p> Method: String checkTitle(String title) </p>
	 *
	 * <p> Description: C-1, TC-1 to TC-5, AIC (Title, priority 1).  A title must be {@link #TITLE_MIN} to
	 * {@link #TITLE_MAX} characters long, not counting spaces at either end, and must contain
	 * at least one letter. </p>
	 *
	 * @param title is the title to check
	 *
	 * @return "" if valid, else an error message
	 */
	public static String checkTitle(String title) {
		// Spaces at either end are removed first, so "      " counts as empty (TC-2b)
		String t = nz(title).trim();
		if (t.length() == 0)									// TC-2
			return "Title is empty. Enter a title between " + TITLE_MIN + " and " + TITLE_MAX
					+ " characters.";
		if (t.length() < TITLE_MIN)								// TC-3
			return "Title too short. Minimum " + TITLE_MIN + " characters. Current: " + t.length();
		if (t.length() > TITLE_MAX)								// TC-4
			return "Title too long. Maximum " + TITLE_MAX + ". Current: " + t.length();
		if (!containsLetter(t))									// TC-5: "!!!!!" isn't a title
			return "Title must contain at least one letter. Entered: " + t;
		return "";
	}


	/*****
	 * <p> Method: String checkDescription(String description) </p>
	 *
	 * <p> Description: C-1, TC-6 to TC-8, AIC (Description, priority 2).  A description must be {@link #DESCRIPTION_MIN} to
	 * {@link #DESCRIPTION_MAX} characters long, not counting spaces at either end. </p>
	 *
	 * @param description is the description to check
	 *
	 * @return "" if valid, else an error message
	 */
	public static String checkDescription(String description) {
		String d = nz(description).trim();
		if (d.length() < DESCRIPTION_MIN)						// TC-7 (also covers empty)
			return "Text entry too short. Minimum " + DESCRIPTION_MIN + " characters. Current: "
					+ d.length();
		if (d.length() > DESCRIPTION_MAX)						// TC-8
			return "Text entry too long. Maximum " + DESCRIPTION_MAX + ". Current: " + d.length();
		return "";
	}


	/*****
	 * <p> Method: String checkCategory(String category) </p>
	 *
	 * <p> Description: TC-9 to TC-11.  The category must exactly match one of
	 * {@link #CATEGORIES}. </p>
	 *
	 * @param category is the category to check
	 *
	 * @return "" if valid, else an error message
	 */
	public static String checkCategory(String category) {
		String c = nz(category);
		for (String allowed : CATEGORIES) if (allowed.equals(c)) return "";	// TC-9
		return "Error: Please enter valid category. (" + String.join(", ", CATEGORIES)
				+ "). Entered: " + (c.length() == 0 ? "(nothing)" : c);	// TC-10, TC-11
	}


	/*****
	 * <p> Method: String checkContributor(String contributor) </p>
	 *
	 * <p> Description: C-1, C-4.  Every lesson must have an owner, or no contributor could
	 * ever see or manage it. </p>
	 *
	 * @param contributor is the userName to check
	 *
	 * @return "" if valid, else an error message
	 */
	public static String checkContributor(String contributor) {
		if (nz(contributor).trim().length() == 0)
			return "Every lesson must have a contributor. No userName was given.";
		return "";
	}


	/*****
	 * <p> Method: String validate() </p>
	 *
	 * <p> Description: Runs every check on this lesson, in the order the fields appear on the
	 * page, and returns the first problem found.  LessonsLearnedList calls this before every
	 * create and update, so an invalid lesson is never saved (C-1). </p>
	 *
	 * @return "" if the whole lesson is valid, else the first error message
	 */
	public String validate() {
		String[] results = {
			checkContributor(contributor),
			checkTitle(title),
			checkDescription(description),
			checkCategory(category),
			checkLength("Curator notes", curatorNotes, LONG_TEXT_MAX),
			checkLength("Curator statistics", curatorStatistics, LONG_TEXT_MAX)
		};
		for (String r : results) if (r.length() > 0) return r;
		return "";
	}


	/*****
	 * <p> Method: String checkLength(String fieldName, String value, int max) </p>
	 *
	 * <p> Description: Checks that an optional text field is no longer than its database column
	 * (DB). </p>
	 *
	 * @param fieldName is the name used in the message
	 *
	 * @param value is the text to check
	 *
	 * @param max is the largest number of characters allowed
	 *
	 * @return "" if valid, else an error message
	 */
	public static String checkLength(String fieldName, String value, int max) {
		String v = nz(value).trim();
		if (v.length() > max) return fieldName + " too long. Maximum " + max + ". Current: " + v.length();
		return "";
	}


	/*-*******************************************************************************************
	 * Locks and what the contributor may see (C-1, C-2, C-4)
	 */

	/*****
	 * <p> Method: boolean isFieldLocked(String field) </p>
	 *
	 * <p> Description: C-2.  Tells the GUI whether a field must be read only.  This is the
	 * same rule the Role1 page uses: a field is locked when it is locked itself or when the
	 * whole lesson is locked. </p>
	 *
	 * @param field is "Title", "Description", or "Category"
	 *
	 * @return true when the contributor may not change that field
	 */
	public boolean isFieldLocked(String field) {
		if (lessonLocked) return true;							// a locked lesson locks every field
		if ("Title".equals(field)) return titleLocked;
		if ("Description".equals(field)) return descriptionLocked;
		if ("Category".equals(field)) return categoryLocked;
		return false;
	}


	/*****
	 * <p> Method: boolean isDeletable() </p>
	 *
	 * <p> Description: C-2: "When a lesson I created is locked or has a locked field, I am
	 * unable to delete that lesson."  This matches Database.deleteLesson and the Role1 page's
	 * Delete button. </p>
	 *
	 * @return true when the contributor may delete this lesson
	 */
	public boolean isDeletable() {
		return !lessonLocked && !titleLocked && !descriptionLocked && !categoryLocked;
	}


	/*****
	 * <p> Method: LessonsLearned contributorView() </p>
	 *
	 * <p> Description: C-1, C-4, CU-4.  Returns a copy the contributor is allowed to see.  The
	 * curator notes and search terms are removed, and the statistics are removed unless the
	 * curator made them visible.  This does the same job as Database.getLessonDetails, which
	 * never reads the curator notes. </p>
	 *
	 * @return a copy of this lesson without the curator-only information
	 */
	public LessonsLearned contributorView() {
		LessonsLearned view = new LessonsLearned(this);
		view.curatorNotes = "";									// C-1: never shown
		view.searchTerms = new ArrayList<String>();				// C-1: added by a curator
		if (!statisticsVisibleToContributor) view.curatorStatistics = "";	// CU-4
		return view;
	}


	/*-*******************************************************************************************
	 * Getters and setters.  The setters don't check the values; LessonsLearnedList calls
	 * validate() before saving.  A null String is stored as "" so the GUI never gets null.
	 */

	/*****
	 * <p> Method: int getId() </p>
	 *
	 * <p> Description: This getter returns the database id. </p>
	 *
	 * @return the id, or 0 if the lesson hasn't been saved
	 */
	public int getId() { return id; }

	/*****
	 * <p> Method: void setId(int id) </p>
	 *
	 * <p> Description: This setter is used by LessonsLearnedList after the lesson is saved. </p>
	 *
	 * @param id is the id the database gave the lesson
	 */
	public void setId(int id) { this.id = id; }

	/*****
	 * <p> Method: String getContributor() </p>
	 *
	 * <p> Description: This getter returns the owner's userName. </p>
	 *
	 * @return the userName of the contributor who added the lesson
	 */
	public String getContributor() { return contributor; }

	/*****
	 * <p> Method: void setContributor(String s) </p>
	 *
	 * <p> Description: This setter sets the owner.  LessonsLearnedList never changes the owner
	 * of a saved lesson. </p>
	 *
	 * @param s is the owner's userName
	 */
	public void setContributor(String s) { contributor = nz(s); }

	/*****
	 * <p> Method: String getTitle() </p>
	 *
	 * <p> Description: This getter returns the title. </p>
	 *
	 * @return the title
	 */
	public String getTitle() { return title; }

	/*****
	 * <p> Method: void setTitle(String s) </p>
	 *
	 * <p> Description: This setter changes the title. </p>
	 *
	 * @param s is the new title
	 */
	public void setTitle(String s) { title = nz(s); }

	/*****
	 * <p> Method: String getDescription() </p>
	 *
	 * <p> Description: This getter returns the description. </p>
	 *
	 * @return the description
	 */
	public String getDescription() { return description; }

	/*****
	 * <p> Method: void setDescription(String s) </p>
	 *
	 * <p> Description: This setter changes the description. </p>
	 *
	 * @param s is the new description
	 */
	public void setDescription(String s) { description = nz(s); }

	/*****
	 * <p> Method: String getCategory() </p>
	 *
	 * <p> Description: This getter returns the category. </p>
	 *
	 * @return the category
	 */
	public String getCategory() { return category; }

	/*****
	 * <p> Method: void setCategory(String s) </p>
	 *
	 * <p> Description: This setter changes the category. </p>
	 *
	 * @param s is one of {@link #CATEGORIES}
	 */
	public void setCategory(String s) { category = nz(s); }

	/*****
	 * <p> Method: boolean getLessonLocked() </p>
	 *
	 * <p> Description: This getter returns the whole-lesson lock. </p>
	 *
	 * @return true if the whole lesson is locked
	 */
	public boolean getLessonLocked() { return lessonLocked; }

	/*****
	 * <p> Method: boolean getTitleLocked() </p>
	 *
	 * <p> Description: This getter returns the title lock. </p>
	 *
	 * @return true if the title is locked
	 */
	public boolean getTitleLocked() { return titleLocked; }

	/*****
	 * <p> Method: boolean getDescriptionLocked() </p>
	 *
	 * <p> Description: This getter returns the description lock. </p>
	 *
	 * @return true if the description is locked
	 */
	public boolean getDescriptionLocked() { return descriptionLocked; }

	/*****
	 * <p> Method: boolean getCategoryLocked() </p>
	 *
	 * <p> Description: This getter returns the category lock. </p>
	 *
	 * @return true if the category is locked
	 */
	public boolean getCategoryLocked() { return categoryLocked; }

	/*****
	 * <p> Method: void setLock(String field, boolean value) </p>
	 *
	 * <p> Description: This setter changes one lock in this object.  To save a lock, a curator
	 * uses LessonsLearnedList.updateLessonLock (CU-5). </p>
	 *
	 * @param field is "Lesson", "Title", "Description", or "Category"
	 *
	 * @param value is true to lock
	 */
	public void setLock(String field, boolean value) {
		if ("Lesson".equals(field)) lessonLocked = value;
		else if ("Title".equals(field)) titleLocked = value;
		else if ("Description".equals(field)) descriptionLocked = value;
		else if ("Category".equals(field)) categoryLocked = value;
	}

	/*****
	 * <p> Method: String getCuratorNotes() </p>
	 *
	 * <p> Description: This getter returns the curator's notes.  Never show these on a
	 * contributor page (C-1). </p>
	 *
	 * @return the curator's notes
	 */
	public String getCuratorNotes() { return curatorNotes; }

	/*****
	 * <p> Method: void setCuratorNotes(String s) </p>
	 *
	 * <p> Description: This setter changes the curator's notes in this object. </p>
	 *
	 * @param s is the new notes
	 */
	public void setCuratorNotes(String s) { curatorNotes = nz(s); }

	/*****
	 * <p> Method: String getDateCreated() </p>
	 *
	 * <p> Description: This getter returns when the lesson was created. </p>
	 *
	 * @return the date and time, or "" if the lesson hasn't been saved
	 */
	public String getDateCreated() { return dateCreated; }

	/*****
	 * <p> Method: void setDateCreated(String s) </p>
	 *
	 * <p> Description: This setter is used by LessonsLearnedList when it reads a lesson. </p>
	 *
	 * @param s is the date and time
	 */
	public void setDateCreated(String s) { dateCreated = nz(s); }

	/*****
	 * <p> Method: List&lt;Experience&gt; getExperiences() </p>
	 *
	 * <p> Description: This getter returns a copy of the experience entries (C-3). </p>
	 *
	 * @return a copy of the experience list
	 */
	public List<Experience> getExperiences() { return new ArrayList<Experience>(experiences); }

	/*****
	 * <p> Method: void setExperiences(List&lt;Experience&gt; list) </p>
	 *
	 * <p> Description: This setter is used by LessonsLearnedList when it reads a lesson. </p>
	 *
	 * @param list is the experience list; null means none
	 */
	public void setExperiences(List<Experience> list) {
		experiences = new ArrayList<Experience>();
		if (list != null) experiences.addAll(list);
	}

	/*****
	 * <p> Method: String getLastEditedBy() </p>
	 *
	 * <p> Description: This getter returns the curator who last edited the lesson (C-4). </p>
	 *
	 * @return a userName, or "" if no curator has edited it
	 */
	public String getLastEditedBy() { return lastEditedBy; }

	/*****
	 * <p> Method: void setLastEditedBy(String s) </p>
	 *
	 * <p> Description: This setter is used by LessonsLearnedList when it reads a lesson. </p>
	 *
	 * @param s is the curator's userName
	 */
	public void setLastEditedBy(String s) { lastEditedBy = nz(s); }

	/*****
	 * <p> Method: List&lt;String&gt; getSearchTerms() </p>
	 *
	 * <p> Description: This getter returns a copy of the curator's search terms (CU-1). </p>
	 *
	 * @return a copy of the search terms
	 */
	public List<String> getSearchTerms() { return new ArrayList<String>(searchTerms); }

	/*****
	 * <p> Method: void setSearchTerms(List&lt;String&gt; list) </p>
	 *
	 * <p> Description: This setter replaces the search terms in this object. </p>
	 *
	 * @param list is the new list; null means none
	 */
	public void setSearchTerms(List<String> list) {
		searchTerms = new ArrayList<String>();
		if (list != null) for (String s : list) if (nz(s).trim().length() > 0) searchTerms.add(s.trim());
	}

	/*****
	 * <p> Method: String getCuratorStatistics() </p>
	 *
	 * <p> Description: This getter returns the merged time and effort statistics (CU-3). </p>
	 *
	 * @return the statistics text
	 */
	public String getCuratorStatistics() { return curatorStatistics; }

	/*****
	 * <p> Method: void setCuratorStatistics(String s) </p>
	 *
	 * <p> Description: This setter changes the statistics in this object. </p>
	 *
	 * @param s is the new statistics text
	 */
	public void setCuratorStatistics(String s) { curatorStatistics = nz(s); }

	/*****
	 * <p> Method: boolean getStatisticsVisibleToContributor() </p>
	 *
	 * <p> Description: This getter returns whether the contributor may see the statistics
	 * (CU-4). </p>
	 *
	 * @return true when the statistics are shown to the contributor
	 */
	public boolean getStatisticsVisibleToContributor() { return statisticsVisibleToContributor; }

	/*****
	 * <p> Method: void setStatisticsVisibleToContributor(boolean b) </p>
	 *
	 * <p> Description: This setter changes whether the contributor may see the statistics. </p>
	 *
	 * @param b is true to show the statistics to the contributor
	 */
	public void setStatisticsVisibleToContributor(boolean b) { statisticsVisibleToContributor = b; }


	/*****
	 * <p> Method: String toString() </p>
	 *
	 * <p> Description: Returns the lesson as "id: title", the same form Database.getLessonList
	 * puts in the Role1 combo box. </p>
	 *
	 * @return the lesson as "id: title"
	 */
	@Override
	public String toString() { return id + ": " + title; }


	/*****
	 * <p> Method: String nz(String s) </p>
	 *
	 * <p> Description: Changes null to "" so no getter ever returns null. </p>
	 *
	 * @param s is the text
	 *
	 * @return s, or "" when s is null
	 */
	private static String nz(String s) { return s == null ? "" : s; }

	/*****
	 * <p> Method: boolean containsLetter(String s) </p>
	 *
	 * <p> Description: Used by checkTitle for TC-5. </p>
	 *
	 * @param s is the text to check
	 *
	 * @return true when at least one character is a letter
	 */
	private static boolean containsLetter(String s) {
		for (int i = 0; i < s.length(); i++) if (Character.isLetter(s.charAt(i))) return true;
		return false;
	}


	/*******
	 * <p> Title: Experience Class </p>
	 *
	 * <p> Description: One experience entry for a lesson learned (C-3).  Each time someone
	 * tries to use a lesson again, the contributor records what was done, how it was done, how
	 * long it took, and the effort used by the team members.  These are the same four fields
	 * as the Role1 page's experience boxes and the experienceDB table (DB), so they are stored
	 * as text. </p>
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
	public static class Experience {

		/** Database id; 0 until saved.  Source: DB (experienceDB.id). */
		private int id;
		/** The lesson this entry belongs to.  Source: C-3 ("to an existing lesson"); DB
		 *  (experienceDB.lessonID). */
		private int lessonId;
		/** userName of the contributor who added the entry.  Source: C-3, C-4; DB. */
		private String contributor;
		/** What was done.  Source: C-3; DB (whatWasDone). */
		private String whatWasDone;
		/** How it was done.  Source: C-3; DB (howItWasDone). */
		private String howItWasDone;
		/** How long it took, e.g. "3 hours".  Source: C-3 ("how long it took"); DB (timeTaken).
		 *  Rationale: kept as text like the Role1 page; CU-3 statistics in TP3 may need a
		 *  number. */
		private String timeTaken;
		/** The effort used by each team member, e.g. "Ana 2h, Ben 1h".  Source: C-3; DB
		 *  (teamEffort). */
		private String teamEffort;


		/*****
		 * <p> Method: Experience(String contributor, String whatWasDone, String howItWasDone,
		 * String timeTaken, String teamEffort) </p>
		 *
		 * <p> Description: Creates an experience entry that hasn't been saved yet (C-3).  The
		 * parameters are in the same order as Database.addExperience. </p>
		 *
		 * @param contributor is the userName of the contributor adding the entry
		 *
		 * @param whatWasDone is what was done
		 *
		 * @param howItWasDone is how it was done
		 *
		 * @param timeTaken is how long it took
		 *
		 * @param teamEffort is the effort used by each team member
		 */
		public Experience(String contributor, String whatWasDone, String howItWasDone,
				String timeTaken, String teamEffort) {
			this.contributor = nz(contributor);
			this.whatWasDone = nz(whatWasDone);
			this.howItWasDone = nz(howItWasDone);
			this.timeTaken = nz(timeTaken);
			this.teamEffort = nz(teamEffort);
		}


		/*****
		 * <p> Method: String validate() </p>
		 *
		 * <p> Description: C-3.  All four fields are required, like on the Role1 page, and none
		 * may be longer than its database column (DB). </p>
		 *
		 * @return "" if the entry is valid, else the first error message
		 */
		public String validate() {
			if (checkContributor(contributor).length() > 0) return checkContributor(contributor);
			String[][] fields = {
				{"What was done", whatWasDone, "" + LONG_TEXT_MAX},
				{"How it was done", howItWasDone, "" + LONG_TEXT_MAX},
				{"Time taken", timeTaken, "" + TIME_TAKEN_MAX},
				{"Team effort", teamEffort, "" + LONG_TEXT_MAX}
			};
			for (String[] f : fields) {
				if (f[1].trim().length() == 0) return f[0] + " is empty. Please fill in all four experience fields.";
				String r = checkLength(f[0], f[1], Integer.parseInt(f[2]));
				if (r.length() > 0) return r;
			}
			return "";
		}

		/*****
		 * <p> Method: String toString() </p>
		 *
		 * <p> Description: Returns the entry in the same form as Database.getExperienceList, so
		 * it can go straight into the Role1 page's experience list. </p>
		 *
		 * @return the entry as "What: ... | How: ... | Time: ... | Effort: ..."
		 */
		@Override
		public String toString() {
			return "What: " + whatWasDone + " | How: " + howItWasDone + " | Time: " + timeTaken
					+ " | Effort: " + teamEffort;
		}

		/*****
		 * <p> Method: int getId() </p>
		 *
		 * <p> Description: This getter returns the database id. </p>
		 *
		 * @return the id, or 0 if not saved
		 */
		public int getId() { return id; }

		/*****
		 * <p> Method: void setId(int id) </p>
		 *
		 * <p> Description: This setter is used by LessonsLearnedList after a save. </p>
		 *
		 * @param id is the id the database gave the entry
		 */
		public void setId(int id) { this.id = id; }

		/*****
		 * <p> Method: int getLessonId() </p>
		 *
		 * <p> Description: This getter returns the lesson this entry belongs to. </p>
		 *
		 * @return the lesson id
		 */
		public int getLessonId() { return lessonId; }

		/*****
		 * <p> Method: void setLessonId(int id) </p>
		 *
		 * <p> Description: This setter is used by LessonsLearnedList when saving. </p>
		 *
		 * @param id is the lesson id
		 */
		public void setLessonId(int id) { lessonId = id; }

		/*****
		 * <p> Method: String getContributor() </p>
		 *
		 * <p> Description: This getter returns who added the entry. </p>
		 *
		 * @return the contributor's userName
		 */
		public String getContributor() { return contributor; }

		/*****
		 * <p> Method: String getWhatWasDone() </p>
		 *
		 * <p> Description: This getter returns what was done. </p>
		 *
		 * @return what was done
		 */
		public String getWhatWasDone() { return whatWasDone; }

		/*****
		 * <p> Method: void setWhatWasDone(String s) </p>
		 *
		 * <p> Description: This setter changes what was done. </p>
		 *
		 * @param s is the new text
		 */
		public void setWhatWasDone(String s) { whatWasDone = nz(s); }

		/*****
		 * <p> Method: String getHowItWasDone() </p>
		 *
		 * <p> Description: This getter returns how it was done. </p>
		 *
		 * @return how it was done
		 */
		public String getHowItWasDone() { return howItWasDone; }

		/*****
		 * <p> Method: void setHowItWasDone(String s) </p>
		 *
		 * <p> Description: This setter changes how it was done. </p>
		 *
		 * @param s is the new text
		 */
		public void setHowItWasDone(String s) { howItWasDone = nz(s); }

		/*****
		 * <p> Method: String getTimeTaken() </p>
		 *
		 * <p> Description: This getter returns how long it took. </p>
		 *
		 * @return the time taken
		 */
		public String getTimeTaken() { return timeTaken; }

		/*****
		 * <p> Method: void setTimeTaken(String s) </p>
		 *
		 * <p> Description: This setter changes how long it took. </p>
		 *
		 * @param s is the new time taken
		 */
		public void setTimeTaken(String s) { timeTaken = nz(s); }

		/*****
		 * <p> Method: String getTeamEffort() </p>
		 *
		 * <p> Description: This getter returns the effort used by each team member. </p>
		 *
		 * @return the team effort
		 */
		public String getTeamEffort() { return teamEffort; }

		/*****
		 * <p> Method: void setTeamEffort(String s) </p>
		 *
		 * <p> Description: This setter changes the team effort. </p>
		 *
		 * @param s is the new team effort
		 */
		public void setTeamEffort(String s) { teamEffort = nz(s); }
	}
}