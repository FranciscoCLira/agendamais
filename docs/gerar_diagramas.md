# Como Gerar Diagramas PNG para AgendaMais V2.0

## 📋 Instruções para Gerar Imagens

### 1. **Diagrama PlantUML → PNG**

```bash
# 1. Baixar PlantUML (versão estável)
curl -L -o plantuml.jar "https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml-1.2024.0.jar"

# 2. Gerar PNG do diagrama V2.0 completo
java -jar plantuml.jar docs/AgendaMais_ERD_V2_PlantUML.txt

# 3. Gerar PNG do diagrama simplificado
java -jar plantuml.jar docs/agenda_mais_erd_v2_simples.puml

# 4. Arquivos gerados:
# - docs/AgendaMais_ERD_V2_PlantUML.png (completo)
# - docs/agenda_mais_erd_v2_simples.png (simplificado para README)
```

### 2. **VS Code - Método Recomendado**

**PlantUML Extension:**

```bash
# 1. Instalar extensão
code --install-extension jebbs.plantuml

# 2. Abrir arquivo .puml no VS Code
code docs/agenda_mais_erd_v2_simples.puml

# 3. Gerar PNG:
# - Ctrl+Shift+P → "PlantUML: Export Current Diagram"
# - Ou: Alt+D para preview e salvar
```

### 3. **Online (Backup)**

**PlantText.com:**

1. Acesse: http://www.plantuml.com/plantuml/uml/
2. Cole o conteúdo de `agenda_mais_erd_v2_simples.puml`
3. Gera automaticamente e permite download PNG
4. Salve como: `docs/agenda_mais_erd_v2.png`

### 4. **Diagrama Mermaid → PNG**

**Mermaid Live Editor:**

1. Acesse: https://mermaid.live/
2. Cole o conteúdo de `docs/AgendaMais_ERD_V2_Mermaid.md`
3. Exporte como PNG
4. Salve como: `docs/agenda_mais_erd_v2_mermaid.png`

---

## 📁 Arquivos de Saída Esperados

```
docs/
├── agenda_mais_erd_v2.png              # Diagrama V2.0 principal
├── agenda_mais_erd_v2_completo.png     # Versão detalhada
├── AgendaMais_ERD_V2_PlantUML.png      # PlantUML auto-gerado
└── agenda_mais_erd_v2_mermaid.png      # Mermaid gerado
```

---

## 🔄 Atualizações no README.md

Após gerar as imagens, atualizar:

```markdown
## 📁 Modelo Entidade-Relacionamento (MER) - V2.0

**Arquitetura Atualizada**: Níveis de acesso por instituição

![ERD V2.0](docs/agenda_mais_erd_v2.png)

### Documentação Completa:

- **[Modelo ER V2.0 - Textual](docs/agenda_mais_modelo_er_v2.md)**
- **[Diagrama PlantUML V2.0](docs/AgendaMais_ERD_V2_PlantUML.txt)**
- **[Diagrama Mermaid V2.0](docs/AgendaMais_ERD_V2_Mermaid.md)**
- **[Comparativo V1→V2](docs/Migracao_Arquitetural_V1_V2_Comparativo.md)**
```
