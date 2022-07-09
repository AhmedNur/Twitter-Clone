package twitterclone.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import twitterclone.models.User;
import twitterclone.repositories.UserRepository;
import twitterclone.util.UsernameExistsException;

import javax.persistence.EntityExistsException;

@Service
public class RegistrationService {
    @Autowired
    UserRepository userRepo;
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    public void registerAccount(String username, String tag, String displayName, String password) throws UsernameExistsException {
        try{
            userDetailsService.loadUserByUsername(username);
            throw new UsernameExistsException("Username unavailable");
        } catch (UsernameNotFoundException e){
            User user = new User();
            user.setUsername(username);
            if(userRepo.findByTag(tag) != null){
                throw new EntityExistsException();
            }
            user.setTag(tag);
            user.setDisplayName(displayName);
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            userRepo.save(user);
        }
    }
}
