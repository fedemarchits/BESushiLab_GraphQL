package it.synclab.sushiLab.controller;


import it.synclab.sushiLab.entity.Menu;
import it.synclab.sushiLab.errorsHandling.InvalidInputError;
import it.synclab.sushiLab.errorsHandling.NotAllowedError;
import it.synclab.sushiLab.errorsHandling.NotFoundError;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import it.synclab.sushiLab.service.GestoreService;
import it.synclab.sushiLab.service.ClientService;
import it.synclab.sushiLab.classes.ListaPiatti;
import it.synclab.sushiLab.entity.PiattoUpload;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

import java.util.Set;


@Controller
public class GestoreController {

    @Autowired
    GestoreService gestoreService;
    @Autowired
    private ClientService clienteService;

    //@Autowired
    //private ErrorHandler errorHandler;

    //record NotAllowedError (String msg) {}

    @QueryMapping
    public Set<Object> ottieniListaPiatti(@Argument String idPersona) {
        //Verifico idPersona corrisponde a un gestore
        Set<Object> list = new HashSet<Object>();
        if (!clienteService.isGestore(idPersona))
            list.add(new NotAllowedError("Operazione non concessa, l'id non è di un gestore o non corrisponde a nessuna persona."));
        else {
            //Ricevo lista piatti
            ListaPiatti listaPiatti = gestoreService.ottieniListaPiatti();
            for (int i = 0; i < listaPiatti.getPiatti().size(); i++) {
                listaPiatti.getPiatti().get(i).setPiattoUpload(null);
                listaPiatti.getPiatti().get(i).setSezionePreview(null);
                list.add(listaPiatti.getPiatti().get(i));
            }
        }

        return list;
    }

    @MutationMapping
    public Object nuovoPiatto(@Argument String idPersona, @Argument PiattoUpload piattoUpload){
        //Verifico idPersona corrisponde a un gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");
        try {
            gestoreService.inserisciPiattoUpload(piattoUpload);
        } catch (Exception e) {
            return new UnknownError("L'operazione non è andata a buon fine.");
        }
        return gestoreService.riceviPiattoUpload(piattoUpload.getId());
    }

    @QueryMapping
    public Object ottieniPiatto(@Argument String idPersona, @Argument String idPiatto){
        //Verifico che idPiatto sia intero
        int id;
        try {
            id = (Integer.parseInt(idPiatto));
        } catch (Exception e) {
            return new InvalidInputError("L'id del piatto inserito non è intero.");
        }
        //Verifico idPersona corrisponde a un gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");
        //Verifico che l'id del piatto esista e ritorno
        try {
            PiattoUpload piatto = gestoreService.riceviPiattoUpload(id);
            if(piatto == null)
                return new NotFoundError("Nessun piatto trovato con id: " + id);
            return piatto;
        } catch (Exception e) {
            return new UnknownError("Operazione non riuscita.");
        }
    }

    /* Elimina piatto DELETE http://localhost:3000/gestore/{idPersona}/piatto/{idPiatto}*/
    @MutationMapping
    public Object eliminaPiatto(@Argument String idPersona, @Argument String idPiatto){
        //Verifico che idPiatto sia intero
        int id;
        try {
            id = (Integer.parseInt(idPiatto));
        } catch (Exception e) {
            return new InvalidInputError("L'id del piatto: " + idPiatto + " non è un valorei intero.");
        }
        //Verifico idPersona corrisponde a un gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");
        //Verifico che l'id del piatto esista
        PiattoUpload pu = gestoreService.riceviPiattoUpload(Integer.parseInt(idPiatto));
        if(!gestoreService.rimuoviPiattoUpload(id))
            return new UnknownError("Il piatto con id: " + idPiatto + " NON è stato eliminato.");
        return pu;
    }

