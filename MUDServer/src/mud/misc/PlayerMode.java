package mud.misc;

/**
 * Mode setting for players to indicate a state.
 * 
 * Normal - normal play Combat - combat (entered when in combat, game
 * behaves a little differently) PVP - player vs. player (when you are
 * somewhere when you can PK - player kill (a mode where you are allowed to
 * kill other players)
 *
 * 
 * @see NOTE: I may need to revise definitions or change this, because I
 *      technically want player killing to always be a possiblity.
 * 
 * 
 * @author Jeremy
 *
 */
public enum PlayerMode {
	NORMAL, COMBAT, PVP, PK
};