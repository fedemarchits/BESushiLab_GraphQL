package it.synclab.sushiLab.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Session {
    @Id
    @Column(name = "id_table")
    private String idTable;
    @OneToMany(mappedBy = "tavolo")
    private List<Utente> clienti;
}
