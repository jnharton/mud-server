package mud;

import java.util.ArrayList;
import java.util.List;

import mud.utils.Utils;

public class ProgramInterpreter {
	
	MUDServer parent;
	
	public ProgramInterpreter(MUDServer parent) {
		this.parent = parent;
	}
	
	/**
	 * Function to evaluate a script/program
	 * 
	 * @param pArg
	 * @return
	 */
	public String interpret(final String pArg)
	{
		System.out.println("pArg: " + pArg);

		String[] ca = new String[0];

		if (pArg.indexOf(":") != -1)
		{
			ca = pArg.split(":");

			for (final String s : ca) {
				System.out.println(s);
			}

			if (pArg.equals("{colors}") || ca[0].equals("{colors")) {
				if (ca[1] != null)
				{
					String[] params = ca[1].substring(0, ca[1].indexOf("}")).split(",");

					parent.debug("Color: " + params[0]);

					if (params.length >= 2) {
						return "-Result: " + parent.colorCode(params[0]) + params[1] + parent.colorCode("white");
					}
					else { return "PGM: Error!"; }
				}
				else
				{
					return "-Result: Incomplete function statement, no parameters!";
				}
			}
			else if (pArg.equals("{rainbow}") || ca[0].equals("{rainbow")) {
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
			else if(pArg.equals("{distance}") || ca[0].equals("{distance")) {

				/*
				 * parameters:
				 * 	2d/3d
				 *  one or two points
				 */

				System.out.println("PGM -distance-");

				if( ca[1] != null ) {
					// params - two points in the form '(x,y)' separated by a ';'.
					// ex. (1,1),(4,4)

					List<String> params = new ArrayList<String>();

					StringBuffer sb = new StringBuffer();

					boolean check = true;
					int dimensions = 0;  // count points, 1 point is between your current position and there, 2 points is between the two points

					// isolate coordinate points
					for(int c = 0; c < ca[1].length(); c++) {
						char ch = ca[1].charAt(c);

						if( check ) {
							if(ch == ')') {
								parent.debug("Current: " + sb.toString(), 3);
								parent.debug("Added character to sb", 3);
								sb.append(ch);
								parent.debug("Final: " + sb.toString(), 3);
								parent.debug("finished point dec", 3);
								check = false;
								params.add(sb.toString());
								sb.delete(0, sb.length());
							}
							else {
								parent.debug("Current: " + sb.toString(), 3);
								parent.debug("Added character to sb", 3);
								sb.append(ch);
							}
						}
						else {
							if(ch == '(') {
								sb.delete(0, sb.length());
								parent.debug("started point dec", 3);
								parent.debug("Current: " + sb.toString(), 3);
								parent.debug("Added character to sb", 3);
								sb.append(ch);
								check = true;
							}
							else if(ch == ',') {

							}
						}
					}

					if(params.size() > 0) {

						List<Point> ptList = new ArrayList<>();

						for(String param : params) {
							ptList.add(Utils.toPoint(param));
						}

						return "-Result: " + ptList + "; distance is " + parent.distance(ptList.get(0), ptList.get(1));

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
		else if (pArg != null)
		{
			if ( pArg.equals("{name}") )
			{
				return "-Result: " + parent.getServerName();
			}
			else if ( pArg.equals("{version}") )
			{
				return "-Result: " + MUDServer.getName() + " " + MUDServer.getVersion();
			}
			else if (pArg.equals("{colors}") || ca[0].equals("{colors"))
			{
				return "-Result: Incomplete function statement, no inputs!";
			}
			else if ( pArg.equals("{tell}") )
			{
				String m = "";
				String r = "";
				if (ca[1] != null)
				{
					return "-Result: You tell " + m + " to " + ca[1] + ".";
				}
				else
				{
					return "-Result: You tell " + m + " to " + r + ".";
				}
			}
			else
			{
				return "PGM: No such function! (1)";
			}
		}

		return "PGM: Error!";
	}
}