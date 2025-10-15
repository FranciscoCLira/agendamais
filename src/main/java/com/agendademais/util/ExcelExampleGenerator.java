package com.agendademais.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelExampleGenerator {

    public static void main(String[] args) {
        try {
            generateExampleExcel();
            System.out.println("Arquivo Excel de exemplo gerado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateExampleExcel() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Exemplo Usuários");

        // Criação do cabeçalho
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "email", "nome", "celular", "pais", "estado", "cidade",
                "comentarios", "instituicaoId", "identificacaoPessoaInstituicao",
                "subInstituicaoId", "identificacaoPessoaSubInstituicao"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Dados de exemplo
        String[][] data = {
                { "joao.silva@teste.com", "João da Silva", "11999999999", "Brasil", "São Paulo", "São Paulo",
                        "Usuário de teste com acentuação", "", "", "", "" },
                { "maria.santos@exemplo.com", "Maria José Santos", "21888888888", "Brasil", "Rio de Janeiro",
                        "Rio de Janeiro", "Usuária de produção com ç", "", "", "", "" },
                { "pedro.oliveira@demo.com", "Pedro José Oliveira", "11987654321", "Brasil", "São Paulo", "Campinas",
                        "Usuário exemplo ação ção", "", "", "", "" },
                { "ana.costa@amostra.com", "Ana Cristina Costa", "85999887766", "Brasil", "Ceará", "Fortaleza",
                        "Demonstração de acentuação gráfica", "", "", "", "" },
                { "carlos.ferreira@modelo.com", "Carlos Antônio Ferreira", "47991234567", "Brasil", "Santa Catarina",
                        "Florianópolis", "Teste de caracteres especiais çãõ", "", "", "", "" },
                { "lucia.mendes@exemplo.com", "Lúcia Helena Mendes", "62988776655", "Brasil", "Goiás", "Goiânia",
                        "Validação de ênfase e tildes", "", "", "", "" },
                { "roberto.nunes@teste.com", "Roberto André Nunes", "84987654321", "Brasil", "Rio Grande do Norte",
                        "Natal", "Exemplo com números e ñ opcional", "", "", "", "" },
                { "beatriz.alves@demo.com", "Beatriz Ação Alves", "11976543210", "Brasil", "São Paulo",
                        "São Bernardo do Campo", "Usuária com açções múltiplas", "", "", "", "" },
                { "manuel.pereira@portugal.com", "Manuel António Pereira", "351912345678", "Portugal", "Lisboa",
                        "Lisboa", "Usuário de teste português", "", "", "", "" },
                { "sofia.rodrigues@portugal.com", "Sofia Maria Rodrigues", "351923456789", "Portugal", "Porto", "Porto",
                        "Teste de validação portuguesa", "", "", "", "" },
                { "antonio.silva@portugal.com", "António José Silva", "351934567890", "Portugal", "Lisboa", "Cascais",
                        "Exemplo de distrito Lisboa", "", "", "", "" },
                { "pierre.dubois@france.com", "Pierre Jean Dubois", "33612345678", "França", "Île-de-France", "Paris",
                        "Utilisateur français de test", "", "", "", "" },
                { "marie.martin@france.com", "Marie Claire Martin", "33623456789", "França", "Île-de-France",
                        "Versailles", "Exemple de validation française", "", "", "", "" }
        };

        // Criação das linhas de dados
        for (int i = 0; i < data.length; i++) {
            Row dataRow = sheet.createRow(i + 1);
            for (int j = 0; j < data[i].length; j++) {
                Cell cell = dataRow.createCell(j);
                cell.setCellValue(data[i][j]);
            }
        }

        // Auto-ajuste das colunas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Salvar arquivo
        String filePath = System.getProperty("user.dir") + "/src/main/resources/static/exemplo-usuarios.xlsx";
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        }

        workbook.close();
    }
}
