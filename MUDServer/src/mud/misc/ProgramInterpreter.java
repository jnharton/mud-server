package mud.misc;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import mud.MUDObject;
import mud.MUDServer;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.Player;
import mud.utils.Log;
import mud.utils.Message;
import mud.utils.Point;
import mud.utils.Utils;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * 
 * @author Jeremy
 *
 */
public class ProgramInterpreter {
	private static final String TRUE = ":true";
	private static final String FALSE = ":false";
	
	private static final String ERROR = ":error";
	
	private final MUDServer parent;
	private Log log;
	
	//
	private Hashtable<String, String> vars;
	
	// configuration
	private boolean use_vars;
	private boolean debug_enabled;
	
	public ProgramInterpreter(final MUDServer parent) {
		this(parent, false);
	}
	
	public ProgramInterpreter(final MUDServer parent, final boolean enable_debug) {
		this.parent = parent;
		this.vars = new Hashtable<String, String>();
		this.use_vars = true;
		
		this.debug_enabled = enable_debug;
	}

	public List<String> lex(String input) {
		List<String> tokens = new LinkedList<String>();

		Character ch;
		StringBuilder sb = new StringBuilder();

		for(int c = 0; c < input.length(); c++) {
			ch = input.charAt(c);

			switch(ch) {
			case '{':
				if(sb.length() > 0) {
					tokens.add(sb.toString());
					sb.delete(0, sb.length());
				}
				tokens.add("" + ch);
				break;
			case '}':
				if(sb.length() > 0) {
					tokens.add(sb.toString());
					sb.delete(0, sb.length());
				}
				tokens.add("" + ch);
				break;
			case '(':
				if(sb.length() > 0) {
					tokens.add(sb.toString());
					sb.delete(0, sb.length());
				}
				tokens.add("" + ch);
				break;
			case ')':
				if(sb.length() > 0) {
					tokens.add(sb.toString());
					sb.delete(0, sb.length());
				}
				tokens.add("" + ch);
				break;
			case ':':
				if(sb.length() > 0) {
					tokens.add(sb.toString());
					sb.delete(0, sb.length());
				}
				tokens.add("" + ch);
				break;
			case ',':
				if(sb.length() > 0) {
					tokens.add(sb.toString());
					sb.delete(0, sb.length());
				}
				tokens.add("" + ch);
				break;
			default:
				sb.append(ch);
				break;
			}
		}

		if( debug_enabled ) System.out.println("Tokens: " + tokens);

		return tokens;
	}

	/*public String interpret(final Script script, final Player player) {
		return interpret( script.getText(), player, null);
	}

	public String interpret(final String script, final Player player) {
		return interpret( script, player, null );
	}*/

	public String interpret(final Script script, final Player player, final MUDObject object) {
		return interpret( script.getText(), player, object );
	}

	/**
	 * Function to evaluate a script/program
	 * 
	 * @param script
	 * @return
	 */
	public String interpret(final String script, final Player player, final MUDObject object) {	
		//System.out.println("PGM: <" + script + ">");
		//System.out.println("pArg: " + script);

		if ( isValidScript( script ) ) {
			if( debug_enabled ) System.out.println("Interpret: " + script);
			
			if( script.indexOf(":") != -1 ) {
				String work = script.substring(1, script.length() - 1); // strip off the outermost squiggly braces ( {} )
				//String work = script.replace("{", "").replace("}", "");

				if( debug_enabled ) System.out.println("work: " + work);

				String[] temp = work.split(":", 2);

				String functionName = temp[0];
				List<String> params = null;

				if( debug_enabled ) System.out.println("Function: " + functionName); // tell us the script function used

				if( temp.length > 1 ) {
					params = Utils.mkList(temp[1].split(",")); // split the arguments on commas ( , )

					fixParams( params ); // sort of fixes the params

					if( debug_enabled ) System.out.println("Params: " + params);

					int index = 0;

					// whenever the function called isn't 'if' or 'with' or 'do', we want to evaluate all parameters as we get them
					if( !functionName.equals("if") && !functionName.equals("with") && !functionName.equals("do") ) {
						for(final String param : params) {
							// evaluate parameters if they are valid sub scripts							
							if( isValidScript( param ) ) {
								params.set(index, interpret(param, player, object));
							}
							
							index++;
						}
					}

					if( debug_enabled ) {
						System.out.println("EVALUATE");
						System.out.println("Evaluate: <" + functionName + "> with " + params);
					}
					
					String result = evaluate(functionName, params.toArray(new String[params.size()]), player, object);

					if( debug_enabled ) System.out.println("Result: " + result);

					return result;
				}
				else { return "Incomplete function statement, no parameters!"; }
			}
			else {
				return evaluate(script, new String[0], player, object);
			}
		}

		return "Invalid Script!" + "\n'" + script + "\'";
	}

