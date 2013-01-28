package mud.protocols;

/**
 * MSP (Mud Sound Protocol) Class
 * 
 * This class partially implements MSP, and supplies static methods to call to
 * generate msp messages.
 * 
 * ---
 * 
 * Example:
 * 
 * msp.play("action.wav", "sound");
 * String msg = msp.generate();
 * send(msg);
 * 
 * All you need to do as shown above, is 1) specify (at minimum) the sound filename and whether it ought to
 * be looped continuously (music), or played once (sound), 2) capture the string from the generate() method
 * and  3) send it to the client.
 * 
 * NOTE: This class's methods are all static and called statically, with this "class" imported. An MSP
 * object is NEVER instantiated. If you modify the server to have a static method to send data to the client,
 * you could conceivably call that in the send() method and always send the music/sound by invoking MSP
 * yet again like this:
 * 
 * MSP.send()
 * 
 * in lieu of the way it is done in the example case, generating the message from the
 * server and sending it "yourself".
 *
 * ---
 * Notes:
 * - This is a *partial* and *incomplete* implementation. It was made simply for the sake of it.
 * - A few things in particular are not supplied or implemented here, such as the the telnet negotiation for MSP. (too complex for me to understand)
 * ---
 * 
 * CREDITS:
 * 	MSP Design Specifications can be found on the web at: http://www.zuggsoft.com/zmud/msp.htm
 * 
 * @author Jeremy N. Harton
 *
 */
public class MSP {
	static enum Type{ MUSIC, SOUND };

	static public String fileName;           /* name of the file to be played [default is the empty string (none)] */
	static public String fileType;           /* type extensions of the file - unused and not specified in protocol [default is the empty string (none)] */
	static int volume = 25;           /* desired volume for the sound to be played [default is 25] */
	static int repeats = 1;           /* desired volume for the sound to be played [default is 25] */
	static int priority = 1;          /* number of times to repeat [default is 1] */
	static int cont = 0;              /* specifies behavior if a duplicate request is sent for a sound which is still playing [default is 0] */
	static Type cType = Type.SOUND;   /* the type - [default is SOUND] */
	static String url = "";           /* a url to download the sound from if the client doesn't have it [default is the empty string (none)] */
	
	/* needs name checking to account for invalid names */
	public static void play(String name) {
		fileName = name;
		fileType = "";
	}

	public static void play(String tFileName, String tType) {
		MSP.play(tFileName);
		if (tType.toLowerCase().equals("music")) { cType = Type.MUSIC; }
		else if (tType.toLowerCase().equals("sound")) { cType = Type.SOUND; }
	}
	
	public static void play(String tFileName, String tType, int tVolume) {
		volume = tVolume;
		MSP.play(tFileName, tType);
	}

	public static void play(String tFileName, String tType, int tVolume, int tRep) {
		repeats = tRep;
		MSP.play(tFileName, tType, tVolume);
	}

	public static void play(String tFileName, String tType, int tVolume, int tRep, int tPriority) {
		priority = tPriority;
		MSP.play(tFileName, tType, tVolume, tRep);
	}

	public static void play(String tFileName, String tType, int tVolume, int tRep, int tPriority, int tCont) {
		cont = tCont;
		MSP.play(tFileName, tType, tVolume, tRep, tPriority);
	}

	public static void play(String tFileName, String tType, int tVolume, int tRep, int tPriority, int tCont, String tURL) {
		url = tURL;
		MSP.play(tFileName, tType, tVolume, tRep, tPriority, tCont);
	}
	
	/**
	 * Resets values to default, because since the class is static,
	 * they are modified for each piece of SOUND/MUSIC played
	 */
	public static void reset() {
		fileName = "";        /* name of the file to be played [default is the empty string (none)]*/
		fileType = "";        /* type extensions of the file - unused and not specified in protocol [default is the empty string (none)]  */
		volume = 25;          /* desired volume for the sound to be played [default is 25] */
		repeats = 1;          /* number of times to repeat [default is 1] */
		priority = 1;         /* priority of the sound - used to determine if it replaces the current sounds, plays concurrently, etc [default is 1] */
		cont = 0;             /* specifies behavior if a duplicate request is sent for a sound which is still playing [default is 0] */
		cType = Type.SOUND;   /* the type - [default is SOUND] */
		url = "";             /* a url to download the sound from if the client doesn't have it [default is the empty string (none)] */
	}

	public static void stop() {
	}
	
	/**
	 * Uses the reserved filename 'Off' in it's intended manner to stop
	 * all sound/music playing
	 */
	public static void stopAll() {
		MSP.play("Off");
	}
	
	/**
	 * Generates and returns an MSP formatted string to send to the client.
	 * 
	 * @return String The MSP formatted string to send to the client.
	 */
	public static String generate() {
		String msg = "";
		
		switch(cType) {
		case MUSIC:
			if ( fileName.equals("Off") ) { msg = "!!MUSIC(Off)"; }
			else {
				msg = "!!MUSIC(" + fileName + " V=" + volume + " L=" + repeats + " P=" + priority + ")";
			}
			System.out.println(msg);
			break;
		case SOUND:
			if ( fileName.equals("Off") ) { msg = "!!MUSIC(Off)"; }
			else {
				msg = "!!SOUND(" + fileName + " V=" + volume + " L=" + repeats + " P=" + priority + ")";
			}
			System.out.println(msg);
			break;
		default:
			msg = "";
			break;
		}
		
		return msg;
	}
}