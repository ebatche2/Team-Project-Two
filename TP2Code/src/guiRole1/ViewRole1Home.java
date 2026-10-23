package guiRole1;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import database.Database;
import entityClasses.User;
import guiRole2.ViewRole2Home;


/*******
 * <p> Title: ViewRole1Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role1 Home Page.  The page is a stub for some role needed for
 * the application.  The widgets on this page are likely the minimum number and kind for other role
 * pages that may be needed.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-20 Initial version
 *  
 */

public class ViewRole1Home {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	/** Width of the screen **/
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	/** Height of the screen **/
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
	// and a button to allow this user to update the account settings
	
	/** The Page Title **/
	protected static Label label_PageTitle = new Label();
	/** The User Details **/
	protected static Label label_UserDetails = new Label();
	/** The button to update the account **/
	protected static Button button_UpdateThisUser = new Button("Account Update");
	
	/** This is a separator and it is used to partition the GUI for various tasks **/
	protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2: This is a stub, so there are no widgets here.  For an actual role page, this are
	// would contain the widgets needed for the user to play the assigned role.
	
	/** Label for the current users Lessons Learned **/
	protected static Label label_SelectLesson = new Label("My Lessons Learned:");
	/** A dropdown menu to select a lesson **/
	protected static ComboBox<String> combobox_SelectLesson = new ComboBox<String>();
	/** A Label for the lesson title **/
	protected static Label label_Title = new Label("Title:");
	/** The Lesson Title **/
	protected static TextField text_Title = new TextField();
	/** Label for if the title is locked **/
	protected static Label label_TitleLocked = new Label();
	/** Label for the description of the current lesson **/
	protected static Label label_Description = new Label("Description:");
	/** Description of the current Lesson **/
	protected static TextArea text_Description = new TextArea();
	/** Label for if the Description is locked **/
	protected static Label label_DescriptionLocked = new Label();
	/** Button to add a new lesson **/
	protected static Button button_AddLesson = new Button("Add New Lesson");
	/** Button to save changes to current lesson **/
	protected static Button button_SaveLesson = new Button("Save Changes");
	/** Button to delete the current lesson **/
	protected static Button button_DeleteLesson = new Button("Delete Lesson");
	
	/** This is a separator and it is used to partition the GUI for various tasks **/
	protected static Line line_Separator2 = new Line(20, 340, width-20, 340);
	
	// GUI Area 2b: Experience information for the selected lesson
	
	/** Label for what was done experience **/
	protected static Label label_WhatWasDone = new Label("What was done:");
	/** What was done **/
	protected static TextField text_WhatWasDone = new TextField();
	/** Label for how it was done **/
	protected static Label label_HowItWasDone = new Label("How it was done:");
	/** How it was done **/
	protected static TextField text_HowItWasDone = new TextField();
	/** Label for the time taken for the lesson **/
	protected static Label label_TimeTaken = new Label("Time taken:");
	/** The time taken for the lesson **/
	protected static TextField text_TimeTaken = new TextField();
	/** Label for the Team Effort in the lesson **/
	protected static Label label_TeamEffort = new Label("Team effort:");
	/** The Team effort in the lesson **/
	protected static TextField text_TeamEffort = new TextField();
	/** Button to add or update the experience of the lesson **/
	protected static Button button_AddExperience = new Button("Add Experience");
	/** A list to display the experience **/
	protected static ListView<String> list_Experience = new ListView<String>();
	
	/** This is a separator and it is used to partition the GUI for various tasks **/
	protected static Line line_Separator4 = new Line(20, 525, width-20,525);
	
	// GUI Area 3: This is last of the GUI areas.  It is used for quitting the application and for
	// logging out.
	
	/** The logout button **/
	protected static Button button_Logout = new Button("Logout");
	/** The quit button **/
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.
	
	/** These attributes are used to configure the page and populate it with this user's information
	 * Used to determine if instantiation of the class is needed
	 */
	private static ViewRole1Home theView;

	/** Reference for the in-memory database so this package has access **/
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/** The Stage that JavaFX has established for us **/
	protected static Stage theStage;
	
	/** The Pane that holds all the GUI widgets **/
	protected static Pane theRootPane;
	
	/** The current logged in User **/
	protected static User theUser;	
	
	/** The shared Scene each invocation populates **/
	private static Scene theViewRole1HomeScene;
	
	/** An integer that stores the Role of the User, should always be 2 on this page for Role1
	 * Admin: 1; Role1: 2; Role2: 3
	 */
	protected static final int theRole = 2;

	/*-*******************************************************************************************

	Constructors
	
	 */


	/**********
	 * <p> Method: displayRole1Home(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Role1 Home page to be displayed.
	 * 
	 * It first sets up every shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GIUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 */
	public static void displayRole1Home(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewRole1Home();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;
		
		label_UserDetails.setText("User: " + theUser.getUserName());
		
		ControllerRole1Home.refreshLessonList();
				
		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundations: Role1 Home Page");
		theStage.setScene(theViewRole1HomeScene);
		theStage.show();
	}
	
