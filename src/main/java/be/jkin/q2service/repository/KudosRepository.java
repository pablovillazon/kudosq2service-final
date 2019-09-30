package be.jkin.q2service.repository;

import be.jkin.q2service.model.Kudos;
//import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;


public interface KudosRepository extends CrudRepository<Kudos, String> {
    Kudos findById(UUID id);
    List<Kudos> findByDestino(String destino); //nickname
}
