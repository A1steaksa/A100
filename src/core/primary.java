package core;
import javax.swing.UnsupportedLookAndFeelException;

public class primary {

	public static MainWindow mainWindow;
	
	public static ProcessingLogic processingLogic;
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
		
		//Instantiate main sections
		//Main window
		mainWindow = new MainWindow();
		//Main logic
		processingLogic = new ProcessingLogic();
		
		//Start main sections
		mainWindow.start();
		processingLogic.start();
		
	}

}
