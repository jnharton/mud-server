package mud.utils;

import java.util.*;

public class EditList {
    final public String name;

    private int currentLine;
    private LinkedList<String> lines = new LinkedList<String>();
    
    public EditList(final String n) {
        name = n;
    }

    public EditList(final String n, List<String> init) {
        name = n;
        if (lines != null)  lines = new LinkedList<String>(init);
    }

    public int getNumLines() {
        return lines.size();
    }

    public void setLineNum(final int n) {
        if (n >= 0 && n < lines.size()) currentLine = n;
    }

    public int getLineNum() {
        return currentLine;
    }

    public String getCurrentLine() {
        return lines.isEmpty() ? "" : lines.get(currentLine);
    }

    public String getLastLine() {
        return lines.isEmpty() ? "" : lines.get(lines.size() - 1);
    }

    public List<String> getLines() {
        return new LinkedList<String>(lines);
    }

    public void addLine(final String line) {
        lines.add(line);
        currentLine = lines.size() - 1;
    }
    
    public void setLine(final int lineNum, final String line) {
    	lines.set(lineNum, line);
    	if( currentLine < lines.size() ) { currentLine += 1; }
    }

    public void removeLine(final int n) {
        if (n >= 0 && n < lines.size()) {
            lines.remove(n);
            if (currentLine == n && currentLine > 0)   currentLine -= 1;
        }
    }
}
