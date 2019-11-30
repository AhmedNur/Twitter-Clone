package me.ahmednur.twitterclone.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NonNull
    private String username;
    @NonNull
    private String password;
    private boolean enabled;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;
    private String tag;
    private String displayName;
    private String bio;
    private Long dateCreated;
    @ElementCollection
    private Collection<Long> tweets;
    @ElementCollection
    private Collection<Long> followed;
    @ElementCollection
    private Collection<Long> followers;
    @ElementCollection
    private Collection<Long> likedTweets;
    @ElementCollection
    private Collection<Long> retweets;
    @ElementCollection
    private Map<Long, Long> parentIdToRetweetId;

    @JsonIgnore
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    public User() {

    }

    public User(String username, String password, Long id) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.enabled = true;
        this.credentialsNonExpired = true;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
    }
}
