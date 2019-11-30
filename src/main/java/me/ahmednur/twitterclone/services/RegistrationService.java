package me.ahmednur.twitterclone.services;

import me.ahmednur.twitterclone.models.User;
import me.ahmednur.twitterclone.repositories.UserRepository;
import me.ahmednur.twitterclone.util.UsernameExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;

@Service
public class RegistrationService {
    @Autowired
    UserRepository userRepo;
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    public void registerAccount(String username, String tag, String displayName, String password) throws UsernameExistsException {
        try {
            userDetailsService.loadUserByUsername(username);
            throw new UsernameExistsException("Username unavailable");
        } catch (UsernameNotFoundException e) {
            if (userRepo.findByTag(tag) != null) {
                throw new EntityExistsException();
            }
            User user = new User();
            user.setUsername(username);
            user.setTag(tag);
            user.setDisplayName(displayName);
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setDateCreated(System.currentTimeMillis());
            userRepo.save(user);
        }
    }
}
