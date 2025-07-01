package S502.virtualPets.service;

import S502.virtualPets.persistence.repository.PetRepository;
import S502.virtualPets.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;


}
