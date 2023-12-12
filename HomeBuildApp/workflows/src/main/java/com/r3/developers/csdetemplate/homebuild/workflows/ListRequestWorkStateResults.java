package com.r3.developers.csdetemplate.homebuild.workflows;

import net.corda.v5.base.types.MemberX500Name;

import java.util.UUID;

public class ListRequestWorkStateResults {

    private MemberX500Name customer; //(A)the Owner.
    private  int age; // age of the customer.
    private  double budget;
    private  MemberX500Name consultant; // (B) The chosen consultant.
    private  MemberX500Name municipality; //(C) One Municipality department.

    private  String requestStatus;
    private  String address; //The Address of the building.
    private  Integer houseHold; // The number of people will live in the house.
    private UUID id;


    public ListRequestWorkStateResults() {
    }

    public ListRequestWorkStateResults(MemberX500Name customer, int age, double budget, MemberX500Name consultant, MemberX500Name municipality, String requestStatus, String address, Integer houseHold, UUID id) {
        this.customer = customer;
        this.age = age;
        this.budget = budget;
        this.consultant = consultant;
        this.municipality = municipality;
        this.requestStatus = requestStatus;
        this.address = address;
        this.houseHold = houseHold;
        this.id = id;
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
}
