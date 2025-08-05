// ========================================================================
// local-form.js - Versão corrigida e unificada para AgendaMais
// Suporta "Outro", autocomplete, e troca segura de país/estado/cidade
// ========================================================================
// Responsável por carregar os selects dinamicamente via AJAX.
// EXECUTA ESTE CODIGO SOMENTE QUANDO A PAGINA JA ESTIVER CARREGADA
// ========================================================================

// Manipula campos "Outro" e carrega listas de estados/cidades por AJAX
// ========================================================================

//  document.addEventListener("DOMContentLoaded", () => {
//  const estadoSelect = document.getElementById("estadoSelect");
//  if (estadoSelect) {
//      console.log("=== DEBUG ESTADO SELECTED (local-form.js)===");
//      console.log("Valor estadoSelect:", estadoSelect.value);
//      [...estadoSelect.options].forEach(op => {
//      console.log(`option: ${op.value}, selected=${op.selected}`);
//      });
//  }
//  });


function paisChange(event) {
    const pais = event?.target?.value || document.getElementById("paisSelect")?.value;
    const paisOutro = document.getElementById("paisOutro");
    const estadoSelect = document.getElementById("estadoSelect");
    const estadoOutro = document.getElementById("estadoOutro");
    const cidadeInput = document.getElementById("cidade");
    const cidadeOutro = document.getElementById("cidadeOutro");

    if (pais === "Outro") {
        // Garante que o select tem a opção "Outro" antes de defini-la
        if (estadoSelect && !Array.from(estadoSelect.options).some(opt => opt.value === "Outro")) {
            estadoSelect.innerHTML = '<option value="">Selecione</option><option value="Outro">Outro</option>';
        }
        
        paisOutro?.classList.remove("hidden");
        estadoOutro?.classList.remove("hidden");
        // Quando país é "Outro", esconde o select de estado mas mantém o campo cidade visível
        estadoSelect?.classList.add("hidden");
        
        // DEFINE o estado como "Outro" automaticamente quando país é "Outro"
        if (estadoSelect) {
            estadoSelect.value = "Outro";
            console.log("Estado definido como 'Outro' porque país é 'Outro'");
        }
        
        // MANTÉM o campo cidade visível para que o usuário possa digitar a cidade
        if (cidadeInput) {
            cidadeInput.classList.remove("hidden");
            // LIMPA o valor do campo cidade quando país é "Outro"
            cidadeInput.value = '';
        }
        
        // LIMPA o datalist de cidades quando país é "Outro"
        const datalist = document.getElementById("listaCidades");
        if (datalist) {
            datalist.innerHTML = '';
        }
        
        // Esconde o campo cidadeOutro
        cidadeOutro?.classList.add("hidden");
        if (cidadeOutro) {
            cidadeOutro.value = "";
        }
    } else {
        paisOutro?.classList.add("hidden");
        // Só esconde estado/cidade "Outro" se não estiverem selecionados como "Outro"
        if (estadoSelect?.value !== "Outro") {
            estadoOutro?.classList.add("hidden");
        }
        // Para cidade, esconde o campo "Outro" se não estiver sendo usado
        const cidadeInputValue = cidadeInput?.value;
        if (cidadeInputValue !== "Outro") {
            cidadeOutro?.classList.add("hidden");
            if (cidadeOutro) {
                cidadeOutro.value = "";
            }
        }
        
        estadoSelect?.classList.remove("hidden");
        if (cidadeInput) {
            cidadeInput.classList.remove("hidden");
        }
    }

    if (pais !== "Outro") {
        buscarEstados(pais);
        
        // Remove duplicatas de "Outro" após trocar de país
        setTimeout(() => {
            if (estadoSelect) {
                removerDuplicatasOutro(estadoSelect);
                debugDuplicatasOutro();
            }
        }, 100);
    }
}

