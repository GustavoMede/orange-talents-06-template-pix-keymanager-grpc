package br.com.zupacademy.keymanager.repository

import br.com.zupacademy.keymanager.model.Chave
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChaveRepository : JpaRepository<Chave, String>{

    fun existsByChavePix(chavePix: String): Boolean
}