	private String evaluate(final String functionName, final String[] params, final Player player, final MUDObject object) {
		if( debug_enabled ) System.out.println("Params: " + params.length);

		if( params.length > 0 ) {
			/*
			 * TODO: resolve this kludge and figure out a way to ensure that each
			 * function doesn't have to worry about receiving the correct number
			 * of parameters.
			 */
			// this a kludge, since a do function call may contain 1 or more parameters/sub scripts.
			if ( functionName.equals("do") ) {
				// {do:script1, script2, ...}
				
				//String temp;

				for(final String param : params) {
					if( debug_enabled ) System.out.println("(DO) INTERPRET: " + param);
					
					//temp = interpret(param, player, object);
					interpret(param, player, object);
					
					//if( debug_enabled ) System.out.println("(DO) Result: " + temp);
				}

				return "";
			}

			if( params.length == 1 ) {
				if( debug_enabled ) System.out.println("Parameter (1): " + params[0]);

				if(functionName.equals("create_item")) {
					// {create_item:identifier}
					
					final Item item = parent.createItem(params[0], true);

					if( item != null ) {
						return "" + item.getDBRef();
					}
					else {
						return "-1";
					}
				}
				else if (functionName.equals("dbref")) {
					// {dbref:object}
					
					MUDObject mobj = parent.getObject(params[0]);
					
					if( mobj != null ) {
						return "" + mobj.getDBRef();
					}
					else return "" + -1;
					//return "" + parent.getObject(params[0]).getDBRef();
				}
				else if (functionName.equals("rainbow")) {
					if( debug_enabled ) System.out.println(params[0]);
					return parent.rainbow(params[0]) + parent.colorCode("white");
				}
				else { return "PGM: No such function!"; }
				//else { return "Incomplete function statement, no parameters!"; }
				//else { return "PGM: Error!"; }
			}
			else if( params.length == 2 ) {
				if( debug_enabled ) {
					System.out.println("Parameter (1): " + params[0]);
					System.out.println("Parameter (2): " + params[1]);
				}

				Integer first = null;
				Integer second = null;

				boolean failNumParse = false;

				// cover some stuff for certain parts here
				if( Utils.mkList("add", "sub", "mul", "and", "eq", "lt", "le", "gt", "ge").contains(functionName) ) {
					if( params[0].startsWith("{") && params[0].endsWith(")") ) {
						params[0] = interpret(params[0], player, object);
					}

					if( params[1].startsWith("{") && params[1].endsWith(")") ) {
						params[1] = interpret(params[1], player, object);
					}
					
					// try to pre-evaluate parameters here for functions which are basically math (trigger for comparing string equivalent)
					if( !functionName.equals("and") ) {
						try {
							first = Integer.parseInt(params[0]);
							second = Integer.parseInt(params[1]);
						}
						catch(NumberFormatException nfe) {
							if( debug_enabled ) System.out.println("-- Stack Trace --");
							nfe.printStackTrace();

							failNumParse = true;
						}

						//System.out.println("first: " + first);
						//System.out.println("Second: " + second);
					}
				}

				if( functionName.equals("colors") ) {
					// {colors:color, string}
					
					if (params.length >= 2) {
						// TODO consider how debug messages will be transmitted
						parent.debug("Color: " + params[0]);
						parent.debug("Text: " + params[1]);

						return parent.colorCode(params[0]) + params[1] + parent.colorCode("white");
					}
					else { return "PGM: Error!"; }
				}
				else if( functionName.equals("cmp") ) {
					// {cmp:string1, string2}
					
					if( params[0].equals(params[1]) ) {
						return TRUE;
					}
					else {
						return FALSE;
					}
				}
				else if( functionName.equals("add") ) {
					// {add:first, second}
					
					if( failNumParse ) return "-1";

					return "" + (first + second);
				}
				else if( functionName.equals("sub") ) {
					// {sub:first, second}
					
					if( failNumParse ) return "-1";

					return "" + (first - second);
				}
				else if( functionName.equals("and") ) {
					// {and:a, b}
					
					if( params[0].equals(TRUE) && params[1].equals(TRUE) ) {
						return TRUE;
					}
					else {
						return FALSE;
					}
				}
				else if( functionName.equals("eq") ) {
					// {eq:first, second}
					
					if( params[0].equals(params[1]) ) {
						return TRUE;
					}
					else {	
						if( failNumParse )   return FALSE;

						if (first == second) return TRUE;
						else                 return FALSE;
					}
				}
				else if( functionName.equals("lt") ) {
					// {lt:first, second}
					
					if( failNumParse )   return FALSE;

					if (first < second)  return TRUE;
					else                 return FALSE;
				}
				else if( functionName.equals("le") ) {
					// {le:first, second}
					
					if( failNumParse )   return FALSE;

					if (first <= second) return TRUE;
					else                 return FALSE;
				}
				else if( functionName.equals("gt") ) {
					// {gt:first, second}
					
					if( failNumParse )   return FALSE;

					if (first > second)  return TRUE;
					else                 return FALSE;
				}
				else if( functionName.equals("ge") ) {
					// {ge:first, second}
					
					if( failNumParse )   return FALSE;

					if (first >= second) return TRUE;
					else                 return FALSE;
				}
				//else { return "Incomplete function statement, no parameters!"; }
				else if( functionName.equals("give") ) {
					// {give:player, item}
					
					final Player p = parent.getPlayer(Utils.toInt(params[0], -1));
					final Item i = parent.getItem(Utils.toInt(params[1], -1));

					if( p != null && i != null ) {
						i.setLocation(p.getDBRef());
						p.getInventory().add(i);
					}

					return "";
				}
				else if( functionName.equals("mul") ) {
					// {mul:factor1, factor2}
					
					if( failNumParse ) return "-1";

					return "" + (first * second);
				}
				else if( functionName.equals("prop") ) {
					// {prop:name, object}
					
					final String property = params[0];
					final int dbref = Utils.toInt(params[1], -1);

					final MUDObject object1 = parent.getObject(dbref);

					if( object1 != null ) {
						if( debug_enabled ) System.out.println("Object1: " + object1.getName());
						return "" +  object1.getProperty(property);
					}
					else return "";
					//else { return "Incomplete function statement, no parameters!"; }
				}
				else if( functionName.equals("tell") ) {
					// {tell:message, player}
					
					final String message = params[0];
					final Player p = parent.getPlayer(Utils.toInt(params[1], -1));
					
					if( message != null && p != null ) {
						// TODO figure out if there's a problem with this method of transmitting info
						parent.addMessage( new Message(message, p) );
						//return message;
					}

					return "";
				}
				else if( functionName.equals("equip") ) {
					// {equip:player, item}
					
					final Player p = parent.getPlayer(Utils.toInt(params[0], -1));
					final Item i = parent.getItem(Utils.toInt(params[1], -1));

					if( p != null && i != null ) {
						// equip the specified item
						p.equip(i, "none");
						// change location?
						p.getInventory().remove(i);
					}

					return "";
				}
				else if( functionName.equals("or") ) {
					// {or:<condition1>,<condition2>}
					
					if( params[0].equals(TRUE) || params[1].equals(TRUE) ) {
						return TRUE;
					}
					else {
						return FALSE;
					}
				}
				else if( functionName.equals("write") ) {
					// {write:message, player}
					
					final String message = params[0];
					final Player p = parent.getPlayer(Utils.toInt(params[1], -1));

					if( message != null && p != null ) {
						player.getClient().write( message );
					}
					
					return "";
				}
				else if( functionName.equals("writeln") ) {
					// {writeln:message, player}
					
					final String message = params[0];
					final Player p = parent.getPlayer(Utils.toInt(params[1], -1));

					if( message != null && p != null ) {
						player.getClient().writeln( message );
					}
					
					return "";
				}
				else { return "PGM: No such function! ( " + functionName + " )"; }
			}
			else if( params.length == 3 ) {
				if( debug_enabled ) {
					System.out.println("Parameter (1): " + params[0]);
					System.out.println("Parameter (2): " + params[1]);
					System.out.println("Parameter (3): " + params[2]);
				}

				if(functionName.equals("if")) {
					// {if: test condition, true: do this, false: do this}
					
					final String result = interpret(params[0], player, object);

					if( debug_enabled ) System.out.println("result: " + result);

					if( result.equals(TRUE) ) {
						return interpret(params[1], player, object);

					}
					else if( result.equals(FALSE) ) {
						return interpret(params[2], player, object);
					}
				}
				else if(functionName.equals("set")) {
					// {set: propname, object, value }
					
					final String property = params[0];
					final int dbref = Utils.toInt(params[1], -1);

					final MUDObject object1 = parent.getObject(dbref);

					final String value = params[2];

					if( object1 != null ) {
						if( debug_enabled ) System.out.println("Object: " + object1.getName());
						object1.setProperty(property, value);
						return "" +  object1.getProperty(property);
					}
					else return "";
				}
				else if (functionName.equals("with")) {
					String result = "";
					
					if( use_vars ) {
						final String varName = params[0];
						final String varValue = params[1];
						
						vars.put(varName, varValue);
						
						result = interpret(params[2], player, object);
						
						vars.remove(varName);
					}
					else {
						result = interpret(params[2], player, object);
					}
					
					return result;
				}
				/*else if( functionName.equals("call" ) ) {
					if( debug_enabled ) System.out.println("got to CALL");
					
					return call( params[0], params[1], params[2] );
				}*/

				return "";
			}
			else {
				// functions that take some arbitrary number of parameters
				if(functionName.equals("distance")) {
					/*
					 * parameters:
					 * 	2d/3d
					 *  one or two points
					 */

					if( debug_enabled ) System.out.println("PGM -distance-");

					List<Point> ptList = Utils.toPoints(Utils.join(params, ","));

					if( ptList != null ) {
						return String.format("%.1f", Utils.distance(ptList.get(0), ptList.get(1)));
						//return ptList + "; distance is " + String.format("%.1f", Utils.distance(ptList.get(0), ptList.get(1)));
					}
					else { return "PGM: Error!"; }
					//else { return "Incomplete function statement, no parameters!"; }
				}
				else if ( functionName.equals("do") ) {
					String temp;

					for(final String param : params) {
						if( debug_enabled ) System.out.println("(DO) INTERPRET: " + param);

						temp = interpret(param, player, object);

						if( debug_enabled ) System.out.println("(DO) Result: " + temp);
						
						if( temp.equals(ERROR) ) {
							return ""; // return early if we encounter an error
						}
					}

					return "";
				}
				else { return "PGM: No such function!"; }
			}
		}
		else {
			switch(functionName) {
			case "{&arg}":
				return "";
			case "{&cmd}":
				return "";
			case "{&how}":
				return "";
			case "{&this}":
				return "" + object.getDBRef();
			case "{name}":
				return parent.getServerName();
			case "{&player}":
				return "" + player.getDBRef();
			case "{version}":
				return MUDServer.getVersion();
			case "{colors}":
				return "Incomplete function statement, no inputs!";
			default:
				if( use_vars ) {
					String temp = functionName.replace("{", "").replace("}", "").replace("&", "");
					String temp1 = vars.get(temp);

					if( temp1 != null ) return temp1;
					else return "";					
				}
				else return functionName;
			}
		}
	}