    /* Aggiorna piatto POST http://localhost:3000/gestore/{idPersona}/piatto/{idPiatto}*/
    @MutationMapping
    public Object aggiornaPiatto(@Argument String idPersona, @Argument PiattoUpload piattoUpload){

        //Verifico che idPiatto sia intero
        int id;
        try {
            id = (Integer.valueOf(piattoUpload.getId()));
        } catch (Exception e) {
            return new InvalidInputError("L'id del piatto: " + piattoUpload.getId() + " non è un valorei intero.");
        }

        //Verifico idPersona corrisponde a un gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");

        //Verifico che idPiatto esista e se c'è effettuo l'update
        if(!gestoreService.aggiornaPiattoUpload(id, piattoUpload))
            return new UnknownError("Il piatto con id: " + piattoUpload.getId() + " NON è stato aggiornato.");
        return gestoreService.riceviPiattoUpload(piattoUpload.getId());
    }

    /* Ottieni lista menù */
    @QueryMapping
    public Object ottieniListaMenu(@Argument String idPersona){
        //Verifico idPersona corrisponde a un gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");
        return gestoreService.ottieniListaMenu();

    }

    /* Nuovo Menu */
    @MutationMapping
    public Object nuovoMenu(@Argument String idPersona, @Argument String name){
        //Verifico idPersona se è gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");

        //Creo nuovo menù
        JSONObject body;
        try{
            body = new JSONObject(name);
        }catch(JSONException e){
            return new UnknownError("Impossibile generare un menù a partire dall'input dato.");
        }
        if(!body.has("name"))
            return new UnknownError("Impossibile generare un menù a partire dall'input dato.");
        int idMenu = gestoreService.inserisciMenu(body.getString("name"));
        return gestoreService.riceviMenu(idMenu);
    }

    /* Ottieni Menu */
    @QueryMapping
    public Object ottieniMenu(@PathVariable String idPersona, @PathVariable int idMenu){
        //Verifico idPersona corrisponde a un gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");
        Menu menu = gestoreService.riceviMenu(idMenu);
        if(menu == null)
            return new NotFoundError("Non esiste alcun menù con id: " + idMenu);
        for(int i = 0; i < menu.getMenu().size(); i++){
            menu.getMenu().get(i).setMenu(null);
            for(int j = 0; j < menu.getMenu().get(i).getPiatti().size(); j++){
                menu.getMenu().get(i).getPiatti().get(j).setPiattoUpload(null);
                menu.getMenu().get(i).getPiatti().get(j).setSezionePreview(null);
            }
        }
        /*
        PiattoPreview p = new PiattoPreview();
        for(int i = 0; i < menu.getFasce().size(); i++){
            menu.getFasce().get(i).setMenu(null);
        }
        JSONObject json = new JSONObject(menu);
        json.remove("id");
        for(int i = 0; i < json.getJSONArray("fasce").length(); i++){
            json.getJSONArray("fasce").getJSONObject(i).remove("id");
        }
        for(int i = 0; i < json.getJSONArray("menu").length(); i++){
            json.getJSONArray("menu").getJSONObject(i).remove("id");
        }
        */

        return menu;
    }

    /* Aggiorna Menu */
    @MutationMapping
    public Object aggiornaMenu(@Argument String idPersona, @Argument Menu menu){
        //Verifico che idPiatto sia intero
        /*
        int id;
        try {
            id = (Integer.valueOf(menu.getId()));
        } catch (Exception e) {
            return new InvalidInputError("L'id del menù inserito non è valido");
        }
        */
        //Verifica idPersona se è gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");

        //Aggiorna menu
        if(!gestoreService.aggiornaMenu(menu.getId(), menu))
            return new UnknownError("Il menù con id: " + menu.getId() + " NON è stato aggiornato.");
        return gestoreService.riceviMenu(menu.getId());
    }

    /* Elimina Menu */
    @MutationMapping
    public Object eliminaMenu(@Argument String idPersona, @Argument String idMenu){
        //Verifico che idPiatto sia intero
        int id;
        try {
            id = (Integer.parseInt(idMenu));
        } catch (Exception e) {
            return new InvalidInputError("L'id del menù inserito non è valido");
        }

        //Verifica idPersona se è gestore
        if(!clienteService.isGestore(idPersona))
            return new NotAllowedError("Operazione non concessa, l'id non è di un gestore.");

        //Elimina menu
        if(!gestoreService.eliminaMenu(id))
            return new UnknownError("Il menù con id: " + idMenu + " NON è stato eliminato.");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}


