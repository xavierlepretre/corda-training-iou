package net.corda.training.service

import net.corda.core.node.PluginServiceHub
import net.corda.training.flow.IOUAcceptorFlow
import net.corda.training.flow.IOUIssueFlow
import net.corda.training.flow.SignTransactionFlow

object IOUService {
    class Service(services: PluginServiceHub) {
        init {
            services.registerFlowInitiator(SignTransactionFlow.Initiator::class.java) {
                SignTransactionFlow.Responder(it)
            }

            services.registerFlowInitiator(IOUIssueFlow::class.java) {
                IOUAcceptorFlow(it)
            }
        }
    }
}