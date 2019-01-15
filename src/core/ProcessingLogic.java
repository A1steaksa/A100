package core;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import misc.Config;
import misc.Strings;

public class ProcessingLogic implements Runnable{

	//A flag that stops the next step from executing when set
	public boolean halt = false;
	
	//A reference to the main window
	private MainWindow window;

	//The actual registers themselves
	public Map<String, Integer> registers = new HashMap<String, Integer>();
	
	//Labels and their associated lines
	public Map<String, Integer> labels = new HashMap<String, Integer>();
	
	//Main memory's array
	public int[] mainMemory = new int[ Config.mainMemoryLength ];
	
	//String buffer linked list
	public String[] stringBuffer = new String[ Config.stringBufferSize ];
	
	//Keeps track of where we are in the string buffer so we don't overwrite anything
	public int stringBufferPosition = 0;
	
	//Tracks our last executed line for error reporting
	public int lastLine = -1;
	
	@Override
	public void run() {
		
		//This is run as a thread while fast forwarding
		fastForward();
		
	}
	
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
	
	//Stores a value into main memory at an address and throws a halting error if that memory address doesn't exist
	public void setMainMemoryValue( int address, int value ) {
		
		//Check for out of bounds exception
		if( address < 0 || address >= Config.mainMemoryLength ) {
			error( Strings.MemoryHeadOutOfBounds );
		}
		
		//Store the value at the address
		mainMemory[ address ] = value;
		
		//Update the UI
		window.setMainMemoryValue( address, value );
		
	}
	
	//Returns the value of main memory at a given address and throws a halting error if that memory address doesn't exist
	public int getMainMemoryValue( int address ) {
		
		//Check for out of bounds exception
		if( address < 0 || address >= Config.mainMemoryLength ) {
			error( Strings.MemoryHeadOutOfBounds );
		}
		
		//Return the cleared value
		return mainMemory[ address ];
		
	}
	
	//Writes a character to the string buffer
	public void writeToStringBuffer( int value ) {
		
		//The value must be in the appropriate ASCII range
		if( value < 32 || value > 126 ) {
			error( Strings.BufferValueOutOfASCIIRange );
		}
		
		//Convert the ASCII value to a string
		String charValue = String.valueOf( ( (char) value ) );
		
		//Write to buffer
		stringBuffer[ stringBufferPosition ] = charValue;
		
		//Update UI
		window.setStringBufferValue( stringBufferPosition, charValue );
		
		//Increment our stringBufferPosition
		stringBufferPosition++;
		
	}
	
	//Returns the full string from the string buffer
	public String readStringBuffer() {
		String output = "";
		
		for (int i = 0; i < stringBufferPosition; i++) {
			output += stringBuffer[ i ];
		}
		
		return output;
	}
	
	//Clears the string buffer
	public void clearStringBuffer() {
		stringBuffer = new String[ Config.stringBufferSize ];
		
		//Reset every character and update the UI
		for (int i = 0; i < stringBuffer.length; i++) {
			stringBuffer[ i ] = "";
			window.setStringBufferValue( i,  "" );
		}
		
		//Reset the string buffer position
		stringBufferPosition = 0;
		
	}
	
