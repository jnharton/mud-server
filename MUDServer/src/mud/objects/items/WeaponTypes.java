package mud.objects.items;

import mud.objects.items.WeaponType.DamageType;

public final class WeaponTypes {
	public static final WeaponType LONGSWORD = new WeaponType(0, "Long Sword", DamageType.SLASHING, "1d6", 19, 20, 2, 2.0);
	public static final WeaponType RAPIER = new WeaponType(1, "Rapier", DamageType.PIERCING, "1d4", 18, 20, 2, 2.0);
	public static final WeaponType SCIMITAR = new WeaponType (2, "Scimitar", DamageType.SLASHING, "1d4", 18, 20, 2, 4.0);
	public static final WeaponType REVOLVER = new WeaponType(3, "Revolver", null, "10", -1, -1, 1, 3.0);
	
	public static final WeaponType getWeaponType(int id) {
		switch(id) {
		case 0: return LONGSWORD;
		case 1: return RAPIER;
		case 2: return SCIMITAR;
		case 3: return REVOLVER;
		default: return null;
		}
	}
}