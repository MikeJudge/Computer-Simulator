/**************************************************************************
* This class runs machine level programs. The machine level programs      *
* are written as a list of five digit numbers with an operation code      *
* corresponding to the first two digits of a five digit decimal number,   * 
* and the last three digits corresponding to an operand.                  *
**************************************************************************/
import java.util.Scanner;
import java.util.Arrays;

public class Simpletron {
	private static final int MEMORY_SIZE = 1000;
	private static final Hex MAX_WORD    = new Hex("+FFFFF");
	private static final Hex MIN_WORD    = new Hex("-FFFFF");

	//operation code constants
	private static final Hex READ       = new Hex(10);
	private static final Hex WRITE      = new Hex(11);
	private static final Hex NEWLINE    = new Hex(12);

	private static final Hex LOAD       = new Hex(20);
	private static final Hex STORE      = new Hex(21);

	private static final Hex ADD        = new Hex(30);
	private static final Hex SUBTRACT   = new Hex(31);
	private static final Hex DIVIDE     = new Hex(32);
	private static final Hex MULTIPLY   = new Hex(33);
	private static final Hex REMAINDER  = new Hex(34);
	private static final Hex POWER      = new Hex(35);

	private static final Hex BRANCH     = new Hex(40);
	private static final Hex BRANCHNEG  = new Hex(41);
	private static final Hex BRANCHZERO = new Hex(42);
	private static final Hex HALT       = new Hex(43);


	private Hex[] memory;		 //program is stored here
	private Hex accumulator;
	private Hex instructionCounter;  //location in memory whose instruction is being performed now

	private Hex operationCode;        //operation being currently performed, 1st two digits of instructionRegister
	private Hex operand;		         //memory location where operation is being operated on last three of instructionRegister
	private Hex instructionRegister;  //full instruction word


	public Simpletron() {
		memory = new Hex[MEMORY_SIZE];
		for (int i = 0; i < memory.length; i++)
			memory[i] = new Hex();
		accumulator 	    = new Hex();
		instructionCounter  = new Hex();
		operationCode       = new Hex();
		operand             = new Hex();
		instructionRegister = new Hex();
	}

	//pre:  index, and word are in range
	//post: word is stored in memory
	public void storeWord(Hex index, Hex word) {
		int i = index.toInt();

		if (word.compareTo(MAX_WORD) > 0 || word.compareTo(MIN_WORD) < 0) {
			fatalError("*** overflow occured ***");
		}
		if (i > (MEMORY_SIZE-1) || i < 0) {
			fatalError("*** index out of bounds ***");
		}

		memory[i] = word;
	}

	public void storeWord(int index, int word) {
		String part1, part2;
		part1 = new Hex(word / 1000).getString(3);
		part2 = new Hex(word % 1000).getString(4);

		storeWord(new Hex(index), new Hex(part1 + part2.substring(1)));
	}

	private Hex getWord(Hex index) {
		int i = index.toInt();
		if (i > (MEMORY_SIZE - 1) || i < 0)
			fatalError("*** index out of bounds ***");

		Hex word = memory[i];

		if (word.compareTo(MAX_WORD) > 0 || word.compareTo(MIN_WORD) < 0) {
			fatalError("*** overflow occured ***");
		}
		return word;
	}

	//returns false if the accumulator has exceeded the max or min WORD_SIZE
	private boolean isAccumulatorValid() {
		if (accumulator.compareTo(MAX_WORD) > 0 || accumulator.compareTo(MIN_WORD) < 0)
			return false;
		return true;
	}

	private boolean isOperandValid() {
		int i = operand.toInt();
		if (i > MEMORY_SIZE-1 || i < 0)
			return false;
		return true;
	}

	private boolean instructionCounterValid() {
		int i = instructionCounter.toInt();
		if (i > MEMORY_SIZE-1 || i < 0)
			return false;
		return true;
	}

