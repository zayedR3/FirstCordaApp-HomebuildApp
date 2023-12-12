package com.r3.developers.csdetemplate.utxoexample.states;


import com.r3.developers.csdetemplate.utxoexample.contracts.WorkState_Contract;
import net.corda.v5.base.annotations.ConstructorForDeserialization;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.crypto.SecureHash;
import net.corda.v5.ledger.utxo.BelongsToContract;
import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;

import java.util.List;
import java.util.UUID;

@BelongsToContract(WorkState_Contract.class)
public class Request_WorkState implements ContractState {
    private  MemberX500Name customer; //(A)the Owner.
    private  int age; // age of the customer.
    private  double budget;
    private  MemberX500Name consultant; // (B) The chosen consultant.
    private  MemberX500Name municipality; //(C) One Municipality department.

    private  String requestStatus;
    private  String address; //The Address of the building.
    private  Integer houseHold; // The number of people will live in the house.
    private UUID id;
    public List<PublicKey> participants;


    @ConstructorForDeserialization
    public Request_WorkState(MemberX500Name customer, int age, double budget, MemberX500Name consultant, MemberX500Name municipality, String requestStatus, String address, Integer houseHold, UUID id, List<PublicKey> participants) {
        this.customer = customer;
        this.age = age;
        this.budget = budget;
        this.consultant = consultant;
        this.municipality = municipality;
        this.requestStatus = requestStatus;
        this.address = address;
        this.houseHold = houseHold;
        this.id = id;
        this.participants = participants;
    }

    public MemberX500Name getCustomer() {
        return customer;
    }

    public int getAge() {
        return age;
    }

    public double getBudget() {
        return budget;
    }

    public MemberX500Name getConsultant() {
        return consultant;
    }

    public MemberX500Name getMunicipality() {
        return municipality;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public String getAddress() {
        return address;
    }

    public Integer getHouseHold() {
        return houseHold;
    }

    public UUID getId() {
        return id;
    }

    @NotNull
    @Override
    public List<PublicKey> getParticipants() {
        return participants;
    }
    public Request_WorkState updateStatus(String requestStatus) {
        return new Request_WorkState(customer,age,budget,consultant,municipality,requestStatus,address,houseHold,id,participants);
    }
    public Request_WorkState updateNewConsultant(MemberX500Name new_consultant,List<PublicKey> newParticipants) {
        return new Request_WorkState(customer,age,budget,new_consultant,municipality,requestStatus,address,houseHold,id,newParticipants);
    }

}

