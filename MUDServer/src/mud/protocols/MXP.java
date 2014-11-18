package mud.protocols;

// http://www.gammon.com.au/mushclient/addingservermxp.htm

public final class MXP {
	/* strings */
	public static final String MXP_BEG = "\u0003"; /* < character */
	public static final String MXP_END = "\u0004"; /* > character */
	public static final String MXP_AMP = "\u0005"; /* & character */ 
	public static final String ESC = "\u001B";     /* esc character */
	
	/* characters */
	public static final char MXP_BEGc = '\u0003';
	public static final char MXP_ENDc = '\u0004';
	public static final char MXP_AMPc = '\u0005';
	
	/* construct an MXP tag with < and > around it */
	public static final String MXP_TAG(final String arg) {
		return MXP_BEG + arg + MXP_END;
	}
	
	
	public static String MXPMODE(final String arg) {
		return ESC + " \"[\" #" + arg + "z";
	}
	
	enum ItemState {
		eItemNothing, /* item is not readily accessible */
		eItemGet,     /* item on ground */
		eItemDrop,    /* item in inventory */
		eItemBid      /* auction item */
	};	
}