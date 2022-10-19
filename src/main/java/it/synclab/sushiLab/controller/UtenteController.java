package it.synclab.sushiLab.controller;

import it.synclab.sushiLab.classes.Ingredienti;
import it.synclab.sushiLab.entity.Utente;
import it.synclab.sushiLab.errorsHandling.NotAllowedError;
import it.synclab.sushiLab.errorsHandling.NotFoundError;
import it.synclab.sushiLab.service.ClientService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


import java.util.List;

@Controller
public class UtenteController {

    @Autowired
    ClientService clientService;


    @QueryMapping
    public Object ottieniUtente(@Argument String idPersona){
        Utente utente = clientService.ottieniUtente(idPersona);
        if(utente == null)
            return new NotFoundError("Utente non trovato");
        utente.setTavolo(null);
        utente.setIdPersona(null);
        return utente;
    }


    @MutationMapping
    public Object registraUtente(@Argument Utente utente){
        if(clientService.insert(utente))
            return utente;
        return new UnknownError("Non è possibile registrare l'utente");
    }

    //DA RIVEDERE TIPO DI RITORNO
    @MutationMapping
    public Object modificaStatoPreferiti(@Argument String idPersona, @Argument String idPiatto, @Argument String param){
        JSONObject json = new JSONObject(param);
        if(!json.has("fav"))
            return new NotAllowedError("Non permesso ...");
        boolean value = json.getBoolean("fav");
        if(!clientService.modificaStatoPreferiti(idPersona, Integer.parseInt(idPiatto), value))
            return new UnknownError("Non è stato possibile modificare lo stato dei preferiti");
        return true;
    }

    //DA RIVEDERE TIPO DI RITORNO
    @MutationMapping
    public Object modificaValutazione(@Argument String idPersona, @Argument String idPiatto, @Argument String param){
        JSONObject json = new JSONObject(param);
        if(!json.has("rate"))
            return new NotAllowedError("Non permesso ...");
        int value = json.getInt("rate");
        if(!clientService.modificaValutazione(idPersona, Integer.parseInt(idPiatto), value))
            return new UnknownError("Non è stato possibile modificare la valutazione");
        return true;
    }

    //DA RIVEDERE TIPO DI RITORNO
    @MutationMapping
    public Object recuperoPassword(@Argument String param){
        JSONObject body = new JSONObject(param);
        if(!body.has("email"))
            return new NotAllowedError("Mail non presente");
        if(!clientService.recuperoPassword(body.getString("email")))
            return new NotFoundError("Utente con email: " + body.getString("email") + " non trovato");
        return true;
    }

    //DA RIVEDERE TIPO DI RITORNO
    @MutationMapping
    public Object verificaCodice(@Argument String param){
        JSONObject body = new JSONObject(param);
        if(!body.has("code"))
            return new NotFoundError("Codice non trovato");
        if(!clientService.verify(body.getString("code")))
            return new NotAllowedError("Verifica terminata con esito negativo ...");
        return true;
    }


    @MutationMapping
    public Object reimpostaPassword(@Argument String body){
        try {
            JSONObject json = new JSONObject(body);
            if(!json.has("email") || !json.has("newpass"))
                return new NotFoundError("Email o la nuova password sono mancanti");
            if(clientService.reimpostaPassword(json.getString("email"), json.getString("newpass")))
                return true;
            return new UnknownError("Reimposta password non riuscito");
        } catch (Exception e) {
            return new UnknownError("Reimposta password non riuscito");
        }
    }


    @MutationMapping
    public Object aggiornaBlacklist(@Argument String idPersona, @Argument Ingredienti ingredienti){
        clientService.aggiornaBlacklist(idPersona, ingredienti);
        return true;
    }

    /* Ottieni la blacklist GET http://localhost:3000/utente/{idPersona}/blacklist*/
    @QueryMapping
    public Object ottieniBlacklist(@Argument String idPersona){
        List<String> list = clientService.ottieniBlacklist(idPersona);
        if(list == null)
            return new NotFoundError("Blacklist non trovata");
        Ingredienti ingredienti = new Ingredienti(list);
        return ingredienti;
    }
}