function estadoChange(event) {
    const estado = event?.target?.value || document.getElementById("estadoSelect")?.value;

    const estadoOutro = document.getElementById("estadoOutro");
    const cidadeOutro = document.getElementById("cidadeOutro");
    
    console.log("Estado mudou para:", estado);
    
    if (estado === "Outro") {
        estadoOutro?.classList.remove("hidden");
        // Quando estado é "Outro", NÃO mostra automaticamente campo cidade "Outro"
        // O usuário pode digitar diretamente no campo cidade principal
    } else {
        estadoOutro?.classList.add("hidden");
        // Se estado não é "Outro", esconde campo cidade "Outro" apenas se não estiver sendo usado
        const cidadeInput = document.getElementById("cidade");
        if (cidadeInput && cidadeInput.value !== "Outro") {
            cidadeOutro?.classList.add("hidden");
        }
        if (estadoOutro) {
            estadoOutro.value = ""; // Limpa o campo quando não é "Outro"
        }
        if (cidadeOutro && (!cidadeInput || cidadeInput.value !== "Outro")) {
            cidadeOutro.value = ""; // Limpa o campo cidade "Outro" se não estiver sendo usado
        }
    }

    if (estado && estado !== "Outro" && estado !== "") {
        buscarCidadesPorEstado(estado);
    } else {
        // Se estado está vazio ou é "Outro", limpa as cidades do datalist
        const datalist = document.getElementById("listaCidades");
        if (datalist) datalist.innerHTML = '';
        // NÃO limpa o valor do campo cidade quando estado é "Outro"
        if (estado === "" || (estado !== "Outro")) {
            const inputCidade = document.getElementById("cidade");
            if (inputCidade) inputCidade.value = '';
        }
    }
}

function cidadeChange(event) {
    const cidade = event?.target?.value || document.getElementById("cidade")?.value;
    const cidadeOutro = document.getElementById("cidadeOutro");
    
    console.log("Cidade mudou para:", cidade);
    
    // Só mostra campo "Outro" se o usuário digitar exatamente "Outro"
    if (cidade && cidade.toLowerCase() === "outro") {
        cidadeOutro?.classList.remove("hidden");
        console.log("Mostrando campo cidadeOutro");
    } else {
        cidadeOutro?.classList.add("hidden");
        if (cidadeOutro) {
            cidadeOutro.value = ""; // Limpa o campo quando não é "Outro"
        }
        console.log("Escondendo campo cidadeOutro");
    }
}

function buscarEstados(pais) {
    const estadoSelect = document.getElementById("estadoSelect");
    const cidadeInput = document.getElementById("cidade");

    if (!pais || !estadoSelect) return;

    console.log("Buscando estados para país:", pais);

    // Limpa completamente o select primeiro
    estadoSelect.innerHTML = '';
    
    // Limpa o campo cidade e seu datalist
    if (cidadeInput) {
        cidadeInput.value = '';
    }
    
    // Limpa o datalist de cidades
    const datalist = document.getElementById("listaCidades");
    if (datalist) {
        console.log("Limpando datalist na troca de país. Opções antes:", datalist.children.length);
        datalist.innerHTML = '';
        console.log("Datalist limpo na troca de país. Opções após:", datalist.children.length);
    }
    
    // Esconde campos "Outro" de estado e cidade
    const estadoOutro = document.getElementById("estadoOutro");
    const cidadeOutro = document.getElementById("cidadeOutro");
    if (estadoOutro) {
        estadoOutro.classList.add("hidden");
        estadoOutro.value = "";
    }
    if (cidadeOutro) {
        cidadeOutro.classList.add("hidden");
        cidadeOutro.value = "";
    }

    if (pais === "Outro") {
        estadoSelect.innerHTML = '<option value="">Selecione</option><option value="Outro">Outro</option>';
        return;
    }

    // Primeiro define o básico, depois busca os estados
    estadoSelect.innerHTML = '<option value="">Carregando...</option>';

    fetch('/api/locais/estados?paisNome=' + encodeURIComponent(pais))
        .then(resp => resp.json())
        .then(estados => {
            console.log("Estados encontrados:", estados.length);
            
            // Cria array de options para evitar duplicação
            const options = ['<option value="">Selecione</option>'];
            
            // Adiciona estados encontrados
            estados.forEach(estado => {
                options.push(`<option value="${estado.nomeLocal}">${estado.nomeLocal}</option>`);
            });
            
            // Adiciona "Outro" sempre no final
            options.push('<option value="Outro">Outro</option>');
            
            // Define todo o conteúdo de uma só vez
            estadoSelect.innerHTML = options.join('');
            
            console.log("Select de estados atualizado. Total de opções:", estadoSelect.options.length);
            
            // Debug para verificar duplicações
            debugDuplicatasOutro();
        })
        .catch(err => {
            console.error('Erro ao buscar estados:', err);
            // Em caso de erro, reconstrói com apenas "Selecione" e "Outro"
            estadoSelect.innerHTML = '<option value="">Selecione</option><option value="Outro">Outro</option>';
        });
}

