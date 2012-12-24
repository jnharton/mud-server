package mud.interfaces;

/**
 * Defines an interface to be implemented by projectile objects. Objects
 * that might implement this interface are classes that represent arrows,
 * throwing knives, throwing daggers, magical constructs/missiles/arrows (may
 * be comprised only of energy), throwing axes, throwing daggers, etc.
 * 
 * With the exception of some objects, like arrows, whose only purpose is to be
 * a moving thing, most such objects will probably also be named/typed as
 * 'throwing' somehow.
 * 
 * @author Jeremy
 *
 * @param <T>
 */
public interface Projectile<T> {
}