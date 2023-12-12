package com.r3.developers.csdetemplate.homebuild_Workflows
class CreateRequestWorkStateArgs {
    var budget = 0.0
        private set
    var age = 0
        private set
    var houseHold = 0
        private set
    var address: String? = null
        private set
    var consultant: String? = null
        private set
    var municipality: String? = null
        private set

    constructor()
    constructor(
        budget: Double,
        age: Int,
        houseHold: Int,
        address: String?,
        consultant: String?,
        municipality: String?
    ) {
        this.budget = budget
        this.age = age
        this.houseHold = houseHold
        this.address = address
        this.consultant = consultant
        this.municipality = municipality
    }
}