package it.synclab.sushiLab.controller;

import it.synclab.sushiLab.classes.*;
import it.synclab.sushiLab.constants.Constants;
import it.synclab.sushiLab.entity.Menu;
import it.synclab.sushiLab.entity.Session;
import it.synclab.sushiLab.errorsHandling.NotAllowedError;
import it.synclab.sushiLab.errorsHandling.NotFoundError;
import it.synclab.sushiLab.service.ClientService;
import it.synclab.sushiLab.utility.Utility;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Controller
public class TavoloController {

    @Autowired
    private ClientService clienteService;

    @MutationMapping
    public Object creaSessione(@Argument String idPersona){
        //Generate random value
        String value = Utility.generateString(Constants.sessionCodeLength, Constants.sessionCodeLength, true, Constants.sessionLetters, Constants.sessionLetters);
        //Check if already exists
        boolean rtrn = false;
        int count = 0;
        while(rtrn == false){
            if(count >= 1000)
                return new UnknownError("Impossibile creare la sessione, riprovare più tardi.");
            rtrn = clienteService.insertSessionCode(value, idPersona);
            count++;
        }
        //JSONObject body = new JSONObject();
        //body.put("id", value);

        Session session = new Session();
        session.setIdTable(value);
        return session;
    }

    // QUI RITORNA UN MENUCOMPATTO CHE BOH, NON CAPISCO IL SENSO
    @QueryMapping
    public Object ottieniSessione(@Argument String idTavolo){
        //Verifico se la sessione esiste
        boolean exist = clienteService.existSessionCode(idTavolo);
        if(!exist)
            return new NotFoundError("Non esiste nessuna sessione con id: " + idTavolo);
        //Ricevo il menù dell'orario giusto
        Menu menu = clienteService.riceviMenu();
        MenuCompatto menuCompatto = new MenuCompatto();
        if(menu == null)
            return new UnknownError("Menù non disponibile");
        menuCompatto.setId(menu.getId());
        menuCompatto.setNome(menu.getNome());
        List<Integer> piatti = new ArrayList<>();
        for(int i = 0; i < menu.getMenu().size(); i++){
            for(int j = 0; j < menu.getMenu().get(i).getPiatti().size(); j++){
                piatti.add(menu.getMenu().get(i).getPiatti().get(j).getId());
            }
        }
        menuCompatto.setPiatti(piatti);

        MenuCompattoSessione menuRisposta = new MenuCompattoSessione(menuCompatto);
        //System.out.println(menuRisposta);
        //JSONObject json = new JSONObject(menuRisposta);
        //System.out.println(json);
        return menuRisposta;
    }

    @MutationMapping
    public Object chiudiSessione(@Argument String idTavolo){
        //Check if exists
        boolean exist = clienteService.existSessionCode(idTavolo);
        if(!exist)
            return new NotFoundError("Non esiste nessuna sessione con id: " + idTavolo);
        Session sessione = new Session();
        sessione.setIdTable(idTavolo);
        clienteService.deleteSession(idTavolo);
        return sessione;
    }

    @QueryMapping
    public Object ottieniOrdiniPersona(@Argument String idTavolo, @Argument String idPersona){
        List<OrdineDettaglio> personali = clienteService.ottieniOrdiniPersonali(idPersona, idTavolo);
        if(personali == null) return new NotFoundError("Non è stato trovato alcun ordine");
        ListaOrdineDettaglio listaOrdineDettaglio = new ListaOrdineDettaglio(personali);
        //JSONObject json = new JSONObject(listaOrdineDettaglio);
        //System.out.println(listaOrdineDettaglio);
        return listaOrdineDettaglio;
    }

    @MutationMapping
    public Object modificaOrdiniPersona(@Argument String idTavolo, @Argument String idPersona, @Argument ListaOrdini ordini){
        //Inserisci ordini con tutte le verifiche
        if(!clienteService.inserisciOrdini(ordini.getOrdini(), idPersona, idTavolo))
            return new NotAllowedError("Non è stato possibile trovare l'ordine");
        return ordini.getOrdini();
    }

    @QueryMapping
    public Object ottieniOrdiniTavolo(@Argument String idTavolo, @Argument String idPersona){
        List<OrdineDettaglio> ordini = clienteService.ottieniOrdiniTavolo(idPersona, idTavolo);
        if(ordini == null)
            return new NotAllowedError("Non è possibile visualizzare gli ordini specificati");
        ListaOrdineDettaglioTavolo listaOrdineDettaglioTavolo = new ListaOrdineDettaglioTavolo(ordini);
        //JSONObject json = new JSONObject(listaOrdineDettaglioTavolo);
        //System.out.println(listaOrdineDettaglio);
        return listaOrdineDettaglioTavolo.getOrdini();
    }

    @QueryMapping
    public Object ottieniGliOrdiniInArrivo(@Argument String idTavolo, @Argument String idPersona){
        List<OrdineDettaglio> list = clienteService.ottieniGliOrdiniInArrivo(idTavolo, idPersona);
        if(list == null)
            return new NotFoundError("Non sono presenti ordini in arrivo");
        InArrivo inArrivo = new InArrivo(list);
        //JSONObject json = new JSONObject(inArrivo);
        return inArrivo.getInarrivo();
    }

    //BISOGNA RAGIONARE SUL TIPO DI RITORNO, COSI' NON VA BENE
    @MutationMapping
    public Object spostaGliOrdiniInArrivo(@PathVariable String idTavolo){
        if(!clienteService.spostaGliOrdiniInArrivo(idTavolo))
            return new NotAllowedError("Impossibile spostare gli ordini in arrivo");
        return true;
    }

    //STESSO PROBLEMA DEL METODO DI SOPRA
    @MutationMapping
    public Object mergeTavolo(@Argument String idTavolo, @Argument ListaOrdineMerge body){
        if(!clienteService.mergeTavolo(idTavolo, body))
            return new NotAllowedError("Impossibile mergiare il tavolo");
        return true;
    }


}
