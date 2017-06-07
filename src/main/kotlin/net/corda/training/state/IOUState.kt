package net.corda.training.state

import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.CompositeKey
import net.corda.core.crypto.Party
import net.corda.core.crypto.keys
import net.corda.training.contract.IOUContract
import java.security.PublicKey
import java.util.*

/**
 * This is where you'll add the definition of your state object. Look at the unit tests in [IOUStateTests] for
 * instructions on how to complete the [IOUState] class.
 *
 * Remove the "val data: String = "data" property before starting the [IOUState] tasks.
 */
data class IOUState(
        val amount: Amount<Currency>,
        val lender: Party,
        val borrower: Party,
        val paid: Amount<Currency> = Amount(0, amount.token),
        override val linearId: UniqueIdentifier = UniqueIdentifier())
    : LinearState {
    override val participants: List<CompositeKey> get() = listOf(
            lender.owningKey,
            borrower.owningKey)

    /**
     * A Contract code reference to the IOUContract. Make sure this is not part of the [IOUState] constructor.
     * **Don't change this definition!**
     */
    override val contract get() = IOUContract()

    override fun isRelevant(ourKeys: Set<PublicKey>): Boolean {
//        return lender.owningKey.containsAny(ourKeys) || borrower.owningKey.containsAny(ourKeys)
        return ourKeys.intersect(participants.keys).isNotEmpty()
    }

//     IOU(92c21ca4-1085-4242-8259-28f7bd365f58): Bob owes Alice 1.00 GBP and has paid 0.00 GBP so far.
    override fun toString(): String {
        val bob = borrower.name
        val alice = lender.name
        return "IOU($linearId): $bob owes $alice $amount and has paid $paid so far."
    }

    fun pay(some: Amount<Currency>): IOUState {
        return IOUState(amount.minus(some), lender, borrower, paid.plus(some), linearId)
    }

    fun withNewLender(newLender: Party): IOUState {
        return IOUState(amount, newLender, borrower, paid, linearId)
    }
}