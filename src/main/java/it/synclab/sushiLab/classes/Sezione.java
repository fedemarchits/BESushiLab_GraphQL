package it.synclab.sushiLab.classes;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sezione {
    private String nome;
    private List<Piatto> piatti;
}
