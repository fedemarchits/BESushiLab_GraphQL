package it.synclab.sushiLab.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.synclab.sushiLab.classes.ArraySezione;
import it.synclab.sushiLab.classes.Fasce;
import it.synclab.sushiLab.classes.Ingredienti;
import it.synclab.sushiLab.classes.ListaOrdineMerge;
import it.synclab.sushiLab.classes.OrdineDettaglio;
import it.synclab.sushiLab.classes.OrdineMerge;
import it.synclab.sushiLab.classes.Piatto;
import it.synclab.sushiLab.classes.Preferiti;
import it.synclab.sushiLab.classes.Sezione;
import it.synclab.sushiLab.constants.Constants;
import it.synclab.sushiLab.entity.Utente;
import it.synclab.sushiLab.entity.Blacklist;
import it.synclab.sushiLab.entity.Code;
import it.synclab.sushiLab.entity.IdToken;
import it.synclab.sushiLab.entity.InformazioniPiatto;
import it.synclab.sushiLab.entity.Menu;
import it.synclab.sushiLab.entity.Ordine;
import it.synclab.sushiLab.entity.PiattoUpload;
import it.synclab.sushiLab.entity.PiattoPreview;
import it.synclab.sushiLab.entity.Session;
import it.synclab.sushiLab.entity.SezionePreview;
import it.synclab.sushiLab.repository.BlacklistRepository;
import it.synclab.sushiLab.repository.ClientRepository;
import it.synclab.sushiLab.repository.CodeRepository;
import it.synclab.sushiLab.repository.IdTokenRepository;
import it.synclab.sushiLab.repository.InformazioniPiattoRepository;
import it.synclab.sushiLab.repository.MenuRepository;
import it.synclab.sushiLab.repository.OrdineRepository;
import it.synclab.sushiLab.repository.SessionRepository;
import it.synclab.sushiLab.repository.PiattoUploadRepository;
import it.synclab.sushiLab.repository.PiattoPreviewRepository;
import it.synclab.sushiLab.utility.Utility;

@Service
@Transactional
public class ClientService{

    @Autowired
    ClientRepository clientRepository;
    @Autowired
    IdTokenRepository idTokenRepository;
    @Autowired
    CodeRepository codeRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    MenuRepository menuRepository;
    @Autowired
    OrdineRepository ordineRepository;
    @Autowired
    PiattoUploadRepository piattoUploadRepository;
    @Autowired
    PiattoPreviewRepository piattoPreviewRepository;
    @Autowired
    InformazioniPiattoRepository informazioniPiattoRepository;
    @Autowired
    BlacklistRepository blacklistRepository;


    public List<Utente> getAllClients() {
        return clientRepository.findAll();
    }

    
    public boolean insert(Utente client) {
        if(!clientRepository.existsById(client.getEmail())){
            clientRepository.save(client);
            return true;
        }
        return false;
    }


    public boolean login(Utente client) {
        if(clientRepository.existsById(client.getEmail()))
            if(clientRepository.findById(client.getEmail()).get().getPassword().compareTo(client.getPassword()) == 0)
                return true;
        return false;
    }

    
    public boolean insertIdToken(Utente utente, String idToken) {
        utente.setIsGestore(clientRepository.findById(utente.getEmail()).get().getIsGestore());
        if(idTokenRepository.existsById(idToken))
            return false;
        IdToken id_token = new IdToken(idToken, utente);
        List<IdToken> list = idTokenRepository.findAll();
        for(int index = 0; index < list.size(); index++){
            if(list.get(index).getCliente().getEmail().toLowerCase().compareTo(utente.getEmail().toLowerCase()) == 0){
                idTokenRepository.deleteById(list.get(index).getIdToken());
                return false;
            }
        }
        utente.setIdPersona(id_token);
        //idTokenRepository.save(id_token);
        clientRepository.save(utente);
        //client.setIdPersona(id_token);
        return true;
    }

    
    public boolean recuperoPassword(String email) {
        //Cerco cliente già registrato nel database
        Boolean existClient = clientRepository.existsById(email);
        //Se non ho trovato il cliente non esiste quindi ritorno 0
        if(!existClient)
            return false;
        //Genero il codice
        /*RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder()
                .withinRange('0', 'Z')
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .build();*/
        //Elimino le istanze di login dell'utente
        String idToken = clientRepository.findById(email).get().getIdPersona().getIdToken();
        clientRepository.findById(email).get().setIdPersona(null);
        clientRepository.findById(email).get().setTavolo(null);
        idTokenRepository.deleteById(idToken);
        String code = Utility.generateString(Constants.recoverCodeLength, Constants.recoverCodeLength, true, false, true);
        //String s = randomStringGenerator.generate(Constants.recoverCodeLength);
        codeRepository.save(new Code(email, code, false));
        return true;
    }

    
    public boolean verify(String code) {
        if(codeRepository.findByCode(code) == null)
            return false;
        Code c = codeRepository.findByCode(code);
        c.setVerify(true);
        codeRepository.save(c);
        return true;
    }
    
