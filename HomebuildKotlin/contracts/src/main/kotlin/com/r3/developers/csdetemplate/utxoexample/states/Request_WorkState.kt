package com.r3.developers.csdetemplate.utxoexample.states

import com.r3.developers.csdetemplate.utxoexample.contracts.WorkState_Contract
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.*

@BelongsToContract(WorkState_Contract::class)
data class Request_WorkState(
    val customer: MemberX500Name,
    val age: Int,
    val budget: Double,
    val consultant: MemberX500Name,
    val municipality: MemberX500Name,
    val requestStatus: String?,
    val address: String?,
    val houseHold: Int,
    val id: UUID,
    private val participants: List<PublicKey>
) : ContractState {
    override fun getParticipants(): List<PublicKey> {
        return participants
    }

    fun updateStatus(requestStatus: String?): Request_WorkState {
        return Request_WorkState(
            customer,
            age,
            budget,
            consultant,
            municipality,
            requestStatus,
            address,
            houseHold,
            id,
            participants
        );
    }

    fun updateNewConsultant(new_consultant: MemberX500Name, newParticipants: List<PublicKey>): Request_WorkState {
        return Request_WorkState(
            customer,
            age,
            budget,
            new_consultant,
            municipality,
            requestStatus,
            address,
            houseHold,
            id,
            newParticipants
        )
    }
}
