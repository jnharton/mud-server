package mud.misc;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.MUDObject;
import mud.MUDServer;
import mud.interfaces.ODBI;
import mud.objects.Item;
import mud.objects.NPC;
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
public final class ProgramInterpreter {
	private static final String TRUE = ":true";
	private static final String FALSE = ":false";
	private static final String ERROR = ":error";
	private static final String NONE = ":none";
	
	private static enum Perms { READ, WRITE };
	
	// TODO I would like to pry out holding a parent reference..
	private final MUDServer parent;
	private final ODBI database;

	// configuration
	private final boolean debug_enabled;
	private final EnumSet<Perms> permissions;

	private final Hashtable<String, String> vars;

	public ProgramInterpreter(final MUDServer parent) {
		this(parent, false);
	}
	
	public ProgramInterpreter(final MUDServer parent, final boolean enable_debug) {
		this(parent, enable_debug, EnumSet.of(Perms.READ));
		
	}
	public ProgramInterpreter(final MUDServer parent, final boolean enable_debug, final EnumSet<Perms> perms) {
		this.parent = parent;
		this.database = parent.getDBInterface();

		//this.debug_enabled = enable_debug;
		this.debug_enabled = true;
		
		this.permissions = EnumSet.copyOf(perms);
		
		this.vars = new Hashtable<String, String>();
	}

	/**
	 * Add a variable to the interpreter's vars.
	 * 
	 * @param value String variable value
	 */
	public void addVar(final String name, final String value) {
		this.vars.put(name, value);
	}
	
	/**
	 * Ask if the interpreter has a variable set by that name,
	 * returns false if the KEY has a null VALUE.
	 * 
	 * @param name variable name
	 * @return
	 */
	public boolean hasVar(final String name) {
		return this.vars.containsKey(name) && this.vars.get(name) != null;
	}
	
	/**
	 * Get the value of an -existing- variable in the interpreter's vars.
	 * 
	 * @param name     String variable name
	 * @return
	 */
	public String getVar(final String name) {
		// TODO consider a dummy result instead of NULL when the var is null?
		return this.vars.get(name);
	}
	
	/**
	 * Set an -existing- variable in the interpreter's vars
	 * to a new value.
	 * 
	 * @param name     String variable name
	 * @param newValue String variable value
	 * @return
	 */
	public void setVar(final String name, final String newValue) {
		this.vars.replace(name, newValue);
	}

	/**
	 * Remove an -existing- variable from the interpreter's vars.
	 * 
	 * @param name String variable name
	 */
	public void delVar(final String name) {
		this.vars.remove(name);
	}

	public String interpret(final Script script, final Player player, final MUDObject object) {
		return interpret( script.getText(), player, object );
	}

	/**
	 * Function to evaluate a script/program
	 * 
	 * @param script
	 * @return
	 */
	private String interpret(final String script, final Player player, final MUDObject object) {	
		//System.out.println("PGM: <" + script + ">");
		//System.out.println("pArg: " + script);
		String result = "";

		if ( isValidScript( script ) ) {
			if( debug_enabled ) System.out.println("Interpret: " + script);

			// no script function equals no script
			if( script.indexOf(":") != -1 ) {
				// TODO fix this, we are assuming it's all one nested script...
				String work = script.substring(1, script.length() - 1); // strip off the outermost squiggly braces ( {} )

				if( debug_enabled ) System.out.println("work: " + work);

				final String[] temp = work.split(":", 2);

				String functionName = temp[0]; // FUNCTION NAME

				if( debug_enabled ) System.out.println("Function: " + functionName); // tell us the script function used

				List<String> params = null;    // FUNCTION PARAMETERS 

				// find the parameters if there are any
				if( temp.length > 1 ) {
					params = Utils.mkList(temp[1].split(",")); // split the arguments on commas ( , )

					if( debug_enabled ) System.out.println("Fixing Params");

					ProgramInterpreter.fixParams( params ); // sort of fixes the params

					//if( debug_enabled ) System.out.println("Params: " + params);

					if( debug_enabled ) {
						System.out.println("Params:");

						for(final String param : params) {
							System.out.println(param);
						}
					}

					// evaluate parameters
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

					result = evaluate(functionName, params.toArray(new String[params.size()]), player, object);

					if( debug_enabled ) System.out.println("Result: " + result);
				}
				else result = "Incomplete function statement, no parameters!";
			}
			else {
				if( debug_enabled ) {
					System.out.println("EVALUATE");
					System.out.println("Evaluate: <" + script.substring(1, script.length() - 1) + "> ");
				}
				
				result = evaluate(script, new String[0], player, object);
				
				if( debug_enabled ) System.out.println("Result: " + result);
			}
		}
		else {
			int p_dbref = (( player != null ) ? player.getDBRef() : 1);
			int o_dbref = (( object != null ) ? object.getDBRef() : 1);
			
			result = "Invalid Script! (" + p_dbref + "," + o_dbref + ")";
		}
		
		return result;
	}

