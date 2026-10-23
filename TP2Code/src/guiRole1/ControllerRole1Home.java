package guiRole1;

import java.util.List;
import database.Database;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*******
 * <p> Title: ControllerRole1Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 1 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page is a stub for establish future roles for the application.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerRole1Home {
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerRole1Home() {
	}
	
	/** Variable to access the database **/
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	/** Stores the current Lesson ID **/
	private static int selectedLessonID = -1;

	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */
	
	/**********
	 * <p> Method: refreshLessonList() </p>
	 * 
	 * <p> Description: This method reloads the combo box with the lessons this contributor has
	 * added and selects "&lt;Select a Lesson&gt;". </p>
	 * 
	 */
	protected static void refreshLessonList() {
		List<String> lessons = theDatabase.getLessonList(ViewRole1Home.theUser.getUserName());
		ViewRole1Home.combobox_SelectLesson.getItems().setAll(lessons);
		ViewRole1Home.combobox_SelectLesson.getSelectionModel().select(0);
	}
	
	/**********
	 * <p> Method: selectLesson() </p>
	 * 
	 * <p> Description: This method is called when the combo box selection changes.  If no lesson
	 * is selected, the fields are cleared so a new lesson can be added.  Otherwise the lesson's
	 * core information is shown, locked fields are made read only, and the lesson's experience
	 * entries are listed. </p>
	 * 
	 **/
	protected static void selectLesson() {
		String selected = ViewRole1Home.combobox_SelectLesson.getValue();
		
		// No lesson selected, so set the page up for adding a new lesson
		if (selected == null || selected.startsWith("<")) {
			selectedLessonID = -1;
			ViewRole1Home.text_Title.setText("");
			ViewRole1Home.text_Title.setEditable(true);
			ViewRole1Home.label_TitleLocked.setText("");
			ViewRole1Home.text_Description.setText("");
			ViewRole1Home.text_Description.setEditable(true);
			ViewRole1Home.label_DescriptionLocked.setText("");
			ViewRole1Home.button_AddLesson.setDisable(false);
			ViewRole1Home.button_SaveLesson.setDisable(true);
			ViewRole1Home.button_DeleteLesson.setDisable(true);
			ViewRole1Home.button_AddExperience.setDisable(true);
			ViewRole1Home.list_Experience.getItems().clear();
			return;
		}
		
		// The entries have the form "id: title", so the id is everything before the first ':'
		selectedLessonID = Integer.parseInt(selected.substring(0, selected.indexOf(':')));
		String userName = ViewRole1Home.theUser.getUserName();
		if (!theDatabase.getLessonDetails(selectedLessonID, userName)) return;
		
		// A locked lesson locks every field
		boolean lessonLocked = theDatabase.getCurrentLessonLocked();
		boolean titleLocked = lessonLocked || theDatabase.getCurrentTitleLocked();
		boolean descriptionLocked = lessonLocked || theDatabase.getCurrentDescriptionLocked();
		
		ViewRole1Home.text_Title.setText(theDatabase.getCurrentLessonTitle());
		ViewRole1Home.text_Title.setEditable(!titleLocked);
		ViewRole1Home.label_TitleLocked.setText(titleLocked ? "Locked" : "Editable");
		
		ViewRole1Home.text_Description.setText(theDatabase.getCurrentLessonDescription());
		ViewRole1Home.text_Description.setEditable(!descriptionLocked);
		ViewRole1Home.label_DescriptionLocked.setText(descriptionLocked ? "Locked" : "Editable");
		
		// A lesson with any lock can not be deleted, and there is nothing to save if all are locked
		ViewRole1Home.button_AddLesson.setDisable(true);
		ViewRole1Home.button_SaveLesson.setDisable(titleLocked && descriptionLocked);
		ViewRole1Home.button_DeleteLesson.setDisable(titleLocked || descriptionLocked);
		ViewRole1Home.button_AddExperience.setDisable(false);
		
		ViewRole1Home.list_Experience.getItems().setAll(
				theDatabase.getExperienceList(selectedLessonID, userName));
	}
	
	
	/**********
	 * <p> Method: performAddLesson() </p>
	 * 
	 * <p> Description: This method adds a new lesson using the title and description fields. </p>
	 * 
	 **/
	protected static void performAddLesson() {
		String title = ViewRole1Home.text_Title.getText().trim();
		String description = ViewRole1Home.text_Description.getText().trim();
		if (title.isEmpty()) {
			showMessage("Missing Title", "Please enter a title for the lesson.");
			return;
		}
		if (theDatabase.doesLessonTitleExist(title, -1)) {
			showMessage("Duplicate Title", "A lesson with this title already exists.");
			return;
		}
		if (theDatabase.addLesson(ViewRole1Home.theUser.getUserName(), title, description)) {
			showMessage("Lesson Added", "The lesson \"" + title + "\" has been added.");
			refreshLessonList();
		}
		else
			showMessage("Error", "The lesson could not be added.");
	}
	
	
	/**********
	 * <p> Method: performSaveLesson() </p>
	 * 
	 * <p> Description: This method saves the fields of the selected lesson that are not locked.
	 * </p>
	 * 
	 **/
	protected static void performSaveLesson() {
		if (selectedLessonID < 0) return;
		String userName = ViewRole1Home.theUser.getUserName();
		String title = ViewRole1Home.text_Title.getText().trim();
		if (title.isEmpty()) {
			showMessage("Missing Title", "The title can not be empty.");
			return;
		}
		
		// Only editable fields are written.  The database also refuses writes to locked fields
		// in case a lock was set after this page loaded the lesson.
		int lessonId = selectedLessonID;
		if (ViewRole1Home.text_Title.isEditable())
			theDatabase.updateLessonTitle(lessonId, userName, title);
		if (ViewRole1Home.text_Description.isEditable())
			theDatabase.updateLessonDescription(lessonId, userName,
					ViewRole1Home.text_Description.getText().trim());
		
		showMessage("Lesson Saved", "Your changes have been saved.");
		refreshLessonList();
		selectLessonById(lessonId);
	}
	
	
	/**********
	 * <p> Method: performDeleteLesson() </p>
	 * 
	 * <p> Description: This method deletes the selected lesson after the user confirms. </p>
	 * 
	 **/
	protected static void performDeleteLesson() {
		if (selectedLessonID < 0) return;
		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle("Delete Lesson");
		confirm.setHeaderText("Delete this lesson and its experience entries?");
		if (confirm.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) != 
				javafx.scene.control.ButtonType.OK) return;
		
		if (theDatabase.deleteLesson(selectedLessonID, ViewRole1Home.theUser.getUserName()))
			showMessage("Lesson Deleted", "The lesson has been deleted.");
		else
			showMessage("Not Deleted", "This lesson is locked and can not be deleted.");
		refreshLessonList();
	}
	
	
	/**********
	 * <p> Method: performAddExperience() </p>
	 * 
	 * <p> Description: This method adds experience information to the selected lesson. </p>
	 * 
	 **/
	protected static void performAddExperience() {
		if (selectedLessonID < 0) return;
		String what = ViewRole1Home.text_WhatWasDone.getText().trim();
		String how = ViewRole1Home.text_HowItWasDone.getText().trim();
		String time = ViewRole1Home.text_TimeTaken.getText().trim();
		String effort = ViewRole1Home.text_TeamEffort.getText().trim();
		if (what.isEmpty() || how.isEmpty() || time.isEmpty() || effort.isEmpty()) {
			showMessage("Missing Information", "Please fill in all four experience fields.");
			return;
		}
		
		String userName = ViewRole1Home.theUser.getUserName();
		if (theDatabase.addExperience(selectedLessonID, userName, what, how, time, effort)) {
			ViewRole1Home.text_WhatWasDone.setText("");
			ViewRole1Home.text_HowItWasDone.setText("");
			ViewRole1Home.text_TimeTaken.setText("");
			ViewRole1Home.text_TeamEffort.setText("");
			ViewRole1Home.list_Experience.getItems().setAll(
					theDatabase.getExperienceList(selectedLessonID, userName));
		}
		else
			showMessage("Error", "The experience could not be added.");
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 * 
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 * 
	 **/
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole1Home.theStage, ViewRole1Home.theUser);
	}	

	/**********
	 * <p> Method: performLogout() </p>
	 * 
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * 
	 **/
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole1Home.theStage);
	}
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 **/	
	protected static void performQuit() {
		System.exit(0);
	}
	
	/*-********************************************************************************************
	 
	Helper methods
 
	 */
	
	/**********
	 * Private local method to select the combo box entry for a given lesson id
	 * 
	 * @param id	The id of the lesson to select
	 **/
	private static void selectLessonById(int id) {
		for (String item : ViewRole1Home.combobox_SelectLesson.getItems()) {
			if (item.startsWith(id + ":")) {
				ViewRole1Home.combobox_SelectLesson.getSelectionModel().select(item);
				return;
			}
		}
	}
	
	/**********
	 * Private local method to show a simple information alert
	 * 
	 * @param header	The header text of the alert
	 * @param content	The content text of the alert
	 **/
	private static void showMessage(String header, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Lessons Learned");
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
