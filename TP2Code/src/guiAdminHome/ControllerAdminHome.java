package guiAdminHome;

import java.util.List;
import java.util.Optional;

import database.Database;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
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

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerAdminHome() {
	}
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void manageInvitations () {
		System.out.println("\n*** WARNING ***: Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("Manage Invitations Issue");
		ViewAdminHome.alertNotImplemented.setContentText("Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: setOnetimePassword () Method. </p>
	 * 
	 * <p> Description: Protected method that asks the admin which username needs a one-time
	 * 		password, asks for the new temporary password (validated the same way as any other
	 * 		password), and assigns it, marking it as a one-time password that must be replaced
	 * 		the next time that user logs in. </p>
	 */
	protected static void setOnetimePassword () {
		TextInputDialog usernameDialog = new TextInputDialog("");
		usernameDialog.setTitle("One-Time Password");
		usernameDialog.setHeaderText("Enter the username that needs a one-time password");

		Optional<String> usernameResult = usernameDialog.showAndWait();
		if (!usernameResult.isPresent() || usernameResult.get().trim().isEmpty()) {
			return;
		}
		String targetUsername = usernameResult.get().trim();

		if (!theDatabase.doesUserExist(targetUsername)) {
			Alert notFoundAlert = new Alert(AlertType.INFORMATION);
			notFoundAlert.setTitle("User Not Found");
			notFoundAlert.setHeaderText("No account with that username was found.");
			notFoundAlert.showAndWait();
			return;
		}

		TextInputDialog passwordDialog = new TextInputDialog("");
		passwordDialog.setTitle("One-Time Password");
		passwordDialog.setHeaderText("Enter the temporary password for \"" + targetUsername + "\"");

		Optional<String> passwordResult = passwordDialog.showAndWait();
		if (!passwordResult.isPresent() || passwordResult.get().trim().isEmpty()) {
			return;
		}
		String oneTimePassword = passwordResult.get().trim();

		String passwordError = passwordEvaluator.PasswordEvaluator.evaluatePassword(oneTimePassword);
		if (passwordError != "" && passwordError.length() > 0) {
			Alert invalidAlert = new Alert(AlertType.INFORMATION);
			invalidAlert.setTitle("Invalid Password");
			invalidAlert.setHeaderText("The temporary password does not meet the requirements.");
			invalidAlert.setContentText(passwordError);
			invalidAlert.showAndWait();
			return;
		}

		theDatabase.setOneTimePassword(targetUsername, oneTimePassword);
		Alert successAlert = new Alert(AlertType.INFORMATION);
		successAlert.setTitle("One-Time Password Set");
		successAlert.setHeaderText("A one-time password has been set for \"" + targetUsername + "\".");
		successAlert.setContentText("They will be required to set a new password the next time they log in.");
		successAlert.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: deleteUser () Method. </p>
	 * 
	 * <p> Description: Protected method that asks the admin which username to delete, confirms
	 * 		with an "Are you sure?" prompt, blocks an admin from deleting their own account, and
	 * 		then removes the account from the database. </p>
	 */
	protected static void deleteUser() {
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Delete User");
		dialog.setHeaderText("Enter the username of the account to delete");

		Optional<String> result = dialog.showAndWait();
		if (!result.isPresent() || result.get().trim().isEmpty()) {
			return;
		}
		String targetUsername = result.get().trim();

		// An admin cannot delete their own account
		if (targetUsername.equals(ViewAdminHome.theUser.getUserName())) {
			Alert selfDeleteAlert = new Alert(AlertType.INFORMATION);
			selfDeleteAlert.setTitle("Action Not Allowed");
			selfDeleteAlert.setHeaderText("You cannot delete your own account.");
			selfDeleteAlert.setContentText("Ask another admin to remove this account if needed.");
			selfDeleteAlert.showAndWait();
			return;
		}

		// Make sure the account actually exists before asking for confirmation
		if (!theDatabase.doesUserExist(targetUsername)) {
			Alert notFoundAlert = new Alert(AlertType.INFORMATION);
			notFoundAlert.setTitle("User Not Found");
			notFoundAlert.setHeaderText("No account with that username was found.");
			notFoundAlert.showAndWait();
			return;
		}

		// Confirm before permanently deleting the account
		Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
		confirmAlert.setTitle("Are You Sure?");
		confirmAlert.setHeaderText("Delete account \"" + targetUsername + "\"?");
		confirmAlert.setContentText("This cannot be undone.");

		Optional<javafx.scene.control.ButtonType> confirmResult = confirmAlert.showAndWait();
		if (confirmResult.isPresent() && confirmResult.get() == javafx.scene.control.ButtonType.OK) {
			theDatabase.deleteUser(targetUsername);
			Alert successAlert = new Alert(AlertType.INFORMATION);
			successAlert.setTitle("User Deleted");
			successAlert.setHeaderText("The account \"" + targetUsername + "\" has been deleted.");
			successAlert.showAndWait();
		}
		
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Protected method that displays every user account in the system, showing
	 * 		each account's username, full name, email address, and assigned roles, in a simple
	 * 		pop-up window. </p>
	 */
	protected static void listUsers() {
		List<String> summaries = theDatabase.getAllUserAccountSummaries();

		Label header = new Label("Username  |  Name  |  Email  |  Roles");
		ListView<String> listView = new ListView<String>();
		listView.getItems().addAll(summaries);

		Pane root = new Pane();
		root.getChildren().addAll(header, listView);
		header.setLayoutX(20);
		header.setLayoutY(10);
		listView.setLayoutX(20);
		listView.setLayoutY(40);
		listView.setPrefSize(560, 400);

		Stage listStage = new Stage();
		listStage.setTitle("All User Accounts");
		listStage.setScene(new Scene(root, 600, 460));
		listStage.show();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code currently only checks to see that the email address is not
	 * empty.  In the future, a syntactic check must be performed and maybe there is a way to check
	 * if a properly email address is active.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0 || emailAddress.length() > 50) {
			ViewAdminHome.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
