/**************************************************************************
* This class runs machine level programs. The machine level programs      *
* are written as a list of five digit hex numbers with an operation code  *
* corresponding to the first two digits of a five digit hex number,       * 
* and the last three digits corresponding to an operand.                  *
**************************************************************************/
import java.util.Scanner;
import java.util.Arrays;
import java.io.FileReader;
import java.io.IOException;

public class Simpletron {
	private static final int MEMORY_SIZE = 1000;
	private static final Hex MAX_WORD    = new Hex("+FFFFF");
	private static final Hex MIN_WORD    = new Hex("-FFFFF");

	//operation code constants
	private static final Hex READ_INT           = new Hex(10);
	private static final Hex WRITE_INT          = new Hex(11);
	private static final Hex NEWLINE            = new Hex(12);
	private static final Hex READSTRING         = new Hex(13);
	private static final Hex WRITESTRING        = new Hex(14);
	private static final Hex READ_DOUBLE        = new Hex(15);
	private static final Hex WRITE_DOUBLE       = new Hex(16);

	private static final Hex LOAD               = new Hex(20);
	private static final Hex STORE              = new Hex(21);

    private static final Hex ADD_DOUBLE         = new Hex(24);
	private static final Hex SUBTRACT_DOUBLE    = new Hex(25);
	private static final Hex DIVIDE_DOUBLE      = new Hex(26);
	private static final Hex MULTIPLY_DOUBLE    = new Hex(27);
	private static final Hex REMAINDER_DOUBLE   = new Hex(28);
	private static final Hex POWER_DOUBLE       = new Hex(29);

	private static final Hex ADD_INT            = new Hex(30);
	private static final Hex SUBTRACT_INT       = new Hex(31);
	private static final Hex DIVIDE_INT         = new Hex(32);
	private static final Hex MULTIPLY_INT       = new Hex(33);
	private static final Hex REMAINDER_INT      = new Hex(34);
	private static final Hex POWER_INT          = new Hex(35);

	private static final Hex BRANCH             = new Hex(40);
	private static final Hex BRANCHNEG          = new Hex(41);
	private static final Hex BRANCHZERO         = new Hex(42);
	private static final Hex HALT               = new Hex(43);


	private Hex[] memory;		      //program is stored here
	private Hex accumulator;
	private Hex instructionCounter;   //location in memory whose instruction is being performed now

	private Hex operationCode;        //operation being currently performed, 1st two digits of instructionRegister
	private Hex operand;		      //memory location where operation is being operated on last three of instructionRegister
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


	//loads a machine level program into memory
	public void loadProgram(String fileName) {
		int index = 0;
		try {
			FileReader inFile = new FileReader(fileName);
			char c;
			StringBuilder s = new StringBuilder();
			while (inFile.ready()) {
				c = (char)inFile.read();
				if (c == '\n') {
					storeWord(index++, Integer.parseInt(s.toString()));
					s.delete(0, s.length());
				} else {
					s.append(c);
				}
			}
			if (s.length() > 0)
				storeWord(index++, Integer.parseInt(s.toString()));

			inFile.close();
		} catch (IOException io) {System.out.println("*** Error! Program not loaded ***");}
		
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

		memory[i].setValue(word);
	}

//this function is for storing words inputted in decimal format
	public void storeWord(int index, int word) {
		String part1, part2;
		part1 = new Hex(word / 1000).getString(3);
		part2 = new Hex(word % 1000).getString(4);

		storeWord(new Hex(index), new Hex(part1 + part2.substring(1)));
	}

	/*stores a string in memory. It does this by storing each charcter of the string as 
	  a half word represented in Hex. Each word in memory can hold two characters. The 
	  first index of the string in memory holds the length of the string followed by
	  string.length half words in succeeding indices.
	*/
	private void storeString(Hex index, String input) {
		//if length = 0 store the length of 0 with no characters
		if (input.length() == 0) {
			storeWord(index, new Hex("+00000"));
			return;
		}

		//store the length with the first character in the first index of the string
		storeWord(index, new Hex(new Hex(input.length()).getString(3) + "0" + toHalfWord(input.charAt(0))));

		int n = 1;
		int i = index.toInt() + 1;

		for (; n + 1 < input.length(); n+=2, i++) {
			storeWord(new Hex(i), new Hex("+" + toHalfWord(input.charAt(n))+ "0" + toHalfWord(input.charAt(n+1))));
		}

		//if the length is even, store the last character in its own word with no second half-word
		if (n < input.length())
			storeWord(new Hex(i), new Hex("+" + toHalfWord(input.charAt(n)) + "000"));
	}


