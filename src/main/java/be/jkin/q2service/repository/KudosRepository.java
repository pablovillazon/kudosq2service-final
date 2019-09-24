package be.jkin.q2service.repository;

import be.jkin.q2service.model.Kudos;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface KudosRepository extends CrudRepository<Kudos, String> {
    Kudos findBy_id(ObjectId _id);

    @Query(value="{ 'destino' : ?0 }", fields = "{'tema' : 1, 'fuente' : 1}")
    List<Kudos> findByDestino(String destino); //nickname
}
