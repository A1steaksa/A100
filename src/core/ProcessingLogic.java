package core;
import misc.Config;
import misc.Strings;

public class ProcessingLogic {

	//A reference to the main window
	private MainWindow window;

	//Program counter
	public int programCounter = 0;

	//The actual registers themselves
	public int[] registers = new int[ Config.registerCount ];

	//Called when all main sections are created in primary
	public void start() {

		//Get the main window reference
		window = primary.mainWindow;
		
	}

	//A wrapper for print
	private void print( String str ) {
		window.print( str );
	}

	//A wrapper for error
	private void error( String str ) {
		window.error( str );
	}

	//Stores value A in register B
	public void storeValueInRegister( int A, int B ) {

		//Store the value
		registers[ B ] = A;

		//Update the UI
		window.registerLabels[ B ].setText( String.valueOf( A  ) );

	}

	//Clears the registers
	public void clearRegisters() {
		for (int i = 0; i < Config.registerCount; i++) {
			storeValueInRegister( 0, i );
		}
	}

	//Returns whether the passed string references a valid register
	public boolean isRegister( String argument ) {

		//No R means it isn't a register
		if( !argument.startsWith( "R" ) ){
			return false;
		}

		//Get rid of the R so we can convert to a number
		argument = argument.replace( "R", "" );

		int registerNumber;

		//Convert to a register number
		try {
			registerNumber = Integer.parseInt( argument );
		} catch( Exception e ) {
			//If we can't convert the remaining text to a number, this was not a register
			return false;
		}

		//But also if the register it references is outside the register count that's not good
		if( registerNumber >= Config.registerCount || registerNumber < 0 ) {
			return false;
		}

		//Only now can we say it's probably a register (probably)
		return true;
	}

	//Parses the register number out of a register string
	public int getRegisterNumber( String argument ) {

		//Remove the R
		argument = argument.replace( "R" ,  "" );

		//Trim the result
		argument = argument.trim();

		//Convert to an int
		int output = Integer.parseInt( argument );

		//Error checking

		//Make sure it's a valid register
		if( output >= Config.registerCount || output < 0 ) {
			error( Strings.InvalidRegisterReference );
		}

		return output;
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

		return programCounter < window.getLineCount();

	}

	//Performs the next step in execution
	//Arguably this is the entire program
	public void step() {

		if( hasNextLine() ) {

			//Highlight the current line
			window.highlightLine( programCounter );
			
			//Get the current line
			String line = window.getLine( programCounter );

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

			programCounter++;

		}else {
			//Stop executing if we have run out of stuff to execute

			print( "Execution finished" );

			window.switchToEditMode();

		}

	}
	
	//Steps until there are no more steps to take
	public void fastForward() {
		
		//As long as there are steps to take, step
		while( hasNextLine() ) {
			step();
		}
		
		//Having come to the end of the available steps, take another one to end execution
		step();
	}


	/*
	 * Opcode methods
	 */

	//Performs the MOV command
	public void MOV( String A, String B ) {

		int valueA = -1;
		int valueB = -1;

		//If A is not a register, we need to get the value from what A references
		if( !isRegister( A ) ) {

			//Convert A to an integer
			valueA = getLiteral( A );

			//Now AInt contains the literal value of A
		}else {
			//If A is a register, we need to go get it's value

			//Get the register number
			int ARegisterNumber = getRegisterNumber( A );

			//Get A's register's value
			valueA = registers[ ARegisterNumber ];
		}

		//Get B's register number
		valueB = getRegisterNumber( B );

		//Error checking

		//Check that B is a register
		if( !isRegister( B ) ) {
			error( Strings.ArgumentIsNotRegister );
		}

		//If there are no errors, store AValue in the register corresponding to BValue
		storeValueInRegister( valueA, valueB );

	}

	public void ADD( String A, String B, String C ) {

		//Stores the register number for C
		int registerC = -1;

		//Process output register C
		if( !isRegister( C ) ) {
			error( Strings.ArgumentIsNotRegister );
			return;
		}else {
			registerC = getRegisterNumber( C );
		}

		//Values of A and B
		int valueA = -1;
		int valueB = -1;

		//Get values for A
		if( isRegister( A ) ) {
			int registerA = getRegisterNumber( A );
			valueA = registers[ registerA ];
		}else {
			//Otherwise it's a literal
			valueA = getLiteral( A );
		}

		//Get values for B
		if( isRegister( B ) ) {
			int registerB = getRegisterNumber( B );
			valueB = registers[ registerB ];
		}else {
			//Otherwise it's a literal
			valueB = getLiteral( B );
		}

		//Do the addition
		int result = valueA + valueB;

		if( result > Config.maxNumberRange || result < Config.minNumberRange ) {
			error( Strings.NumberOutOfBounds );
		}

		//Store the outcome
		storeValueInRegister( result, registerC );

	}

	public void SUB( String A, String B, String C ) {

		//Stores the register number for C
		int registerC = -1;

		//Process output register C
		if( !isRegister( C ) ) {
			error( Strings.ArgumentIsNotRegister );
			return;
		}else {
			registerC = getRegisterNumber( C );
		}

		//Values of A and B
		int valueA = -1;
		int valueB = -1;

		//Get values for A
		if( isRegister( A ) ) {
			int registerA = getRegisterNumber( A );
			valueA = registers[ registerA ];
		}else {
			//Otherwise it's a literal
			valueA = getLiteral( A );
		}

		//Get values for B
		if( isRegister( B ) ) {
			int registerB = getRegisterNumber( B );
			valueB = registers[ registerB ];
		}else {
			//Otherwise it's a literal
			valueB = getLiteral( B );
		}

		//Do the addition
		int result = valueA - valueB;

		if( result > Config.maxNumberRange || result < Config.minNumberRange ) {
			error( Strings.NumberOutOfBounds );
		}

		//Store the outcome
		storeValueInRegister( result, registerC );

	}

}
