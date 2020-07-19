package br.eng.rcc;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Pessoa {
    
    private String nome;
    private LocalDate nascimento;
    
    private int idade;
    
}
