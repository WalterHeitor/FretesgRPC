package br.com.zup.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FreteGrpcServer: FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FreteGrpcServer::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        logger.info("Calculando frete para request: $request")

        val cep = request?.cep
        if (cep == null || cep.isBlank()){

        //    throw IllegalArgumentException("o cep de ser informado")
        //    throw StatusException("o cep de ser informado")       usadas para menssagens checadas
/*
            val e = StatusRuntimeException(Status.INVALID_ARGUMENT
                .withDescription("o cep deve ser informado"))  // boa pratica e favorecer runtime

 */
            val e = Status.INVALID_ARGUMENT
                .withDescription("o cep deve ser informado")
                .asException()
            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())){
            val e = Status.INVALID_ARGUMENT
                        .withDescription("o cep é inválido")
                        .augmentDescription("o formato válido e 99999-999")
                        .asException()
            responseObserver?.onError(e)
        }

        // SIMULAR uma verificação de segurança
        if(cep.endsWith("333")){
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("usuario nao podee acessar este recurso")
                .addDetails(Any.pack(ErrorDetails.newBuilder()
                    .setCode(401)
                    .setMessage("token expirado")
                    .build()))
                .build()
            val e = StatusProto.toStatusRuntimeException(statusProto)     //do protobuf
            responseObserver?.onError(e)
        }

        var valor = 0.0
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.0)
            if(valor > 100.0){
                throw IllegalArgumentException("Erro inesperado ao executar a logica do negocio")
            }
        }catch (e: Exception){
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException())
        }

        val response = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(valor)
            .build()
        logger.info("Calculado frete para response: $response")
        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}