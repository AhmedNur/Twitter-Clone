package twitterclone.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import twitterclone.models.User;
import twitterclone.repositories.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("Check Credentials");
        }
        return user;
    }

    private Collection<GrantedAuthority> getAuthorities(User u) {
        Collection<GrantedAuthority> authorities = u.getAuthorities();
        return authorities;
    }
}