    /* Metodo per creare sessione */
    public boolean insertSessionCode(String code, String idPersona) {
        boolean exist = sessionRepository.existsById(code);
        if(!exist){
            Session s = new Session();
            s.setIdTable(code);
            if(!idTokenRepository.existsById(idPersona)) return false;
            List<Utente> list = new ArrayList<>();
            Utente utente = idTokenRepository.findById(idPersona).get().getCliente();
            list.add(utente);
            s.setClienti(list);
            //sessionRepository.save(s);
            //utente.setTavolo(sessionRepository.getReferenceById(code));
            utente.setTavolo(s);
            clientRepository.save(utente);
            return true;
        }
        return false;
    }

    
    public boolean existSessionCode(String idTavolo) {
        return sessionRepository.existsById(idTavolo);
    }

    
    public void deleteSession(String idTavolo) {
        //Devo mettere a null tutte le sessioni dei clienti:
        List<Utente> lista_clienti = clientRepository.findAll();
        for(int i = 0; i < lista_clienti.size(); i++){
            if(lista_clienti.get(i).getTavolo() != null && lista_clienti.get(i).getTavolo().getIdTable().compareTo(idTavolo)==0){
                lista_clienti.get(i).setTavolo(null);
            }
        }
        Session s = sessionRepository.findById(idTavolo).get();
        s.getClienti().clear();
        sessionRepository.deleteById(idTavolo);

    }

    
    public boolean isGestore(String idPersona) {
        if(!idTokenRepository.existsById(idPersona))
            return false;
        if(idTokenRepository.findById(idPersona).get().getCliente().getIsGestore())
            return true;
        return false;
    }

    public Menu riceviMenu(){
        List<Menu> menu = menuRepository.findAll();
        if(menu == null) return null;
        for(int i = 0; i < menu.size(); i++){
            for(int j = 0; j < menu.get(i).getFasce().size(); j++){
                String giorno = menu.get(i).getFasce().get(j).getGiorno();
                int oraInizio = menu.get(i).getFasce().get(j).getOraInizio();
                int oraFine = menu.get(i).getFasce().get(j).getOraFine();
                if(Utility.verifyDate(giorno, oraInizio, oraFine)) {
                    Menu rtrn = menu.get(i);
                    
                    return rtrn;
                }
            }
        }
        return null;
    }
    
    public boolean inserisciOrdini(List<Ordine> ordini, String idPersona, String idTavolo){
        if(!idTokenRepository.existsById(idPersona)) return false;
        if (!sessionRepository.existsById(idTavolo)) return false;
        //Elimino gli ordini precedenti
        List<Ordine> lista = ordineRepository.findAll();
        for(int i = 0; lista != null && i < lista.size(); i++){
            if(lista.get(i).getIdPersona().getIdToken().compareTo(idPersona) == 0 && lista.get(i).getIdTavolo().getIdTable().compareTo(idTavolo) == 0){
                ordineRepository.delete(lista.get(i));
            }
        }
        for(int i = 0; i < ordini.size(); i++){
            //Aggiungo quelli nuovi dopo aver verificato che esistano nel upload e nel menu
            if(!piattoUploadRepository.existsById(ordini.get(i).getIdPiatto())) return false;
            if(!piattoPreviewRepository.existsById(ordini.get(i).getIdPiatto())) return false;

            ordini.get(i).setIdPersona(idTokenRepository.findById(idPersona).get());
            ordini.get(i).setIdTavolo(sessionRepository.findById(idTavolo).get());
            //Salvo tutto
            ordineRepository.save(ordini.get(i));
        }
        return true;
    }

