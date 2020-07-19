package br.eng.rcc;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.batch.item.ItemProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PessoaProcessador implements ItemProcessor<Pessoa, Pessoa> {

    private LocalDate hoje = LocalDate.now();

    @Override
    public Pessoa process(Pessoa pessoa) throws Exception {
        
        Period diff = Period.between(pessoa.getNascimento(), hoje);
        pessoa.setIdade( diff.getYears() );
        log.info("Pessoa: {}", pessoa.getNome() );

        return pessoa;
    }

}