package be.jkin.q2service.controllers;

import be.jkin.q2service.model.Kudos;
import be.jkin.q2service.model.KudosMessage;
import be.jkin.q2service.model.MessageType;
import be.jkin.q2service.queue.Publisher;
import be.jkin.q2service.repository.KudosRepository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


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

    final String baseUri = "http://localhost:8090/api/v1/users";
    private static final Logger LOGGER = LoggerFactory.getLogger(KudosController.class);

    @GetMapping("/kudos")
    public Iterable<Kudos> getAllKudos()
    {
        return kudosRepository.findAll();
    }

    @GetMapping("/kudos/{id}")
    public ResponseEntity<Kudos> getKudosById(@PathVariable(value = "id") UUID id) throws Exception
    {
        Kudos kudos = kudosRepository.findById(id);
        return ResponseEntity.ok().body(kudos);
    }

    @GetMapping("/kudos/simpleList")
    @ResponseBody
    public void getKudosSimpleList()
    {
        /*
        List<Kudos> allKudos = kudosRepository.findAll();

*/

        Gson gson = new Gson();
        String json = gson.toJson(kudosRepository.findAll());
    }

    @GetMapping("/kudos/ByTargetNickname/{nickname}")
    public ResponseEntity<List<Kudos>> getKudosByNickname(@PathVariable(value = "nickname") String nickname) throws Exception
    {
        List<Kudos> kudos = kudosRepository.findByDestino(nickname);
        return ResponseEntity.ok().body(kudos);
    }

    @PostMapping("/kudos")
    public Kudos createKudos(@Valid @RequestBody Kudos kudos) {
        //Validate if the Users (fuente and destino) are valid on the API Users side
        String usuarioFuente = kudos.getFuente();
        String usuarioDestino = kudos.getDestino();

        //Call API Users - getUsersByNickname
        try{
            String apiResponse = restTemplate.getForObject(baseUri + "/users/{id}", String.class, 5);
            LOGGER.info("This is the response {}", apiResponse);

        } catch (RestClientException e) {
            LOGGER.error(e.getMessage());
        }


/*
        JsonObject jsonObject = new JsonParser().parse(secureDeleteBody).getAsJsonObject();
        String usuarioFuente = jsonObject.get("UsuarioFuenteNickname").getAsString();
*/
        kudos.setId(UUID.randomUUID());

        @Valid
        Kudos newKudos = kudosRepository.save(kudos);

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
    public Map<String, Boolean> secureDelete(@PathVariable(value = "id") String kudosId,
                                             @Valid @RequestBody String secureDeleteBody) throws Exception
    {

        Map<String, Boolean> response = new HashMap<>();
        //Body:   {
        //         "UsuarioFuenteNickname": “ariel”
        //        }
        JsonObject jsonObject = new JsonParser().parse(secureDeleteBody).getAsJsonObject();
        String usuarioFuente = jsonObject.get("UsuarioFuenteNickname").getAsString();

        Kudos kudos = kudosRepository.findById(String.valueOf(kudosId)).orElseThrow(()->new Exception("Kudos not found on::"+kudosId));

        if(kudos != null)
        {
            if(!kudos.getFuente().equalsIgnoreCase(usuarioFuente))
            {
                response.put("INVALID_USER", Boolean.FALSE);
                return response;
            }

            kudosRepository.delete(kudos);
            response.put("Kudos deleted", Boolean.TRUE);

            //Send message to Queue
            KudosMessage kudosMessage = new KudosMessage(kudos.getId(),
                                                         kudos.getFuente(),
                                                         kudos.getDestino(),
                                                         MessageType.DELETE_KUDOS);
            Gson gson = new Gson();
            String json = gson.toJson(kudosMessage);
            publisher.SendMessageToQueue(json);
            //
        }

        return response;
    }

    @PutMapping("/kudos/{id}")
    public ResponseEntity<Kudos> updateKudos(@PathVariable(value = "id") Long kudosId,
                                             @Valid @RequestBody Kudos kudosDetails) throws Exception
    {
        Kudos kudos = kudosRepository.findById(String.valueOf(kudosId)).orElseThrow(()->new Exception("Kudos not found on::"+ kudosId));

        kudos.setFuente(kudosDetails.getFuente());
        kudos.setDestino(kudosDetails.getDestino());
        kudos.setTema(kudosDetails.getTema());

        final Kudos updatedKudos = kudosRepository.save(kudos);
        return ResponseEntity.ok(updatedKudos);
    }

    @DeleteMapping("/kudos/{id}")
    public Map<String, Boolean> deleteKudos(@PathVariable(value = "id") long kudosId) throws Exception
    {
        Kudos kudos = kudosRepository.findById(String.valueOf(kudosId)).orElseThrow(()->new Exception("Kudos not found on::"+kudosId));

        kudosRepository.delete(kudos);
        Map<String, Boolean> response = new HashMap<>();
        response.put("Kudos deleted", Boolean.TRUE);

        return response;
    }
}
