package misc;
public class Strings {
	
	public static String NumberOutOfBounds = "A number was outside the bounds [" + Config.minNumberRange + ", " + Config.maxNumberRange  + "]!";
	
	public static String MemoryHeadOutOfBounds = "The main memory read/write head is outside the bounds [0, " + ( Config.mainMemoryLength - 1 ) + "]!";
	
	public static String ArgumentIsNotRegister = "An argument was expected to be a register but was not or was out of bounds!";
	
	public static String UnrecognizedOpcode = "An invalid opcode was found!";
	
	public static String InvalidRegisterReference = "A reference to a non-existant register was made!";
	
	public static String InvalidLabelReference = "A reference to a non-existant label was made!";
	
	public static String UnrecognizedDataType = "An argument was neither a register reference or a literal number!";
	
	public static String WrongNumberOfArguments = "The wrong number of arguments passed!";
	
	public static String LabelContainedSpaces = "A label contained spaces!";
	
	public static String EmptyFile = "You cannot run a file without executable commands!";
	
	/*
	 * String buffer errors
	 */
	
	public static String BufferValueOutOfASCIIRange = "A value outside of the range [32, 126] was written to the string buffer!";
	
	
	/*
	 * Exit condition messages
	 */
	public static String ExitWithError = "Execution halted with error(s)";
	
	public static String ExitNormal = "Execution finished";
	
}
