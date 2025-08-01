package com.agendademais.services;

import com.agendademais.entities.Local;
import com.agendademais.repositories.LocalRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class LocalService {
    private final LocalRepository localRepository;
    
    public LocalService(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }
    
    public List<Local> listarPorTipoAndPai(int tipo, Long idPai) {
        if (idPai == null) return Collections.emptyList();
        return localRepository.findByTipoLocalAndLocalPaiId(tipo, idPai);
    }   
    
    /**
     * Busca ou cria um Local por tipo, nome e opcionalmente localPai.
     * tipoLocal: 1=País, 2=Estado, 3=Cidade
     */
    public Local buscarOuCriar(int tipoLocal, String nomeLocal, Local localPai) {
        // Buscar por tipo, nome e pai (atenção: pai pode ser null se tipo=1)
        Local local = localRepository
            .findByTipoLocalAndNomeLocalAndLocalPai(tipoLocal, nomeLocal.trim(), localPai)
            .orElse(null);

        if (local == null) {
            Local novo = new Local();
            novo.setTipoLocal(tipoLocal);
            novo.setNomeLocal(nomeLocal.trim());
            novo.setLocalPai(localPai);
            local = localRepository.save(novo);
        }
        return local;
    }    

    public List<Local> listarPorTipo(int tipo) { return localRepository.findByTipoLocal(tipo); }

    public List<Local> listarEstadosPorPaisNome(String pais) {
        return localRepository.findByTipoLocalAndLocalPaiNomeLocal(2, pais);
    }
    public List<Local> listarCidadesPorEstadoNome(String estado) {
        return localRepository.findByTipoLocalAndLocalPaiNomeLocal(3, estado);
    }


    public List<Local> listarEstadosPorPais(String nomePais) {
        return localRepository.findByTipoLocalAndLocalPaiNomeLocal(2, nomePais);
    }
    public List<Local> listarCidadesPorEstado(String nomeEstado) {
        return localRepository.findByTipoLocalAndLocalPaiNomeLocal(3, nomeEstado);
    }
    
    
    public List<Local> buscarPorNomeParcial(int tipo, String termo) {
        return localRepository.findByTipoLocalAndNomeLocalContainingIgnoreCase(tipo, termo);
    }
    public Local salvar(Local local) { return localRepository.save(local); }
    
    
    public List<Local> listarPorTipo(Integer tipoLocal) {
        return localRepository.findByTipoLocal(tipoLocal);
    }
	
    public Optional<Local> buscarPorId(Long id) {
        return localRepository.findById(id);
    }
    

    /**
     * Busca Local por nome (ignorando acentos e caixa) e tipo.
     */
    public Optional<Local> buscarPorNomeETipo(String nomeLocal, int tipoLocal) {
        if (nomeLocal == null || nomeLocal.isBlank()) return Optional.empty();
        String nomeBuscado = normalizar(nomeLocal);
        List<Local> locais = localRepository.findByTipoLocal(tipoLocal);
        return locais.stream()
            .filter(l -> normalizar(l.getNomeLocal()).equals(nomeBuscado))
            .findFirst();
    }

    /**
     * Remove acentos e normaliza string para comparação.
     */
    private String normalizar(String valor) {
        if (valor == null) return "";
        String semAcento = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return semAcento.toUpperCase();
    }
    

    public List<Local> listarPorTipoAndPaiNome(int tipo, String nomePai) {
        Optional<Local> pai = localRepository.findByNomeLocalAndTipoLocal(nomePai, 1);
        if (pai.isPresent()) {
            return localRepository.findByTipoLocalAndLocalPai(2, pai.get());
        }
        return Collections.emptyList();
    }
    
    public List<Local> autocompletePorNome(int tipoLocal, String termo) {
        return localRepository.findByTipoLocalAndNomeLocalContainingIgnoreCase(tipoLocal, termo);
    }
    
    public List<Local> autocompleteCidades(Local estado, String prefixo) {
        List<Local> cidades = localRepository.findByTipoLocalAndLocalPai(3, estado);
        if (prefixo == null || prefixo.isBlank()) return cidades;
        return cidades.stream()
            .filter(c -> c.getNomeLocal().toLowerCase().startsWith(prefixo.toLowerCase()))
            .toList();
    }


}
