package mud.utils;

import java.util.*;

public class EditList {
    final public String name;

    private int currentLine;
    private LinkedList<String> lines = new LinkedList<String>();
    
    public EditList(final String n) {
    	this.name = n;
    }

    public EditList(final String n, List<String> init) {
    	this.name = n;
        this.lines = new LinkedList<String>(init);
    }

    public int getNumLines() {
        return this.lines.size();
    }

    public void setLineNum(final int n) {
    	if (n >= 0 && n <= this.lines.size()) this.currentLine = n;
    }

    public int getLineNum() {
        return this.currentLine;
    }

    public String getCurrentLine() {
        return atEnd() ? "" : this.lines.isEmpty() ? "" : this.lines.get(this.currentLine);
    }

    public String getLastLine() {
        return this.lines.isEmpty() ? "" : this.lines.get(this.lines.size() - 1);
    }

    public List<String> getLines() {
        return new LinkedList<String>(this.lines);
    }

    public void addLine(final String line) {
    	if( atEnd() ) {
    		this.lines.add(line);
    		this.currentLine = this.lines.size();
    	}
    	else {
    		this.lines.add(this.currentLine, line);
    		this.currentLine++;
    	}
    }
    
    public void setLine(final int lineNum, final String line) {
    	this.lines.set(lineNum, line);
    	if( this.currentLine < this.lines.size() ) { this.currentLine += 1; }
    }

    public void removeLine(final int n) {
        if (n >= 0 && n < this.lines.size()) {
        	this.lines.remove(n);
            if (this.currentLine == n && this.currentLine > 0) this.currentLine -= 1;
        }
    }
    
    public boolean atEnd() {
    	return this.currentLine == this.lines.size();
    }
}
