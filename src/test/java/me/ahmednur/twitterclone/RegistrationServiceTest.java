package me.ahmednur.twitterclone;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.ahmednur.twitterclone.repositories.UserRepository;
import me.ahmednur.twitterclone.services.RegistrationService;
import me.ahmednur.twitterclone.services.UserDetailsServiceImpl;
import me.ahmednur.twitterclone.util.UsernameExistsException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import me.ahmednur.twitterclone.models.User;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class RegistrationServiceTest {
    @Mock
    UserRepository userRepo;
    @Mock
    UserDetailsServiceImpl userDetailsService;
    @InjectMocks
    RegistrationService registrationService;

    public RegistrationServiceTest(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenValidNonTakenRegistrationDetails_whenRegisterAccount_thenSaveEnabledUserWithEncodedPasswordInRepo() throws UsernameExistsException {
        val username = "username";
        val password = "password";
        val tag = "Tag";
        val displayName = "displayName";
        val expected = new User();
        expected.setUsername(username);
        expected.setPassword(new BCryptPasswordEncoder().encode(password));
        expected.setTag(tag);
        expected.setDisplayName(displayName);

        Mockito.when(userDetailsService.loadUserByUsername(username))
                .thenThrow(UsernameNotFoundException.class);
        registrationService.registerAccount(username, tag, displayName, password);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepo).save(userCaptor.capture());

        val result = userCaptor.getValue();
        Assertions.assertThat(result.getUsername()).isEqualTo(expected.getUsername());
        Assertions.assertThat(result.getTag()).isEqualTo(expected.getTag());
        Assertions.assertThat(result.getDisplayName()).isEqualTo(expected.getDisplayName());
        Assertions.assertThat(password).isNotEqualTo(expected.getPassword());
    }

    @Test(expected = UsernameExistsException.class)
    public void givenTakenUsername_whenRegisterAccount_thenThrowUsernameExistsException() throws UsernameExistsException {
        User taken = new User();
        taken.setUsername("takenUsername");
        taken.setPassword(new BCryptPasswordEncoder().encode("password1"));

        Mockito.when(userDetailsService.loadUserByUsername("takenUsername")).thenReturn(taken);

        registrationService.registerAccount("takenUsername", "tag", "name", "password2");
    }
}
