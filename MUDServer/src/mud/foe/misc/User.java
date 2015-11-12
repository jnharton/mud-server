package mud.foe.misc;

public class User {
	private String name;
	private String password;
	private Integer perm;
	
	public User(final String userName, final String userPass, final Integer userPerm) {
		this.name = userName;
		this.password = userPass;
		this.perm = userPerm;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPassword() {
		return this.password;
	}
}