package model;

import java.util.Objects;

public class AuthorPojo {
	private int authorId;
	private String authorFirstName;
	private String authorLastName;
	
	public AuthorPojo(int authorId, String authorFirstName, String authorLastName) {
		super();
		this.authorId = authorId;
		this.authorFirstName = authorFirstName;
		this.authorLastName = authorLastName;
	}

	public int getAuthorId() {
		return authorId;
	}

	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	public String getAuthorFirstName() {
		return authorFirstName;
	}

	public void setAuthorFirstName(String authorFirstName) {
		this.authorFirstName = authorFirstName;
	}

	public String getAuthorLastName() {
		return authorLastName;
	}

	public void setAuthorLastName(String authorLastName) {
		this.authorLastName = authorLastName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(authorFirstName, authorId, authorLastName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AuthorPojo other = (AuthorPojo) obj;
		return Objects.equals(authorFirstName, other.authorFirstName) && authorId == other.authorId
				&& Objects.equals(authorLastName, other.authorLastName);
	}

	@Override
	public String toString() {
		return "AuthorPojo [authorId=" + authorId + ", authorFirstName=" + authorFirstName + ", authorLastName="
				+ authorLastName + "]";
	}
}