	// TODO fix this, this is low quality function checking
	private static boolean isFunction(String s) {
		if( s.startsWith("{") && s.endsWith("}") ) return true;
		else return false;
	}

	/**
	 * isValidScript
	 * 
	 * Checks to see if the provided script is terminated by curly
	 * brackets and if all curly brackets have a matching end to their
	 * start.
	 * 
	 * @param script
	 * @return
	 */
	public boolean isValidScript(String script) {
		if (script.startsWith("{") && script.endsWith("}")) {
			final int numLeftBrace = Utils.countNumOfChar(script, '{');
			final int numRightBrace = Utils.countNumOfChar(script, '}');

			if( numLeftBrace != 0 && numRightBrace != 0 && numLeftBrace == numRightBrace ) {
				return true;
			}
		}

		return false;
	}

	public void exec(Script script) {
		exec(script, null);
	}

	public void exec(Script script, Client client) {
		interpret( script, parent.getPlayer(client), null );
	}

	/**
	 * Fix incorrect breaks in parameters due to splitting on commas
	 * 
	 * TODO should this be private, public or ?
	 * @param params
	 */
	private void fixParams(List<String> params) {
		boolean done = false; // are we done fixing any incorrect breaks

		int leftCurlyCount = 0;
		int rightCurlyCount = 0;

		int index = 0;    // index of the param we started fixing at
		int offset = 0;
		String temp = "";

		// while we haven't run out of parameters to process
		while( index < params.size() - 1 ) {
			int count = 0; // counter that is is used to place a number next to params as we print them out

			done = false;

			offset = 0;

			// List Parameters
			if( debug_enabled ) {
				System.out.println("---");
				System.out.println("Params: ");
			}

			for(String s : params) {
				if( debug_enabled ) System.out.println(count + " " + s);
				count++;
			}

			// grab the parameter at the current index
			temp = params.get(index);

			if( debug_enabled ) {
				System.out.println("---");
				System.out.println("Temp: " + temp);
			}

			while( !done ) {
				leftCurlyCount = Utils.countNumOfChar(temp, '{');
				rightCurlyCount = Utils.countNumOfChar(temp, '}');

				if( debug_enabled ) {
					System.out.println("lcc: " + leftCurlyCount);
					System.out.println("rcc: " + rightCurlyCount);
					System.out.println("offset: " + offset);
				}

				//|| leftCurlyCount <= 1 || rightCurlyCount <= 1
				if( leftCurlyCount != rightCurlyCount ) {
					offset++;                                       // increase offset
					temp = temp + "," + params.get(index + offset); // pull in the next param in initial list
					if( debug_enabled ) System.out.println("TEMP: " + temp);
				}
				else {
					done = true;
					if( debug_enabled ) System.out.println("Result: " + temp);
				}
			}

			if( debug_enabled ) {
				System.out.println("---");
				System.out.println("INDEX: " + index);
			}

			// if no changes were necessary (our workspace is equal to the param at the inital index)
			if( params.get(index).equals(temp) ) {
				index++; // move to next param
				continue;
			}
			else {
				// offset != 0, since that would imply no change

				params.set(index, temp);

				for(int e=index + offset; e > index; e--) params.remove(e);

				index++;
			}
		}

		/*for(int i = 0; i < params.size(); i++) {
			//leftCurlyCount = Utils.count(params.get(i), '{');
			//rightCurlyCount = Utils.count(params.get(i), '}');

			//String temp1 = params.get(i);
			//String temp2 = params.get(i+1);

			if( ( params.get(i).startsWith("{") && !params.get(i).endsWith("}") )  
					&& ( !params.get(i+1).startsWith("}") && params.get(i+1).endsWith("}") ) ) {
				params.set(i, (params.get(i) + "," + params.get(i+1)).trim());
				params.remove(i+1);
			}

			//if( leftCurlyCount != rightCurlyCount ) {
			//params.set(i, (params.get(i) + "," + params.get(i+1)).trim());
			//params.remove(i+1);
			//}
		}*/
	}
	
	private void debug(final String output) {
		if( debug_enabled ) {
		}
	}
	
	// TODO: what exactly is this supposed to do?
	/*private String call(final String functionName, final String...params) {
		return interpret("{" + functionName + ":" + params[0] + "," + params[1] + "}", null, null);
		
		switch(functionName) {
		case "colors":
			if( debug_enabled ) {
				System.out.println("CALL");
				System.out.println(functionName);
				System.out.println(params[0]);
				System.out.println(params[1]);
			}

			String result = parent.colors( params[0], params[1] );

			if( debug_enabled ) System.out.println(result);

			return result;
		default:
			break;
		}

		return "";
	}*/
}