package be.jkin.q2service.controllers;

import be.jkin.q2service.model.Kudos;
import be.jkin.q2service.model.KudosMessage;
import be.jkin.q2service.model.MessageType;
import be.jkin.q2service.queue.Publisher;
import be.jkin.q2service.repository.KudosRepository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;


import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1")
public class KudosController {
    @Autowired
    private KudosRepository kudosRepository;

    @Autowired
    Publisher publisher;

    @Autowired
    private RestTemplate restTemplate;
    //ApiServiceBuilder serviceBuilder;


    final String baseUriUsers = "http://localhost:8090/api/v1";
    private static final Logger LOGGER = LoggerFactory.getLogger(KudosController.class);

    @GetMapping("/kudos")
    public Iterable<Kudos> getAllKudos()
    {
        LOGGER.info("Kudos.findAll()");
        return kudosRepository.findAll();
    }

    @GetMapping("/kudos/{id}")
    public ResponseEntity<Kudos> getKudosById(@PathVariable(value = "id") UUID id) throws Exception
    {
        LOGGER.info(String.format("Kudos.findAll(%s)", id));

        Kudos kudos = kudosRepository.findById(id);
        return ResponseEntity.ok().body(kudos);
    }

    @GetMapping("/kudos/simpleList")
    @ResponseBody
    public void getKudosSimpleList()
    {
        LOGGER.info(String.format("Kudos.findAll()"));
        /*
        List<Kudos> allKudos = kudosRepository.findAll();
        */

        Gson gson = new Gson();
        String json = gson.toJson(kudosRepository.findAll());
    }

