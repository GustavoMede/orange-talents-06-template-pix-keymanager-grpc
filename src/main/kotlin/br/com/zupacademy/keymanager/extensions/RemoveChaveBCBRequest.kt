package br.com.zupacademy.keymanager.extensions

import br.com.zupacademy.keymanager.RemoveRequest
import br.com.zupacademy.keymanager.clients.dto.BCBRemocaoRequest

fun removeChaveBCBRequest(
    request: RemoveRequest
): BCBRemocaoRequest {
    return BCBRemocaoRequest(request.key, request.participant)
}
