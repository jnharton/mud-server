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

	public ProgramInterpreter(final MUDServer parent) {
		this.parent = parent;
		this.vars = null;
		this.use_vars = false;
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

		System.out.println("Tokens: " + tokens);

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

		System.out.println("Interpret: " + script);

		if ( isValidScript( script ) ) {
			if( script.indexOf(":") != -1 ) {
				String work = script.substring(1, script.length() - 1); // strip off the outermost squiggly braces ( {} )
				//String work = script.replace("{", "").replace("}", "");

				System.out.println("work: " + work);

				String[] temp = work.split(":", 2);

				String functionName = temp[0];
				List<String> params = null;

				System.out.println("Function: " + functionName); // tell us the script function used

				if( temp.length > 1 ) {
					params = Utils.mkList(temp[1].split(",")); // split the arguments on commas ( , )

					fixParams( params ); // sort of fixes the params

					System.out.println("Params: " + params);

					int index = 0;

					// whenever the function called isn't 'if' or 'with' or 'do', we want to evaluate all parameters as we get them
					if( !functionName.equals("if") && !functionName.equals("with") && !functionName.equals("do") ) {
						for(String param : params) {
							if( param.startsWith("{") && param.endsWith("}") ) {
								params.set(index, interpret(param, player, object));
							}
							index++;
						}
					}

					System.out.println("EVALUATE");

					System.out.println("Evaluate: <" + functionName + "> with " + params);
					//return evaluate(functionName, params.toArray(new String[params.size()]), player);
					String result = evaluate(functionName, params.toArray(new String[params.size()]), player, object);
					System.out.println("Result: " + result);
					return result;
				}
				else { return "Incomplete function statement, no parameters!"; }
			}
			else {
				return evaluate(script, new String[0], player, object);
			}
		}

		return "Invalid Script!";
	}

	private String evaluate(final String functionName, final String[] params, final Player player, final MUDObject object) {
		System.out.println("Params: " + params.length);

		if( params.length > 0 ) {

			/*
			 * TODO: resolve this kludge and figure out a way to ensure that each
			 * function doesn't have to worry about receiving the correct number
			 * of parameters.
			 */
			if ( functionName.equals("do") ) {
				String temp;

				for(final String param : params) {
					System.out.println("(DO) INTERPRET: " + param);
					temp = interpret(param, player, object);
					System.out.println("(DO) Result: " + temp);
					//parent.notify(parent.getPlayer(client), temp);
				}

				return "";
			}

			if( params.length == 1 ) {
				System.out.println("Parameter (1): " + params[0]);

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
					System.out.println(params[0]);
					return parent.rainbow(params[0]) + parent.colorCode("white");
				}
				else { return "PGM: No such function!"; }
				//else { return "Incomplete function statement, no parameters!"; }
				//else { return "PGM: Error!"; }
			}
			else if( params.length == 2 ) {
				System.out.println("Parameter (1): " + params[0]);
				System.out.println("Parameter (2): " + params[1]);

				Integer first = null;
				Integer second = null;
				
				boolean failNumParse = false;

				// cover some stuff for certain parts here
				if( Utils.mkList("add", "sub", "and", "eq", "lt", "le", "gt", "ge").contains(functionName) ) {
					if( params[0].startsWith("{") && params[0].endsWith(")") ) {
						params[0] = interpret(params[0], player, object);
					}

					if( params[1].startsWith("{") && params[1].endsWith(")") ) {
						params[1] = interpret(params[1], player, object);
					}
					
					if( !functionName.equals("and") ) {
						try {
							first = Integer.parseInt(params[0]);
							second = Integer.parseInt(params[1]);
						}
						catch(NumberFormatException nfe) {
							System.out.println("-- Stack Trace --");
							nfe.printStackTrace();
							
							failNumParse = true;
						}
						
						//System.out.println("first: " + first);
						//System.out.println("Second: " + second);
					}
				}

				if( functionName.equals("colors") ) {
					if (params.length >= 2) {
						parent.debug("Color: " + params[0]);
						parent.debug("Text: " + params[1]);

						return parent.colorCode(params[0]) + params[1] + parent.colorCode("white");
					}
					else { return "PGM: Error!"; }
				}
				else if( functionName.equals("add") ) {
					// {add:5,5} -> 10
					if( failNumParse ) return "-1";
					
					return "" + (first + second);
				}
				else if( functionName.equals("sub") ) {
					// {sub:5,3} -> 2
					if( failNumParse ) return "-1";
					
					return "" + (first - second);
				}
				else if( functionName.equals("and") ) {
					// {and:<condition1>,<condition2>}
					if( params[0].equals(":true") && params[1].equals(":true") ) {
						return ":true";
					}
					else {
						return ":false";
					}
				}
				else if( functionName.equals("eq") ) {
					// {eq:<string 1>,<string 2>} -> {eq:test,test} -> :true
					if( params[0].equals(params[1]) ) {
						return ":true";
					}
					else {	
						if( failNumParse ) return ":false";

						if (first == second) return ":true";
						else                 return ":false";
					}
				}
				else if( functionName.equals("lt") ) {
					// {lt:12,13} -> :true
					if( failNumParse ) return ":false";
					
					if (first < second) return ":true";
					else                return ":false";
				}
				else if( functionName.equals("le") ) {
					// {le:5,5} -> :true
					if( failNumParse ) return ":false";

					if (first <= second) return ":true";
					else                 return ":false";
				}
				else if( functionName.equals("gt") ) {
					// {gt:25,20} -> :true
					if( failNumParse ) return ":false";

					if (first > second) return ":true";
					else                return ":false";
				}
				else if( functionName.equals("ge") ) {
					// {ge:2056,2056} -> :true
					if( failNumParse ) return ":false";

					if (first >= second) return ":true";
					else                 return ":false";
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
				else if( functionName.equals("prop") ) {
					// {prop:<property name>,<dbref number of object to get property from>}
					final String property = params[0];
					final int dbref = Utils.toInt(params[1], -1);

					final MUDObject object1 = parent.getObject(dbref);

					if( object != null ) {
						System.out.println("Object: " + object1.getName());
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
				else { return "PGM: No such function!"; }
			}
			else if( params.length == 3 ) {
				System.out.println("Parameter (1): " + params[0]);
				System.out.println("Parameter (2): " + params[1]);
				System.out.println("Parameter (3): " + params[2]);

				if(functionName.equals("if")) {
					// {if: test condition, true: do this, false: do this}
					final String result = interpret(params[0], player, object);

					System.out.println("result: " + result);

					if( result.equals(":true") ) {
						return interpret(params[1], player, object);

					}
					else if( result.equals(":false") ) {
						return interpret(params[2], player, object);
					}
				}
				else if(functionName.equals("set")) {
					// {set: propname, object, value }

					final String property = params[0];
					final int dbref = Utils.toInt(params[1], -1);

					final MUDObject object1 = parent.getObject(dbref);

					final String value = params[2];

					if( object != null ) {
						System.out.println("Object: " + object1.getName());
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

					System.out.println("PGM -distance-");

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
						System.out.println("(DO) INTERPRET: " + param);
						temp = interpret(param, player, object);
						System.out.println("(DO) Result: " + temp);
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
	 * @param params
	 */
	public void fixParams(List<String> params) {	
		List<String> tempList = new ArrayList<String>();

		boolean done = false; // are we done fixing any incorrect breaks

		int leftCurlyCount = 0;
		int rightCurlyCount = 0;

		int test = 0;
		int index = 0;    // index of the param we started fixing at
		int offset = 0;
		String temp = "";

		/*
		 * NOTE:
		 * everywhere test is used, it was replaced with params.size()
		 * and all test assignments were commented out
		 */
		//test = params.size();

		// while we haven't run out of parameters to process
		while( index < params.size() - 1 ) {
			int count = 0; // counter that is is used to place a number next to params as we print them out

			done = false;

			offset = 0;

			// List Parameters
			System.out.println("---");
			System.out.println("Params: ");
			for(String s : params) {
				System.out.println(count + " " + s);
				count++;
			}

			// grab the parameter at the current index
			temp = params.get(index);

			System.out.println("---");
			System.out.println("Temp: " + temp);

			while( !done ) {
				leftCurlyCount = Utils.countNumOfChar(temp, '{');
				rightCurlyCount = Utils.countNumOfChar(temp, '}');

				System.out.println("lcc: " + leftCurlyCount);
				System.out.println("rcc: " + rightCurlyCount);
				System.out.println("offset: " + offset);

				//|| leftCurlyCount <= 1 || rightCurlyCount <= 1
				if( leftCurlyCount != rightCurlyCount ) {
					offset++;                                       // increase offset
					temp = temp + "," + params.get(index + offset); // pull in the next param in initial list
					System.out.println("TEMP: " + temp);
				}
				else {
					System.out.println("Result: " + temp);
					done = true;
				}
			}

			System.out.println("---");
			System.out.println("INDEX: " + index);

			// if no changes were necessary (our workspace is equal to the param at the inital index)
			if( params.get(index).equals(temp) ) {
				index++; // move to next param
				//test = params.size();
				continue;
			}
			else { // offset != 0, since that would imply no change

				/*
				 * currently we put everything from the current index up to the offset
				 * in tempList and ask params to retain only what is in temp list
				 * then we clear tempList and add our modified param into params
				 * at the appropriate spot
				 * 
				 * maybe we should simply be accruing params to combine inside tempList
				 * then joining tempList into a string, replacing the param
				 * at the current index and removing every string in params after the index
				 * up to -offset- number of elements?
				 * 
				 */

				// OLD METHOD
				/*if( index == 0 ) {
					// keep the list after the first and any we used
					tempList.addAll( params.subList(offset + 1, params.size()) );
				}
				else {
					// keep the list after the first and any we used
					tempList.addAll( params.subList(0, index) );
					tempList.addAll( params.subList(index + offset + 1, params.size()) );
				};

				params.retainAll( tempList );

				tempList.clear();

				params.add(index, temp);*/

				params.set(index, temp);

				for(int e=index + offset; e > index; e--) params.remove(e);

				index++;
				//test = params.size();

				//System.out.println("COMBINED: "); // tells us which elements of the original list were combined
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
}