	public void executeProgram() {
		Scanner input = new Scanner(System.in);
		while (true) {
			//case when branch jumps the program out of bounds
			if (!instructionCounterValid())
				fatalError("*** program execution failed ***");

			instructionRegister.setValue(getWord(instructionCounter));
			operationCode.setValue(Hex.divide(instructionRegister, new Hex("+1000")));
			operand.setValue(Hex.mod(instructionRegister, new Hex("+1000")));

			if (!isAccumulatorValid())
				fatalError("*** Overflow occured ***");

			if (!isOperandValid())
				fatalError("*** operand index out of bounds ***");



			if (operationCode.equals(READ)) 
			{
				System.out.print("Enter an integer: ");
				storeWord(operand, new Hex(input.nextLong()));
			} 
			else if (operationCode.equals(WRITE)) 
			{
				System.out.print(getWord(operand).toLong());
			} 
			else if (operationCode.equals(NEWLINE)) 
			{
				System.out.println();
			} 
			else if (operationCode.equals(LOAD)) 
			{
				accumulator = getWord(operand);
			} 
			else if (operationCode.equals(STORE)) 
			{
				storeWord(operand, accumulator);
				accumulator.setValue(0);
			} 
			else if (operationCode.equals(ADD)) 
			{
				accumulator.setValue(Hex.add(accumulator, getWord(operand)));
			} 
			else if (operationCode.equals(SUBTRACT)) 
			{
				accumulator.setValue(Hex.subtract(accumulator, getWord(operand)));
			} 
			else if (operationCode.equals(DIVIDE)) 
			{
				if (getWord(operand).equals(new Hex(0))) //can't divide by zero
        			fatalError("*** attempt to divide by zero ***");
				accumulator.setValue(Hex.divide(accumulator, getWord(operand))); 
			} 
			else if (operationCode.equals(MULTIPLY)) 
			{
				accumulator.setValue(Hex.multiply(accumulator, getWord(operand))); 
			} 
			else if (operationCode.equals(REMAINDER)) 
			{
				if (getWord(operand).equals(new Hex(0))) //can't divide by zero
					fatalError("*** attempt to divide by zero ***");
				accumulator.setValue(Hex.mod(accumulator, getWord(operand))); 
			} 
			else if (operationCode.equals(POWER)) 
			{
				accumulator.setValue(Hex.exponent(accumulator, getWord(operand).toInt()));
			} /////////////modify this to work with longs
			else if (operationCode.equals(BRANCH)) 
			{
			  	instructionCounter.setValue(operand);
		    } 
		    else if (operationCode.equals(BRANCHNEG)) 
		    {
		    	if (accumulator.compareTo(new Hex(0)) < 0) 
					instructionCounter.setValue(operand);
				else
					instructionCounter.setValue(Hex.add(instructionCounter, new Hex(1)));
		    } 
		    else if (operationCode.equals(BRANCHZERO)) 
		    {
		    	if (accumulator.equals(new Hex(0))) 
					instructionCounter.setValue(operand);
				else
					instructionCounter.setValue(Hex.add(instructionCounter, new Hex(1)));
		    } 
		    else if (operationCode.equals(HALT)) 
		    {
		    	System.out.println("*** Simpletron execution terminated ***");
		    	return;
		    } 
		    else 
		    {
		    	fatalError("*** Invalid operation code ***");
		    }

		    if (operationCode.compareTo(BRANCH) < 0)
		    	instructionCounter.setValue(Hex.add(instructionCounter, new Hex(1)));

		}
	}


	//post: all of the variables are printed to the screen
	public void dumpMemory() {
		System.out.println("REGISTERS:");
		System.out.println("accumulator" + "          " + accumulator.getString(6));
		System.out.println("instructionCounter" + "   " + "  " + instructionCounter.getString(4));
		System.out.println("instructionRegister" + "  " + instructionRegister.getString(6));
		System.out.println("operationCode" + "        " + "   " + operationCode.getString(3));
		System.out.println("operand" + "              " + "  " +  operand.getString(4));
		System.out.println("\n" + "MEMORY:");
		System.out.print("   ");

		final int DIMEN = 10;
		for (int i = 0; i < DIMEN; i++) {
			System.out.print("      " + i);
		}
		System.out.println();

		for (int i = 0; i < DIMEN*DIMEN; i++) {
			if (i == 0)
				System.out.print("  0");
			else if (i < 10)
				System.out.print(" " + i + "0");
			else
				System.out.print(i + "0");
			for (int n = 0; n < DIMEN; n++) {
				System.out.print(" " + memory[i*DIMEN + n].getString(6));
			}
			System.out.println();
		}
	}
/*
	//post: returns word in +vwxyz or -vwxyz format
	private String formatWord(int word) {
		String s = Math.abs(word) + "";
		while (s.length() < ((MAX_WORD_SIZE+"").length()))
			s = "0" + s;
		if (word >= 0)
			s = "+" + s;
		else
			s = "-" + s;

		return s;
	}

    //post: returns an n digit string with preceding 0's if necessary
	private String formatCode(int code, int n) {
		String s = code + "";
		while (s.length() < n)
			s = "0" + s;
		return s;
	}
*/
	private void fatalError(String errorMessage) {
		System.out.println(errorMessage);
		System.out.println("*** Simpletron execution abnormally terminated ***");
		dumpMemory();
		System.exit(-1);
	}


	public static void main(String [] args) {
		
		final int SENTINAL = -999999;
		Scanner input = new Scanner(System.in);
		System.out.println("*** Welcome to Simpletron! ***");
		System.out.println("*** Please enter your program one instruction  ***");
		System.out.println("*** (or data word) at a time into the input    ***");
		System.out.println("*** text field. I will display the location    ***");
		System.out.println("*** number and a question mark (?). You then   ***");
		System.out.println("*** type the word for that location. Press the ***");
		System.out.println("*** Done button to stop entering your program. ***");

		int word = 0;
		int index = 0;
		Simpletron test = new Simpletron();

		while (true) {
			if (index < 10)
				System.out.print("0" + index + " ? ");
			else {
				System.out.print(index + " ? ");
			}
			word = input.nextInt();
			if (word == SENTINAL)
				break;
			test.storeWord(index++, word);
		}


		System.out.println("*** Program loading completed ***");
		System.out.println("*** Program execution begins  ***");
		test.executeProgram();
		test.dumpMemory();
		
		
	}


}