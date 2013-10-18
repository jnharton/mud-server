package mud.utils;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

public enum GameMode {
	NORMAL("Normal"),           // normal      - normal gameplay
	DEVELOPMENT("Development"), // development - no new player creation at login screen
	WIZARD("Wizard"),           // wizard      - no logins by anyone with less than WIZARD permissions
	MAINTENANCE("Maintenance"); // maintenance - no logins by anyone with less than GOD permissions

	final private String name;

	private GameMode(String name) {
		this.name = name;
	}

    static public boolean isValidString(final char c) {
		switch (c) {
            case 'n':   return true;
            case 'd':   return true;
            case 'w':   return true;
            case 'm':   return true;
            default:    return false;
        }
    }

    static public GameMode fromChar(final char c) {
		switch (c) {
			case 'n':   return GameMode.NORMAL;
			case 'd':   return GameMode.DEVELOPMENT;
            case 'w':   return GameMode.WIZARD;
            case 'm':   return GameMode.MAINTENANCE;
            default:    return GameMode.NORMAL;
        }
    }

	public String toString() {
		return this.name;
	}
}