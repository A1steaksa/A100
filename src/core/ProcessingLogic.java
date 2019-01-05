package core;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;

import misc.Config;
import misc.Strings;

public class ProcessingLogic {

	//A flag that stops the next step from executing when set
	public boolean halt = false;
	
	//A reference to the main window
	private MainWindow window;

	//The actual registers themselves
	public Map<String, Integer> registers = new HashMap<String, Integer>();
	
	//Labels and their associated lines
	public Map<String, Integer> labels = new HashMap<String, Integer>();

	//Tracks our last executed line for error reporting
	public int lastLine = -1;
	
	//Called when all main sections are created in primary
	public void start() {

		//Get the main window reference
		window = primary.mainWindow;

	}

	//Adds a register to the logic system
	//We do this directly to avoid invalid register issues
	public void addRegister( String name, int value ) {
		registers.put( name, value );
	}

	//Places a value into a register and throws a halting error if that register does not exist
	public void setRegisterValue( String name, int value ) {

		//Get the value of the register to check if it exists
		//Registers are initialized to some value (usually 0) so if it's null then we know it's invalid
		if( registers.get( name ) == null ) {
			error( Strings.InvalidRegisterReference );
		}
		
		//Check for out of bounds errors
		if( value > Config.maxNumberRange || value < Config.minNumberRange ) {
			error( Strings.NumberOutOfBounds );
		}

		//Put the value into the register
		registers.put( name,  value );

		//Update the UI
		window.setRegisterValue( name, value );

	}

	//Returns the value of a register and throws a halting error if that register does not exist
	public int getRegisterValue( String name ) {


		Integer registerValue = registers.get( name );

		//Registers are initialized to some value (usually 0) so if it's null then we know it's invalid
		if( registerValue == null ) {
			error( Strings.InvalidRegisterReference );
		}

		//Return the cleared value
		return registerValue;
		
	}

	//A wrapper for print
	private void print( String str ) {
		window.print( str );
	}

	//A wrapper for error
	private void error( String str ) {
		window.error( str );
		halt = true;
	}
	
	//Resets logic to run again
	public void getReadyToRun() {
		
		//Clear registers
		clearRegisters();
		
		//Reset halt
		halt = false;
		
		//Reset last line
		lastLine = -1;
		
		//Move the program counter to the first executable line
		if( shouldSkipLine( 0 ) ) {
			incrementProgramCounter();
		}
		
		//Highlight the first executable line
		window.highlightLine( getRegisterValue( "PC" ) );
		
	}

	//Clears the registers
	public void clearRegisters() {
		for (Entry<String, Integer> entry : registers.entrySet()) {
			setRegisterValue( entry.getKey(), 0 );
		}
		
		setRegisterValue( "PC", 0 );
	}

	//Returns whether the passed string references a valid register
	public boolean isRegister( String name ) {

		//If it exists in the registers hashmap, it's a register
		if( registers.get( name ) == null ) {
			return false;
		}

		//If it does exist, it's a register
		return true;

	}

	//Converts an argument string into a literal int
	public int getLiteral( String argument ) {
		int output = 0;

		try {
			output = Integer.parseInt( argument );
		} catch ( Exception e ) {
			//If this isn't a register, and it isn't a literal number, we don't know what it is then
			error( Strings.UnrecognizedDataType );
		}

		//Check the output for out of bounds error
		if( output > Config.maxNumberRange || output < Config.minNumberRange ) {
			error( Strings.NumberOutOfBounds );
		}

		return output;
	}
	
	//Gets the value of an argument string or its register's value
	public int getArgumentValue( String argument ) {
		if( isRegister( argument ) ) {
			return getRegisterValue( argument );
		}else {
			//Otherwise it's a literal
			return getLiteral( argument );
		}
	}
	
	//Gets the line number of a label
	public int getLabelLineNumber( String labelName ){
		
		//Get the label's line number
		Integer lineNumber = labels.get( labelName );
		
		//Make sure we have a valid label
		if( lineNumber == null ) {
			error( Strings.InvalidLabelReference );
		}
		
		return lineNumber;
	}

	//Returns whether or not there is a next line available for execution
	public boolean hasNextLine() {
		
		//Halt if we need to
		if( halt ) {
			return false;
		}
		
		//Checks if the program counter is below the line count
		return getRegisterValue( "PC" ) < window.getLineCount();
	}
	
