package mud;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import mud.net.Client;
import mud.objects.Item;
import mud.objects.Player;
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
	private final OutputStream error;
	
	private Hashtable<String, String> vars;
	private boolean use_vars;

	public ProgramInterpreter(final MUDServer parent) {
		this.parent = parent;
		this.error = null;
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

	/**
	 * Function to evaluate a script/program
	 * 
	 * @param script
	 * @return
	 */
	public String interpret(final String script, final Client client)
	{	
		//System.out.println("PGM: <" + script + ">");
		//System.out.println("pArg: " + script);

		System.out.println("Interpret: " + script);

		if (script.startsWith("{") && script.endsWith("}")) {
			if( script.indexOf(":") != -1 ) {
				String work = script.substring(1, script.length() - 1);
				//String work = script.replace("{", "").replace("}", "");

				System.out.println("work: " + work);

				String[] temp = work.split(":", 2);

				String functionName = temp[0];
				List<String> params = null;

				System.out.println("Function: " + functionName);

				if( temp.length > 1 ) {
					params = Utils.mkList(temp[1].split(","));
					
					for(int i = 0; i < params.size(); i++) {
						if( ( params.get(i).startsWith("{") && !params.get(i).endsWith("}") ) 
								&& ( !params.get(i+1).startsWith("}") && params.get(i+1).endsWith("}") ) ) {
							params.set(i, params.get(i) + "," + params.get(i+1));
							params.remove(i+1);
						}
					}

					System.out.println("Params: " + params);

					int index = 0;

					if( !functionName.equals("if") && !functionName.equals("with") ) {
						for(String param : params) {
							//System.out.println("Param (before): " + param);
							if( param.startsWith("{") && param.endsWith("}") ) {
								params.set(index, interpret(param, client));
								//System.out.println("Param (after): " + param);
							}
							index++;
						}
					}

					System.out.println("Evaluate: <" + functionName + "> with " + params);
					//return evaluate(functionName, params.toArray(new String[params.size()]), client);
					String result = evaluate(functionName, params.toArray(new String[params.size()]), client);
					System.out.println("Result: " + result);
					return result;
				}
				else { return "Incomplete function statement, no parameters!"; }
			}
			else {
				return evaluate(script, new String[0], client);
			}
		}

		return "Invalid Script!";
	}

	/*class Script {
		List<String> lines;

		public List<String> getLines() {
			return this.lines;
		}
	}

	private void execute(Script s) {
		for(String string : s.getLines()) {
			interpret(string, null);
		}
	}*/

	private String evaluate(final String functionName, final String[] params, final Client client) {
		if( params.length > 0 ) {
			if( params.length == 1 ) {
				if(functionName.equals("create_item")) {
					final Item item = parent.createItem(params[0]);

					if( item != null ) {
						return "" + item.getDBRef();
					}
					else {
						return "-1";
					}
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
				
				if (functionName.equals("colors")) {
					if (params.length >= 2) {
						parent.debug("Color: " + params[0]);
						parent.debug("Text: " + params[1]);

						return parent.colorCode(params[0]) + params[1] + parent.colorCode("white");
					}
					else { return "PGM: Error!"; }
				}
				else if(functionName.equals("eq")) { // {eq:<string 1>,<string 2>} -> {eq:test,test} -> :true
					if( params[0].startsWith("{") && params[0].endsWith(")") ) {
						params[0] = interpret(params[0], client);
					}
					
					final Integer first = Integer.parseInt(params[0]);
					final Integer second = Integer.parseInt(params[1]);

					System.out.println("first: " + first);
					System.out.println("Second: " + second);
					
					if( first == second) {
						return ":true";
					}
					else {
						return ":false";
					}
				}
				//else { return "Incomplete function statement, no parameters!"; }
				else if (functionName.equals("give")) {
					final Player p = parent.getPlayer(Utils.toInt(params[0], -1));
					final Item i = parent.getItem(Utils.toInt(params[1], -1));

					if( p != null && i != null ) {
						i.setLocation(p.getDBRef());
						p.getInventory().add(i);
					}

					return "";
				}
				else if(functionName.equals("prop")) { // {prop:<property name>,<dbref of object to get property from>}
					final String property = params[0];
					final int dbref = Utils.toInt(params[1], -1);
					
					final MUDObject object = parent.getObject(dbref);
					
					if( object != null ) return (String) object.getProperty(property);
					else return "";
					//else { return "Incomplete function statement, no parameters!"; }
				}
				else { return "PGM: No such function!"; }
			}
			else if( params.length == 3 ) {
				if(functionName.equals("if")) {
					final String result = interpret(params[0], client);

					System.out.println("result: " + result);

					if( result.equals(":true") ) {
						return interpret(params[1], client);

					}
					else if( result.equals(":false") ) {
						return interpret(params[2], client);
					}
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
				else if (functionName.equals("exec")) {
					if( params != null ) {
						if( params.length == 1 ) {


						}
						else if( params.length == 2 ) {

						}
						else {

						}
					}
					else {};

					return "";
				}
				else if (functionName.equals("tell")) {
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
							if( !var_defined ) vars.put(last_var, interpret(param, client));
							else params1.set(index, interpret(param, client));
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
			case "{&player}":
				return "" + parent.getPlayer(client).getDBRef();
			case "{name}":
				return parent.getServerName();
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
	
	private static boolean isFunction(String s) {
		if( s.startsWith("{") && s.endsWith("}") ) return true;
		else return false;
	}

	public static void main(String[] args) {
		ProgramInterpreter p = new ProgramInterpreter(null);
		p.lex("{distance:(0,0),(1,1)}");
		p.lex("{rainbow:output this string in rainbow colors}");
		System.out.println(p.interpret("{distance:(0,0),(2,3)}", null));
	}
}