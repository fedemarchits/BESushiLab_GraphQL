package it.synclab.sushiLab.classes;

import java.util.List;

import it.synclab.sushiLab.entity.PiattoPreview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListaPiatti {
    private List<PiattoPreview> piatti;
}
