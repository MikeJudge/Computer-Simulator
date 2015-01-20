import java.util.Scanner;

public class Simpletron {
//operation code constants
	private static final int READ  = 10;
	private static final int WRITE = 11;

	private static final int LOAD  = 20;
	private static final int STORE = 21;

	private static final int ADD      = 30;
	private static final int SUBTRACT = 31;
	private static final int DIVIDE   = 32;
	private static final int MULTIPLY = 33;

	private static final int BRANCH     = 40;
	private static final int BRANCHNEG  = 41;
	private static final int BRANCHZERO = 42;
	private static final int HALT       = 43;


	private int[] memory;
	private int accumulator;
	private int instructionCounter;  //location in memory whose instruction is being performed now

	private int operationCode;       //operation being currently performed, 1st two numbers of instructionRegister
	private int operand;		     //memory location where operation is being operated on 2nd two numbers of instructionRegister
	private int instructionRegister; //full instruction word


	public Simpletron() {
		memory = new int[100];
		accumulator = 0;
		instructionCounter = 0;
		operationCode = 0;
		operand = 0;
		instructionRegister = 0;
	}

	//pre:  index, and word are in range
	//post: word is stored in memory
	public boolean storeWord(int index, int word) {
		if (word > 9999 || word < -9999) {
			System.out.println("invalid word");
			return false;
		}
		if (index > 99 || index < 0) {
			System.out.println("index not in range");
			return false;
		}

		memory[index] = word;
		return true;
	}

	public void executeProgram() {
		Scanner input = new Scanner(System.in);

		while (true) {
			instructionRegister = memory[instructionCounter];
			operationCode = instructionRegister / 100;
			operand = instructionRegister % 100;

			switch (operationCode) {
				//condense code branch, and branchneg are the only ops that don't instructioncounter++
				case READ:        System.out.print("Enter an integer: ");
							      memory[operand] = input.nextInt();
							      instructionCounter++;
							      break;
				case WRITE:       System.out.println(memory[operand]);
							      instructionCounter++;
							      break;
				case LOAD:        accumulator = memory[operand];
							      instructionCounter++;
							      break;
				case STORE:       memory[operand] = accumulator;
							      instructionCounter++;
							      accumulator = 0;
							      break;
				case ADD:         accumulator += memory[operand];
								  instructionCounter++;
								  break;
				case SUBTRACT:    accumulator -= memory[operand];
								  instructionCounter++;
								  break;
				case DIVIDE:      accumulator /= memory[operand];     ///error checking here
						          instructionCounter++;
						          break;
				case MULTIPLY:    accumulator *= memory[operand];
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
				case HALT:      System.out.println("*** Simpletron execution terminated ***");
							    return;

			}

		}
	}

	//post: all of the variables are printed off to the screen
	public void dumpMemory() {
		System.out.println("REGISTERS:");
		System.out.println("accumulator" + "          " + formatWord(accumulator));
		System.out.println("instructionCounter" + "   " + instructionCounter);
		System.out.println("instructionRegister" + "  " + formatWord(instructionRegister));
		System.out.println("operationCode" + "        " + operationCode);
		System.out.println("operand" + "              " + operand);
		System.out.println("\n" + "MEMORY:");
		System.out.print("  ");

		final int DIMEN = 10;
		for (int i = 0; i < DIMEN; i++) {
			System.out.print("     " + i);
		}
		System.out.println();

		for (int i = 0; i < DIMEN; i++) {
			if (i == 0)
				System.out.print(" 0");
			else
				System.out.print(i + "0");
			for (int n = 0; n < DIMEN; n++) {
				System.out.print(" " + formatWord(memory[i*DIMEN + n]));
			}
			System.out.println();
		}
	}

	//post: returns word in +wxyz or -wxyz format
	private String formatWord(int word) {
		String s = word + "";
		while (s.length() < 4)
			s = "0" + s;
		if (word >= 0)
			s = "+" + s;
		else
			s = "-" + s;

		return s;
	}


	public static void main(String [] args) {
		final int SENTINAL = -99999;
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
			if (test.storeWord(index, word)) 
				index++;
		}

		System.out.println("*** Program loading completed ***");
		System.out.println("*** Program execution begins  ***");
		test.dumpMemory();
		System.out.println();
		test.executeProgram();
		
		
	}


}