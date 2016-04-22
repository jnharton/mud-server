package mud.utils;

import java.util.ArrayList;
import java.util.List;

public class NTuple<E> {
	private List<E> elements;
	
	@SuppressWarnings("unchecked")
	public NTuple(final E...elements) {
		this.elements = new ArrayList<E>(elements.length);
		
		for(final E e : elements) {
			this.elements.add(e);
		}
	}
	
	public E get(int index) throws IndexOutOfBoundsException {
		if( index < numValues() ) {
			return this.elements.get(index);
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	public int numValues() {
		return this.elements.size();
	}
}