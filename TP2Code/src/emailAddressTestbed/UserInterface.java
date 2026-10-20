
package emailAddressTestbed;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * <p> Title: UserInterface Class. </p>
 * 
 * <p> Description: The Java/FX-based input validator to develop and test UI ideas.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2018-02-03 The JavaFX-based GUI for the implementation of a testbed
 * @version 2.00		2022-01-02 Re-implementation to support checking an email address
 * @version 3.00		2025-09-04 Reimplement to focus on input validation of an email address
 *  
 */

public class UserInterface {
	
	/**********************************************************************************************

	Attributes
	
	**********************************************************************************************/
	
	/* Constants used to parameterize the graphical user interface.  We do not use a layout manager
	   for this application. Rather we manually control the location of each graphical element for
	   exact control of the look and feel. */
	private final double BUTTON_WIDTH = 40;

	// These are the application values required by the user interface
	private Label label_ApplicationTitle = new Label("Email Address Input Validator");
	private Label label_EmailAddress = new Label("Enter the Email Address here and then press 'Go'!");
	private TextField text_EmailAddress = new TextField();
	private Button button_Go= new Button("Go");
	private Label label_errEmailAddress = new Label("");	
    private Label noInputFound = new Label("");
	private TextFlow errEmailAddress;
    private Text errEmailAddressPart1 = new Text();
    private Text errEmailAddressPart2 = new Text();
    private Label validEmailAddress = new Label("");

	
	/**********************************************************************************************

	Constructors
	
	**********************************************************************************************/

	/**********
	 * This method initializes all of the elements of the graphical user interface. These assignments
	 * determine the location, size, font, color, and change and event handlers for each GUI object.
	 * 
	 * @param theRoot is the GUI Pane where the UI will be viewed
	 */
	public UserInterface(Pane theRoot) {
		
		// Label theScene with the name of the testbed, centered at the top of the pane
		setupLabelUI(label_ApplicationTitle, "Arial", 24, EmailAddressGUITestbed.WINDOW_WIDTH, 
				Pos.CENTER, 0, 10);
		
		// Label the email address input field with a title just above it, left aligned
		setupLabelUI(label_EmailAddress, "Arial", 14, EmailAddressGUITestbed.WINDOW_WIDTH-10, 
				Pos.BASELINE_LEFT, 10, 50);
		
		// Establish the text input operand field and when anything changes in the email address,
		// the code will process the entire input to ensure that it is valid or in error.
		setupTextUI(text_EmailAddress, "Arial", 18, EmailAddressGUITestbed.WINDOW_WIDTH-20,
				Pos.BASELINE_LEFT, 10, 70, true);
		text_EmailAddress.textProperty().addListener((observable, oldValue, newValue) 
				-> {setEmailAddress(); });
		
		// Establish an error message for when there is no input
		noInputFound.setTextFill(Color.RED);
		noInputFound.setAlignment(Pos.BASELINE_LEFT);
		setupLabelUI(noInputFound, "Arial", 14, EmailAddressGUITestbed.WINDOW_WIDTH-10, 
				Pos.BASELINE_LEFT, 10, 110);		
		
		// Establish an error message for the email address, left aligned
		label_errEmailAddress.setTextFill(Color.RED);
		label_errEmailAddress.setAlignment(Pos.BASELINE_RIGHT);
		setupLabelUI(label_errEmailAddress, "Arial", 14,  
						EmailAddressGUITestbed.WINDOW_WIDTH-150-10, Pos.BASELINE_LEFT, 22, 126);		
		
		// Establish the Go button, position it, and link it to methods to accomplish its work
		setupButtonUI(button_Go, "Symbol", 24, BUTTON_WIDTH, Pos.BASELINE_LEFT, 
						EmailAddressGUITestbed.WINDOW_WIDTH/2-BUTTON_WIDTH/2, 180);
		button_Go.setOnAction((event) -> { performGo(); });
		
		// Error Message components for the Email Address
		errEmailAddressPart1.setFill(Color.BLACK);
	    errEmailAddressPart1.setFont(Font.font("Arial", FontPosture.REGULAR, 18));
	    errEmailAddressPart2.setFill(Color.RED);
	    errEmailAddressPart2.setFont(Font.font("Arial", FontPosture.REGULAR, 24));
	    errEmailAddress = new TextFlow(errEmailAddressPart1, errEmailAddressPart2);
		errEmailAddress.setMinWidth(EmailAddressGUITestbed.WINDOW_WIDTH-10); 
		errEmailAddress.setLayoutX(20);  
		errEmailAddress.setLayoutY(100);
		
		// Valid Email Address message
		validEmailAddress.setTextFill(Color.GREEN);
		validEmailAddress.setAlignment(Pos.BASELINE_RIGHT);
		setupLabelUI(validEmailAddress, "Arial", 18,  
						EmailAddressGUITestbed.WINDOW_WIDTH-150-10, Pos.BASELINE_LEFT, 22, 126);				
		
		// Place all of the just-initialized GUI elements into the pane
		theRoot.getChildren().addAll(label_ApplicationTitle, label_EmailAddress, text_EmailAddress, 
				noInputFound, label_errEmailAddress, button_Go, errEmailAddress, validEmailAddress);

	}
	
	/**********
	 * Private local method to initialize the standard fields for a label
	 */
	private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	/**********
	 * Private local method to initialize the standard fields for a text field
	 */
	private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);		
		t.setEditable(e);
	}
	
	/**********
	 * Private local method to initialize the standard fields for a button
	 */
	private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}
	
	
	/**********************************************************************************************

	User Interface Actions
	
	**********************************************************************************************/

	private void setEmailAddress() {
		label_errEmailAddress.setText("");
		noInputFound.setText("");
		errEmailAddressPart1.setText("");
		errEmailAddressPart2.setText("");
		validEmailAddress.setText("");
//		performGo();			// To debug, remove the comment "//" at the beginning of the line
	}
	
	
	private void performGo() {
		String inputText = text_EmailAddress.getText();
		if (inputText.isEmpty())
		    noInputFound.setText("No input text found!");
		else
		{
			String errMessage = EmailAddressRecognizer.checkEmailAddress(inputText);
			if (errMessage != "") {
				System.out.println(errMessage);
				label_errEmailAddress.setText(EmailAddressRecognizer.emailAddressErrorMessage);
				if (EmailAddressRecognizer.emailAddressIndexofError <= -1) return;
				String input = EmailAddressRecognizer.emailAddressInput;
				errEmailAddressPart1.setText(input.substring(0, 
						EmailAddressRecognizer.emailAddressIndexofError));
				errEmailAddressPart2.setText("\u21EB");
			}
			else {
				System.out.println("Success! The email address is valid.");
				validEmailAddress.setText("Success! The email address is valid.");
			}
		}
	}
}
