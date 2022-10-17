package it.synclab.sushiLab.classes;

import java.util.List;

import it.synclab.sushiLab.entity.FasciaOraria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fasce {
    private List<FasciaOraria> fasce;
}
