# 📊 Atualização do Modelo de Dados - Entidade Local

## 🔄 Mudanças no Diagrama ER

### ➕ **Nova Entidade: Local**

```sql
entity Local {
  *id : BIGINT
  *nomeLocal : VARCHAR(100)
  *tipoLocal : INT              -- 1=País, 2=Estado, 3=Cidade
  revisado : BOOLEAN            -- Flag para controle de qualidade
  dataInclusao : DATE
  dataUltimaAtualizacao : DATE
  localPai_id : BIGINT         -- Referência hierárquica
}
```

### 🔧 **Entidade Pessoa Refatorada**

**Antes:**

```sql
*nomePaisPessoa : VARCHAR(80)
*nomeEstadoPessoa : VARCHAR(80)
*nomeCidadePessoa : VARCHAR(80)
```

**Depois:**

```sql
pais_id : BIGINT     -- FK para Local (tipoLocal=1)
estado_id : BIGINT   -- FK para Local (tipoLocal=2)
cidade_id : BIGINT   -- FK para Local (tipoLocal=3)
```

### 🔗 **Novos Relacionamentos**

- `Local }o--o| Local : "localPai"` - Hierarquia entre locais
- `Pessoa }o--|| Local : "pais"` - Pessoa → País
- `Pessoa }o--|| Local : "estado"` - Pessoa → Estado
- `Pessoa }o--|| Local : "cidade"` - Pessoa → Cidade

### 📋 **Estrutura Hierárquica**

```
País (tipoLocal=1, localPai_id=NULL)
├── Estado (tipoLocal=2, localPai_id=ID_DO_PAÍS)
│   ├── Cidade (tipoLocal=3, localPai_id=ID_DO_ESTADO)
│   └── Cidade (tipoLocal=3, localPai_id=ID_DO_ESTADO)
└── Estado (tipoLocal=2, localPai_id=ID_DO_PAÍS)
    └── Cidade (tipoLocal=3, localPai_id=ID_DO_ESTADO)
```

### ✅ **Benefícios da Normalização**

- **Consistência**: Eliminação de duplicatas em nomes de locais
- **Integridade**: Referências garantem dados válidos
- **Performance**: Consultas otimizadas com índices
- **Manutenibilidade**: Correções centralizadas na entidade Local
- **Flexibilidade**: Fácil adição de novos países/estados/cidades

---

_Arquivo gerado automaticamente em agosto de 2025 como parte da documentação do diagrama ER atualizado._
