/**
 * The module for the Foundations application, which needs JavaFX for the interface and
 * java.sql for the database.
 */
module FoundationsF26 {
	requires javafx.controls;
	requires java.sql;
	
	opens applicationMain to javafx.graphics, javafx.fxml;
}
