package com.luminos.woosh.domain.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.luminos.woosh.domain.Acceptance;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.Scan;
import com.luminos.woosh.util.RandomString;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author Ben
 */
@Entity(name="users")
public class User implements UserDetails, Comparable<User> {

	private static final long serialVersionUID = 2667226600487694113L;

	private static final Integer INVITATION_KEY_LENGTH = 6;	

	public static final User ANONYMOUS_USER = new User("anonymous-user");	

	
	@Id
	@GeneratedValue(generator="default")
	@GenericGenerator(name="default", strategy="uuid", parameters={@Parameter(name="separator",value="-")})
	private String id = null;

	@Version
	private Integer version = null;

	@Column(nullable=false)
	private String username = null;

	@Column(nullable=false)
	private String password = null;
	
	@Column(nullable=false)
	private String email = null;

	private boolean accountNonExpired = true;
	
	private boolean accountNonLocked = true;
	
	private boolean credentialsNonExpired = true;
	
	private boolean enabled = Boolean.TRUE;

	@ManyToMany
	private List<Role> authorities = null;

	@Type(type="org.hibernatespatial.GeometryUserType")
	private Point lastKnownLocation = null;
	
	// last login time from the users device (not the web)
	private Timestamp lastLogin = null;
	
	// the creation date for the user
	private Timestamp memberSince = null;
			
	private String invitationalKey = null;
	
	@OneToOne
	private User invitedBy = null;
	
	// the list of cards that this user owns (created)
	@ManyToMany
	private List<Card> cards = null;

	// the list of cards that this user has taken from other users
	@OneToMany
	private List<Acceptance> acceptances = null;

	// we keep a log of the scans (wooshes) that a user performs
	@OneToMany
	private List<Scan> scans = null;
	
	
	public User() {
		
	}

	private User(String username) {
		this.username = username;
	}
	
	public User(String username, String password, String email) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.invitationalKey = new RandomString(INVITATION_KEY_LENGTH).nextString();

		// set the sign-up time of the user
		Calendar c = Calendar.getInstance();
		this.memberSince = new Timestamp(c.getTimeInMillis());
	}

	public User(String username, String password, String email, User invitedBy) {
		this(username, password, email);
		this.invitedBy = invitedBy;
	}
	
	@Override
	public int compareTo(User other) {
		return this.getUsername().compareTo(other.getUsername());
	}

	public void addAuthority(Role authority) {
		if (this.authorities == null) {
			this.authorities = new ArrayList<Role>();
		}
		this.authorities.add(authority);
	}

	public void revokeAuthority(Role authority) {
		if (this.authorities == null) {
			return;
		}
		this.authorities.remove(authority);
	}

	public List<String> getRoles() {
		List<String> roleNames = new ArrayList<String>();
		
		for (GrantedAuthority auth : this.getAuthorities()) {
			if (auth != null) { 
				roleNames.add(auth.getAuthority());
			}
		}
		
		return roleNames;
	}

	/**
	 * 
	 * @param offer
	 */
	public void addCard(Card card) {
		if (card == null) {
			throw new IllegalArgumentException("The card argument must not be NULL.");
		}
		
		if (this.cards == null) {
			this.cards = new ArrayList<Card>();
		}
		
		this.cards.add(card);
	}

	/**
	 * 
	 * @param acceptance
	 */
	public void addAcceptance(Acceptance acceptance) {
		if (acceptance == null) {
			throw new IllegalArgumentException("The acceptance argument must not be NULL.");
		}
		
		if (this.acceptances == null) {
			this.acceptances = new ArrayList<Acceptance>();
		}
		
		this.acceptances.add(acceptance);
	}
	
	/**
	 * 
	 * @param scan
	 */
	public void addScan(Scan scan) {
		if (scan == null) {
			throw new IllegalArgumentException("The scan argument must not be NULL.");
		}
		
		if (this.scans == null) {
			this.scans = new ArrayList<Scan>();
		}
		
		this.scans.add(scan);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<GrantedAuthority> getAuthorities() {
		return new ArrayList<GrantedAuthority>(this.authorities);
	}

	public void setAuthorities(List<Role> authorities) {
		this.authorities = authorities;
	}

	public Point getLastKnownLocation() {
		return lastKnownLocation;
	}

	public void setLastKnownLocation(Point lastKnownLocation) {
		this.lastKnownLocation = lastKnownLocation;
	}

	public Timestamp getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Timestamp lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Timestamp getMemberSince() {
		return memberSince;
	}

	public void setMemberSince(Timestamp memberSince) {
		this.memberSince = memberSince;
	}

	public String getInvitationalKey() {
		return invitationalKey;
	}

	public void setInvitationalKey(String invitationalKey) {
		this.invitationalKey = invitationalKey;
	}

	public User getInvitedBy() {
		return invitedBy;
	}

	public void setInvitedBy(User invitedBy) {
		this.invitedBy = invitedBy;
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}

	public List<Acceptance> getAcceptances() {
		return acceptances;
	}

	public void setAcceptances(List<Acceptance> acceptances) {
		this.acceptances = acceptances;
	}

	public List<Scan> getScans() {
		return scans;
	}

	public void setScans(List<Scan> scans) {
		this.scans = scans;
	}

}
