package com.carexpress.bizdev.data.seed

import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Segment.CORPORATE_BANK
import com.carexpress.bizdev.data.local.Segment.EMBASSY_NGO
import com.carexpress.bizdev.data.local.Segment.GOVERNMENT
import com.carexpress.bizdev.data.local.Segment.PARASTATAL
import com.carexpress.bizdev.data.local.Segment.PRIVATE_HOSPITAL
import com.carexpress.bizdev.data.local.Stage
import com.carexpress.bizdev.data.local.Stage.CLOSED_WON
import com.carexpress.bizdev.data.local.Stage.INITIAL_CONTACT
import com.carexpress.bizdev.data.local.Stage.NEGOTIATION_PRAZ
import com.carexpress.bizdev.data.local.Stage.ON_HOLD_LOST
import com.carexpress.bizdev.data.local.Stage.PROPOSAL_SENT
import com.carexpress.bizdev.data.local.VisitEntity
import com.carexpress.bizdev.data.local.VisitType
import com.carexpress.bizdev.data.local.VisitType.INTRO_CALL
import com.carexpress.bizdev.data.local.VisitType.IN_PERSON
import com.carexpress.bizdev.data.local.VisitType.TENDER_PMU
import com.carexpress.bizdev.data.local.VisitType.WELLNESS_PITCH
import java.time.LocalDateTime
import java.time.LocalTime

/** Harare Avenues / CBD institutional target directory (autocomplete + segment auto-select, unlimited custom additions). */
object TargetDirectory {
    val organisations: Map<String, Segment> = linkedMapOf(
        // Private Hospital / Clinic (Harare Avenues Medical Precinct)
        "The Avenues Clinic" to PRIVATE_HOSPITAL,
        "West End Clinic & Hospital" to PRIVATE_HOSPITAL,
        "Baines Intercare Medical Centre" to PRIVATE_HOSPITAL,
        "Corporate 24 Hospital" to PRIVATE_HOSPITAL,
        "St. Clements Clinic" to PRIVATE_HOSPITAL,
        "Parirenyatwa Group of Hospitals" to PRIVATE_HOSPITAL,
        "Trauma Centre Borrowdale / Avenues" to PRIVATE_HOSPITAL,
        "St. Anne's Hospital" to PRIVATE_HOSPITAL,
        "Borrowdale Medical Centre" to PRIVATE_HOSPITAL,
        "Montagu Clinic" to PRIVATE_HOSPITAL,
        // Corporate / Bank (Harare CBD & Samora Machel Ave)
        "Reserve Bank of Zimbabwe (RBZ)" to CORPORATE_BANK,
        "CBZ Holdings" to CORPORATE_BANK,
        "Stanbic Bank Zimbabwe" to CORPORATE_BANK,
        "Nedbank Zimbabwe" to CORPORATE_BANK,
        "Metbank Limited" to CORPORATE_BANK,
        "Delta Corporation" to CORPORATE_BANK,
        "Old Mutual Zimbabwe" to CORPORATE_BANK,
        "Econet Wireless Zimbabwe" to CORPORATE_BANK,
        "First Capital Bank Zimbabwe" to CORPORATE_BANK,
        "FBC Holdings" to CORPORATE_BANK,
        "NMB Bank Limited" to CORPORATE_BANK,
        "Innscor Africa Limited" to CORPORATE_BANK,
        // Government Ministries
        "Ministry of Health and Child Care (MoHCC)" to GOVERNMENT,
        "Ministry of Finance" to GOVERNMENT,
        "Ministry of Transport" to GOVERNMENT,
        "Ministry of Public Service, Labour and Social Welfare" to GOVERNMENT,
        "Ministry of Higher and Tertiary Education" to GOVERNMENT,
        // Parastatals
        "NSSA" to PARASTATAL,
        "ZESA Holdings" to PARASTATAL,
        "TelOne Zimbabwe" to PARASTATAL,
        "ZIMRA" to PARASTATAL,
        "NetOne Cellular" to PARASTATAL,
        "NOIC (National Oil Infrastructure Co.)" to PARASTATAL,
        "ZINARA" to PARASTATAL,
        // Embassies / NGOs (Avenues & Belgravia Diplomatic Precinct)
        "United Nations Zimbabwe Agencies" to EMBASSY_NGO,
        "US Embassy Harare" to EMBASSY_NGO,
        "British Embassy Harare" to EMBASSY_NGO,
        "European Union Delegation to Zimbabwe" to EMBASSY_NGO,
        "Population Solutions for Health (PSH)" to EMBASSY_NGO,
    )