function buscarCidadesPorEstado(estado, preservarValorCidade = false) {
    const datalist = document.getElementById("listaCidades");
    const inputCidade = document.getElementById("cidade");

    console.log("Buscando cidades para estado:", estado, "preservarValorCidade:", preservarValorCidade);

    if (!estado || !datalist || !inputCidade) return;

    // Salva o valor atual da cidade se deve preservar
    const valorCidadeAtual = preservarValorCidade ? inputCidade.value : '';

    // SEMPRE limpa o datalist e o campo (exceto se deve preservar)
    limparDatalistCompleto(datalist);
    
    if (!preservarValorCidade) {
        inputCidade.value = '';
    }

    if (estado === "Outro" || estado === "") {
        console.log("Estado é 'Outro' ou vazio, não buscando cidades");
        return;
    }

    fetch('/api/locais/cidades?estadoNome=' + encodeURIComponent(estado) + '&t=' + Date.now())
        .then(resp => resp.json())
        .then(cidades => {
            console.log("Cidades encontradas para", estado + ":", cidades.length);
            
            // FORÇA limpeza completa do datalist antes de adicionar
            limparDatalistCompleto(datalist);
            
            if (cidades.length === 0) {
                console.log("Nenhuma cidade cadastrada, usuário pode digitar livremente");
            } else {
                // Cria um Set para evitar duplicatas
                const cidadesUnicas = new Set();
                
                console.log("Adicionando cidades ao datalist...");
                cidades.forEach((cidade, index) => {
                    const nomeCidade = cidade.nomeLocal;
                    
                    if (!cidadesUnicas.has(nomeCidade)) {
                        cidadesUnicas.add(nomeCidade);
                        console.log(`Adicionando cidade ${index + 1}:`, nomeCidade);
                        
                        const opt = document.createElement("option");
                        opt.value = nomeCidade;
                        datalist.appendChild(opt);
                    } else {
                        console.warn(`Cidade duplicada IGNORADA:`, nomeCidade);
                    }
                });
                
                console.log("Cidades adicionadas. Total de opções no datalist:", datalist.children.length);
                console.log("Cidades únicas processadas:", cidadesUnicas.size);
                
                // VERIFICAÇÃO ADICIONAL: Remove duplicatas que possam ter sido criadas
                const todasOpcoes = Array.from(datalist.children);
                const valoresUnicos = new Set();
                const opcoesDuplicadas = [];
                
                todasOpcoes.forEach(opcao => {
                    if (valoresUnicos.has(opcao.value)) {
                        opcoesDuplicadas.push(opcao);
                        console.warn("DUPLICATA DETECTADA E REMOVIDA:", opcao.value);
                    } else {
                        valoresUnicos.add(opcao.value);
                    }
                });
                
                // Remove as duplicatas encontradas
                opcoesDuplicadas.forEach(opcao => opcao.remove());
                
                // Debug final - lista todas as opções do datalist
                console.log("DEBUG: Todas as opções FINAIS do datalist de cidades:");
                Array.from(datalist.children).forEach((opt, index) => {
                    console.log(`  ${index + 1}: "${opt.value}"`);
                });
            }
            
            // Restaura o valor da cidade se deve preservar
            if (preservarValorCidade && valorCidadeAtual) {
                inputCidade.value = valorCidadeAtual;
                console.log("Valor da cidade restaurado:", valorCidadeAtual);
            }
        })
        .catch(err => {
            console.error('Erro ao buscar cidades:', err);
        });
}