    public List<OrdineDettaglio> ottieniOrdiniPersonali(String idPersona, String idTavolo){
        if(!idTokenRepository.existsById(idPersona)) return null;
        if (!sessionRepository.existsById(idTavolo)) return null;
        List<Ordine> lista_2 = ordineRepository.findAll();
        //System.out.println(lista_2);
        for(int i = 0; i < lista_2.size(); i++){
            if(lista_2.get(i).getIdTavolo().getIdTable().compareTo(idTavolo)!=0) {
                lista_2.remove(lista_2.get(i));
                i--;
            }
        }
        //System.out.println(lista_2);
        List<Ordine> lista = new ArrayList<>();
        for(int i = 0; i < lista_2.size(); i++){
            if(lista_2.get(i).getIdPersona().getIdToken().compareTo(idPersona)==0) lista.add(lista_2.get(i));
        }
        List<OrdineDettaglio> lista_dettaglio = new ArrayList<>();
        for(int i = 0; i < lista.size(); i++){
            PiattoUpload piattoUpload = null;
            if(piattoUploadRepository.existsById(lista.get(i).getIdPiatto()))
                piattoUpload = piattoUploadRepository.getReferenceById(lista.get(i).getIdPiatto());
            PiattoPreview piattoPreview = null;
            if(piattoPreviewRepository.existsById(lista.get(i).getIdPiatto()))
                piattoPreview = piattoPreviewRepository.getReferenceById(lista.get(i).getIdPiatto());
            
            if(piattoUpload == null || piattoPreview == null) return null;
            Piatto piatto = new Piatto(piattoUpload.getId(), piattoUpload.getNumero(), piattoUpload.getVariante(), piattoUpload.getNome(), piattoUpload.getPrezzo(), piattoUpload.getAllergeni(), piattoUpload.getIngredienti(), piattoPreview.getLimite(), 0, 0, false, "false", false, piattoPreview.getConsigliato(), piattoUpload.getImmagine(), piattoUpload.getAlt());
            List<InformazioniPiatto> list = informazioniPiattoRepository.findAll();
            int valutazione_tot = 0;
            int divisore = 0;
            for(int j = 0; j < list.size(); j++){
                if(list.get(j).getPiatto().getId() == piattoUpload.getId() ){
                    divisore++;
                    valutazione_tot += list.get(j).getValutazione();
                    if(list.get(j).getUtente().getIdPersona().getIdToken().compareTo(idPersona)==0){
                        piatto.setPreferito(list.get(j).isPreferito());
                        piatto.setValutazioneUtente(list.get(j).getValutazione());
                    }
                }
            }
            if(divisore != 0)
                piatto.setValutazioneMedia(valutazione_tot/divisore);
            //Merge degli ordini
            int count = 0;
            List<String> note = new ArrayList<>();
            for(int j = 0; j < lista_2.size(); j++){
                if(lista_2.get(i).getIdPiatto() == lista_2.get(j).getIdPiatto()){
                    count++;
                    note.add(lista_2.get(j).getNote());
                }
            }
            boolean insert = true;
            for(int j = 0; j < lista_dettaglio.size(); j++){
                if(lista_dettaglio.get(j).getPiatto().getId() == piatto.getId())
                    insert = false;
            }
            if (insert) lista_dettaglio.add(new OrdineDettaglio(piatto, count, note));
        }
        return lista_dettaglio;
    }