	//Determines if a line can be skipped (Comments, labels, etc.)
	public boolean shouldSkipLine( int lineNumber ) {
		
		String line = window.getLine( lineNumber );
		line = line.toUpperCase();
		line = line.trim();
		
		if( line.startsWith( "#" ) || line.trim().length() == 0 || line.endsWith(":") ) {
			return true;
		}else {
			return false;
		}
		
	}
	
	//Finds the next executable line and moves the program counter to it
	public void incrementProgramCounter() {
		
		//Increment the program counter
		//Using a do while because have to move at least 1 line forward
		do {
			//Increment the program counter
			setRegisterValue( "PC",  getRegisterValue( "PC" ) + 1 );
			
			//If we have run out of lines, stop here
			if( getRegisterValue( "PC" ) >= window.getLineCount() ) {
				break;
			}
		
		//Loop if the line we just moved to needs to be skipped also
		}while( shouldSkipLine( getRegisterValue( "PC" ) ) );
		
	}

	//Performs the next step in execution
	//Arguably this is the entire program
	public void step() {
		
		//Get the current line
		String line = window.getLine( getRegisterValue( "PC" ) );
		line = line.toUpperCase();
		line = line.trim();
		
		//Update last line
		lastLine = getRegisterValue( "PC" );
		
		//Increment the program counter
		//setRegisterValue( "PC",  getRegisterValue( "PC" ) + 1 );
		incrementProgramCounter();
		
		//Break the line apart by spaces as those are our delimiter
		String[] splitLine = line.split( "\\s+" );

		//Trim everything to avoid errors later
		for (int i = 0; i < splitLine.length; i++) {
			splitLine[ i ] = splitLine[ i ].trim();
		}
		
		//We're going to use a switch case for this
		switch( splitLine[ 0 ] ) {
		case "MOV":

			if( splitLine.length != 3 ) {
				error( Strings.WrongNumberOfArguments );
			}

			MOV( splitLine[ 1 ], splitLine[ 2 ] );
			break;
		case "ADD":

			if( splitLine.length != 4 ) {
				error( Strings.WrongNumberOfArguments );
			}

			ADD( splitLine[ 1 ], splitLine[ 2 ], splitLine[ 3 ] );
			break;
		case "SUB":

			if( splitLine.length != 4 ) {
				error( Strings.WrongNumberOfArguments );
			}

			SUB( splitLine[ 1 ], splitLine[ 2 ], splitLine[ 3 ] );
			break;

		case "BNE":
			
			if( splitLine.length != 4 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			BNE( splitLine[ 1 ], splitLine[ 2 ], splitLine[ 3 ] );
			
			break;
		
		case "BEQ":
			
			if( splitLine.length != 4 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			BEQ( splitLine[ 1 ], splitLine[ 2 ], splitLine[ 3 ] );
			
			break;
		
		case "BGT":
			
			if( splitLine.length != 4 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			BGT( splitLine[ 1 ], splitLine[ 2 ], splitLine[ 3 ] );
			
			break;
			
		case "BLT":
			
			if( splitLine.length != 4 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			BLT( splitLine[ 1 ], splitLine[ 2 ], splitLine[ 3 ] );
			
			break;
			
		case "JMP":
			
			if( splitLine.length != 2 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			JMP( splitLine[ 1 ] );
			
			break;
			
			
		default:
			
			System.out.println( "Ran into default line:" + splitLine[ 0 ] + ":Which should really not happen" );
			
			error( Strings.UnrecognizedOpcode );
		}

		//Check if we have reached the end of the code
		if( !hasNextLine() ) {
			
			//Depending on how we finished, print out a success or failure message
			if( halt ) {
				print( Strings.ExitWithError );
			}else {
				print( Strings.ExitNormal );
			}

			//Switch away from execution mode
			window.switchToEditMode();
		}else {
			
			//Highlight the new line
			window.highlightLine( getRegisterValue( "PC" ) );
		}

	}

	//Steps until there are no more steps to take
	public void fastForward() {

		//As long as there are steps to take, step
		while( hasNextLine() ) {
			step();
		}
		
	}
	
	//Look through the code and do any preprocessing that is needed before running
	public void preprocess() {
		
		//Look for labels
		
		//Check every line
		for (int i = 0; i < window.getLineCount(); i++) {
			
			//Get this line
			String line = window.getLine( i );
			
			line = line.trim();
			
			//Check if it's a label
			if( line.endsWith( ":" ) ) {
				
				//Get the name without the ending
				String labelName = line.substring( 0, line.length() - 1 );
				
				//Remember, no spaces
				if( labelName.indexOf( " " ) != -1 ) {
					error( Strings.LabelContainedSpaces );
				}
				
				//save it in the labels map
				labels.put( labelName,  i );
				
			}
			
		}
		
	}


	/*
	 * Opcode methods
	 */

	//Performs the MOV command
	public void MOV( String A, String B ) {

		//Get A's value
		int valueA = getArgumentValue( A );

		//Error checking
		
		//Check that B is a register
		if( !isRegister( B ) ) {
			error( Strings.ArgumentIsNotRegister );
		}

		//If there are no errors, store AValue in the register corresponding to BValue
		setRegisterValue( B, valueA );

	}

	public void ADD( String A, String B, String C ) {

		//Process output register C
		if( !isRegister( C ) ) {
			error( Strings.ArgumentIsNotRegister );
			return;
		}

		//Values of A and B
		int valueA = getArgumentValue( A );
		int valueB = getArgumentValue( B );

		//Do the addition
		int result = valueA + valueB;

		//Store the outcome
		setRegisterValue( C, result );

	}

	public void SUB( String A, String B, String C ) {

		//Process output register C
		if( !isRegister( C ) ) {
			error( Strings.ArgumentIsNotRegister );
			return;
		}

		//Values of A and B
		int valueA = getArgumentValue( A );
		int valueB = getArgumentValue( B );

		//Do the subtraction
		int result = valueA - valueB;

		//Store the outcome
		setRegisterValue( C, result );

	}
	
	/*
	 * Branching
	 */
	
	//Branch not equal
	public void BNE( String A, String B, String labelName ) {
		
		//Get values of A and B
		int valueA = getArgumentValue( A );
		int valueB = getArgumentValue( B );
		
		//Get the label's line number
		int lineNumber = getLabelLineNumber( labelName );
		
		//Check if they're not equal
		if( valueA != valueB ) {
			//Branch to line number
			setRegisterValue( "PC", lineNumber );
			
			//Jump to the next executable command
			incrementProgramCounter();
		}
		
	}
	
	//Branch equal
	public void BEQ( String A, String B, String labelName ) {

		//Get values of A and B
		int valueA = getArgumentValue( A );
		int valueB = getArgumentValue( B );

		//Get the label's line number
		int lineNumber = getLabelLineNumber( labelName );

		//Check if they're not equal
		if( valueA == valueB ) {
			//Branch to line number
			setRegisterValue( "PC", lineNumber );
			
			//Jump to the next executable command
			incrementProgramCounter();
		}

	}
	
	//Branch greater than
	public void BGT( String A, String B, String labelName ) {

		//Get values of A and B
		int valueA = getArgumentValue( A );
		int valueB = getArgumentValue( B );

		//Get the label's line number
		int lineNumber = getLabelLineNumber( labelName );

		//Check if they're not equal
		if( valueA > valueB ) {
			//Branch to line number
			setRegisterValue( "PC", lineNumber );
			
			//Jump to the next executable command
			incrementProgramCounter();
		}

	}
	
	//Branch greater than
	public void BLT( String A, String B, String labelName ) {

		//Get values of A and B
		int valueA = getArgumentValue( A );
		int valueB = getArgumentValue( B );

		//Get the label's line number
		int lineNumber = getLabelLineNumber( labelName );

		//Check if they're not equal
		if( valueA < valueB ) {
			//Branch to line number
			setRegisterValue( "PC", lineNumber );
			
			//Jump to the next executable command
			incrementProgramCounter();
		}

	}
	
	//Jump to label
	public void JMP( String labelName ) {
		
		//Get the label's line number
		int lineNumber = getLabelLineNumber( labelName );
		
		//Branch to line number
		setRegisterValue( "PC", lineNumber );
		
		//Jump to the next executable command
		incrementProgramCounter();
		
	}
	

}