    val roleChips = listOf(
        "Head of HR", "Wellness Manager", "Medical Superintendent",
        "Chief Pharmacist", "PMU Director", "Permanent Secretary",
    )
}

object SeedData {
    private data class Row(
        val org: String, val person: String, val role: String, val type: VisitType, val stage: Stage,
        val lives: Int, val value: Double, val score: Int, val notes: String, val next: String,
        val daysAgo: Long, val hour: Int, val followUpInDays: Long?,
    )

    private val rows = listOf(
        Row("The Avenues Clinic", "Dr. T. Moyo", "Medical Superintendent", IN_PERSON, CLOSED_WON, 420, 6800.0, 5,
            "Signed after-hours emergency dispensing MOU. Wants weekly stock-out report.", "Onboard ward pharmacists to ordering portal", 18, 9, 3),
        Row("West End Clinic & Hospital", "Sr. R. Chikwanha", "Chief Pharmacist", IN_PERSON, CLOSED_WON, 260, 4200.0, 5,
            "MOU for overflow scripts and chronic refills confirmed.", "Deliver first consignment & SLA pack", 15, 11, 2),
        Row("Baines Intercare Medical Centre", "Dr. F. Ndlovu", "Medical Superintendent", WELLNESS_PITCH, NEGOTIATION_PRAZ, 180, 3100.0, 4,
            "Keen on 24h direct billing. Reviewing pricing schedule vs current supplier.", "Send revised tariff with 5% volume rebate", 9, 14, 2),
        Row("Corporate 24 Hospital", "Mr. K. Banda", "Chief Pharmacist", IN_PERSON, PROPOSAL_SENT, 300, 5200.0, 4,
            "Proposal sent for ICU consumables & ED top-up stock.", "Follow up on board approval", 7, 10, 5),
        Row("St. Clements Clinic", "Ms. P. Dube", "Chief Pharmacist", INTRO_CALL, INITIAL_CONTACT, 90, 1200.0, 3,
            "Small clinic; open to a trial for after-hours scripts.", "Book in-person demo", 4, 15, 4),
        Row("Montagu Clinic", "Dr. A. Rusike", "Medical Superintendent", IN_PERSON, CLOSED_WON, 210, 3500.0, 5,
            "Emergency theatre & ward back-up MOU signed for Avenues precinct.", "Activate 30-minute courier dispatch SLA", 16, 10, 4),
        Row("Reserve Bank of Zimbabwe (RBZ)", "Mrs. L. Mapfumo", "Head of HR", WELLNESS_PITCH, CLOSED_WON, 1200, 9500.0, 5,
            "Staff wellness & chronic medication delivery contract won.", "Schedule on-site wellness day", 21, 10, 7),
        Row("CBZ Holdings", "Mr. T. Gumbo", "Wellness Manager", WELLNESS_PITCH, CLOSED_WON, 850, 7200.0, 5,
            "Approved quarterly screening + script delivery to branches.", "Kick-off meeting with branch HR leads", 12, 9, 6),
        Row("Stanbic Bank Zimbabwe", "Ms. N. Sibanda", "Head of HR", IN_PERSON, NEGOTIATION_PRAZ, 640, 5600.0, 4,
            "Negotiating payment terms (30 vs 45 days).", "Finance sign-off on terms", 6, 13, 3),
        Row("Nedbank Zimbabwe", "Mr. G. Mutasa", "Wellness Manager", INTRO_CALL, PROPOSAL_SENT, 410, 3400.0, 3,
            "Interested in flu vaccination drive.", "Send vaccine drive costing", 5, 11, 7),
        Row("Metbank Limited", "Mrs. E. Chari", "Head of HR", INTRO_CALL, INITIAL_CONTACT, 150, 900.0, 2,
            "Early conversation, existing medical aid arrangement.", "Re-engage next quarter", 3, 16, 30),
        Row("Delta Corporation", "Dr. S. Mhlanga", "Wellness Manager", IN_PERSON, CLOSED_WON, 1800, 8800.0, 5,
            "Won chronic medication supply for Harare plants.", "Set up monthly reconciliation", 25, 9, 10),
        Row("Econet Wireless Zimbabwe", "Mrs. R. Mutemeri", "Wellness Manager", WELLNESS_PITCH, PROPOSAL_SENT, 950, 6400.0, 4,
            "Pitched executive & call-centre chronic refill desk delivery.", "Present SLA to wellness committee", 6, 14, 4),
        Row("Ministry of Health and Child Care (MoHCC)", "Mr. B. Zvobgo", "PMU Director", TENDER_PMU, NEGOTIATION_PRAZ, 2000, 4500.0, 4,
            "PRAZ registration complete; bid submitted for emergency stock.", "Track tender evaluation", 11, 10, 14),
        Row("Ministry of Finance", "Mrs. J. Nyathi", "Permanent Secretary", IN_PERSON, PROPOSAL_SENT, 700, 2800.0, 3,
            "Staff clinic top-up proposal under review.", "Courtesy follow-up call", 8, 14, 7),
        Row("Ministry of Transport", "Mr. C. Marufu", "PMU Director", TENDER_PMU, ON_HOLD_LOST, 500, 600.0, 1,
            "Budget frozen for the current fiscal year.", "Revisit after budget statement", 30, 11, 60),
        Row("NSSA", "Ms. V. Mlambo", "PMU Director", TENDER_PMU, CLOSED_WON, 380, 3900.0, 4,
            "PRAZ win: workers' compensation script fulfilment.", "Confirm delivery schedule", 14, 9, 3),
        Row("ZESA Holdings", "Eng. W. Musona", "Head of HR", WELLNESS_PITCH, NEGOTIATION_PRAZ, 1500, 6100.0, 4,
            "Shortlisted; PRAZ supplier number submitted.", "Prepare clarification responses", 10, 12, 5),
        Row("TelOne Zimbabwe", "Mr. A. Kanengoni", "Wellness Manager", INTRO_CALL, PROPOSAL_SENT, 450, 2500.0, 3,
            "Proposal for employee chronic programme sent.", "Follow-up with procurement", 2, 10, 7),
        Row("ZIMRA", "Mrs. H. Chidzikwe", "Head of HR", IN_PERSON, INITIAL_CONTACT, 900, 1800.0, 2,
            "Intro meeting; existing contracted provider.", "Send capability statement", 1, 9, 14),
        Row("United Nations Zimbabwe Agencies", "Ms. A. Okafor", "Wellness Manager", IN_PERSON, PROPOSAL_SENT, 320, 4100.0, 4,
            "Requires UN vendor registration; proposal submitted.", "Complete UNGM vendor registration", 13, 15, 10),
        Row("Population Solutions for Health (PSH)", "Dr. M. Chitando", "Chief Pharmacist", IN_PERSON, CLOSED_WON, 290, 3800.0, 5,
            "Signed regional clinic buffer stock & staff wellness agreement.", "Dispatch initial order", 19, 11, 5),
    )

    fun visits(now: LocalDateTime = LocalDateTime.now()): List<VisitEntity> = rows.map { r ->
        val ts = now.toLocalDate().minusDays(r.daysAgo).atTime(LocalTime.of(r.hour, (r.daysAgo * 7 % 60).toInt()))
        VisitEntity(
            organisation = r.org,
            segment = TargetDirectory.organisations.getValue(r.org),
            keyPerson = r.person,
            role = r.role,
            visitType = r.type,
            stage = r.stage,
            coveredLives = r.lives,
            monthlyValue = r.value,
            leadScore = r.score,
            notes = r.notes,
            nextAction = r.next,
            followUpDate = r.followUpInDays?.let { now.toLocalDate().plusDays(it) },
            visitTimestamp = ts,
            createdAt = ts,
            updatedAt = ts,
        )
    }
}
