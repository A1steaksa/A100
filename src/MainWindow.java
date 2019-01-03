import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class MainWindow extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//The program title
	String titleBase = "A100";

	//The currently open file
	String openFile = "";

	//The file extension to expect
	String fileExtension = "A1";

	//Program counter
	int programCounter = 0;
	
	//The number of registers
	int registerCount = 8;

	//The actual registers themselves
	int[] registers = new int[ registerCount ];

	//Stores the labels associated with the registers
	JLabel[] registerLabels = new JLabel[ registerCount ];

	//Path to the icons
	String iconPath = "resources/icons/";

	//Keeps track of whether or not the file has been changed
	boolean fileHasChanged = false;

	//Keeps track of whether the code is currently executing
	boolean isRunning = false;

	//The font size
	int fontSize = 18;

	//The path of the most recently opened file
	//Default to execution path
	File currentFile = new File( System.getProperty("user.dir") );

	//UI Variables
	//Buttons
	JButton saveButton;
	
	//Text area
	JTextPane codeTextPane;

	//Execution highlighter
	HighlightPainter linePainter = new DefaultHighlighter.DefaultHighlightPainter( Color.gray );
	
	MainWindow() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		super();
		
		//Font
		Charset.forName( "UTF-8" );
		Font font = new Font( "Consolas", Font.PLAIN, fontSize );

		//Main icon
		ImageIcon img = new ImageIcon( iconPath + "icon.png" );
		this.setIconImage( img.getImage() );

		//Make this look good
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		//Main window properties
		this.setTitle( titleBase );
		this.setSize( new Dimension( 500, 750 ) );
		this.setMinimumSize( new Dimension( 500, 500 ) );
		this.setLocationByPlatform( true );
		this.setLayout( new BorderLayout() );
		this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );

		//Confirm close if there are unsaved changes 
		this.addWindowListener( new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {}

			@Override
			public void windowClosed(WindowEvent arg0) {}

			@Override
			public void windowClosing(WindowEvent arg0) {

				//Ask them if they want to close without saving
				if( fileHasChanged ) {

					int dialogResult = JOptionPane.showConfirmDialog(
							codeTextPane,
							"There are unsaved changes to this file.\n\nWould you like to save before exiting?",
							"Warning",
							JOptionPane.YES_NO_CANCEL_OPTION
							);

					//If they do want to save
					if( dialogResult == JOptionPane.YES_OPTION ){

						//Let them save
						doASave();
					}

					//If they click cancel, pretend they didn't try to exit
					if( dialogResult == JOptionPane.CANCEL_OPTION ){
						return;
					}
				}

				//Having dealt with that, exit the program
				System.exit( 1 );

			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {}

			@Override
			public void windowDeiconified(WindowEvent arg0) {}

			@Override
			public void windowIconified(WindowEvent arg0) {}

			@Override
			public void windowOpened(WindowEvent arg0) {}

		});

		//Text pane
		codeTextPane = new JTextPane();
		codeTextPane.setFont( font );
		//codeTextArea.setTabSize( 3 );
		codeTextPane.setDocument( new UpperCaseDocument() );

		//Listener to catch changes
		codeTextPane.getDocument().addDocumentListener( new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setFileHasChanged( true );
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if( codeTextPane.getText().length() == 0 ) {
					setFileHasChanged( false );
				}else {
					setFileHasChanged( true );
				}
			}

		});

		//Keyboard listener for ctrl-s
		codeTextPane.addKeyListener( new KeyListener() {

			boolean controlDown = false;

			@Override
			public void keyPressed( KeyEvent event ) {

				//Check for control
				if( event.getKeyCode() == KeyEvent.VK_CONTROL ) {
					controlDown = true;
				}

				//Check for S
				if( event.getKeyCode() == KeyEvent.VK_S ) {

					//Now this is a save
					if( controlDown ) {
						doASave();

						event.consume();
					}
				}

			}

			@Override
			public void keyReleased( KeyEvent event ) {

				//Check for control
				if( event.getKeyCode() == KeyEvent.VK_CONTROL ) {
					controlDown = false;
				}

			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}

		});

		//Code line numbers
		TextLineNumber codeLineNumber = new TextLineNumber( codeTextPane );
		codeLineNumber.setFont( font );

		//Code scroll pane
		JScrollPane codeScrollPane = new JScrollPane( codeTextPane );
		codeScrollPane.setRowHeaderView( codeLineNumber );

		//Registers
		JPanel registersPanel = new JPanel();
		registersPanel.setLayout( new BoxLayout( registersPanel, BoxLayout.X_AXIS ) );
		registersPanel.setPreferredSize( new Dimension( 50 * registerCount, 50 ) );

		for (int i = 0; i < registerCount; i++) {

			//The panel to contain the register
			JPanel register = new JPanel();
			register.setLayout( new BorderLayout() );
			register.setMinimumSize( new Dimension( 50, -1 ) );
			register.setPreferredSize( new Dimension( 50, -1 ) );

			//Add a border
			register.setBorder(BorderFactory.createLineBorder(Color.black));

			//The label to hold the register name
			JLabel topLabel = new JLabel( "R" + i );
			topLabel.setHorizontalAlignment( JLabel.CENTER );
			topLabel.setFont( font );
			register.add( topLabel, BorderLayout.NORTH );

			//The label to hold the register value
			JLabel bottomLabel = new JLabel( "0" );
			bottomLabel.setHorizontalAlignment( JLabel.CENTER );
			bottomLabel.setFont( font );
			register.add( bottomLabel, BorderLayout.SOUTH );

			//Add the bottom register to the registerLabels list so it can be edited later
			registerLabels[ i ] = bottomLabel;

			//Add this register to the list
			registersPanel.add( register );
		}


		//Registers scroll pane
		JScrollPane registersPanelScrollPane = new JScrollPane( registersPanel );

		//Console
		JTextArea consoleTextArea = new JTextArea( 3, 10 );
		consoleTextArea.setFont( font );
		consoleTextArea.setTabSize( 3 );
		consoleTextArea.setEditable( false );

		//Console scroll pane
		JScrollPane consoleScrollPane = new JScrollPane( consoleTextArea );

		//Panel for the console and registers
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout( new BorderLayout() );
		bottomPanel.add( registersPanelScrollPane, BorderLayout.NORTH );
		bottomPanel.add( consoleScrollPane, BorderLayout.CENTER );

		//Split pane to split code from the bottom pieces
		JSplitPane topSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		topSplitPane.setTopComponent( codeScrollPane );
		topSplitPane.setBottomComponent( bottomPanel );
		topSplitPane.setDividerLocation( 500 );
		this.add( topSplitPane, BorderLayout.CENTER );

		//New button
		ImageIcon newIcon = new ImageIcon( iconPath + "new.png" );
		JButton newButton = new JButton( newIcon );
		newButton.setFocusable( false );
		newButton.setToolTipText( "New file" );
		newButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				//Make sure they want to lose everything if there is something to lose
				if( fileHasChanged ) {
					int dialogResult = JOptionPane.showConfirmDialog(
							codeTextPane,
							"Creating a new file will destroy any changes in the current file.\n\nAre you sure you want to lose everything?",
							"Warning",
							JOptionPane.YES_NO_OPTION
							);

					//If they do
					if(dialogResult == JOptionPane.NO_OPTION){
						return;
					}

					//Close the current file
					closeFile();
				}
			}

		});

		//Open button
		ImageIcon openIcon = new ImageIcon( iconPath + "open.png" );
		JButton openButton = new JButton( openIcon );
		openButton.setFocusable( false );
		openButton.setToolTipText( "Open file" );
		openButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				//Have them select a new file
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter( new FileNameExtensionFilter( "A100 Files", fileExtension )  );
				fileChooser.setCurrentDirectory( currentFile ); 

				//Get their response
				int returnVal = fileChooser.showOpenDialog( openButton );

				//Open a file if they chose one
				if( returnVal == JFileChooser.APPROVE_OPTION ) {

					//Make sure they want to lose everything if there is something to lose
					if( fileHasChanged ) {

						//Make sure they want to lose everything
						int dialogResult = JOptionPane.showConfirmDialog(
								codeTextPane,
								"Opening a file will destroy any changes in the current file.\n\nAre you sure you want to lose everything?",
								"Warning",
								JOptionPane.YES_NO_OPTION
								);

						//If they do
						if(dialogResult == JOptionPane.NO_OPTION){
							return;
						}

					}

					//Close the current file
					closeFile();

					//Open the new file
					File file = fileChooser.getSelectedFile();
					openFile( file );
				}

			}

		});

		//Save button
		ImageIcon saveIcon = new ImageIcon( iconPath + "save.png" );
		saveButton = new JButton( saveIcon );
		saveButton.setFocusable( false );
		saveButton.setToolTipText( "Save file" );
		saveButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				doASave();
			}

		});

		//Step button
		ImageIcon stepIcon = new ImageIcon( iconPath + "step.png" );
		JButton stepButton = new JButton( stepIcon );
		stepButton.setFocusable( false );
		stepButton.setEnabled( false );
		stepButton.setToolTipText( "Step forward" );
		stepButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					step();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});


		//Fast forward button
		ImageIcon fastForwardIcon = new ImageIcon( iconPath + "fastforward.png" );
		JButton fastForwardButton = new JButton( fastForwardIcon );
		fastForwardButton.setFocusable( false );
		fastForwardButton.setEnabled( false );
		fastForwardButton.setToolTipText( "Run to next pause" );
		fastForwardButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				

			}

		});

		//Run/Stop button
		ImageIcon runIcon = new ImageIcon( iconPath + "run.png" );
		ImageIcon stopIcon = new ImageIcon( iconPath + "stop.png" );
		JButton runStopButton = new JButton( runIcon );
		runStopButton.setFocusable( false );
		runStopButton.setToolTipText( "Start running" );
		runStopButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				//Enable/Disable buttons
				saveButton.setEnabled( isRunning );
				newButton.setEnabled( isRunning );
				openButton.setEnabled( isRunning );

				stepButton.setEnabled( !isRunning );
				fastForwardButton.setEnabled( !isRunning );
				
				//Enable/Disable editing
				codeTextPane.setEditable( isRunning );
				codeTextPane.setFocusable( isRunning );
				
				//If we're running, act as a stop button
				if( isRunning ) {

					//Change the icon
					runStopButton.setIcon( runIcon );
					
					//Update the tooltip
					runStopButton.setToolTipText( "Start running" );
					
					isRunning = false;

				}else {
					//Otherwise, act as a play button

					//Change the icon
					runStopButton.setIcon( stopIcon );
					
					//Update the tooltip
					runStopButton.setToolTipText( "Stop running" );
					
					isRunning = true;
				}

			}

		});
		
		
		//Toolbar
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable( false );
		
		toolbar.add( newButton );
		toolbar.add( openButton );
		toolbar.add( saveButton );
		
		toolbar.addSeparator();
		
		toolbar.add( runStopButton );
		toolbar.add( stepButton );
		toolbar.add( fastForwardButton );



		this.add( toolbar, BorderLayout.NORTH );

		this.setVisible( true );

		//Make sure we properly set up the empty default file
		closeFile();

	}
	
	//Performs the next step in execution
	//Arguably this is the entire program
	public void step() throws BadLocationException {
		
		programCounter++;
		
	}

	//Returns whether or not a file is open
	public boolean isFileOpen() {
		return !openFile.equals( "" );
	}

	//Figures out what kind of save to do and does it
	public void doASave() {

		//If the file has changed, show the save dialog
		//If there is already a file open, just save to that
		if( isFileOpen() ) {
			saveFile( currentFile );
		}else {

			//Otherwise, show the dialog
			showSaveFileDialog();
		}

	}

	//Shows the save file dialog
	public void showSaveFileDialog() {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter( new FileNameExtensionFilter( "A100 Files", fileExtension )  );
		fileChooser.setCurrentDirectory( currentFile ); 

		//Get their response
		int returnVal = fileChooser.showSaveDialog( codeTextPane );

		//Open a file if they chose one
		if( returnVal == JFileChooser.APPROVE_OPTION ) {

			File saveFile = fileChooser.getSelectedFile();

			//If it doesn't end in the appropriate extension, add it on
			if( !saveFile.getName().toLowerCase().endsWith( "." + fileExtension.toLowerCase() ) ) {

				saveFile = new File( saveFile.getAbsolutePath() + "." + fileExtension );

			}

			saveFile( saveFile );

		}

	}

	//Actually saves the currently open file
	public void saveFile( File file ) {

		//Writer to save the file
		try {
			FileWriter writer = new FileWriter( file );

			//Save the file
			writer.write( codeTextPane.getText() );

			//Close the writer
			writer.flush();
			writer.close();

			//If we just saved, make sure we know we don't have changes left
			setFileHasChanged( false );

			//Update our recent file path
			currentFile = file;

			//Keep track of our file name
			openFile = file.getName();

			//Update the title
			updateTitle();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	//Opens a file
	public void openFile( File file ) {

		//Update the open file
		openFile = file.getName();

		//Mark down that this is the path to the most recent file
		currentFile = file;

		//The contents of the file
		String fileContents = "";

		try {

			//Scanner to read the file
			Scanner scanner = new Scanner( new FileReader( file ) );

			//Read the contents
			while( scanner.hasNext() ) {

				String line = scanner.nextLine();

				fileContents += line + System.lineSeparator();

			}

			//Close the scanner
			scanner.close();

			//Put the loaded file in the text area
			codeTextPane.setText( fileContents );

			//Update the title
			updateTitle();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Just opened files have no changes
		setFileHasChanged( false );

	}

	//Closes the current file
	public void closeFile() {

		//We have no file open
		openFile = "";

		//Update the title to reflect that
		updateTitle();

		//Empty any text that might linger
		codeTextPane.setText( "" );

		//Empty files have no changes
		setFileHasChanged( false );
	}

	//Updates the title of the window
	public void updateTitle() {
		if( isFileOpen() ) {
			this.setTitle( titleBase + " - " + openFile );
		}else {
			this.setTitle( titleBase + " - Untitled" );
		}
	}

	//Updates the file having changed
	public void setFileHasChanged( boolean newValue ) {

		fileHasChanged = newValue;

	}

}
