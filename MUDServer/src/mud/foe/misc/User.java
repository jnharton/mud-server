package mud.foe.misc;

public class User {
	public static final int USER = 0;
	public static final int ADMIN = 1;
	
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
	
	public void setPerm(final int newPerm) {
		this.perm = newPerm;
	}
	
	public Integer getPerm() {
		return this.perm;
	}
}