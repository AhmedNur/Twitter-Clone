package me.ahmednur.twitterclone;

import lombok.val;
import me.ahmednur.twitterclone.repositories.UserRepository;
import me.ahmednur.twitterclone.services.UserDetailsServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import me.ahmednur.twitterclone.models.User;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsServiceTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    public UserDetailsServiceTest(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenValidUsername_whenLoadUserByUsername_thenReturnUser() {
        val username = "username";
        val tag = "tag";
        val name = "name";
        val password = "password";
        User expected = new User();
        expected.setUsername(username);
        expected.setPassword(new BCryptPasswordEncoder().encode(password));
        expected.setTag(tag);
        expected.setDisplayName(name);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(expected);

        Assertions.assertThat(userDetailsService.loadUserByUsername(username)).isEqualTo(expected);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void givenNonExistantUsername_whenLoadUserByUsername_thenThrowUsernameNotFoundException() {
        val username = "DoesNotExist";

        Mockito.when(userRepository.findByUsername(username)).thenReturn(null);

        val actual = userDetailsService.loadUserByUsername(username);
    }
}
