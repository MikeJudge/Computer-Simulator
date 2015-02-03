public class Hex {
	private String value;

	public Hex() {
		value = decToHex(0);
	}

	public Hex(long num) {
		value = decToHex(num);
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

	public void setValue(long num) {
		value = decToHex(num);
	}

	public void setValue(String num) {
		if (num.charAt(0) != '+' && num.charAt(0) != '-') {
			setValue(0);
			return;
		}

		for (int i = 1; i < num.length(); i++) {
			char c = num.charAt(i);
			if (!(c <= '9' && c >= '0') && !(c >= 'A' && c <= 'F')) {
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

	//returns String with size n, prepend zeros if n > length of this
	public String getString(int n) {
		if (n < length())
			return value;

		String zeros = "";
		for (int i = 0; i < (n - length()); i++) {
			zeros = zeros + "0";
		}

		return value.charAt(0) + zeros + value.substring(1);		
	}


	//post: string with hex representation of number
	private String decToHex(long num) {
		String s = Long.toHexString(Math.abs(num)).toUpperCase();

		if (num >= 0)
			s = "+" + s;
		else
			s = "-" + s;
		return s;
	}

	//post: converts hexadecimal string to signed long
	public long toLong() {
		long num = 0;
		int n = 0;
		char c;
		for (int i = length() - 1; i > 0; i--) {
			c = value.charAt(i);
			if (c >= '0' && c <= '9')
				num += (long)Math.pow(16, n) * (c - '0');
			else
				num += (long)Math.pow(16, n) * (c - 'A' + 10);
			n++;
		}
		if (value.charAt(0) == '-')
			num *= -1;

		return num;
	}

	public int toInt() {
		int num = 0;
		int n = 0;
		char c;
		for (int i = length() - 1; i > 0; i--) {
			c = value.charAt(i);
			if (c >= '0' && c <= '9')
				num += (int)Math.pow(16, n) * (c - '0');
			else
				num += (int)Math.pow(16, n) * (c - 'A' + 10);
			n++;
		}
		if (value.charAt(0) == '-')
			num *= -1;

		return num;
	}

	public static Hex add(Hex num1, Hex num2) {
		long n1 = num1.toLong();
		long n2 = num2.toLong();
		return new Hex(n1 + n2);
	}

	public static Hex subtract(Hex num1, Hex num2) {
		long n1 = num1.toLong();
		long n2 = num2.toLong();
		return new Hex(n1 - n2);
	}

	public static Hex multiply(Hex num1, Hex num2) {
		long n1 = num1.toLong();
		long n2 = num2.toLong();
		return new Hex(n1 * n2);
	}

	public static Hex divide(Hex num1, Hex num2) {
		long n1 = num1.toLong();
		long n2 = num2.toLong();
		return new Hex(n1 / n2);
	}

	public static Hex mod(Hex num1, Hex num2) {
		long n1 = num1.toLong();
		long n2 = num2.toLong();
		return new Hex(n1 % n2);
	}

	public static Hex exponent(Hex num1, int n) {
		long num = num1.toLong();
		return new Hex((long)Math.pow(num, n));

	}

	//returns +1 if this is greater
	//returns -1 if this is smaller
	//returns  0 if equal
	public int compareTo(Hex hex) {
		long num1 = toLong();
		long num2 = hex.toLong();

		if (num1 > num2)
			return 1;
		else if (num1 < num2)
			return -1;
		else
			return 0;

	}

	public boolean equals(Hex hex) {
		long num1 = toLong();
		long num2 = hex.toLong();

		return (num1 == num2);
	}



	public static void main(String [] args) {

		Hex test = new Hex("+A123");
		Hex test1 = new Hex("+1000");

		System.out.println(test.getString(6));
		System.out.println(test1.getString(6));

		System.out.println(divide(test,test1).getValue());


	}



}