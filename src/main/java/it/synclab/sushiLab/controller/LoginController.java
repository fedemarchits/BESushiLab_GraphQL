package it.synclab.sushiLab.controller;


import it.synclab.sushiLab.entity.IdToken;
import it.synclab.sushiLab.entity.Utente;
import it.synclab.sushiLab.errorsHandling.NotFoundError;
import it.synclab.sushiLab.repository.ClientRepository;
import it.synclab.sushiLab.service.ClientService;
import it.synclab.sushiLab.utility.Utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;

import org.springframework.stereotype.Controller;


@Controller
public class LoginController {

    @Autowired
    private ClientService clienteService;

    @Autowired
    private ClientRepository clientiRepository;

    @MutationMapping
    public Object eseguiLogin(@Argument Utente utente){

        Utente u = clientiRepository.findById(utente.getEmail()).get();
        //System.out.println(u.getEmail());
        //Long expiresIn = Long.parseLong(Utility.generateString(10, 10, true, false, false));

        if(clienteService.login(utente) && u.getIdPersona()==null) {
            //Genera valori
            String idToken = Utility.generateString(18, 18, true, false, false);
            //Verifica se sono già presenti nel database, se non ci sono li inserisce

            boolean insert_value = clienteService.insertIdToken(utente, idToken);
            while(insert_value == false){
                //expiresIn = Long.parseLong(Utility.generateString(10, 10, true, false, false));
                idToken = Utility.generateString(18, 18, true, false, false);
                //Verifica se sono già presenti nel database, se non ci sono li inserisce
                insert_value = clienteService.insertIdToken(utente, idToken);
            }

            //rtrnObject.put("expiresIn", expiresIn);
            //rtrnObject.put("idToken", idToken);
            IdToken idtoken = new IdToken();
            idtoken.setIdToken(idToken);
            idtoken.setCliente(u);
            //u.setIdPersona(idtoken);

            return idToken;
        }
        if (u.getIdPersona().getIdToken()!=null) {
            //expiresIn = Long.parseLong(Utility.generateString(10, 10, true, false, false));
            //rtrnObject.put("expiresIn", expiresIn);
            //rtrnObject.put("idToken", u.getIdPersona().getIdToken());
            return u.getIdPersona().getIdToken();
        }
        return new NotFoundError("Utente non trovato.");
    }


}
