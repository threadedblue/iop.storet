package mtf;

import ec.util.MersenneTwisterFast;

public class MTF {
	
	private static MersenneTwisterFast mtf;
	
	public static boolean getNextAsBoolean() {
		return getMtf().nextBoolean();
	}
	
	public static boolean getNextAsBoolean(double probability) {
		return getMtf().nextBoolean(probability);
	}
	
	public static boolean getNextAsBoolean(float probability) {
		return getMtf().nextBoolean(probability);
	}
	
	public static double getNextAsDouble(){
		return getMtf().nextDouble();
	}

	public static int getNextAsInteger() {
		return getMtf().nextInt();
	}

	public static int getNextAsInteger(int range) {
		return getMtf().nextInt(range);
	}
	
	private static MersenneTwisterFast getMtf() {
		if (mtf == null) {
			mtf = new MersenneTwisterFast();
		}
		return mtf;
	}
}