	//Stops execution as soon as it is safe to
	public void halt() {
		//Depending on how we finished, print out a success or failure message
		if( halt ) {
			print( Strings.ExitWithError );
		}else {
			print( Strings.ExitNormal );
		}

		//Switch away from execution mode
		window.switchToEditMode();
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
		lastLine = 0;
		
		//Reset string buffer
		clearStringBuffer();
		
		//Reset the memory head
		setRegisterValue( "MH", 0 );
		
		//Move the program counter to the first executable line
		if( shouldSkipLine( 0 ) ) {
			incrementProgramCounter();
		}
		
		//If there's nothing to execute, throw an error to that effect
		if( getRegisterValue( "PC" ) >= window.getLineCount() ) {
			error( Strings.EmptyFile );
			
			halt();
		}
		
		//Highlight the first executable line
		window.highlightLine( getRegisterValue( "PC" ) );
		
		//Highlight the memory head
		window.highlightMemoryAddress( getRegisterValue( "MH" ) );
		
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
	
	//Gets the line number of an argument either as a literal or from a label
	public int getArgumentLineNumber( String argument ){
		
		//Check if this is a literal
		try {
			int lineNumber = Integer.parseInt( argument ) - 1;
			
			//If it is a literal, just return that
			return lineNumber;
		}catch( Exception e ) {}
		
		//If it's not a literal then it should be a label
		
		//Make sure we have a valid label
		if( !labels.containsKey( argument ) ) {
			error( Strings.InvalidLabelReference );
		}
		
		//Get the label's line number
		int lineNumber = labels.get( argument );
		
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
			
		case "BR":
			
			if( splitLine.length != 2 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			BR( splitLine[ 1 ] );
			
			break;
			
		case "LOAD":
			
			if( splitLine.length != 2 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			LOAD( splitLine[ 1 ] );
			
			break;
		
		case "STORE":
			
			if( splitLine.length != 2 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			STORE( splitLine[ 1 ] );
			
			break;
			
		case "APND":
			
			if( splitLine.length != 2 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			APND( splitLine[ 1 ] );
			
			break;
			
		case "DUMP":
			
			if( splitLine.length != 1 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			DUMP();

		case "PRNT":
			
			if( splitLine.length != 1 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			PRNT();
			
			break;
			
		case "CLR":
			
			if( splitLine.length != 1 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			CLR();
			
		case "ASL":
			
			if( splitLine.length != 4 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			ASL( splitLine[ 1 ], splitLine[ 2 ], splitLine[ 3 ] );
			
			break;
			
		case "ASR":
			
			if( splitLine.length != 4 ) {
				error( Strings.WrongNumberOfArguments );
			}
			
			ASR( splitLine[ 1 ], splitLine[ 2 ], splitLine[ 3 ] );
			
			break;
		default:
			
			System.out.println( "Ran into default line:" + splitLine[ 0 ] + ":Which should really not happen" );
			
			error( Strings.UnrecognizedOpcode );
		}
		
		//Highlight the current memory head position if it's valid and throw an error if it's not
		if( getRegisterValue( "MH" ) < 0 || getRegisterValue( "MH" ) >= Config.mainMemoryLength ) {
			
			error( Strings.MemoryHeadOutOfBounds );
			
		}else {
			window.highlightMemoryAddress( getRegisterValue( "MH" ) );
		}

		//Check if we have reached the end of the code
		if( !hasNextLine() ) {
			
			halt();
			
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
			try {
				Thread.sleep( 100 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
	//Performs the addition operation
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
	
	//Performs the subtraction operation
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
		int lineNumber = getArgumentLineNumber( labelName );
		
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
		int lineNumber = getArgumentLineNumber( labelName );

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
		int lineNumber = getArgumentLineNumber( labelName );

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
		int lineNumber = getArgumentLineNumber( labelName );

		//Check if they're not equal
		if( valueA < valueB ) {
			//Branch to line number
			setRegisterValue( "PC", lineNumber );
			
			//Jump to the next executable command
			incrementProgramCounter();
		}

	}
	
	//Jump to label
	public void BR( String labelName ) {
		
		//Get the label's line number
		int lineNumber = getArgumentLineNumber( labelName );
		
		//Branch to line number
		setRegisterValue( "PC", lineNumber );
		
		//Jump to the next executable command
		incrementProgramCounter();
		
	}
	
	//Load from main memory
	public void LOAD( String A ) {
		
		//Get what's in memory at MH
		int mainMemoryValue = getMainMemoryValue( getRegisterValue( "MH" ) );
		
		//Load it into register A
		setRegisterValue( A, mainMemoryValue );
		
	}
	
	//Store into main memory
	public void STORE( String A ) {
		
		//Get A's value
		int valueA = getArgumentValue( A );
		
		//Save it into main memory at MH
		setMainMemoryValue( getRegisterValue( "MH" ), valueA );
		
	}
	
	/*
	 * String buffer
	 */
	
	//Appends a character to the string buffer
	public void APND( String A ) {
		
		//Get A's value
		int valueA = getArgumentValue( A );
		
		//Write to the buffer
		writeToStringBuffer( valueA );
		
 	}
	
	//Clears the string buffer
	public void DUMP() {
		clearStringBuffer();
	}
	
	//Flushes the string buffer to the console
	public void PRNT() {
		print( readStringBuffer() );
		clearStringBuffer();
	}
	
	//Clears the console
	public void CLR() {
		window.clearConsole();
	}
	
	//Performs an arithmetic shift left of A by B amount and stores it in register C
	public void ASL( String A, String B, String C ) {
		
		//Get A and B values
		int valueA = getArgumentValue( A );
		int valueB = getArgumentValue( B );
		
		//Shift A by amount B
		int output = valueA << valueB;
		
		//Store the output in register C
		setRegisterValue( C, output );
		
	}
	
	//Performs an arithmetic shift right of A by B amount and stores it in register C
	public void ASR( String A, String B, String C ) {
		
		//Get A and B values
		int valueA = getArgumentValue( A );
		int valueB = getArgumentValue( B );
		
		//Shift A by amount B
		int output = valueA >> valueB;
		
		//Store the output in register C
		setRegisterValue( C, output );
		
	}
}
