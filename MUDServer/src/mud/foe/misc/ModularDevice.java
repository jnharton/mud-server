package mud.foe.misc;

import java.util.List;

public interface ModularDevice extends Device {
	public List<Module> getModules();
	
	public boolean addModule(Module mod);
	
	public boolean removeModule(Module mod);
	
	public Module getModule(final String moduleName);
}