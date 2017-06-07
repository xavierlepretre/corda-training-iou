package net.corda.training.contract

import net.corda.contracts.asset.Cash
import net.corda.contracts.asset.sumCash
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.by
import net.corda.core.crypto.CompositeKey
import net.corda.core.crypto.SecureHash
import net.corda.training.state.IOUState

/**
 * This is where you'll add the contract code which defines how the [IOUState] behaves. Looks at the unit tests in
 * [IOUContractTests] for instructions on how to complete the [IOUContract] class.
 */
class IOUContract : Contract {
    /**
     * Legal prose reference. This is just a dummy string for the time being.
     */
    override val legalContractReference: SecureHash = SecureHash.sha256("Prose contract.")

    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a range of commands which implement this interface.
     */
    interface Commands : CommandData {
        // Add commands here.
        // E.g
        // class DoSomething : TypeOnlyCommandData(), Commands
        class Issue : TypeOnlyCommandData(), Commands
    }

    /**
     * The contract code for the [IOUContract].
     * The constraints are self documenting so don't require any additional explanation.
     */
    override fun verify(tx: TransactionForContract) {
        // Add contract code here.
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Issue -> {
                requireThat {
                    "No inputs should be consumed when issuing an IOU." by tx.inputs.isEmpty()
                    "Only one output state should be created when issuing an IOU." by (tx.outputs.size == 1)
                    val output = tx.outputs.first() as IOUState
                    // With overloaded > operator
                    "A newly issued IOU must have a positive amount." by (output.amount > Amount(0, output.amount.token))
                    "A newly issued IOU must have a positive amount." by (output.amount.quantity > 0)
                    "The lender and borrower cannot be the same identity." by (output.lender.owningKey != output.borrower.owningKey)
                    "Both lender and borrower together only may sign IOU issue transaction." by (command.signers.toSet() == output.participants.toSet())
                }
            }
            else -> throw IllegalArgumentException("Invalid command $command.")
        }
    }
}