	//returns a two digit unsigned hex representation of a character
	private String toHalfWord(char c) {
		return new Hex((int)c).getString(3).substring(1);
	}

    //prints a string represented by half words. the operand is the index
    //of the first data for the string. It contains two half words-
    //the length of the string and the first character of the string
    //if there is one. string.length half words follow in the following
    //indices.
	private void printString(Hex operand) {
		int [] arr = getHalfWords(operand);
		int length = arr[0];
		if (arr[0] == 0)
			return;

		System.out.print((char)arr[1]);

		int i = 1;
		for (int n = 1; n < length; n+=2) {
			arr = getHalfWords(Hex.add(operand, new Hex(i), Hex.INTEGER));
			System.out.print((char)arr[0]);
			if (arr[1] != 0)
				System.out.print((char)arr[1]);
			i++;
		}
	}

	private int[] getHalfWords(Hex operand) {
		Hex word = getWord(operand);
		int [] arr = new int[2];
		arr[0] = Hex.divide(word, new Hex("+1000"), Hex.INTEGER).toInt(); //first half word
		arr[1] = Hex.mod(word, new Hex("+100"), Hex.INTEGER).toInt();     //last half word
		return arr;
	}

	private Hex getWord(Hex index) {
		int i = index.toInt();
		if (i > (MEMORY_SIZE - 1) || i < 0)
			fatalError("*** index out of bounds ***");

		Hex word = new Hex(memory[i]);

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

	private boolean isInstructionCounterValid() {
		int i = instructionCounter.toInt();
		if (i > MEMORY_SIZE-1 || i < 0)
			return false;
		return true;
	}

	public void executeProgram() {
		Scanner input = new Scanner(System.in);
		while (true) {
			//case when branch instruction jumps the program out of bounds
			if (!isInstructionCounterValid())
				fatalError("*** program execution failed ***");

			instructionRegister.setValue(getWord(instructionCounter));
			operationCode.setValue(Hex.divide(instructionRegister, new Hex("+1000"), Hex.INTEGER));
			operand.setValue(Hex.mod(instructionRegister, new Hex("+1000"), Hex.INTEGER));

			if (!isAccumulatorValid())
				fatalError("*** Overflow occured ***");

			if (!isOperandValid())
				fatalError("*** operand index out of bounds ***");


			if (operationCode.equals(READ_INT)) 
			{
				System.out.print("Enter an integer: ");
				storeWord(operand, new Hex(input.nextInt()));
			} 
			else if (operationCode.equals(READ_DOUBLE)) 
			{
				System.out.print("Enter a float: ");
				storeWord(operand, new Hex(input.nextDouble()));
			}
			else if (operationCode.equals(WRITE_INT)) 
			{
				System.out.print(getWord(operand).toInt());
			} 
			else if (operationCode.equals(WRITE_DOUBLE)) 
			{
				System.out.print(getWord(operand).toDouble());
			} 
			else if (operationCode.equals(NEWLINE)) 
			{
				System.out.println();
			} 
			else if (operationCode.equals(READSTRING))
			{
				System.out.print("Enter a string: ");
				storeString(operand, input.next());
			}
			else if (operationCode.equals(WRITESTRING))
			{
				printString(operand);
			}


			else if (operationCode.equals(LOAD)) 
			{
				accumulator.setValue(getWord(operand));
			} 
			else if (operationCode.equals(STORE)) 
			{
				storeWord(operand, accumulator);
				accumulator.setValue(0);
			} 


			else if (operationCode.equals(ADD_INT)) 
			{
				accumulator.setValue(Hex.add(accumulator, getWord(operand), Hex.INTEGER));
			}
			else if (operationCode.equals(ADD_DOUBLE))
			{
				accumulator.setValue(Hex.add(accumulator, getWord(operand), Hex.DOUBLE));
			} 

			else if (operationCode.equals(SUBTRACT_INT)) 
			{
				accumulator.setValue(Hex.subtract(accumulator, getWord(operand), Hex.INTEGER));
			} 
			else if (operationCode.equals(SUBTRACT_DOUBLE)) 
			{
				accumulator.setValue(Hex.subtract(accumulator, getWord(operand), Hex.DOUBLE));
			} 

			else if (operationCode.equals(DIVIDE_INT)) 
			{
				if (getWord(operand).equals(new Hex(0))) //can't divide by zero
        			fatalError("*** attempt to divide by zero ***");
				accumulator.setValue(Hex.divide(accumulator, getWord(operand), Hex.INTEGER)); 
			} 
			else if (operationCode.equals(DIVIDE_DOUBLE)) 
			{
				if (getWord(operand).equals(new Hex(0))) //can't divide by zero
        			fatalError("*** attempt to divide by zero ***");
				accumulator.setValue(Hex.divide(accumulator, getWord(operand), Hex.DOUBLE)); 
			} 

			else if (operationCode.equals(MULTIPLY_INT)) 
			{
				accumulator.setValue(Hex.multiply(accumulator, getWord(operand), Hex.INTEGER)); 
			} 
			else if (operationCode.equals(MULTIPLY_DOUBLE)) 
			{
				accumulator.setValue(Hex.multiply(accumulator, getWord(operand), Hex.DOUBLE)); 
			} 

			else if (operationCode.equals(REMAINDER_INT)) 
			{
				if (getWord(operand).equals(new Hex(0))) //can't divide by zero
					fatalError("*** attempt to divide by zero ***");
				accumulator.setValue(Hex.mod(accumulator, getWord(operand), Hex.INTEGER)); 
			} 
			else if (operationCode.equals(REMAINDER_DOUBLE)) 
			{
				if (getWord(operand).equals(new Hex(0))) //can't divide by zero
					fatalError("*** attempt to divide by zero ***");
				accumulator.setValue(Hex.mod(accumulator, getWord(operand), Hex.DOUBLE)); 
			} 

			else if (operationCode.equals(POWER_INT)) 
			{
				accumulator.setValue(Hex.exponent(accumulator, getWord(operand), Hex.INTEGER));
			} 
			else if (operationCode.equals(POWER_DOUBLE)) 
			{
				accumulator.setValue(Hex.exponent(accumulator, getWord(operand), Hex.DOUBLE));
			} 


			else if (operationCode.equals(BRANCH)) 
			{
			  	instructionCounter.setValue(operand);
		    } 
		    else if (operationCode.equals(BRANCHNEG)) 
		    {
		    	if (accumulator.compareTo(new Hex(0)) < 0) 
					instructionCounter.setValue(operand);
				else
					instructionCounter.setValue(Hex.add(instructionCounter, new Hex(1), Hex.INTEGER));
		    } 
		    else if (operationCode.equals(BRANCHZERO)) 
		    {
		    	if (accumulator.equals(new Hex(0))) 
					instructionCounter.setValue(operand);
				else
					instructionCounter.setValue(Hex.add(instructionCounter, new Hex(1), Hex.INTEGER));
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
		    	instructionCounter.setValue(Hex.add(instructionCounter, new Hex(1), Hex.INTEGER));

		}
	}


	//post: all of the variables are printed to the screen
	public void dumpMemory() {
		System.out.println("REGISTERS:");
		System.out.println("accumulator" + "          " + accumulator.getString(6));
		System.out.println("instructionCounter" + "   " + "   " + instructionCounter.getString(4).substring(1));
		System.out.println("instructionRegister" + "  " + instructionRegister.getString(6));
		System.out.println("operationCode" + "        " + "    " + operationCode.getString(3).substring(1));
		System.out.println("operand" + "              " + "   " +  operand.getString(4).substring(1));
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

	private void fatalError(String errorMessage) {
		System.out.println(errorMessage);
		System.out.println("*** Simpletron execution abnormally terminated ***");
		dumpMemory();
		System.exit(-1);
	}


	public static void main(String [] args) {
	/*	
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
		*/
		Simpletron test = new Simpletron();
		test.loadProgram("program1NC.txt");
		test.executeProgram();
		test.dumpMemory();
		
	}


}