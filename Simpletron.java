/**************************************************************************
* This class runs machine level programs. The machine level programs      *
* are written as a list of five digit numbers with an operation code     *
* corresponding to the first two digits of a five digit decimal number,   * 
* and the last three digits corresponding to an operand.                    *
**************************************************************************/
import java.util.Scanner;
import java.util.Arrays;

public class Simpletron {
	private static final int MEMORY_SIZE    = 1000;
	private static final int MAX_WORD_SIZE  = 99999;
	private static final int MIN_WORD_SIZE  = -99999;
	private static final int MAX_HEX_LENGTH = Integer.toHexString(MAX_WORD_SIZE).length();

	//operation code constants
	private static final int READ       = 10;
	private static final int WRITE      = 11;
	private static final int NEWLINE    = 12;

	private static final int LOAD  = 20;
	private static final int STORE = 21;

	private static final int ADD       = 30;
	private static final int SUBTRACT  = 31;
	private static final int DIVIDE    = 32;
	private static final int MULTIPLY  = 33;
	private static final int REMAINDER = 34;
	private static final int POWER     = 35;

	private static final int BRANCH     = 40;
	private static final int BRANCHNEG  = 41;
	private static final int BRANCHZERO = 42;
	private static final int HALT       = 43;


	private String[] memory;		 //program is stored here
	private int accumulator;
	private int instructionCounter;  //location in memory whose instruction is being performed now

	private int operationCode;       //operation being currently performed, 1st two numbers of instructionRegister
	private int operand;		     //memory location where operation is being operated on last three of instructionRegister
	private int instructionRegister; //full instruction word


	public Simpletron() {
		memory = new String[MEMORY_SIZE];
		Arrays.fill(memory, intToHex(0));
		accumulator 	    = 0;
		instructionCounter  = 0;
		operationCode       = 0;
		operand             = 0;
		instructionRegister = 0;
	}

	//pre:  index, and word are in range
	//post: word is stored in memory
	public void storeWord(int index, int word) {
		if (word > MAX_WORD_SIZE || word < MIN_WORD_SIZE) {
			fatalError("*** overflow occured ***");
		}
		if (index > (MEMORY_SIZE-1) || index < 0) {
			fatalError("*** index out of bounds ***");
		}

		memory[index] = intToHex(word);
	}

	//post: string with hex representation of number
	private String intToHex(int num) {
		String s = Integer.toHexString(num).toUpperCase();
		while (s.length() < MAX_HEX_LENGTH)
			s = "0" + s;
		if (num >= 0)
			s = "+" + s;
		else
			s = "-" + s;
		return s;
	}

	private int hexToInt(String hexString) {
		int num = 0;
		int n = 0;
		char c;
		for (int i = hexString.length() - 1; i > 0; i--) {
			c = hexString.charAt(i);
			if (c >= '0' && c <= '9')
				num += (int)Math.pow(16, n) * (c - '0');
			else
				num += (int)Math.pow(16, n) * (c - 'A' + 10);
			n++;
		}
		if (hexString.charAt(0) == '-')
			num *= -1;

		return num;

	}

	//returns false if the accumulator has overflowed the max or min WORD_SIZE
	private boolean isAccumulatorValid() {
		if (accumulator > MAX_WORD_SIZE || accumulator < MIN_WORD_SIZE)
			return false;
		return true;
	}

	public void executeProgram() {
		Scanner input = new Scanner(System.in);
		while (true) {
			//case when branch jumps the program out of bounds
			if (instructionCounter >= MEMORY_SIZE || instructionCounter < 0)
				fatalError("*** program execution failed ***");

			instructionRegister = hexToInt(memory[instructionCounter]);
			operationCode = instructionRegister / 1000;
			operand = instructionRegister % 1000;

			if (!isAccumulatorValid())
				fatalError("*** Overflow occured ***");

			if (operand >= MEMORY_SIZE || operand < 0)
				fatalError("*** operand index out of bounds ***");

			switch (operationCode) {
				//condense code branch, and branchneg are the only ops that don't instructioncounter++
				case READ:        System.out.print("Enter an integer: ");
							      storeWord(operand, input.nextInt());
							      instructionCounter++;
							      break;
				case WRITE:       System.out.print(hexToInt(memory[operand]));
							      instructionCounter++;
							      break;
			    case NEWLINE:     System.out.println();
			                      instructionCounter++;
			                      break;
				case LOAD:        accumulator = hexToInt(memory[operand]);
							      instructionCounter++;
							      break;
				case STORE:       storeWord(operand, accumulator);
							      instructionCounter++;
							      accumulator = 0;
							      break;
				case ADD:         accumulator += hexToInt(memory[operand]);
								  instructionCounter++;
								  break;
				case SUBTRACT:    accumulator -= hexToInt(memory[operand]);
								  instructionCounter++;
								  break;
				case DIVIDE:      if (hexToInt(memory[operand]) == 0) //can't divide by zero
        					      	 fatalError("*** attempt to divide by zero ***");
								  accumulator /= hexToInt(memory[operand]);     
						          instructionCounter++;
						          break;
				case MULTIPLY:    accumulator *= hexToInt(memory[operand]);
								  instructionCounter++;
								  break;
				case REMAINDER:   if (hexToInt(memory[operand]) == 0) //can't divide by zero
									 fatalError("*** attempt to divide by zero ***");
								  accumulator %= hexToInt(memory[operand]);
								  instructionCounter++;
								  break;
				case POWER:       accumulator = (int)Math.pow(accumulator,hexToInt(memory[operand]));
								  instructionCounter++;
								  break;
				case BRANCH:      instructionCounter = operand;
							      break;
				case BRANCHNEG:   if (accumulator < 0) 
									  instructionCounter = operand;
							      else
							      	  instructionCounter++;
				                  break;
				case BRANCHZERO:  if (accumulator == 0) 
									  instructionCounter = operand;
								  else
								  	  instructionCounter++;
								  break;
				case HALT:        System.out.println("*** Simpletron execution terminated ***");
							      return;
				//invalid operation code
				default:		  fatalError("*** Invalid operation code ***");

			}

		}
	}


	//post: all of the variables are printed to the screen
	public void dumpMemory() {
		System.out.println("REGISTERS:");
		System.out.println("accumulator" + "          " + formatWord(accumulator));
		System.out.println("instructionCounter" + "   " + "   " + formatCode(instructionCounter, 3));
		System.out.println("instructionRegister" + "  " + formatWord(instructionRegister));
		System.out.println("operationCode" + "        " + "    " + formatCode(operationCode, 2));
		System.out.println("operand" + "              " + "   " + formatCode(operand, 3));
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
				System.out.print(" " + memory[i*DIMEN + n]);
			}
			System.out.println();
		}
	}

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