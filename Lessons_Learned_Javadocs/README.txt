Lessons_Learned_Javadocs - TP2 Task 3
Team: Rohan Arora, Ronald Yalung, Elijah Batchelor, Hung Ngo

This folder has the Javadoc for the LessonsLearned and LessonsLearnedList classes in the
entityClasses package, and screenshots of their source code and comments.  The Javadoc was
generated with Private visibility so the private attributes and their Source and Rationale
comments are included.

Requirement names used in the code comments (explained in full on the LessonsLearned page):
  C-1 to C-4   Contributor User Stories (add/manage a lesson, list my lessons and see locks,
               add experience, only see my own content)
  V-1 to V-3   Viewer User Stories (Phase Three)
  CU-1 to CU-5 Curator epics (Phase Three)
  EC-1         Contributor AI epic (Phase Three)
  TC           Our TP2 Test Cases (TC-1 to TC-24)
  AIC          Our AICoaching.pdf
  DB           The lessonsDB and experienceDB tables in our Database class


HTML FILES
----------
index.html                                                   Start here (open in a browser)
FoundationsF26/entityClasses/LessonsLearned.html             The lesson class
FoundationsF26/entityClasses/LessonsLearned.Experience.html  One experience entry (C-3)
FoundationsF26/entityClasses/LessonsLearnedList.html         The list class with all CRUD methods


SCREENSHOTS
-----------

3.1 - Classes in the entityClasses package
  3.1_entityClasses_package.png
      Package Explorer showing LessonsLearned.java and LessonsLearnedList.java in the
      entityClasses package.

3.2 - CRUD shown by the Javadoc
  3.2_CRUD_list.png
      The LessonsLearnedList class comment listing every Create, Read, Update, and Delete
      method for lessons and experience entries.

3.3 - Attributes cover the requirements
  3.3_attributes_source_rationale (1).png
  3.3_attributes_source_rationale (2).png
  3.3_attributes_source_rationale (3).png
      The attribute declarations of LessonsLearned.  Each comment gives the Source (a
      Contributor User Story C-1 to C-4, a curator epic CU-1 to CU-5, a Test Case, or the DB
      tables) and the Rationale for the attribute.  They cover the core lesson information,
      the four locks, the curator's notes, the experience entries (C-3), and the attributes
      stored now for the TP3 epics (lastEditedBy, searchTerms, curatorStatistics,
      statisticsVisibleToContributor).

3.4 - Purpose, attributes, sources, operations, and how they enable the user stories
  (The attributes and their sources and rationale are shown in the 3.3 screenshots above.)
  3.4_LessonsLearned_purpose.png
      Purpose of the LessonsLearned class and the list of requirement names.
  3.4_LessonsLearned_user_stories.png
      How LessonsLearned supports C-1 to C-4, how it is ready for AI coaching (EC-1, AIC),
      and its CRUD operations.
  3.4_LessonsLearnedList_purpose_stories.png
      Purpose of the LessonsLearnedList class, how its methods match our Database class,
      how it supports C-1 to C-4, and its CRUD operations.
  3.4_operation_example (1).png
  3.4_operation_example (2).png
      The create() operation: its Javadoc and code, tagged with the user story it satisfies
      (C-1).

3.5 - Which lines satisfy which requirement, and why some code is written the way it is

  Lines tagged with the requirement they satisfy:
  3.5_title_description_checks (1).png
      Each validation rule in checkTitle, checkDescription, and checkCategory is tagged with
      the test case it satisfies (TC-2 to TC-11).
  3.5_contributor_view.png
      Lines tagged C-1 and CU-4 in contributorView() hide the curator's notes, search terms,
      and hidden statistics from the contributor.
  3.5_new_columns.png
      Each new database column is tagged with the requirement that needs it.
  3.5_update_ownership_locks (1).png
  3.5_update_ownership_locks (2).png
      In update(), the line tagged C-4 stops contributors from changing other people's
      lessons, and the lines tagged C-1 and CU-5 enforce the curator's locks.
  3.5_delete_rules.png
      In delete(), the line tagged C-4 checks the owner and the line tagged C-2 refuses to
      delete a lesson that has any lock.

  Comments explaining code that isn't obvious:
  3.5_why_locked_field_rule.png
      Explains why a locked field may be sent back unchanged (the page shows it read only)
      but not with a new value.
  3.5_why_experience_on_locked.png
      Explains why a locked lesson still accepts new experience entries: a lock stops changes
      to what was written, but an experience entry is a new attempt at using the lesson.
  3.5_why_search_and_sql (1).png
  3.5_why_search_and_sql (2).png
      Explains why % and _ are escaped in keyword searches (so they are searched for as
      typed), and why lock column names never come from the caller (which keeps the SQL
      query safe).
