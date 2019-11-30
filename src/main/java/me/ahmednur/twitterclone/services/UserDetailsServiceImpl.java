package me.ahmednur.twitterclone.services;

import me.ahmednur.twitterclone.models.User;
import me.ahmednur.twitterclone.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Check Credentials");
        }
        return user;
    }

    private Collection<GrantedAuthority> getAuthorities(User u) {
        Collection<GrantedAuthority> authorities = u.getAuthorities();
        return authorities;
    }
}