function buscarCidadesAuto(query) {
    const estado = document.getElementById("estadoSelect")?.value;
    if (!estado || !query) return;

    fetch(`/api/locais/cidades/auto?estado=${encodeURIComponent(estado)}&query=${encodeURIComponent(query)}`)
        .then(resp => resp.json())
        .then(cidades => {
            const sugestoes = document.getElementById("sugestoesCidade");
            if (!sugestoes) return;

            sugestoes.innerHTML = '';
            cidades.forEach(cid => {
                const li = document.createElement("li");
                li.textContent = cid.nomeLocal;
                li.onclick = function () {
                    document.getElementById("cidadeAuto").value = cid.nomeLocal;
                    sugestoes.innerHTML = '';
                };
                sugestoes.appendChild(li);
            });
        });
}

// Inicialização segura
document.addEventListener("DOMContentLoaded", function () {
    const pais = document.getElementById("paisSelect");
    const estado = document.getElementById("estadoSelect");
    const cidade = document.getElementById("cidade");
    const cidadeAuto = document.getElementById("cidadeAuto");

    console.log("local-form.js: Inicializando...");

    if (pais) pais.addEventListener("change", paisChange);
    if (estado) estado.addEventListener("change", estadoChange);
    if (cidade) {
        cidade.addEventListener("change", cidadeChange);
        cidade.addEventListener("input", cidadeChange); // Detecta quando o usuário está digitando
    }
    if (cidadeAuto) {
        cidadeAuto.addEventListener("input", function () {
            buscarCidadesAuto(this.value);
        });
    }

    // Garante que campos "Outro" estejam escondidos por padrão
    const paisOutro = document.getElementById("paisOutro");
    const estadoOutro = document.getElementById("estadoOutro");
    const cidadeOutro = document.getElementById("cidadeOutro");
    
    // Sempre esconde os campos "Outro" por padrão, serão mostrados apenas quando necessário
    if (paisOutro) paisOutro.classList.add("hidden");
    if (estadoOutro) estadoOutro.classList.add("hidden");
    if (cidadeOutro) cidadeOutro.classList.add("hidden");

    // Verifica se precisa lidar com estado que não existe na lista atual
    const estadoPessoa = estado?.value;
    if (estadoPessoa && estadoPessoa.trim() !== "" && estado) {
        // Aguarda um pequeno delay para processar depois do carregamento
        setTimeout(function() {
            verificarEstadoExistente();
        }, 50);
    }
    
    // Remove duplicatas de "Outro" na inicialização
    setTimeout(() => {
        if (estado) {
            removerDuplicatasOutro(estado);
            debugDuplicatasOutro();
        }
    }, 200);

    // Verifica se os valores atuais precisam mostrar campos "Outro"
    if (pais && pais.value === "Outro") {
        console.log("País é 'Outro', aplicando paisChange");
        paisChange({ target: pais });
    }
    if (estado && estado.value === "Outro") {
        console.log("Estado é 'Outro', aplicando estadoChange");
        estadoChange({ target: estado });
    }
    if (cidade && cidade.value === "Outro") {
        console.log("Cidade é 'Outro', aplicando cidadeChange");
        cidadeChange({ target: cidade });
    }

    // SOMENTE executa change se não há seleção prévia (evita sobrescrever dados do servidor)
    if (pais && !pais.value) {
        paisChange({ target: pais });
    }
    if (estado && !estado.value) {
        estadoChange({ target: estado });
    }
    if (cidade && !cidade.value) {
        cidadeChange({ target: cidade });
    }

    // Se há estado selecionado, carrega as cidades correspondentes PRESERVANDO o valor atual
    if (estado && estado.value && estado.value !== "" && estado.value !== "Outro") {
        console.log("Carregando cidades para estado inicial:", estado.value);
        buscarCidadesPorEstado(estado.value, true); // true = preservar valor da cidade
    }

    console.log("local-form.js: Inicialização concluída");
    console.log("País:", pais?.value, "Estado:", estado?.value, "Cidade:", cidade?.value);
});

