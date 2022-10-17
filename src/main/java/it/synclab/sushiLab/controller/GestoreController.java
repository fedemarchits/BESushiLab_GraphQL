package it.synclab.sushiLab.controller;

//import it.synclab.sushiLab.constants.ErrorHandler;
import it.synclab.sushiLab.errorsHandling.InvalidInputError;
import it.synclab.sushiLab.errorsHandling.NotAllowedError;
import it.synclab.sushiLab.errorsHandling.NotFoundError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;

import org.springframework.stereotype.Controller;

import it.synclab.sushiLab.service.GestoreService;
import it.synclab.sushiLab.service.ClientService;
import it.synclab.sushiLab.entity.PiattoPreview;
import it.synclab.sushiLab.classes.ListaPiatti;
import it.synclab.sushiLab.constants.*;
import it.synclab.sushiLab.entity.PiattoUpload;

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
        return piattoUpload.getId();
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

}


