package com.r3.developers.csdetemplate.homebuild.workflows;

public class CreateRequestWorkStateArgs {
    private double budget;
    private int age;
    private int houseHold;
    private String address;

    private String consultant;


    private String municipality;


    public CreateRequestWorkStateArgs() {
    }

    public CreateRequestWorkStateArgs(double budget, Integer age, Integer houseHold, String address, String consultant, String municipality) {
        this.budget = budget;
        this.age = age;
        this.houseHold = houseHold;
        this.address = address;
        this.consultant = consultant;
        this.municipality = municipality;
    }

    public double getBudget() {
        return budget;
    }


    public int getAge() {
        return age;
    }

    public int getHouseHold() {
        return houseHold;
    }

    public String getAddress() {
        return address;
    }

    public String getConsultant() {
        return consultant;
    }

    public String getMunicipality() {
        return municipality;
    }
}