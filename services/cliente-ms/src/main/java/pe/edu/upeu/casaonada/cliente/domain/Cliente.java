package pe.edu.upeu.casaonada.cliente.domain;

import jakarta.persistence.*;
@Entity @Table(name="clientes") public class Cliente {
 @Id public Long id;
 @Column(nullable=false,length=160) public String nombres;
 @Column(nullable=false,unique=true,length=200) public String email;
 @Column(nullable=false) public boolean activo=true;
 @Column(unique=true,length=8) public String dni;
 @Column(length=30) public String telefono;
 public Cliente(){}
}
