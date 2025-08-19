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
                Local franca = localRepository.save(new Local(1, "França", null));
                // Local argentina = localRepository.save(new Local(1, "Argentina", null));

                // Carga de Estados (Exemplo Brasil)
                Local ac = localRepository.save(new Local(2, "Acre", brasil));
                Local al = localRepository.save(new Local(2, "Alagoas", brasil));
                Local ap = localRepository.save(new Local(2, "Amapá", brasil));
                Local am = localRepository.save(new Local(2, "Amazonas", brasil));
                Local ba = localRepository.save(new Local(2, "Bahia", brasil));
                Local ce = localRepository.save(new Local(2, "Ceará", brasil));
                Local df = localRepository.save(new Local(2, "Distrito Federal", brasil));
                Local es = localRepository.save(new Local(2, "Espírito Santo", brasil));
                Local go = localRepository.save(new Local(2, "Goiás", brasil));
                Local ma = localRepository.save(new Local(2, "Maranhão", brasil));
                Local mt = localRepository.save(new Local(2, "Mato Grosso", brasil));
                Local ms = localRepository.save(new Local(2, "Mato Grosso do Sul", brasil));
                Local mg = localRepository.save(new Local(2, "Minas Gerais", brasil));
                Local pa = localRepository.save(new Local(2, "Pará", brasil));
                Local pb = localRepository.save(new Local(2, "Paraíba", brasil));
                Local pr = localRepository.save(new Local(2, "Paraná", brasil));
                Local pe = localRepository.save(new Local(2, "Pernambuco", brasil));
                Local pi = localRepository.save(new Local(2, "Piauí", brasil));
                Local rj = localRepository.save(new Local(2, "Rio de Janeiro", brasil));
                Local rn = localRepository.save(new Local(2, "Rio Grande do Norte", brasil));
                Local rs = localRepository.save(new Local(2, "Rio Grande do Sul", brasil));
                Local ro = localRepository.save(new Local(2, "Rondônia", brasil));
                Local rr = localRepository.save(new Local(2, "Roraima", brasil));
                Local sc = localRepository.save(new Local(2, "Santa Catarina", brasil));
                Local sp = localRepository.save(new Local(2, "São Paulo", brasil));
                Local se = localRepository.save(new Local(2, "Sergipe", brasil));
                Local to = localRepository.save(new Local(2, "Tocantins", brasil));

                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Rio Branco", ac),
                                new Local(3, "Maceió", al),
                                new Local(3, "Macapá", ap),
                                new Local(3, "Manaus", am),
                                new Local(3, "Salvador", ba),
                                new Local(3, "Fortaleza", ce),
                                new Local(3, "Brasília", df),
                                new Local(3, "Vitória", es),
                                new Local(3, "Goiânia", go),
                                new Local(3, "São Luís", ma),
                                new Local(3, "Belo Horizonte", mt),
                                new Local(3, "Campo Grande", ms),
                                new Local(3, "Cuiabá", mg),
                                new Local(3, "Belém", pa),
                                new Local(3, "João Pessoa", pb),

                                new Local(3, "Curitiba", pr),
                                new Local(3, "Cascavel", pr),

                                new Local(3, "Recife", pe),
                                new Local(3, "Teresina", pi),

                                new Local(3, "Rio de Janeiro", rj),
                                new Local(3, "Niterói", rj),
                                new Local(3, "Campos", rj),
                                new Local(3, "Volta Redonda", rj),

                                new Local(3, "Natal", rn),
                                new Local(3, "Porto Alegre", rs),
                                new Local(3, "Porto Velho", ro),
                                new Local(3, "Boa Vista", rr),
                                new Local(3, "Florianópolis", sc),

                                new Local(3, "São Paulo", sp),
                                new Local(3, "São Caetano do Sul", sp),
                                new Local(3, "São Bernardo do Campo", sp),
                                new Local(3, "Santo André", sp),
                                new Local(3, "Mauá", sp),
                                new Local(3, "Osasco", sp),
                                new Local(3, "Guarulhos", sp),
                                new Local(3, "Santos", sp),
                                new Local(3, "São Vicente", sp),
                                new Local(3, "Mongaguá", sp),
                                new Local(3, "Itanhahém", sp),
                                new Local(3, "Campinas", sp),
                                new Local(3, "Mogi das Cruzes", sp),

                                new Local(3, "Aracaju", se),
                                new Local(3, "Palmas", to)));

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

                // Carga de Regiões da França
                Local idf = localRepository.save(new Local(2, "Île-de-France", franca));
                Local paca = localRepository.save(new Local(2, "Provence-Alpes-Côte d'Azur", franca));
                Local ara = localRepository.save(new Local(2, "Auvergne-Rhône-Alpes", franca));
                Local occ = localRepository.save(new Local(2, "Occitanie", franca));

                // Cidades da Île-de-France
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Paris", idf),
                                new Local(3, "Versailles", idf),
                                new Local(3, "Boulogne-Billancourt", idf)));

                // Cidades de Provence-Alpes-Côte d'Azur
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Marseille", paca),
                                new Local(3, "Nice", paca),
                                new Local(3, "Cannes", paca)));

                // Cidades de Auvergne-Rhône-Alpes
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Lyon", ara),
                                new Local(3, "Grenoble", ara),
                                new Local(3, "Saint-Étienne", ara)));

                // Cidades de Occitanie
                localRepository.saveAll(Arrays.asList(
                                new Local(3, "Toulouse", occ),
                                new Local(3, "Montpellier", occ),
                                new Local(3, "Perpignan", occ)));

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
