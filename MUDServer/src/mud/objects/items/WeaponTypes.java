package mud.objects.items;

import mud.objects.items.WeaponType.DamageType;

public final class WeaponTypes {
	// int id, String name, DamageType dType, String damage, int critMin, int critMax, int critical, double weight 
	public static final WeaponType LONG_SWORD = new WeaponType(0, "Long Sword", DamageType.SLASHING, "1d6",  19, 20, 2, 2.0);
	public static final WeaponType RAPIER     = new WeaponType(1, "Rapier",     DamageType.PIERCING, "1d4",  18, 20, 2, 2.0);
	public static final WeaponType SCIMITAR   = new WeaponType(2, "Scimitar",   DamageType.SLASHING, "1d4",  18, 20, 2, 4.0);
	public static final WeaponType REVOLVER   = new WeaponType(3, "Revolver",   DamageType.PIERCING, "10d1", -1, -1, 1, 3.0);
	public static final WeaponType BOW        = new WeaponType(4, "Bow",        DamageType.PIERCING, "2d8",  15, 20, 2, 7.0);
	
	public static final WeaponType getWeaponType(int id) {
		switch(id) {
		case 0: return LONG_SWORD;
		case 1: return RAPIER;
		case 2: return SCIMITAR;
		case 3: return REVOLVER;
		case 4: return BOW;
		default: return null;
		}
	}
}