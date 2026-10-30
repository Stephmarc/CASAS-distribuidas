package pe.edu.upeu.casaonada.cliente.domain;

import jakarta.persistence.*;
@Entity @Table(name="clientes") public class Cliente {
 @Id public Long id;
 @Column(nullable=false,length=160) public String nombres;
 @Column(nullable=false,unique=true,length=200) public String email;
 @Column(nullable=false) public boolean activo=true;
 public Cliente(){}
}
