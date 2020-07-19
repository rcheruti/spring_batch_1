package br.eng.rcc;

import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
@EnableBatchProcessing
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // --------------------------------------------

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    // --------------------------------------------

    @Bean
    public FlatFileItemReader<Pessoa> leitura() {
        return new FlatFileItemReaderBuilder<Pessoa>()
            .name("personItemReader")
            .resource(new ClassPathResource("dados.csv"))
            .delimited()
            .names(new String[]{"nome", "nascimento"})
            .fieldSetMapper(new BeanWrapperFieldSetMapper<Pessoa>() {{
                // configurar para usar o nosso conversor de datas
                setConversionService( conversionService() );
                setTargetType(Pessoa.class);
            }})
            .build();
    }

    @Bean
    public PessoaProcessador processador() {
        return new PessoaProcessador();
    }

    @Bean
    public LogWriter<Pessoa> escrita() {
        return new LogWriter<Pessoa>();
    }

    @Bean // esse método é para configurar o executor de cada passo na tarefa
    public Step passos() {
        return stepBuilderFactory.get("pessoaStep")
            .<Pessoa, Pessoa> chunk(4)
            .reader( leitura() )
            .processor( processador() )
            .writer( escrita() )
            .build();
    }

    @Bean // esse método é para configurar a execução das tarefas
    public Job configBatch(OuvinteBatch ouvinte, Step passo) {
        return jobBuilderFactory.get("pessoaJob")
            .incrementer( new RunIdIncrementer() )
            .listener( ouvinte )
            .flow( passo )
            .end()
            .build();
    }

    // configurar o conversor de datas
    public ConversionService conversionService() {
        DefaultConversionService serv = new DefaultConversionService();
        serv.addConverter( new ConverterDateLocal() );
        DefaultConversionService.addDefaultConverters( serv );
        return serv;
    }

    // -------

    // Para converter as datas no arquivo CSV de texto para LocalDate
    private class ConverterDateLocal implements Converter<String, LocalDate> {
        public LocalDate convert(String text) {
            return LocalDate.parse( text );
        }
    }

}
