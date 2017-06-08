package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.TransactionType
import net.corda.core.crypto.Party
import net.corda.core.flows.FlowLogic
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.DUMMY_NOTARY
import net.corda.core.utilities.unwrap
import net.corda.flows.FinalityFlow
import net.corda.training.contract.IOUContract
import net.corda.training.state.IOUState

/**
 * This is the flow which handles issuance of new IOUs on the ledger.
 * Look at the unit tests in [IOUIssueFlowTests] for how to complete the [call] method of this class.
 */
class IOUIssueFlow(val state: IOUState, val otherParty: Party) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryNodes.single().notaryIdentity
        val txBuilder = TransactionType.General.Builder(notary)
        val command = Command(
                IOUContract.Commands.Issue(),
                state.participants)
        txBuilder.withItems(command, state)
        txBuilder.toWireTransaction().toLedgerTransaction(serviceHub).verify()
        txBuilder.signWith(serviceHub.legalIdentityKey)
        val partiallySigned = txBuilder.toSignedTransaction(false)
        val fullySigned = sendAndReceive<SignedTransaction>(otherParty, partiallySigned).unwrap { tx -> tx }
        if (fullySigned.id != partiallySigned.id) {
            throw Exception("Tx tampered with")
        }
        // Another way to check it has not been tampered with, because if it had the signature would no longer
        // be valid
        fullySigned.toLedgerTransaction(serviceHub).verify()
        val involvedParties = setOf(serviceHub.myInfo.legalIdentity, otherParty)
        subFlow(FinalityFlow(fullySigned, involvedParties))
        return fullySigned;
    }
}
