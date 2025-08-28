<script>
const input = document.getElementById('solicitanteInput');
const suggestions = document.getElementById('solicitanteSuggestions');
const hiddenId = document.getElementById('solicitanteId');
let debounceTimeout;
console.log('Autocomplete solicitante JS carregado');
input.addEventListener('input', function() {
  clearTimeout(debounceTimeout);
  const query = this.value.trim();
  // Sempre limpa o hidden ao digitar
  hiddenId.value = '';
  console.log('Input digitado:', query);
  if (query.length < 2) {
    suggestions.innerHTML = '';
    return;
  }
  debounceTimeout = setTimeout(() => {
    console.log('Fazendo fetch para autocomplete:', query);
    fetch(`/api/pessoas/autocomplete?q=${encodeURIComponent(query)}`)
      .then(res => res.json())
      .then(data => {
        console.log('Dados recebidos do backend:', data);
        suggestions.innerHTML = '';
        data.forEach(pessoa => {
          const item = document.createElement('button');
          item.type = 'button';
          item.className = 'list-group-item list-group-item-action';
          item.textContent = pessoa.nome + ' (' + pessoa.email + ')';
          item.onclick = () => {
            input.value = pessoa.nome + ' (' + pessoa.email + ')';
            hiddenId.value = pessoa.id;
            suggestions.innerHTML = '';
            console.log('Selecionado:', pessoa);
          };
          suggestions.appendChild(item);
        });
      });
  }, 300);
});
document.addEventListener('click', function(e) {
  if (!input.contains(e.target) && !suggestions.contains(e.target)) {
    suggestions.innerHTML = '';
  }
});
</script>
