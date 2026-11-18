package com.carexpress.bizdev.data.local

enum class Segment(val label: String) {
    PRIVATE_HOSPITAL("Private Hospital/Clinic"),
    CORPORATE_BANK("Corporate/Bank"),
    GOVERNMENT("Government Ministry"),
    PARASTATAL("Parastatal"),
    EMBASSY_NGO("Embassy/NGO");

    companion object {
        fun fromLabel(label: String) = entries.firstOrNull { it.label == label }
    }
}

enum class Stage(val code: Int, val label: String) {
    INITIAL_CONTACT(1, "1 - Initial Contact"),
    PROPOSAL_SENT(2, "2 - Pitch/Proposal Sent"),
    NEGOTIATION_PRAZ(3, "3 - Negotiation/PRAZ"),
    CLOSED_WON(4, "4 - Closed Won"),
    ON_HOLD_LOST(5, "5 - On Hold/Lost");

    val shortLabel: String get() = label.substringAfter(" - ")

    /** Spreadsheet rule: Proposals Sent counts stages 2, 3 and 4. */
    val isProposalOrBeyond: Boolean
        get() = this == PROPOSAL_SENT || this == NEGOTIATION_PRAZ || this == CLOSED_WON
}

enum class VisitType(val label: String) {
    IN_PERSON("In-Person"),
    INTRO_CALL("Intro Call"),
    WELLNESS_PITCH("Wellness Pitch"),
    TENDER_PMU("Tender/PMU"),
}
