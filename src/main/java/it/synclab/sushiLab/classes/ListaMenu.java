package it.synclab.sushiLab.classes;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.synclab.sushiLab.classes.MenuRidotto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListaMenu {
    private List<MenuRidotto> list;
}
