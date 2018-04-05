package rabinizer.deleteOld;


/**
 * Global state & pervasive methods.
 * 
 * @author Ruslan Ledesma-Garza
 *
 */
public class Misc {
	
	public static boolean verbose;
	
	public static void verboseln(String str) {
		if(verbose) { System.out.println(str); }
	}
	
	public static void verbose(String str) {
		if(verbose) { System.out.print(str); }
	}
	
}
