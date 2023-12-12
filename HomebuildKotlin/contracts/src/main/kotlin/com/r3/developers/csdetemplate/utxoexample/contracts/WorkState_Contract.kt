package com.r3.developers.csdetemplate.utxoexample.contracts

import com.r3.developers.csdetemplate.utxoexample.states.Request_WorkState
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class WorkState_Contract : Contract {
    class create : Command
    class Update : Command
    class Transfer : Command



    override fun verify(transaction: UtxoLedgerTransaction) {
        // Ensures that there is only one command in the transaction
        requireThat(transaction.commands.size == 1, "Require a single command.")
        val command = transaction.commands[0]
        val output = transaction.getOutputStates(Request_WorkState::class.java)[0] // Connection with the state.
        requireThat(output.getParticipants().size >= 2, "The output state should have at least two participants.")
        // Switches case based on the command
        if (command.javaClass == create::class.java) { // Rules applied only to transactions with the Issue Command.
            requireThat(
                transaction.outputContractStates.size == 1,
                "Only one output states should be created when issuing an IOU."
            )
            requireThat(output.age >= 20, "Sorry you are not in the legal age")
            requireThat(output.budget >= 500000, "Your budget less than the needed amount")
        }
    }

    private fun requireThat(asserted: Boolean, errorMessage: String) {
        if (!asserted) throw CordaRuntimeException("Failed requirement: $errorMessage")
    }
}