    /* Ottieni ordini Tavolo */
    public List<OrdineDettaglio> ottieniOrdiniTavolo(String idPersona, String idTavolo){
        if(!idTokenRepository.existsById(idPersona)) return null;
        if (!sessionRepository.existsById(idTavolo)) return null;
        List<Ordine> lista_2 = ordineRepository.findAll();
        for(int i = 0; i < lista_2.size(); i++){
            if(lista_2.get(i).getIdTavolo().getIdTable().compareTo(idTavolo)!=0) lista_2.remove(lista_2.get(i));
        }
        List<OrdineDettaglio> lista_dettaglio = new ArrayList<>();
        for(int i = 0; i < lista_2.size(); i++){
            Piatto piatto = ottienPiatto(idPersona, lista_2.get(i).getIdPiatto(), false);
            /*PiattoUpload piattoUpload = null;
            if(piattoUploadRepository.existsById(lista_2.get(i).getIdPiatto()))
                piattoUpload = piattoUploadRepository.getReferenceById(lista_2.get(i).getIdPiatto());
            PiattoPreview piattoPreview = null;
            if(piattoPreviewRepository.existsById(lista_2.get(i).getIdPiatto()))
                piattoPreview = piattoPreviewRepository.getReferenceById(lista_2.get(i).getIdPiatto());
            if(piattoUpload == null || piattoPreview == null) return null;
            Piatto piatto = new Piatto(piattoUpload.getId(), piattoUpload.getNumero(), piattoUpload.getVariante(), piattoUpload.getNome(), piattoUpload.getPrezzo(), piattoUpload.getAllergeni(), piattoUpload.getIngredienti(), piattoPreview.getLimite(), 0, 0, false, "false", false, piattoPreview.getConsigliato(), piattoUpload.getImmagine(), piattoUpload.getAlt());
            List<InformazioniPiatto> list = informazioniPiattoRepository.findAll();
            int valutazione_tot = 0;
            int divisore = 0;
            for(int j = 0; j < list.size(); j++){
                if(list.get(j).getPiatto().getId() == piattoUpload.getId() ){
                    divisore++;
                    valutazione_tot += list.get(j).getValutazione();
                    if(list.get(j).getUtente().getIdPersona().getIdToken().compareTo(idPersona)==0){
                        piatto.setPreferito(list.get(j).isPreferito());
                        piatto.setValutazioneUtente(list.get(j).getValutazione());
                    }
                }
            }
            if(divisore != 0)
                piatto.setValutazioneMedia(valutazione_tot/divisore);*/
            //Merge degli ordini
            int count = 0;
            List<String> note = new ArrayList<>();
            for(int j = 0; j < lista_2.size(); j++){
                if(lista_2.get(i).getIdPiatto() == lista_2.get(j).getIdPiatto()){
                    count++;
                    if(lista_2.get(j).getNote() != null) note.add(lista_2.get(j).getNote());
                }
            }
            boolean insert = true;
            for(int j = 0; j < lista_dettaglio.size(); j++){
                if(lista_dettaglio.get(j).getPiatto().getId() == piatto.getId())
                    insert = false;
            }
            if (insert) lista_dettaglio.add(new OrdineDettaglio(piatto, count, note));
        }
        return lista_dettaglio;
    }

