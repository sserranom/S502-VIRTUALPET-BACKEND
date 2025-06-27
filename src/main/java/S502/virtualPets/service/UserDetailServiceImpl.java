package S502.virtualPets.service;

import S502.virtualPets.controller.dto.AuthLoginRequestDTO;
import S502.virtualPets.controller.dto.AuthResponseDTO;
import S502.virtualPets.persistence.entity.UserEntity;
import S502.virtualPets.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = userRepository.findUserEntityByUseName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " does not exist"));

        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();

        userEntity.getRoles()
                .forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))));

        userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissionEntities().stream())
                .forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission.getPermissionsEnum().name())));

        return new User(userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.isEnable(),
                userEntity.isAccountNotExpired(),
                userEntity.isCredentialNoExpired(),
                userEntity.isAccountNoLocked(),
                authorityList);

    }

    public AuthResponseDTO loginUser(AuthLoginRequestDTO authLoginRequestDTO){

    }
}
