package com.r3.developers.csdetemplate.homebuild_Workflows

import net.corda.v5.base.types.MemberX500Name
import java.util.*

class ListRequestWorkStateResults {
    var customer: MemberX500Name? = null //(A)the Owner.
        private set
    var age = 0 // age of the customer.
        private set
    var budget = 0.0
        private set
    var consultant: MemberX500Name? = null // (B) The chosen consultant.
        private set
    var municipality: MemberX500Name? = null //(C) One Municipality department.
        private set
    var requestStatus: String? = null
        private set
    var address: String? = null //The Address of the building.
        private set
    var houseHold: Int? = null // The number of people will live in the house.
        private set
    var id: UUID? = null
        private set

    constructor()
    constructor(
        customer: MemberX500Name?,
        age: Int,
        budget: Double,
        consultant: MemberX500Name?,
        municipality: MemberX500Name?,
        requestStatus: String?,
        address: String?,
        houseHold: Int?,
        id: UUID?
    ) {
        this.customer = customer
        this.age = age
        this.budget = budget
        this.consultant = consultant
        this.municipality = municipality
        this.requestStatus = requestStatus
        this.address = address
        this.houseHold = houseHold
        this.id = id
    }
}