	/**********
	 * <p> Method: ViewRole1Home() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.</p>
	 * 
	 * <p> This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayRole2Home method.</p>
	 * 
	 */
	private ViewRole1Home() {

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theViewRole1HomeScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// Set the title for the window
		
		// Populate the window with the title and other common widgets and set their static state
		
		// GUI Area 1
		label_PageTitle.setText("Role1 Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> {ControllerRole1Home.performUpdate(); });
				
		// GUI Area 2a
		setupLabelUI(label_SelectLesson, "Arial", 16, 160, Pos.BASELINE_LEFT, 20, 110);
		setupComboBoxUI(combobox_SelectLesson, "Dialog", 16, 480, 180, 105);
		combobox_SelectLesson.getSelectionModel().selectedItemProperty()
			.addListener((_, _, _) -> {ControllerRole1Home.selectLesson(); });
		
		setupLabelUI(label_Title, "Arial", 16, 100, Pos.BASELINE_LEFT, 20, 152);
		setupTextUI(text_Title, "Arial", 16, 480, Pos.BASELINE_LEFT, 130, 147, true);
		setupLabelUI(label_TitleLocked, "Arial", 16, 100, Pos.BASELINE_LEFT, 625, 152);
		
		setupLabelUI(label_Description, "Arial", 16, 100, Pos.BASELINE_LEFT, 20, 192);
		text_Description.setFont(Font.font("Arial", 16));
		text_Description.setWrapText(true);
		text_Description.setPrefSize(480, 95);
		text_Description.setLayoutX(130);
		text_Description.setLayoutY(187);
		setupLabelUI(label_DescriptionLocked, "Arial", 16, 100, Pos.BASELINE_LEFT, 625, 192);
		
		setupButtonUI(button_AddLesson, "Dialog", 16, 150, Pos.CENTER, 130, 292);
		button_AddLesson.setOnAction((_) -> {ControllerRole1Home.performAddLesson(); });
		
		setupButtonUI(button_SaveLesson, "Dialog", 16, 150, Pos.CENTER, 295, 292);
		button_SaveLesson.setOnAction((_) -> {ControllerRole1Home.performSaveLesson(); });
		
		setupButtonUI(button_DeleteLesson, "Dialog", 16, 150, Pos.CENTER, 460, 292);
		button_DeleteLesson.setOnAction((_) -> {ControllerRole1Home.performDeleteLesson(); });
		
		// GUI Area 2b
		setupLabelUI(label_WhatWasDone, "Arial", 16, 120, Pos.BASELINE_LEFT, 20, 355);
		setupTextUI(text_WhatWasDone, "Arial", 16, 240, Pos.BASELINE_LEFT, 150, 350, true);
		
		setupLabelUI(label_HowItWasDone, "Arial", 16, 120, Pos.BASELINE_LEFT, 405, 355);
		setupTextUI(text_HowItWasDone, "Arial", 16, 240, Pos.BASELINE_LEFT, 540, 350, true);
		
		setupLabelUI(label_TimeTaken, "Arial", 16, 120, Pos.BASELINE_LEFT, 20, 395);
		setupTextUI(text_TimeTaken, "Arial", 16, 240, Pos.BASELINE_LEFT, 150, 390, true);
		
		setupLabelUI(label_TeamEffort, "Arial", 16, 120, Pos.BASELINE_LEFT, 405, 395);
		setupTextUI(text_TeamEffort, "Arial", 16, 240, Pos.BASELINE_LEFT, 540, 390, true);
		
		setupButtonUI(button_AddExperience, "Dialog", 16, 150, Pos.CENTER, 20, 432);
		button_AddExperience.setOnAction((_) -> {ControllerRole1Home.performAddExperience(); });
		
		list_Experience.setPrefSize(600, 85);
		list_Experience.setLayoutX(180);
		list_Experience.setLayoutY(432);
		
		// GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerRole1Home.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerRole1Home.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children
        theRootPane.getChildren().addAll(
    			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
    			label_SelectLesson, combobox_SelectLesson, label_Title, text_Title, label_TitleLocked,
    			label_Description, text_Description, label_DescriptionLocked, button_AddLesson,
    			button_SaveLesson, button_DeleteLesson, line_Separator2,
    			label_WhatWasDone, text_WhatWasDone, label_HowItWasDone, text_HowItWasDone,
    			label_TimeTaken, text_TimeTaken, label_TeamEffort, text_TeamEffort,
    			button_AddExperience, list_Experience,
    	        line_Separator4, button_Logout, button_Quit);
}
	
	
	/*-********************************************************************************************

	Helper methods to reduce code length

	 */
	
	/**********
	 * Private local method to initialize the standard fields for a label
	 * 
	 * @param l		The Label object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, 
			double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	
	/**********
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b		The Button object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, 
			double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}
	
	/**********
	 * Private local method to initialize the standard fields for a text field
	 * 
	 * @param t		The TextField object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the TextField
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 * @param e		Is this TextField user editable?
	 */
	private static void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, 
			double y, boolean e){
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);		
		t.setEditable(e);
	}
	
	
	/**********
	 * Private local method to initialize the standard fields for a ComboBox
	 * 
	 * @param c		The ComboBox object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the ComboBox
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupComboBoxUI(ComboBox <String> c, String ff, double f, double w, 
			double x, double y){
		c.setStyle("-fx-font: " + f + " " + ff + ";");
		c.setMinWidth(w);
		c.setLayoutX(x);
		c.setLayoutY(y);
	}
}
