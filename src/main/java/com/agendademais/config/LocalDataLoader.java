package com.agendademais.config;

import com.agendademais.entities.Local;
import com.agendademais.repositories.LocalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
@Order(1) // Executa antes do DataLoader principal
public class LocalDataLoader implements CommandLineRunner {

        private final LocalRepository localRepository;

        @Value("${app.reload-data:false}")
        private boolean reloadData;

        @Value("${spring.jpa.hibernate.ddl-auto}")
        private String ddlAuto;

        public LocalDataLoader(LocalRepository localRepository) {
                this.localRepository = localRepository;
        }

        @Override
        public void run(String... args) throws Exception {
                if (!shouldLoadData()) {
                        System.out.println("*** ");
                        System.out.println(
                                        "*** /config/LocalDataLoader.java: Dados já existentes ou configuração não exige recarga.");
                        System.out.println("*** ");
                        return;
                }

                System.out.println("*** ");
                System.out.println(
                                "*** /config/LocalDataLoader.java: Carregando a Tabela Local (Paises, Estados, Cidades).");
                System.out.println("*** ");

                // Limpa a tabela se necessário (opcional)
                localRepository.deleteAll();

                // Carga de Países
                Local brasil = localRepository.save(new Local(1, "Brasil", null));
                Local portugal = localRepository.save(new Local(1, "Portugal", null));
                Local argentina = localRepository.save(new Local(1, "Argentina", null));

                // Carga de Estados (exemplo Brasil)
                // Local ac = localRepository.save(new Local(2, "AC", brasil));
                // Local al = localRepository.save(new Local(2, "AL", brasil));
                // Local ap = localRepository.save(new Local(2, "AP", brasil));
                // Local am = localRepository.save(new Local(2, "AM", brasil));
                // Local ba = localRepository.save(new Local(2, "BA", brasil));
                // Local ce = localRepository.save(new Local(2, "CE", brasil));
                // Local df = localRepository.save(new Local(2, "DF", brasil));
                // Local es = localRepository.save(new Local(2, "ES", brasil));
                // Local go = localRepository.save(new Local(2, "GO", brasil));
                // Local ma = localRepository.save(new Local(2, "MA", brasil));
                // Local mt = localRepository.save(new Local(2, "MT", brasil));
                // Local ms = localRepository.save(new Local(2, "MS", brasil));
                // Local mg = localRepository.save(new Local(2, "MG", brasil));
                // Local pa = localRepository.save(new Local(2, "PA", brasil));
                // Local pb = localRepository.save(new Local(2, "PB", brasil));
                Local pr = localRepository.save(new Local(2, "PR", brasil));
                // Local pe = localRepository.save(new Local(2, "PE", brasil));
                // Local pi = localRepository.save(new Local(2, "PI", brasil));
                Local rj = localRepository.save(new Local(2, "RJ", brasil));
                // Local rn = localRepository.save(new Local(2, "RN", brasil));
                // Local rs = localRepository.save(new Local(2, "RS", brasil));
                // Local ro = localRepository.save(new Local(2, "RO", brasil));
                // Local rr = localRepository.save(new Local(2, "RR", brasil));
                // Local sc = localRepository.save(new Local(2, "SC", brasil));
                Local sp = localRepository.save(new Local(2, "SP", brasil));
                // Local se = localRepository.save(new Local(2, "SE", brasil));
                // Local to = localRepository.save(new Local(2, "TO", brasil));

                // Cidades de SP
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "São Paulo", sp),
                                new Local(3, "São Caetano do Sul", sp),
                                new Local(3, "São Bernardo do Campo", sp),
                                new Local(3, "Santo André", sp),
                                new Local(3, "Santos", sp),
                                new Local(3, "São Vicente", sp),
                                new Local(3, "Campinas", sp),
                                new Local(3, "Mogi das Cruzes", sp)));

                // Cidades de RJ
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Rio de Janeiro", rj),
                                new Local(3, "Niterói", rj),
                                new Local(3, "Campos", rj),
                                new Local(3, "Volta Redonda", rj)));

                // Cidades de PR
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Curitiba", pr),
                                new Local(3, "Cascavel", pr)));

                // Carga de Estados - Provincias de Portugal
                Local li = localRepository.save(new Local(2, "Lisboa", portugal));
                Local co = localRepository.save(new Local(2, "Coimbra", portugal));
                Local po = localRepository.save(new Local(2, "Porto", portugal));

                // Cidades de li - Lisboa: Lisboa, Sintra, Amadora
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Lisboa", li),
                                new Local(3, "Sintra", li),
                                new Local(3, "Amadora", li)));

                // Cidades de co - Coimbra: Coimbra, Figueira da Foz, Cantanhede
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Coimbra", co),
                                new Local(3, "Figueira da Foz", co),
                                new Local(3, "Cantanhede", co)));

                // Cidades de po - Porto: Porto, Vila Nova de Gaia, Matosinhos.
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Porto", po),
                                new Local(3, "Vila Nova de Gaia", po),
                                new Local(3, "Matosinhos", po)));

                System.out.println("*** ");
                System.out.println("*** /config/LocalDataLoader.java: Dados iniciais da Tabela Local carregados.");
                System.out.println("*** ");

        }

        private boolean shouldLoadData() {
                // Só carrega se explicitamente solicitado OU se banco for criado do zero
                if (reloadData)
                        return true;
                return "create".equalsIgnoreCase(ddlAuto) || "create-drop".equalsIgnoreCase(ddlAuto);
        }
}
