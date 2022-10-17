package it.synclab.sushiLab.classes;

import java.util.List;

import it.synclab.sushiLab.entity.Ordine;
import lombok.Data;


@Data
public class ListaOrdini {
    private List<Ordine> ordini;
}
