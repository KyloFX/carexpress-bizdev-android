package com.carexpress.bizdev.domain

import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Stage
import com.carexpress.bizdev.data.local.VisitEntity

data class BannerMetrics(
    val totalVisits: Int,
    val hotWarmLeads: Int,
    val proposalsSent: Int,
    val contractsWon: Int,
    val monthlyPipeline: Double,
)

enum class KpiStatus(val label: String) { EXCEEDED("Exceeded"), ON_TRACK("On Track"), IN_PROGRESS("In Progress"), NEEDS_FOCUS("Needs Focus") }

enum class KpiId { VISITS, CORPORATE_CLOSED, HOSPITAL_MOUS, GOVT_PRAZ, MEDICAL_AID_LISTINGS, PIPELINE_VALUE }

data class KpiResult(
    val id: KpiId,
    val title: String,
    val description: String,
    val target: Double,
    val actual: Double,
    /** Weight as a fraction (0.15 = 15%). */
    val weight: Double,
    val isCurrency: Boolean = false,
    val isManual: Boolean = false,
) {
    /** Uncapped progress, e.g. 1.32 = 132%. */
    val progress: Double get() = if (target <= 0) 0.0 else actual / target
    /** Capped progress used for bars and weighting. */
    val cappedProgress: Double get() = progress.coerceIn(0.0, 1.0)
    /** Contribution to the total execution score in percentage points (0..weight*100). */
    val weightedScore: Double get() = cappedProgress * weight * 100.0
    val status: KpiStatus get() = KpiEngine.statusFor(progress)
}

enum class Tier(val number: Int, val title: String, val range: IntRange) {
    EARLY(1, "Early Stage / Pipeline Building", 0..29),
    MODERATE(2, "Moderate / Strong Progress", 30..59),
    HIGH(3, "High Momentum / Scale Ready", 60..84),
    MASTER(4, "Master Execution / Target Exceeded", 85..100);

    val label get() = "Tier $number: $title"
}

data class SegmentBreakdown(
    val segment: Segment,
    val visits: Int,
    val proposals: Int,
    val won: Int,
    val coveredLives: Int,
    val pipeline: Double,
) {
    val conversionRate: Double get() = if (visits == 0) 0.0 else won.toDouble() / visits
}

data class KpiSnapshot(
    val banner: BannerMetrics,
    val kpis: List<KpiResult>,
    val totalScore: Double,
    val tier: Tier,
    val segments: List<SegmentBreakdown>,
)

/** Pure replica of the spreadsheet's KPI Tracker formulas. */
object KpiEngine {
    const val TARGET_VISITS = 25
    const val TARGET_CORPORATE = 5
    const val TARGET_HOSPITAL_MOUS = 4
    const val TARGET_GOVT_PRAZ = 4
    const val TARGET_LISTINGS = 6
    const val TARGET_PIPELINE = 50_000.0
    const val HIGH_IMPACT_LIVES = 250

    fun statusFor(progress: Double): KpiStatus = when {
        progress >= 1.0 -> KpiStatus.EXCEEDED
        progress >= 0.70 -> KpiStatus.ON_TRACK
        progress >= 0.40 -> KpiStatus.IN_PROGRESS
        else -> KpiStatus.NEEDS_FOCUS
    }

    fun tierFor(scorePercent: Double): Tier {
        val s = scorePercent.toInt().coerceIn(0, 100)
        return Tier.entries.first { s in it.range }
    }

    /** Pipeline counts every stage's estimated monthly value. */
    fun pipeline(visits: List<VisitEntity>): Double = visits.sumOf { it.monthlyValue }

    fun banner(visits: List<VisitEntity>) = BannerMetrics(
        totalVisits = visits.size,
        hotWarmLeads = visits.count { it.leadScore >= 4 },
        proposalsSent = visits.count { it.stage.isProposalOrBeyond },
        contractsWon = visits.count { it.stage == Stage.CLOSED_WON },
        monthlyPipeline = pipeline(visits),
    )

    fun kpis(visits: List<VisitEntity>, medicalAidListings: Int): List<KpiResult> {
        val won = visits.filter { it.stage == Stage.CLOSED_WON }
        return listOf(
            KpiResult(KpiId.VISITS, "Total Institutional Visits", "All logged field visits",
                TARGET_VISITS.toDouble(), visits.size.toDouble(), 0.15),
            KpiResult(KpiId.CORPORATE_CLOSED, "High-Impact Corporate Accounts Closed",
                "Closed Won with $HIGH_IMPACT_LIVES+ covered lives",
                TARGET_CORPORATE.toDouble(), won.count { it.coveredLives >= HIGH_IMPACT_LIVES }.toDouble(), 0.25),
            KpiResult(KpiId.HOSPITAL_MOUS, "Private Hospital & Clinic Emergency MOUs",
                "Private Hospital/Clinic · Closed Won",
                TARGET_HOSPITAL_MOUS.toDouble(), won.count { it.segment == Segment.PRIVATE_HOSPITAL }.toDouble(), 0.20),
            KpiResult(KpiId.GOVT_PRAZ, "Govt / Parastatal PRAZ Registrations & Wins",
                "Ministry or Parastatal · Negotiation/PRAZ or Closed Won",
                TARGET_GOVT_PRAZ.toDouble(),
                visits.count {
                    (it.segment == Segment.GOVERNMENT || it.segment == Segment.PARASTATAL) &&
                        (it.stage == Stage.NEGOTIATION_PRAZ || it.stage == Stage.CLOSED_WON)
                }.toDouble(), 0.15),
            KpiResult(KpiId.MEDICAL_AID_LISTINGS, "Medical Aid Direct Billing Network Listings",
                "Manual milestone counter",
                TARGET_LISTINGS.toDouble(), medicalAidListings.toDouble(), 0.10, isManual = true),
            KpiResult(KpiId.PIPELINE_VALUE, "Monthly Institutional Pipeline Value",
                "Sum of est. monthly value across all stages",
                TARGET_PIPELINE, pipeline(visits), 0.15, isCurrency = true),
        )
    }

    fun segments(visits: List<VisitEntity>): List<SegmentBreakdown> =
        listOf(Segment.CORPORATE_BANK, Segment.PRIVATE_HOSPITAL, Segment.GOVERNMENT, Segment.PARASTATAL, Segment.EMBASSY_NGO)
            .map { seg ->
                val v = visits.filter { it.segment == seg }
                SegmentBreakdown(
                    segment = seg,
                    visits = v.size,
                    proposals = v.count { it.stage.isProposalOrBeyond },
                    won = v.count { it.stage == Stage.CLOSED_WON },
                    coveredLives = v.sumOf { it.coveredLives },
                    pipeline = pipeline(v),
                )
            }

    fun snapshot(visits: List<VisitEntity>, medicalAidListings: Int): KpiSnapshot {
        val kpis = kpis(visits, medicalAidListings)
        val total = kpis.sumOf { it.weightedScore }.coerceIn(0.0, 100.0)
        return KpiSnapshot(banner(visits), kpis, total, tierFor(total), segments(visits))
    }
}
