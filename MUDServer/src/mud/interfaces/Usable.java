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

import java.util.List;

import mud.magic.Spell;
import mud.misc.Effect;

/**
 * Defines an interface for "usable" objects. (i.e. potions, wands, etc)
 * 
 * @author Jeremy
 *
 * @param <T> Some object type that will implement usable.
 */
public interface Usable<T> {
	public Spell getSpell();
	
	public List<Spell> getSpells();
	
	public Effect getEffect();
	
	/**
	 * 
	 * NOTE: Object types that either have no effects, or only one
	 * should implement this method as below.
	 * 
	 * return new List<Effect>(); // return an empty list with no effects by default
	 * 
	 * @return
	 */
	public List<Effect> getEffects();
}