package com.r3.developers.csdetemplate.utxoexample.contracts;

import com.r3.developers.csdetemplate.utxoexample.states.Request_WorkState;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class WorkState_Contract implements Contract {

    public static class create implements Command {
    }
    public static class Update implements Command { }
    public static class Transfer implements Command { }



    @Override
    public void verify(@NotNull UtxoLedgerTransaction transaction) {
        // Ensures that there is only one command in the transaction
        requireThat(transaction.getCommands().size() == 1, "Require a single command.");
        Command command = transaction.getCommands().get(0);
        Request_WorkState output = transaction.getOutputStates(Request_WorkState.class).get(0);// Connection with the state.
        requireThat(output.getParticipants().size() >= 2, "The output state should have at least two participants.");
        // Switches case based on the command
        if (command.getClass() == WorkState_Contract.create.class) {// Rules applied only to transactions with the Issue Command.
            requireThat(transaction.getOutputContractStates().size() == 1, "Only one output states should be created when issuing an IOU.");
            requireThat(output.getAge() >=20,"Sorry you are not in the legal age");
            requireThat(output.getBudget() >=500000,"Your budget less than the needed amount");

        }

    }

    private void requireThat(boolean asserted, String errorMessage) {

        if (!asserted)


            throw new CordaRuntimeException("Failed requirement: " + errorMessage);
    }

}