    public boolean modificaStatoPreferiti(String idPersona, int idPiatto, boolean value){
        if(!piattoUploadRepository.existsById(idPiatto)) return false;
        if(!idTokenRepository.existsById(idPersona)) return false;
        List<InformazioniPiatto> list = informazioniPiattoRepository.findAll();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getPiatto().getId() == idPiatto && list.get(i).getUtente().getIdPersona().getIdToken().compareTo(idPersona)==0){
                list.get(i).setPreferito(value);
                informazioniPiattoRepository.save(list.get(i));
                return true;
            }
        }
        InformazioniPiatto informazioniPiatto = new InformazioniPiatto();
        informazioniPiatto.setUtente(idTokenRepository.findById(idPersona).get().getCliente());
        informazioniPiatto.setPiatto(piattoUploadRepository.findById(idPiatto).get());
        informazioniPiatto.setPreferito(value);
        informazioniPiattoRepository.save(informazioniPiatto);
        return true;
    }

    public boolean modificaValutazione(String idPersona, int idPiatto, int value){
        if(!piattoUploadRepository.existsById(idPiatto)) return false;
        if(!idTokenRepository.existsById(idPersona)) return false;
        List<InformazioniPiatto> list = informazioniPiattoRepository.findAll();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getPiatto().getId() == idPiatto && list.get(i).getUtente().getIdPersona().getIdToken().compareTo(idPersona)==0){
                list.get(i).setValutazione(value);
                informazioniPiattoRepository.save(list.get(i));
                return true;
            }
        }
        InformazioniPiatto informazioniPiatto = new InformazioniPiatto();
        informazioniPiatto.setUtente(idTokenRepository.findById(idPersona).get().getCliente());
        informazioniPiatto.setPiatto(piattoUploadRepository.findById(idPiatto).get());
        informazioniPiatto.setValutazione(value);
        informazioniPiattoRepository.save(informazioniPiatto);
        return true;
    }

    public boolean spostaGliOrdiniInArrivo(String idTavolo){
        if (!sessionRepository.existsById(idTavolo)) return false;
        List<Ordine> list = ordineRepository.findAll();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getIdTavolo().getIdTable().compareTo(idTavolo)==0){
                list.get(i).setInarrivo(true);
                ordineRepository.save(list.get(i));
            }
        }
        return true;
    }

    public List<OrdineDettaglio> ottieniGliOrdiniInArrivo(String idTavolo, String idPersona){
        if (!sessionRepository.existsById(idTavolo)) return null;
        List<Ordine> list = ordineRepository.findAll();
        List<OrdineDettaglio> list_dettaglio = ottieniOrdiniPersonali(idPersona, idTavolo);
        if(list_dettaglio == null || list == null) return null;
        for(int i = 0; i < list.size(); i++){
            for(int j = 0; j < list_dettaglio.size(); j++){
                if(list_dettaglio.get(j).getPiatto().getId() == list.get(i).getIdPiatto() && !list.get(i).isInarrivo()){
                    list_dettaglio.remove(list_dettaglio.get(j--));
                }
            }
        }
        return list_dettaglio;
    }

    public List<String> ottieniBlacklist(String idPersona){
        if(!idTokenRepository.existsById(idPersona))
            return null;
        List<Blacklist> list = blacklistRepository.findAll();
        List<String> lista = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getUtente().getIdPersona().getIdToken().compareTo(idPersona)==0){
                lista = list.get(i).getIngredienti();
            }
        }
        return lista;
    }

    public boolean aggiornaBlacklist(String idPersona, Ingredienti ingredienti){
        if(!idTokenRepository.existsById(idPersona)) return false;
        List<Blacklist> list = blacklistRepository.findAll();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getUtente().getIdPersona().getIdToken().compareTo(idPersona)==0){
                list.get(i).setIngredienti(ingredienti.getIngredienti());
                blacklistRepository.save(list.get(i));
                return true;
            }
        }
        Blacklist blacklist = new Blacklist();
        blacklist.setUtente(idTokenRepository.getReferenceById(idPersona).getCliente());
        blacklist.setIngredienti(ingredienti.getIngredienti());
        blacklistRepository.save(blacklist);
        return true;
    }

    public ArraySezione ottieniMenu(int idMenu, String idPersona){
        if(!menuRepository.existsById(idMenu)) return null;
        if(!idTokenRepository.existsById(idPersona)) return null;
        List<SezionePreview> list_preview = menuRepository.findById(idMenu).get().getMenu();
        List<Sezione> list_sezione = new ArrayList<>();
        if(list_preview == null) return null;
        for(int i = 0; i < list_preview.size(); i++){
            Sezione sezione;
            List<Piatto> piatti = new ArrayList<>();
            for(int j = 0; j < list_preview.get(i).getPiatti().size(); j++){
                Piatto piatto = ottienPiatto(idPersona, list_preview.get(i).getPiatti().get(j).getId(), true);
                if(piatto != null) piatti.add(piatto);
            }
            sezione = new Sezione(list_preview.get(i).getNome(), piatti);
            list_sezione.add(sezione);
        }
        ArraySezione arraySezione = new ArraySezione(list_sezione);
        return arraySezione;
    }

    public Fasce ottieniFasce(int idMenu){
        if(!menuRepository.existsById(idMenu)) return null;
        if(menuRepository.getReferenceById(idMenu).getFasce() == null) return null;
        Fasce fasce = new Fasce(menuRepository.getReferenceById(idMenu).getFasce());
        return fasce;
    }

    public Preferiti ottieniListaPreferiti(int idMenu, String idPersona){
        if(!menuRepository.existsById(idMenu)) return null;
        if(!idTokenRepository.existsById(idPersona)) return null;
        Preferiti preferiti;
        List<Piatto> piatti = new ArrayList<>();
        Menu menu = menuRepository.getReferenceById(idMenu);
        for(int i = 0; i < menu.getMenu().size(); i++){
            for(int j = 0; j < menu.getMenu().get(i).getPiatti().size(); j++){
                Piatto piatto = ottienPiatto(idPersona, menu.getMenu().get(i).getPiatti().get(j).getId(), false);
                if(piatto.isPreferito()) piatti.add(piatto);
            }
        }
        preferiti = new Preferiti(piatti);
        return preferiti;
    }

    public Utente ottieniUtente(String idPersona){
        if(!idTokenRepository.existsById(idPersona)) return null;
        Utente utente = idTokenRepository.getReferenceById(idPersona).getCliente();
        return utente;
    }

    public boolean reimpostaPassword(String email, String newpass){
        if(!codeRepository.findById(email).get().isVerify()) return false;
        clientRepository.getReferenceById(email).setPassword(newpass);
        codeRepository.deleteById(email);
        return true;
    }

    private Piatto ottienPiatto(String idPersona, int idPiatto, boolean filtered){
        Piatto piatto;
        PiattoPreview piattoPreview = piattoPreviewRepository.findById(idPiatto).get();
        PiattoUpload piattoUpload = piattoUploadRepository.findById(idPiatto).get();
        if(filtered){
            List<Blacklist> allergeni = blacklistRepository.findAll();
            for(int i = 0; i < allergeni.size(); i++){
                if(allergeni.get(i).getUtente() == idTokenRepository.findById(idPersona).get().getCliente()){
                    for(int j = 0; j < allergeni.get(i).getIngredienti().size(); j++){
                        for(int k = 0; k < piattoUpload.getAllergeni().size(); k++){
                            if(allergeni.get(i).getIngredienti().get(j).compareTo(piattoUpload.getAllergeni().get(k)) == 0) return null;
                        }
                    }
                }
            }
        }
        if(piattoPreview == null || piattoUpload == null) return null;
        List<InformazioniPiatto> list = informazioniPiattoRepository.findAll();
        int valutazione_media = 0;
        int valutazione_utente = 0;
        int valutazione_tot = 0;
        int divisore = 0;
        boolean preferito = false;
        for(int j = 0; j < list.size(); j++){
            if(list.get(j).getPiatto().getId() == idPiatto){
                divisore++;
                valutazione_tot += list.get(j).getValutazione();
                if(list.get(j).getUtente().getIdPersona().getIdToken().compareTo(idPersona)==0){
                    preferito = list.get(j).isPreferito();
                    valutazione_utente = list.get(j).getValutazione();
                }
            }
        }
        if(divisore != 0) valutazione_media = valutazione_tot/divisore;
        boolean popolare = false;
        if(valutazione_media > 4) popolare = true;

        piatto = new Piatto(
            piattoPreview.getId(), 
            piattoUpload.getNumero(), 
            piattoUpload.getVariante(), 
            piattoPreview.getNome(), 
            piattoUpload.getPrezzo(), 
            piattoUpload.getAllergeni(), 
            piattoUpload.getIngredienti(), 
            piattoPreview.getLimite(), 
            valutazione_media, 
            valutazione_utente, 
            preferito, 
            "ultimoOrdine", 
            popolare, 
            piattoPreview.getConsigliato(), 
            piattoUpload.getImmagine(), 
            piattoUpload.getAlt()
            );
        return piatto;
    }


    public boolean mergeTavolo(String idTavolo, ListaOrdineMerge body) {
        if (!sessionRepository.existsById(idTavolo)) return false;
        for(int i = 0; i < body.getOrdini().size(); i++){
            if(!idTokenRepository.existsById(body.getOrdini().get(i).getIdPersona())) return false;
        }
        
        for(int i = 0; i < body.getOrdini().size(); i++){
            OrdineMerge ordine = body.getOrdini().get(i);
            List<Ordine> ordini = new ArrayList<>();
            Ordine e = new Ordine();
            e.setCount(ordine.getCount());
            e.setIdPiatto(ordine.getIdPiatto());
            e.setNote(ordine.getNote());
            e.setIdPersona(idTokenRepository.getReferenceById(ordine.getIdPersona()));
            ordini.add(e);
            inserisciOrdini(ordini, ordine.getIdPersona(), idTavolo);
        }
        return true;
    }

}
