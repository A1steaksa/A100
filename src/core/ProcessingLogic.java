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
	public void reset() {
		
		//Clear registers
		clearRegisters();
		
		//Reset halt
		halt = false;
		
	}

	//Clears the registers
	public void clearRegisters() {
		for (Entry<String, Integer> entry : registers.entrySet()) {
			setRegisterValue( entry.getKey(), 0 );
		}
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

	//Returns whether or not there is a next line available for execution
	public boolean hasNextLine() {
		
		//Halt if we need to
		if( halt ) {
			return false;
		}
		
		//Checks if the program counter is below the line count
		return getRegisterValue( "PC" ) < window.getLineCount();
	}

	//Performs the next step in execution
	//Arguably this is the entire program
	public void step() {

		//Get the current line
		String line = window.getLine( getRegisterValue( "PC" ) );

		//Set to true to skip this line
		boolean skip = false;

		//Check if this is a comment line or an empty line
		if( line.startsWith( "#" ) || line.trim().length() == 0 ) {
			//ignore it
			skip = true;
		}

		//If we don't skip, continue processing
		if( !skip ) {
			line = line.toUpperCase();

			//Break the line apart by spaces as those are our delimiter
			String[] splitLine = line.split( " " );

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

			default:
				error( Strings.UnrecognizedOpcode );
			}

		}

		//Increment the program counter
		setRegisterValue( "PC",  getRegisterValue( "PC" ) + 1 );

		//Check if we have reached the end of the code
		if( !hasNextLine() ) {
			print( "Execution finished" );

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


	/*
	 * Opcode methods
	 */

	//Performs the MOV command
	public void MOV( String A, String B ) {

		int valueA = -1;

		//If A is not a register, we need to get the value from what A references
		if( !isRegister( A ) ) {
			//Convert A to an integer
			valueA = getLiteral( A );
		}else {
			//Otherwise we can just get it directly

			//Get A's register's value
			valueA = getRegisterValue( A );
		}

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
		int valueA = -1;
		int valueB = -1;

		//Get values for A
		if( isRegister( A ) ) {
			valueA = getRegisterValue( A );
		}else {
			//Otherwise it's a literal
			valueA = getLiteral( A );
		}

		//Get values for B
		if( isRegister( B ) ) {
			valueB = getRegisterValue( B );
		}else {
			//Otherwise it's a literal
			valueB = getLiteral( B );
		}

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
		int valueA = -1;
		int valueB = -1;

		//Get values for A
		if( isRegister( A ) ) {
			valueA = getRegisterValue( A );
		}else {
			//Otherwise it's a literal
			valueA = getLiteral( A );
		}

		//Get values for B
		if( isRegister( B ) ) {
			valueB = getRegisterValue( B );
		}else {
			//Otherwise it's a literal
			valueB = getLiteral( B );
		}

		//Do the subtraction
		int result = valueA - valueB;

		//Store the outcome
		setRegisterValue( C, result );

	}

}