// Função para verificar se o estado atual existe na lista
function verificarEstadoExistente() {
    const estadoSelect = document.getElementById("estadoSelect");
    const estadoOutro = document.getElementById("estadoOutro");
    
    if (!estadoSelect) return;
    
    // Remove duplicatas de "Outro" se houver
    removerDuplicatasOutro(estadoSelect);
    
    const estadoAtual = estadoSelect.value;
    if (!estadoAtual || estadoAtual === "" || estadoAtual === "Outro") return;
    
    // Verifica se o estado existe nas opções disponíveis
    let estadoEncontrado = false;
    for (let option of estadoSelect.options) {
        if (option.value && option.value.toLowerCase() === estadoAtual.toLowerCase()) {
            estadoEncontrado = true;
            break;
        }
    }
    
    // Se não encontrou o estado na lista, muda para "Outro"
    if (!estadoEncontrado) {
        console.log("Estado", estadoAtual, "não encontrado na lista atual, mudando para 'Outro'");
        
        // Garante que existe uma opção "Outro"
        let temOutro = [...estadoSelect.options].some(opt => opt.value === "Outro");
        if (!temOutro) {
            const optionOutro = document.createElement("option");
            optionOutro.value = "Outro";
            optionOutro.textContent = "Outro";
            estadoSelect.appendChild(optionOutro);
        }
        
        // Seleciona "Outro" e mostra o campo
        estadoSelect.value = "Outro";
        if (estadoOutro) {
            estadoOutro.classList.remove("hidden");
            estadoOutro.value = estadoAtual;
        }
    }
}

// Função para remover duplicatas de "Outro" em um select
function removerDuplicatasOutro(selectElement) {
    if (!selectElement) return;
    
    const optionsOutro = [];
    const outrasOptions = [];
    
    // Separa opções "Outro" das outras
    Array.from(selectElement.options).forEach(option => {
        if (option.value === "Outro") {
            optionsOutro.push(option);
        } else {
            outrasOptions.push(option);
        }
    });
    
    // Se há mais de uma opção "Outro", remove as duplicatas
    if (optionsOutro.length > 1) {
        console.log("Removendo", optionsOutro.length - 1, "duplicatas de 'Outro'");
        
        // Remove todas as opções "Outro" exceto a primeira
        for (let i = 1; i < optionsOutro.length; i++) {
            optionsOutro[i].remove();
        }
    }
}

// Função de debug para detectar duplicações
function debugDuplicatasOutro() {
    const estadoSelect = document.getElementById("estadoSelect");
    if (!estadoSelect) return;
    
    const optionsOutro = Array.from(estadoSelect.options).filter(opt => opt.value === "Outro");
    
    console.log("DEBUG: Total de opções no select de estado:", estadoSelect.options.length);
    console.log("DEBUG: Opções 'Outro' encontradas:", optionsOutro.length);
    
    if (optionsOutro.length > 1) {
        console.warn("ATENÇÃO: Duplicação de 'Outro' detectada!");
        console.warn("Opções duplicadas:", optionsOutro);
    } else {
        console.log("✓ Nenhuma duplicação de 'Outro' detectada");
    }
    
    // Lista todas as opções para debug
    console.log("DEBUG: Todas as opções do select de estado:");
    Array.from(estadoSelect.options).forEach((opt, index) => {
        console.log(`  ${index}: value="${opt.value}", text="${opt.textContent}"`);
    });
}

// Função utilitária para limpar completamente um datalist
function limparDatalistCompleto(datalist) {
    if (!datalist) return;
    
    console.log("Limpando datalist. Opções antes:", datalist.children.length);
    
    // Método 1: innerHTML
    datalist.innerHTML = '';
    
    // Método 2: removeChild em loop (garantia extra)
    while (datalist.firstChild) {
        datalist.removeChild(datalist.firstChild);
    }
    
    // Método 3: Remoção via querySelectorAll (garantia extra)
    const opcoes = datalist.querySelectorAll('option');
    opcoes.forEach(opcao => opcao.remove());
    
    console.log("Datalist COMPLETAMENTE limpo. Opções finais:", datalist.children.length);
    
    if (datalist.children.length > 0) {
        console.error("ATENÇÃO: Datalist ainda tem opções após limpeza!", datalist.children.length);
    }
}
