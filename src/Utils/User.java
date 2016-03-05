package Utils;

public class User {
	private String name;
	private String user_link;
	private String icon_link;
	private String other;
	
	public User() {
		super();
		// TODO Auto-generated constructor stub
	}
	public User(String name, String user_link, String icon_link, String other) {
		super();
		this.name = name;
		this.user_link = user_link;
		this.icon_link = icon_link;
		this.other = other;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUser_link() {
		return user_link;
	}
	public void setUser_link(String user_link) {
		this.user_link = user_link;
	}
	public String getIcon_link() {
		return icon_link;
	}
	public void setIcon_link(String icon_link) {
		this.icon_link = icon_link;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	@Override
	public String toString() {
		return "User [name=" + name + ", user_link=" + user_link
				+ ", icon_link=" + icon_link + ", other=" + other + "]";
	}
}
