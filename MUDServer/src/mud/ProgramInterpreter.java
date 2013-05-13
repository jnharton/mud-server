package mud;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

	MUDServer parent;

	public ProgramInterpreter(MUDServer parent) {
		this.parent = parent;
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

	private void parse(final String statement) {
		lex(statement);	
	}

	/**
	 * Function to evaluate a script/program
	 * 
	 * @param pArg
	 * @return
	 */
	public String interpret(final String pArg)
	{	
		System.out.println("PGM: <" + pArg + ">");
		System.out.println("pArg: " + pArg);

		String[] ca = null;

		if (pArg.indexOf(":") != -1) {
			ca = pArg.split(":");

			System.out.println("Input: " + Arrays.asList(ca));

			if (ca[0].equals("{colors")) {
				String[] params = ca[1].substring(0, ca[1].indexOf("}")).split(",");

				System.out.println("Params: " + Arrays.asList(params));

				if (params.length >= 2) {
					parent.debug("Color: " + params[0]);
					parent.debug("Text: " + params[1]);

					return "-Result: " + parent.colorCode(params[0]) + params[1] + parent.colorCode("white");
				}
				else { return "PGM: Error!"; }
			}
			else if(ca[0].equals("{distance")) {

				/*
				 * parameters:
				 * 	2d/3d
				 *  one or two points
				 */

				System.out.println("PGM -distance-");

				if( ca[1] != null ) {
					List<Point> ptList = Utils.toPoints(ca[1]);

					if( ptList != null ) {
						return "-Result: " + ptList + "; distance is " + MUDServer.distance(ptList.get(0), ptList.get(1));
					}
					else { return "PGM: Error!"; }
				}
				else
				{
					return "-Result: Incomplete function statement, no parameters!";
				}
			}
			else if (ca[0].equals("{rainbow")) {
				if (ca[1] != null)
				{
					String param = ca[1].substring(0, ca[1].indexOf("}"));

					if (param != null) {
						return "-Result: " + parent.rainbow(param) + parent.colorCode("white");
					}
					else { return "PGM: Error!"; }
				}
				else
				{
					return "-Result: Incomplete function statement, no parameters!";
				}
			}
			else { return "PGM: Error!"; }
		}
		/*else if(ca[0].equals("{create")) {
			return "PGM: Error!";
		}*/
		else  if (pArg != null) {
			switch(pArg) {
			case "{name}":
				return "-Result: " + parent.getServerName();
			case "{version}":
				return "-Result: " + MUDServer.getName() + " " + MUDServer.getVersion();
			case "{colors}":
				return "-Result: Incomplete function statement, no inputs!";
			default:
				return "PGM: No such function! (1)";
			}
		}
		else {			
			return "PGM: Error!";
		}
	}

	public static void main(String[] args) {
		ProgramInterpreter p = new ProgramInterpreter(null);
		p.parse("{distance:(0,0),(1,1)}");
		p.parse("{rainbow:output this string in rainbow colors}");
		System.out.println(p.interpret("{distance:(0,0),(2,3)}"));
		System.out.println(p.interpret("{rainbow:output this string in rainbow colors}"));
	}
}