package mud.utils;

/**
 * Class to hold data for Character Generation (a.k.a. 'chargen')
 * 
 * t      - what part of the evaluation we are in (0=input, 1=answer)
 * step   - what step we are on
 * answer - what answer we picked at the current step
 * 
 * @author Jeremy
 *
 */
public class cgData {
	public int t;
	public int step;
	public int answer;

	public cgData(int t, int step, int answer) {
		this.t = t;
		this.step = step;
		this.answer = answer;
	}
}