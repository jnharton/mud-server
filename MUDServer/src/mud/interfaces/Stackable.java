package mud.interfaces;

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

/**
 * Defines an interface for "stackable" objects. (money?)
 * 
 * @author Jeremy
 *
 * @param <E> Some object type that will implement stackable.
 */
public interface Stackable<E> {
	public int maxDepth = 10;
	
	/**
	 * Takes a stackable object e and returns a boolean
	 * indicating whether it could be stacked, in theory I could
	 * try to stack potions and arrows, but that would be invalid.
	 * So stacking identical arrow types with each other would work.
	 * 
	 * i.e.
	 * Flaming Arrow (1) stacks with Flaming Arrow (1) to give Flaming Arrow(2)
	 * 
	 * @param object
	 * @return
	 */
	boolean stack(E object);
	
	/**
	 * Split a stack, the number supplied is how many you'd like to split
	 * from the given stack.
	 * 
	 * @param number
	 * @return
	 */
	E split(int number);
	
	/**
	 * Calculate and return the size of the stack, use of recursion
	 * is recommended in implementation.
	 * 
	 * @return
	 */
	int stackSize();
}