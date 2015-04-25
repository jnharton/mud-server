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
	private final MUDServer parent;
	private Hashtable<String, String> vars;
	private boolean use_vars;

	private boolean debug_enabled = false;
	
	private static final String TRUE = ":true";
	private static final String FALSE = ":false";
	
	public ProgramInterpreter(final MUDServer parent) {
		this(parent, false);
	}
	
	public ProgramInterpreter(final MUDServer parent, final boolean enable_debug) {
		this.parent = parent;
		this.vars = null;
		this.use_vars = false;
		
		this.debug_enabled = enable_debug;
	}

	private List<String> lex(String input) {
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
						for(String param : params) {
							// evaluate parameters if they are valid sub scripts
							/*if( param.startsWith("{") && param.endsWith("}") ) {
								params.set(index, interpret(param, player, object));
							}
							index++;*/
							
							if( isValidScript( param ) ) {
								params.set(index, interpret(param, player, object));
							}
							
							index++;
						}
					}

					if( debug_enabled ) System.out.println("EVALUATE");

					if( debug_enabled ) System.out.println("Evaluate: <" + functionName + "> with " + params);

					//return evaluate(functionName, params.toArray(new String[params.size()]), player);
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
			if ( functionName.equals("do") ) {
				String temp;

				for(final String param : params) {
					if( debug_enabled ) System.out.println("(DO) INTERPRET: " + param);
					temp = interpret(param, player, object);
					if( debug_enabled ) System.out.println("(DO) Result: " + temp);
					//parent.notify(parent.getPlayer(client), temp);
				}

				return "";
			}

			if( params.length == 1 ) {
				if( debug_enabled ) System.out.println("Parameter (1): " + params[0]);

				if(functionName.equals("create_item")) {
					final Item item = parent.createItem(params[0], true);

					if( item != null ) {
						return "" + item.getDBRef();
					}
					else {
						return "-1";
					}
				}
				else if (functionName.equals("dbref")) {
					return "" + parent.getObject(params[0]).getDBRef();
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
					if (params.length >= 2) {
						// TODO consider how debug messages will be transmitted
						parent.debug("Color: " + params[0]);
						parent.debug("Text: " + params[1]);

						return parent.colorCode(params[0]) + params[1] + parent.colorCode("white");
					}
					else { return "PGM: Error!"; }
				}
				else if( functionName.equals("cmp") ) {
					if( params[0].equals(params[1]) ) {
						return TRUE;
					}
					else {
						return FALSE;
					}
				}
				else if( functionName.equals("add") ) {
					if( failNumParse ) return "-1";

					return "" + (first + second);
				}
				else if( functionName.equals("sub") ) {
					if( failNumParse ) return "-1";

					return "" + (first - second);
				}
				else if( functionName.equals("and") ) {
					if( params[0].equals(TRUE) && params[1].equals(TRUE) ) {
						return TRUE;
					}
					else {
						return FALSE;
					}
				}
				else if( functionName.equals("eq") ) {
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
					if( failNumParse )   return FALSE;

					if (first < second)  return TRUE;
					else                 return FALSE;
				}
				else if( functionName.equals("le") ) {
					if( failNumParse )   return FALSE;

					if (first <= second) return TRUE;
					else                 return FALSE;
				}
				else if( functionName.equals("gt") ) {
					if( failNumParse )   return FALSE;

					if (first > second)  return TRUE;
					else                 return FALSE;
				}
				else if( functionName.equals("ge") ) {
					if( failNumParse )   return FALSE;

					if (first >= second) return TRUE;
					else                 return FALSE;
				}
				//else { return "Incomplete function statement, no parameters!"; }
				else if( functionName.equals("give") ) {
					final Player p = parent.getPlayer(Utils.toInt(params[0], -1));
					final Item i = parent.getItem(Utils.toInt(params[1], -1));

					if( p != null && i != null ) {
						i.setLocation(p.getDBRef());
						p.getInventory().add(i);
					}

					return "";
				}
				else if( functionName.equals("mul") ) { 
					if( failNumParse ) return "-1";

					return "" + (first * second);
				}
				else if( functionName.equals("prop") ) {
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
					final String message = params[0];
					//final Player p = parent.getPlayer(Utils.toInt(params[1], -1));
					final Player p;

					// TODO fix this kludge...
					/*if( params[1].trim().equalsIgnoreCase("{&player}") ) {
						p = player;
					}
					else {
						p = parent.getPlayer(Utils.toInt(params[1], -1));
					}*/

					p = parent.getPlayer(Utils.toInt(params[1], -1));

					if( message != null && p != null ) {
						// TODO figure out if there's a problem with this method of transmitting info
						parent.addMessage( new Message(message, p) );
						//return message;
					}

					return "";
				}
				else if( functionName.equals("equip") ) {
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
					final String message = params[0];
					
					final Player p = parent.getPlayer(Utils.toInt(params[1], -1));

					if( message != null && p != null ) {
						player.getClient().write( message );
					}
					
					return "";
				}
				else if( functionName.equals("writeln") ) {
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

				return "";
			}
			else {
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
					}

					return "";
				}
				else if (functionName.equals("exec")) {
					if( params != null ) {
						if( params.length == 1 ) {


						}
						else if( params.length == 2 ) {

						}
						else {

						}
					}

					return "";
				}
				else if (functionName.equals("with")) {
					List<String> params1 = new ArrayList<String>(params.length);

					boolean function_found = false;
					boolean var_defined = false;

					String last_var = "";

					int index = 0;

					// resolve all sub functions
					for(String param : params) {
						if( !isFunction(param) && !function_found ) {
							function_found = true;

							if( !use_vars ) {
								use_vars = true;
								vars = new Hashtable<String, String>();

								vars.put(param, "");

								last_var = param;
							}
						}
						else {
							if( !var_defined ) {
								vars.put(last_var, interpret(param, player, object));
							}
							else params1.set(index, interpret(param, player, object));
						}

						index++;
					}

					// do things

					// vars to null
					vars = null;

					// use_vars to false
					use_vars = false;

					return Utils.join(params1, " ");
				}
				else { return "PGM: No such function!"; }
				//else { return "PGM: Error!"; }
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
				//return "-Result: " + MUDServer.getName() + " " + MUDServer.getVersion();
				return MUDServer.getVersion();
			case "{colors}":
				return "Incomplete function statement, no inputs!";
			default:
				if( use_vars ) {
					String temp = functionName.replace("{", "").replace("}", "");
					String temp1 = vars.get(temp);

					if( temp1 != null ) return temp1;
					else return "";					
				}
				else return functionName;
			}

			/*if( !script.contains("{") && !script.contains("}") ) { // not a script...
				return script;
			}
			else { // malformed script?
				return "PGM: No such function! (1)";
			}*/
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
}