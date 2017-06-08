package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.crypto.Party
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.unwrap
import net.corda.flows.ResolveTransactionsFlow
import net.corda.training.state.IOUState

class IOUAcceptorFlow(val otherParty: Party) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTx: SignedTransaction = receive<SignedTransaction>(otherParty).unwrap { tx -> tx }

        // Unnecessary here because there is no input
        subFlow(ResolveTransactionsFlow(signedTx.tx, otherParty))

        signedTx.tx.toLedgerTransaction(serviceHub).verify()
        signedTx.verifySignatures(serviceHub.myInfo.legalIdentity.owningKey)
        // TODO Check that we are ok with this tx
        if ((signedTx.tx.outputs[0].data as IOUState).amount.quantity >= 1000) {
            throw FlowException("Too big number")
        }
        val mySig = signedTx.signWithECDSA(serviceHub.legalIdentityKey)
        send(otherParty, signedTx + mySig)
    }
}