    @GetMapping("/kudos/ByTargetNickname/{nickname}")
    public ResponseEntity<List<Kudos>> getKudosByNickname(@PathVariable(value = "nickname") String nickname) throws Exception
    {
        LOGGER.info(String.format("Kudos.findByDestino(%s)", nickname));

        List<Kudos> kudos = kudosRepository.findByDestino(nickname);
        return ResponseEntity.ok().body(kudos);
    }

/*
    @GetMapping("/kudos/AdvancedSearch/{text}")
    public ResponseEntity<List<Kudos>> getKudosAdvSearch(@PathVariable(value = "text") String text) throws Exception
    {
        LOGGER.info(String.format("Kudos.getKudosAdvSearch(%s)", text));

        List<Kudos> kudos = kudosRepository.findByDestino(text);

        LOGGER.info(String.format("Kudos matching (%s): [%s]", text, kudos));

        return ResponseEntity.ok().body(kudos);
    }
*/
    @PostMapping("/kudos")
    public Kudos createKudos(@Valid @RequestBody Kudos kudos) {

        LOGGER.info(String.format("Kudos.save(%s)", kudos));

        //Validate if the Users (fuente and destino) are valid on the API Users side
        String usuarioFuente = kudos.getFuente();
        String usuarioDestino = kudos.getDestino();

        //Call API Users - getUsersByNickname
        String uri = String.format("%s%s", baseUriUsers , "/users/ByNickname/");
        String newUri = "";
        try{

            //get user Fuente: users/ByNickname/{nickname}
            newUri = uri + usuarioFuente;
            String apiResponseUserFuente = restTemplate.getForObject(newUri, String.class);
            JsonObject jsonUserFuente = new JsonParser().parse(apiResponseUserFuente).getAsJsonObject();

            if(jsonUserFuente.get("nickname").getAsString().isEmpty())
            {
                LOGGER.warn(String.format("Usuario Fuente (%s) no valido",usuarioFuente));

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invalid user Fuente"
                );
            }

            //get user Destino: /users/ByNickname/{nickname}
            newUri = uri + usuarioDestino;
            String apiResponseUserDestino = restTemplate.getForObject(newUri, String.class);
            JsonObject jsonUserDestino = new JsonParser().parse(apiResponseUserDestino).getAsJsonObject();

            if(jsonUserDestino.get("nickname").getAsString().isEmpty())
            {
                LOGGER.warn(String.format("Usuario Destino (%s) no valido",usuarioFuente));
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invalid user Destino"
                );
            }

        } catch (RestClientException e) {
            LOGGER.error(String.format("Error on requesting (%s) with: %s", newUri, e.getMessage()));
            //throw e;
            return null;
        } catch(Exception e){
            LOGGER.error(String.format("Error with: %s",e.getMessage()));
            return null;
        }


        //All good, build and save the Kudos
        kudos.setId(UUID.randomUUID());

        @Valid
        Kudos newKudos = kudosRepository.save(kudos);

        LOGGER.info("New Kudos saved: " + newKudos);

        //Send to Queue
        // Send message to Queue
        try {
            KudosMessage kudosMessage = new KudosMessage(newKudos.getId(),
                    newKudos.getFuente(),
                    newKudos.getDestino(),
                    MessageType.ADD_KUDOS);

            Gson gson = new Gson();
            String json = gson.toJson(kudosMessage);
            publisher.SendMessageToQueue(json);
        }
        catch (Exception ex)
        {
            LOGGER.error(ex.getMessage());
        }

        return newKudos;
    }

    @PostMapping("/kudos/SecureDelete/{id}")
    public Map<String, Boolean> secureDelete(@PathVariable(value = "id") UUID kudosId,
                                             @Valid @RequestBody String secureDeleteBody) throws Exception
    {
        LOGGER.info(String.format("Kudos.secureDelete(%s)", kudosId));

        Map<String, Boolean> response = new HashMap<>();
        //Body:   {
        //         "UsuarioFuenteNickname":"ariel"
        //        }
        JsonObject jsonObject = new JsonParser().parse(secureDeleteBody).getAsJsonObject();
        String usuarioFuente = jsonObject.get("UsuarioFuenteNickname").getAsString();

        Kudos kudos = kudosRepository.findById(kudosId);

        if(kudos != null)
        {
            if(!kudos.getFuente().equalsIgnoreCase(usuarioFuente))
            {
                LOGGER.warn(String.format("Invalid user, not allowed to delete this Kudos. Requested user: %s . Requested KudosId: %s ", usuarioFuente, kudosId));
                response.put("INVALID_USER", Boolean.TRUE);
                return response;
            }

            kudosRepository.delete(kudos);
            LOGGER.info(String.format("Kudos Deleted: (%s)", kudosId));
            response.put("Kudos deleted", Boolean.TRUE);

            //Send message to Queue
            KudosMessage kudosMessage = new KudosMessage(kudos.getId(),
                                                         kudos.getFuente(),
                                                         kudos.getDestino(),
                                                         MessageType.DELETE_KUDOS);
            Gson gson = new Gson();
            String json = gson.toJson(kudosMessage);
            publisher.SendMessageToQueue(json);
            LOGGER.info(String.format("Kudos Delete command sent to Queue:(%s)", json));

        }

        return response;
    }

    @PutMapping("/kudos/{id}")
    public ResponseEntity<Kudos> updateKudos(@PathVariable(value = "id") Long kudosId,
                                             @Valid @RequestBody Kudos kudosDetails) throws Exception
    {
        LOGGER.info(String.format("Kudos.save(%s)", kudosDetails));

        Kudos kudos = kudosRepository.findById(String.valueOf(kudosId)).orElseThrow(()->new Exception("Kudos not found on::"+ kudosId));

        kudos.setFuente(kudosDetails.getFuente());
        kudos.setDestino(kudosDetails.getDestino());
        kudos.setTema(kudosDetails.getTema());

        final Kudos updatedKudos = kudosRepository.save(kudos);
        return ResponseEntity.ok(updatedKudos);
    }

    @DeleteMapping("/kudos/{id}")
    public Map<String, Boolean> deleteKudos(@PathVariable(value = "id") UUID kudosId,
                                            @Valid @RequestBody String secureDeleteBody) throws Exception
    {
        LOGGER.info(String.format("Kudos.delete(%s)", kudosId));

        /*
        Kudos kudos = kudosRepository.findById(kudosId);

        kudosRepository.delete(kudos);
        Map<String, Boolean> response = new HashMap<>();
        response.put("Kudos deleted", Boolean.TRUE);
        */

        Map<String, Boolean> response = new HashMap<>();
        //Body:   {
        //         "UsuarioFuenteNickname":"ariel"
        //        }
        JsonObject jsonObject = new JsonParser().parse(secureDeleteBody).getAsJsonObject();
        String usuarioFuente = jsonObject.get("UsuarioFuenteNickname").getAsString();

        Kudos kudos = kudosRepository.findById(kudosId);

        if(kudos != null)
        {
            if(!kudos.getFuente().equalsIgnoreCase(usuarioFuente))
            {
                LOGGER.warn(String.format("Invalid user, not allowed to delete this Kudos. Requested user: %s . Requested KudosId: %s ", usuarioFuente, kudosId));
                response.put("INVALID_USER", Boolean.TRUE);
                return response;
            }

            kudosRepository.delete(kudos);
            LOGGER.info(String.format("Kudos Deleted: (%s)", kudosId));
            response.put("Kudos deleted", Boolean.TRUE);

            //Send message to Queue
            KudosMessage kudosMessage = new KudosMessage(kudos.getId(),
                    kudos.getFuente(),
                    kudos.getDestino(),
                    MessageType.DELETE_KUDOS);
            Gson gson = new Gson();
            String json = gson.toJson(kudosMessage);
            publisher.SendMessageToQueue(json);
            LOGGER.info(String.format("Kudos Delete command sent to Queue:(%s)", json));

        }
        else{
            LOGGER.info(String.format("Kudos Id not found:(%s)", kudosId));
            response.put("INVALID_KUDOS_ID", Boolean.TRUE);
        }
        return response;
    }
}
