package com.agendademais.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CSVUTFWriter {
    
    public static void createExampleCSVWithBOM() throws IOException {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/static/exemplo-usuarios.csv";
        
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
            
            // Adicionar BOM UTF-8
            fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            
            // Cabeçalho
            writer.write("email;nome;celular;pais;estado;cidade;comentarios;instituicaoId;identificacaoPessoaInstituicao;subInstituicaoId;identificacaoPessoaSubInstituicao");
            writer.newLine();
            
            // Dados de exemplo
            writer.write("joao.silva@teste.com;João da Silva;11999999999;Brasil;São Paulo;São Paulo;Usuário de teste com acentuação;;;");
            writer.newLine();
            writer.write("maria.santos@exemplo.com;Maria José Santos;21888888888;Brasil;Rio de Janeiro;Rio de Janeiro;Usuária de produção com ç;;;");
            writer.newLine();
            writer.write("pedro.oliveira@demo.com;Pedro José Oliveira;11987654321;Brasil;São Paulo;Campinas;Usuário exemplo ação ção;;;");
            writer.newLine();
            writer.write("ana.costa@amostra.com;Ana Cristina Costa;85999887766;Brasil;Ceará;Fortaleza;Demonstração de acentuação gráfica;;;");
            writer.newLine();
            writer.write("carlos.ferreira@modelo.com;Carlos Antônio Ferreira;47991234567;Brasil;Santa Catarina;Florianópolis;Teste de caracteres especiais çãõ;;;");
            writer.newLine();
            writer.write("lucia.mendes@exemplo.com;Lúcia Helena Mendes;62988776655;Brasil;Goiás;Goiânia;Validação de ênfase e tildes;;;");
            writer.newLine();
            writer.write("roberto.nunes@teste.com;Roberto André Nunes;84987654321;Brasil;Rio Grande do Norte;Natal;Exemplo com números e ñ opcional;;;");
            writer.newLine();
            writer.write("beatriz.alves@demo.com;Beatriz Ação Alves;11976543210;Brasil;São Paulo;São Bernardo do Campo;Usuária com açções múltiplas;;;");
            writer.newLine();
        }
        
        System.out.println("Arquivo CSV criado com UTF-8 BOM: " + filePath);
    }
    
    public static void main(String[] args) {
        try {
            createExampleCSVWithBOM();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
