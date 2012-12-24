package mud.interfaces;

public interface Filter {
	
	public Object filter(Object o, Filter...filters);

}