public class Hex {
	public static final int INTEGER = 1;
	public static final int DOUBLE  = 2;

	private String value;

	public Hex() {
		value = intToHex(0);
	}

	public Hex(long num) {
		value = intToHex(num);
	}

	public Hex(double num) {
		value = doubleToHex(num);
	}

	public Hex(String num) {
		setValue(num);
	}

	public Hex(Hex hex) {
		setValue(hex.getValue());
	}


	public String getValue() {
		return value;
	}

		//returns String with size n, prepend zeros if n > length of this
	public String getString(int n) {
		if (n < length()) 
			return value.substring(0, n);

		String zeros = "";
		for (int i = 0; i < (n - length()); i++) {
			zeros = zeros + "0";
		}

		return value.charAt(0) + zeros + value.substring(1);		
	}

	public void setValue(long num) {
		value = intToHex(num);
	}

	public void setValue(double num) {
		value = doubleToHex(num);
	}

	//pre: num.charAt(0) must be a sign, and all other characters must be
	//     in the range of a hex number
	//post: if not valid input value = 0 otherwise value = num
	public void setValue(String num) {
		if (num.charAt(0) != '+' && num.charAt(0) != '-') {
			setValue(0);
			return;
		}
		int dotCount = 0;

		for (int i = 1; i < num.length(); i++) {
			char c = num.charAt(i);
			//checks for the case if there is more than one decimal point
			if (c == '.' && (++dotCount) > 1) {
				setValue(0);
				return;
			}
				
			if (!(c <= '9' && c >= '0') && !(c >= 'A' && c <= 'F') && (c != '.')) {
				setValue(0);
				return;
			}
		}
		value = num;
	}


	public void setValue(Hex hex) {
		value = hex.getValue();
	}

	public int length() {
		return value.length();
	}


	//post: string with hex representation of number
	private String intToHex(long num) {
		String s = Long.toHexString(Math.abs(num)).toUpperCase();

		if (num >= 0)
			s = "+" + s;
		else
			s = "-" + s;
		return s;
	}


	private String doubleToHex(double num) {
		//creates the hex for the integer part of number
		String s = Integer.toHexString(Math.abs((int)num)).toUpperCase();
		s = s + ".";

		if (num >= 0)
			s = "+" + s;
		else
			s = "-" + s;

		num = Math.abs(num); //to avoid unwanted - signs being added to the Hex value
		int hexDigit;
		for (int i = 0; i < 20; i++) {
			num = num -(int)num;  //remove the integer portion
			num *= 16.0;          //create new hex digit in the int portion
			hexDigit = (int)num % 100;  //rip the new hex digit
			//add the hex digit to the hex-string
			if (hexDigit < 10)
				s = s + hexDigit;
			else {
				s = s + ((char)('A' + hexDigit-10));
			}
		}
		return s;
	}



	//post: converts hexadecimal string to signed long
	public long toLong() {
		int index = value.indexOf('.');
		if (index != -1)
			return Long.parseLong(value.substring(0, index), 16);

		return Long.parseLong(value, 16);
	}

	public int toInt() {
		int index = value.indexOf('.');
		if (index != -1)
			return Integer.parseInt(value.substring(0, index), 16);

		return Integer.parseInt(value, 16);
	}

	public double toDouble() {
		int i = value.indexOf('.');
		if (i == -1)
			return (double)toInt();

		double num = Integer.parseInt(value.substring(1, i));
		//gets the integer portion from the hex value into the num and ignore sign
		//reason: function will add the unsigned representation of value after decimal
		//        point, and get the final signed representation in the end
		
		char c;
		int n = -1;
		i++;
		//algorithm to translate the hex-value into a floating-point num
		for (; i < length(); i++) {
			c = value.charAt(i);
			if (c <= '9' && c >= '0')
				num = num + Math.pow(16, n) * (c - '0');
			else
				num = num + Math.pow(16, n) * (c - 'A' + 10);
			n--;
		}

		if (value.charAt(0) == '-') {
			num  *= -1;
		}
		return num;
	}

	public static Hex add(Hex num1, Hex num2, int opCode) {
		double n1 = num1.toDouble();
		double n2 = num2.toDouble();

		if (opCode == INTEGER)
			return new Hex((int)(n1 + n2));
		else
			return new Hex(n1 + n2);
	}

	public static Hex subtract(Hex num1, Hex num2, int opCode) {
		double n1 = num1.toDouble();
		double n2 = num2.toDouble();

		if (opCode == INTEGER)
			return new Hex((int)(n1 - n2));
		else
			return new Hex(n1 - n2);
	}

	public static Hex multiply(Hex num1, Hex num2, int opCode) {
		double n1 = num1.toDouble();
		double n2 = num2.toDouble();

		if (opCode == INTEGER)
			return new Hex((int)(n1 * n2));
		else
			return new Hex(n1 * n2);
	}

	public static Hex divide(Hex num1, Hex num2, int opCode) {
		double n1 = num1.toDouble();
		double n2 = num2.toDouble();

		if (opCode == INTEGER)
			return new Hex((int)(n1 / n2));
		else
			return new Hex(n1 / n2);
	}

	public static Hex mod(Hex num1, Hex num2, int opCode) {
		double n1 = num1.toDouble();
		double n2 = num2.toDouble();

		if (opCode == INTEGER)
			return new Hex((int)(n1 % n2));
		else
			return new Hex(n1 % n2);
	}

	public static Hex exponent(Hex num1, Hex num2, int opCode) {
		double n1 = num1.toDouble();
		double n2 = num2.toDouble();

		if (opCode == INTEGER)
			return new Hex((int)(Math.pow(n1, n2)));
		else
			return new Hex(Math.pow(n1, n2));
	}

	//returns +1 if this is greater
	//returns -1 if this is smaller
	//returns  0 if equal
	public int compareTo(Hex hex) {
		double num1 = toDouble();
		double num2 = hex.toDouble();

		if (num1 > num2)
			return 1;
		else if (num1 < num2)
			return -1;
		else
			return 0;

	}

	public boolean equals(Hex hex) {
		return this.compareTo(hex) == 0;
	}



	public static void main(String [] args) {
		Hex test = new Hex(-4.5);
		Hex test2 = new Hex(5);

		System.out.println(test.getValue());
		System.out.println(test2.getValue());
		System.out.println(Hex.add(test,test2, Hex.DOUBLE).toDouble());
		//System.out.println(Math.PI + Math.PI);



	}



}