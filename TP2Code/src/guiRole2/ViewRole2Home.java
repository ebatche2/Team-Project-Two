package guiRole2;

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
import javafx.scene.control.CheckBox;
import database.Database;
import entityClasses.User;


/*******
 * <p> Title: ViewRole2Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role2 Home Page.  The page is a stub for some role needed for
 * the application.  The widgets on this page are likely the minimum number and kind for other role
 * pages that may be needed.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-04-20 Initial version
 *  
 */

public class ViewRole2Home {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	
	/** The width of the window **/
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	/** The height of the window **/
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
	// and a button to allow this user to update the account settings
	
	/** Label for Page Title **/
	protected static Label label_PageTitle = new Label();
	/** Label for User Details **/
	protected static Label label_UserDetails = new Label();
	/** Account Update Button **/
	protected static Button button_UpdateThisUser = new Button("Account Update");
		
	/** This is a separator and it is used to partition the GUI for various tasks **/
	protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2: This is a stub, so there are no widgets here.  For an actual role page, this are
	// would contain the widgets needed for the user to play the assigned role.
	
	/** Label for All Lessons Learned **/
	protected static Label label_SelectLesson = new Label("All Lessons Learned:");
	/** Dropdown menu to select a lesson **/
	protected static ComboBox<String> combobox_SelectLesson = new ComboBox<String>();
	/** Label for title of lesson **/
	protected static Label label_Title = new Label("Title:");
	/** Title of lesson **/
	protected static TextField text_Title = new TextField();
	/** Label for description of lesson **/
	protected static Label label_Description = new Label("Description:");
	/** Description of lesson **/
	protected static TextArea text_Description = new TextArea();
	/** Button to Lock Entire Lesson **/
	protected static CheckBox checkbox_LockLesson = new CheckBox("Lock Entire Lesson");
	/** Button to Lock Lesson Title **/
	protected static CheckBox checkbox_LockTitle = new CheckBox("Lock Title");
	/** Button to Lock Lesson Description **/
	protected static CheckBox checkbox_LockDescription = new CheckBox("Lock Description");
	
	/** This is a separator and it is used to partition the GUI for various tasks **/
	protected static Line line_Separator4 = new Line(20, 525, width-20,525);
	
	// GUI Area 3: This is last of the GUI areas.  It is used for quitting the application and for
	// logging out.
	
	/** Logout Button **/
	protected static Button button_Logout = new Button("Logout");
	/** Quit Button **/
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.
	
	/** These attributes are used to configure the page and populate it with this user's information
	 * Used to determine if instantiation of the class is needed
	 */
	private static ViewRole2Home theView;

	/** Reference for the in-memory database so this package has access **/
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/** The Stage that JavaFX has established for us **/
	protected static Stage theStage;
	
	/** The Pane that holds all the GUI widgets **/
	protected static Pane theRootPane;
	
	/** The currently logged in User **/
	protected static User theUser;
	
	/** The shared Scene each invocation populates **/
	private static Scene theRole2HomeScene;
	
	/** An integer that stores the Role of the User, should always be 3 on this page for Role2
	 * Admin: 1; Role1: 2; Role2: 3
	 */
	protected static final int theRole = 3;

	/*-*******************************************************************************************

	Constructors
	
	 */

	/**********
	 * <p> Method: displayRole2Home(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Role2 Home page to be displayed.
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
	public static void displayRole2Home(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewRole2Home();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;
		
		label_UserDetails.setText("User: " + theUser.getUserName());// Set the username
		
		// Load every lesson.  This must happen on every display, since contributors may have
		// added or changed lessons since the last time this page was shown.
		ControllerRole2Home.refreshLessonList();

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundations: Role2 Home Page");
		theStage.setScene(theRole2HomeScene);						// Set this page onto the stage
		theStage.show();											// Display it to the user
	}
	
	/**********
	 * <p> Method: ViewRole2Home() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object. </p>
	 * 
	 * <p>This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayRole2Home method.</p>
	 * 
	 */
	private ViewRole2Home() {
		
		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theRole2HomeScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// Set the title for the window
		
		// Populate the window with the title and other common widgets and set their static state
		
		// GUI Area 1
		label_PageTitle.setText("Role2 Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> {ControllerRole2Home.performUpdate(); });
		
		// GUI Area 2
		
		setupLabelUI(label_SelectLesson, "Arial", 16, 160, Pos.BASELINE_LEFT, 20, 110);
		setupComboBoxUI(combobox_SelectLesson, "Dialog", 16, 480, 180, 105);
		combobox_SelectLesson.getSelectionModel().selectedItemProperty()
			.addListener((_, _, _) -> {ControllerRole2Home.selectLesson(); });
		
		// The curator only views the core information, so these are read only
		setupLabelUI(label_Title, "Arial", 16, 100, Pos.BASELINE_LEFT, 20, 152);
		setupTextUI(text_Title, "Arial", 16, 480, Pos.BASELINE_LEFT, 130, 147, false);
		
		setupLabelUI(label_Description, "Arial", 16, 100, Pos.BASELINE_LEFT, 20, 192);
		text_Description.setFont(Font.font("Arial", 16));
		text_Description.setWrapText(true);
		text_Description.setEditable(false);
		text_Description.setPrefSize(480, 95);
		text_Description.setLayoutX(130);
		text_Description.setLayoutY(187);
		
		setupCheckBoxUI(checkbox_LockLesson, "Arial", 16, 130, 300);
		checkbox_LockLesson.setOnAction((_) -> 
			{ControllerRole2Home.performLockChange("Lesson", checkbox_LockLesson.isSelected()); });
		
		setupCheckBoxUI(checkbox_LockTitle, "Arial", 16, 320, 300);
		checkbox_LockTitle.setOnAction((_) -> 
			{ControllerRole2Home.performLockChange("Title", checkbox_LockTitle.isSelected()); });
		
		setupCheckBoxUI(checkbox_LockDescription, "Arial", 16, 450, 300);
		checkbox_LockDescription.setOnAction((_) -> 
			{ControllerRole2Home.performLockChange("Description", 
					checkbox_LockDescription.isSelected()); });
		
		
		// GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerRole2Home.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerRole2Home.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children
        theRootPane.getChildren().addAll(
    			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
    			label_SelectLesson, combobox_SelectLesson, label_Title, text_Title,
    			label_Description, text_Description,
    			checkbox_LockLesson, checkbox_LockTitle, checkbox_LockDescription,
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
	
	
	/**********
	 * Private local method to initialize the standard fields for a CheckBox
	 * 
	 * @param c		The CheckBox object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupCheckBoxUI(CheckBox c, String ff, double f, double x, double y){
		c.setFont(Font.font(ff, f));
		c.setLayoutX(x);
		c.setLayoutY(y);
	}
}
