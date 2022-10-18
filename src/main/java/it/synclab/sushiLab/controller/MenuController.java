package it.synclab.sushiLab.controller;

import it.synclab.sushiLab.classes.*;
import it.synclab.sushiLab.entity.FasciaOraria;
import it.synclab.sushiLab.entity.Menu;
import it.synclab.sushiLab.entity.PiattoPreview;
import it.synclab.sushiLab.entity.SezionePreview;
import it.synclab.sushiLab.errorsHandling.NotFoundError;
import it.synclab.sushiLab.service.ClientService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MenuController {
    @Autowired
    ClientService clientService;

    @QueryMapping
    public Object ottieniMenu(@Argument int idMenu, @Argument String idPersona){
        ArraySezione arraySezione = clientService.ottieniMenu(idMenu, idPersona);
        if(arraySezione == null)
            return new NotFoundError("Menù vuoto.");
        return arraySezione;
    }

    @QueryMapping
    public Object ottieniListaFasce(@Argument int idMenu){
        Fasce fasce = clientService.ottieniFasce(idMenu);
        if(fasce == null) return new NotFoundError("Menù con id: " + idMenu + "non è stato trovato.");
        List<FasciaOraria> list = fasce.getFasce();
        for(int i = 0; i < list.size(); i++){
            list.get(i).setMenu(null);
        }
        //JSONObject json = new JSONObject(fasce);
        //System.out.println(json.toString());
        //JSONArray jsonArray = json.getJSONArray("fasce");
        //for(int i = 0; i < jsonArray.length(); i++){
        //    jsonArray.getJSONObject(i).remove("id");
        //}
        return list;
    }

    @QueryMapping
    public Object ottieniListaPreferiti(@Argument int idMenu, @Argument String idPersona){
        Preferiti preferiti = clientService.ottieniListaPreferiti(idMenu, idPersona);
        if(preferiti == null) return new NotFoundError("Menù con id: " + idMenu + "non è stato trovato.");
        //JSONObject json = new JSONObject(preferiti);
        return preferiti;
    }

    /*@QueryMapping
    public Object ottieniTema(@PathVariable String idMenu){
        String s = "{\"test\":\"test\"}";
        return new ResponseEntity<>(s, HttpStatus.OK);
    }*/
}
