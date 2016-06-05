package mud.foe.misc;

public interface IODevice extends Device {
	public void write(final String string);
	public String read();
}