	private String evaluate(final String functionName, final String[] params, final Player player, final MUDObject object) {
		
		// TODO fix kludge?
		// what should cmd, arg, how be set to?
		addVar("cmd", "");
		addVar("arg", "");
		addVar("how", "");
		addVar("player", "" + player.getDBRef());
		addVar("this", "" + object.getDBRef());
		
		if( debug_enabled ) {
			System.out.println("# Params: " + params.length);
			System.out.println("");
			
			System.out.println("Params:");
			
			for(final String param : params) System.out.println(param);
		}

		if( params.length > 0 ) {
			/*
			 * TODO: resolve this kludge and figure out a way to ensure that each
			 * function doesn't have to worry about receiving the correct number
			 * of parameters.
			 */
			// this a kludge, since a do function call may contain 1 or more parameters/sub scripts.
			if ( functionName.equals("do") ) {
				// {do:script1, script2, ...}
				String temp;

				for(final String param : params) {
					if( debug_enabled ) System.out.println("(DO) INTERPRET: " + param);

					temp = interpret(param, player, object);

					if( debug_enabled ) System.out.println("(DO) Result: " + temp);
				}

				return "";
			}

			// Functions that take 1 parameter
			if( params.length == 1 ) {
				if( debug_enabled ) System.out.println("Parameter (1): " + params[0]);

				if(functionName.equals("create_item")) {
					// {create_item:identifier}
					final Item item = parent.createItem(params[0], true);

					if( item != null ) return "" + item.getDBRef();
					else               return "" + -1;
				}
				else if (functionName.equals("dbref")) {
					// {dbref:object}
					final MUDObject mobj = database.getByName(params[0]);

					if( mobj != null ) return "" + mobj.getDBRef(); 
					else               return "" + -1;
				}
				else if (functionName.equals("rainbow")) {
					if( debug_enabled ) System.out.println(params[0]);
					
					return Utils.rainbow(params[0], parent.getColors());
				}
				else if ( functionName.equals("test") ) {
					final Integer i = Utils.toInt(params[0], -1);
					
					parent.cmd("interact " + ((NPC) object).getName(), player.getClient());
					parent.cmd("list", player.getClient());
					
					return NONE;
				}
				else { return "PGM: No such function!"; }
				//else { return "Incomplete function statement, no parameters!"; }
				//else { return "PGM: Error!"; }
			}

			// Functions that take 2 parameters
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
					if( params[0].startsWith("{") && params[0].endsWith("}") ) {
						params[0] = interpret(params[0], player, object);
					}

					if( params[1].startsWith("{") && params[1].endsWith("}") ) {
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
						if( debug_enabled)  {
							System.out.println("Color: " + params[0]);
							System.out.println("Text: " + params[1]);
						}

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
					final Item i = database.getItem(Utils.toInt(params[1], -1));

					if( p != null && i != null ) {
						i.setLocation(p.getDBRef());
						p.getInventory().add(i);
					}

					return "";
				}
				else if( functionName.equals("list") ) {
					// {list:listname, object}
					final String property = params[0];
					final int dbref = Utils.toInt(params[1], -1);
					
					return list(property, dbref);
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
					
					return prop(property, dbref);
					/*final MUDObject object1 = database.getById(dbref);

					if( object1 != null ) {
						if( debug_enabled ) System.out.println("Object1: " + object1.getName());

						return "" +  object1.getProperty(property);
					}
					else return "";*/
					
					//else { return "Incomplete function statement, no parameters!"; }
				}
				else if( functionName.equals("propdir") ) {
					// {prop:name, object}
					final String property = params[0];
					final int dbref = Utils.toInt(params[1], -1);
					
					return propdir(property, dbref);
				}
				else if( functionName.equals("set") ) {
					final String var = params[0];
					final String val = params[1];
					
					if( hasVar(var) ) setVar(var, val);
					else              addVar(var, val);
					
					return val;
				}
				else if( functionName.equals("tell") ) {
					// {tell:message, player}

					// TODO resolve this kludge, since I may need to parse for hidden formatting data
					final String message = params[0].replace("#c", ",");
					final Player p = parent.getPlayer(Utils.toInt(params[1], -1));

					if( message != null && p != null ) {
						// TODO figure out if there's a problem with this method of transmitting info
						parent.addMessage( new Message(null, p, message) );
						//return message;
					}

					return "";
				}
				else if( functionName.equals("equip") ) {
					// {equip:player, item}

					// TODO items to equip should come out of the character's inventory
					final Player p = parent.getPlayer(Utils.toInt(params[0], -1));
					final Item i = database.getItem(Utils.toInt(params[1], -1));

					if( p != null && i != null ) {
						// equip the specified item
						// TODO implement programmatic equip

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
				/*else if( functionName.equals("write") ) {
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
				}*/
				else { return "PGM: No such function! ( " + functionName + " )"; }
			}

			// Functions that take 3 parameters
			else if( params.length == 3 ) {
				if( debug_enabled ) {
					System.out.println("Parameter (1): " + params[0]);
					System.out.println("Parameter (2): " + params[1]);
					System.out.println("Parameter (3): " + params[2]);
				}

				if( functionName.equals("if") ) {
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
				else if( functionName.equals("store") ) {
					// {store: propname, object, value }

					final String property = params[0];
					final int dbref = Utils.toInt(params[1], -1);

					final MUDObject object1 = database.getById(dbref);

					final String value = params[2];

					if( object1 != null ) {
						if( debug_enabled ) System.out.println("Object: " + object1.getName());

						object1.setProperty(property, value);

						return "" +  object1.getProperty(property);
					}
					else return "";
				}
				else if( functionName.equals("with") ) {
					String result = "";
					
					final String varName = params[0];
					final String varValue = params[1];

					addVar(varName, varValue);

					result = interpret(params[2], player, object);

					delVar(varName);

					return result;
				}

				return "";
			}

			// functions that take some arbitrary number of parameters
			else {
				if( functionName.equals("distance") ) {
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
			//return functionName.substring(2, functionName.length() - 2) + ": Incomplete function statement, insufficient parameters!";
			/* all ZERO parameter functions */
			switch(functionName) {
			case "{name}":
				return parent.getServerName();
			case "{version}":
				return MUDServer.getVersion();
			default:
				String temp = functionName.substring(1, functionName.length() - 1);

				// e.g. {&arg}
				if( temp.startsWith("&") ) {
					/*
					 * special
					 * &cmd - command that started this
					 * &arg - function argument?
					 * &this - object code is executing from?
					 * &player - player context of executing code?
					 */

					final String varName = temp.substring(1);
					final String value = getVar(varName);

					if( value != null ) {
						return value;
					}
					else return "";
				}
				else return functionName;
			}
			
			// TODO convert all & 'functions' to var retrievals and store data in vars
			// TODO consider making a separate area for protected/static vars
			/*switch(functionName) {
			case "{&arg}":
				return vars.get("arg");
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
			default:
				String temp = functionName.substring(1, functionName.length() - 1);
				
				//if( functionName.startsWith("&") ) {
				if( temp.startsWith("&") ) {
					final String varName = temp.substring(1);
					final String value = getVar(varName);

					if( value != null ) {
						return value;
					}
					else return "";
				}
				else return functionName;
			}*/
		}
	}
	
	/* Functions */
	private String list(final String listName, final Integer objDBREF) {
		// {list:listname}
		// {list:listname,obj} 
		// propname#/?
		final MUDObject object = database.getById(objDBREF);
		
		if( object != null ) {
			if( listName.endsWith("#/") && propdir(listName, objDBREF).equals(TRUE) ) {
				final Map<String, String> props = object.getProperties(listName);
				
				final String[] strings = new String[props.size()];

				for(final String name : props.keySet()) {
					String temp = name.substring( name.indexOf('/') + 1 );
					
					int n = Utils.toInt(temp, -1);
					
					if( n != -1 ) {
						strings[n - 1] = props.get(name);
					}
				}
				
				final StringBuilder sb = new StringBuilder();
				
				for(final String s : strings) {
					sb.append(s).append('\n');
				}
				
				return sb.toString();
			}
		}
		
		return "";
	}
	
	//{list:listname,obj}
	private void lexec(final String listName, final Integer objDBREF) {
		final MUDObject object = database.getById(objDBREF);
		
		if( object != null ) {
			if( propdir(listName, objDBREF).equals(TRUE) ) {
			}
		}
	}
	
	// {prop:name, object}
	// throws PermissionException
	private String prop(final String propName, final Integer objDBREF) {
		final MUDObject object1 = database.getById(objDBREF);

		if( object1 != null ) {
			if( debug_enabled ) System.out.println("Object1: " + object1.getName());

			return "" +  object1.getProperty(propName);
		}
		else return "";
	}
	
	private String propdir(final String propName, final Integer objDBREF) {
		String retval;
		
		final MUDObject object = database.getById(objDBREF);
		
		if( object != null ) {
			Map<String, String> propdir = object.getProperties(propName);
			
			if( propdir != null && propdir.size() > 0) retval = TRUE;
			else                                       retval = FALSE;
			
		}
		else retval = FALSE;
		
		return retval;
	}
	
	private static void fixParams(final List<String> params) {
		ProgramInterpreter.fixParams(params, false);
	}
	
	/**
	 * Fix incorrect breaks in parameters due to splitting on commas
	 * 
	 * TODO should this be private, public or ?
	 * @param params
	 */
	private static void fixParams(final List<String> params, boolean debug) {
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
			if( debug ) {
				System.out.println("---");
				System.out.println("Params: ");
			}

			for(final String s : params) {
				if( debug ) System.out.println(count + " " + s);

				count++;
			}

			// grab the parameter at the current index
			temp = params.get(index);

			if( debug ) {
				System.out.println("---");
				System.out.println("Temp: " + temp);
			}

			while( !done ) {
				leftCurlyCount = Utils.countNumOfChar(temp, '{');
				rightCurlyCount = Utils.countNumOfChar(temp, '}');

				if( debug ) {
					System.out.println("lcc: " + leftCurlyCount);
					System.out.println("rcc: " + rightCurlyCount);
					System.out.println("offset: " + offset);
				}

				//|| leftCurlyCount <= 1 || rightCurlyCount <= 1
				if( leftCurlyCount != rightCurlyCount ) {
					offset++;                                       // increase offset
					temp = temp + "," + params.get(index + offset); // pull in the next param in initial list

					if( debug ) System.out.println("TEMP: " + temp);
				}
				else {
					done = true;
					if( debug ) System.out.println("Result: " + temp);
				}
			}

			if( debug ) {
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

	// TODO fix this, this is low quality function checking
	private static boolean isFunction(final String s) {
		boolean isFunction = false;

		if( s.startsWith("{") && s.endsWith("}") && Utils.countNumOfChar(s, ':') == 1 ) {
			if( Utils.countNumOfChar(s, '{') == Utils.countNumOfChar(s, '}') ) {
				isFunction = true;
			}
		}

		return isFunction;
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
	private static boolean isValidScript(final String script) {
		if (script.startsWith("{") && script.endsWith("}")) {
			final int numLeftBrace = Utils.countNumOfChar(script, '{');
			final int numRightBrace = Utils.countNumOfChar(script, '}');

			if( numLeftBrace != 0 && numRightBrace != 0 && numLeftBrace == numRightBrace ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * lex
	 * 
	 * break the input into tokens
	 * 
	 * NOTE: not currently used
	 * 
	 * @param input
	 * @return
	 */
	public static List<String> lex(final String input) {
		List<String> tokens = new LinkedList<String>();

		StringBuilder sb = new StringBuilder();

		boolean token_found = false;

		for(final char ch : input.toCharArray()) {
			switch(ch) {
			case '{': token_found = true; break;
			case '}': token_found = true; break;
			case '(': token_found = true; break;
			case ')': token_found = true; break;
			case ':': token_found = true; break;
			case ',': token_found = true; break;
			default:  break;
			}

			if( token_found ) {
				if(sb.length() > 0) {
					tokens.add(sb.toString());
					sb.delete(0, sb.length());
				}

				tokens.add("" + ch);

				token_found = false;
			}
			else sb.append(ch);
		}

		/*if( debug_enabled ) {
				for( final String token : tokens) {
					System.out.println(token);
				}

				//System.out.println("Tokens: " + tokens);
			}*/

		for( final String token : tokens) {
			System.out.println(token);
		}

		return tokens;
	}
}