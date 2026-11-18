package com.carexpress.bizdev.domain

import com.carexpress.bizdev.data.local.Segment
import com.carexpress.bizdev.data.local.Stage
import com.carexpress.bizdev.data.local.VisitEntity
import com.carexpress.bizdev.data.local.VisitType
import com.carexpress.bizdev.data.seed.SeedData
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.zip.ZipInputStream

class KpiEngineTest {

    private fun v(seg: Segment, stage: Stage, lives: Int = 0, value: Double = 0.0, score: Int = 3) = VisitEntity(
        organisation = "Org", segment = seg, keyPerson = "", role = "", visitType = VisitType.IN_PERSON, stage = stage,
        coveredLives = lives, monthlyValue = value, leadScore = score, notes = "", nextAction = "", followUpDate = null,
        visitTimestamp = LocalDateTime.of(2026, 11, 18, 9, 0),
    )

    @Test fun emptyDataGivesOnlyManualListingScore() {
        val s = KpiEngine.snapshot(emptyList(), 5)
        // listings 5/6 * 10% = 8.33
        assertEquals(8.333, s.totalScore, 0.01)
        assertEquals(Tier.EARLY, s.tier)
    }

    @Test fun bannerCounts() {
        val list = listOf(
            v(Segment.CORPORATE_BANK, Stage.CLOSED_WON, 300, 1000.0, 5),
            v(Segment.CORPORATE_BANK, Stage.PROPOSAL_SENT, value = 500.0, score = 4),
            v(Segment.PARASTATAL, Stage.NEGOTIATION_PRAZ, value = 200.0),
            v(Segment.GOVERNMENT, Stage.ON_HOLD_LOST, value = 50.0),
            v(Segment.GOVERNMENT, Stage.INITIAL_CONTACT),
        )
        val b = KpiEngine.banner(list)
        assertEquals(5, b.totalVisits)
        assertEquals(2, b.hotWarmLeads)
        assertEquals(3, b.proposalsSent)
        assertEquals(1, b.contractsWon)
        assertEquals(1750.0, b.monthlyPipeline, 0.0) // every stage counts
    }

    @Test fun kpiFormulas() {
        val list = listOf(
            v(Segment.CORPORATE_BANK, Stage.CLOSED_WON, 300),
            v(Segment.CORPORATE_BANK, Stage.CLOSED_WON, 249), // below threshold
            v(Segment.PRIVATE_HOSPITAL, Stage.CLOSED_WON, 100),
            v(Segment.GOVERNMENT, Stage.NEGOTIATION_PRAZ),
            v(Segment.PARASTATAL, Stage.CLOSED_WON, 260),
            v(Segment.PARASTATAL, Stage.PROPOSAL_SENT),
        )
        val k = KpiEngine.kpis(list, 6).associateBy { it.id }
        assertEquals(6.0, k.getValue(KpiId.VISITS).actual, 0.0)
        assertEquals(2.0, k.getValue(KpiId.CORPORATE_CLOSED).actual, 0.0) // any segment, 250+ lives & won
        assertEquals(1.0, k.getValue(KpiId.HOSPITAL_MOUS).actual, 0.0)
        assertEquals(2.0, k.getValue(KpiId.GOVT_PRAZ).actual, 0.0)
        assertEquals(KpiStatus.EXCEEDED, k.getValue(KpiId.MEDICAL_AID_LISTINGS).status)
    }

    @Test fun cappingAndStatusAndTiers() {
        val r = KpiResult(KpiId.VISITS, "", "", 25.0, 50.0, 0.15)
        assertEquals(2.0, r.progress, 0.0)
        assertEquals(15.0, r.weightedScore, 0.0)
        assertEquals(KpiStatus.ON_TRACK, KpiEngine.statusFor(0.70))
        assertEquals(KpiStatus.IN_PROGRESS, KpiEngine.statusFor(0.40))
        assertEquals(KpiStatus.NEEDS_FOCUS, KpiEngine.statusFor(0.399))
        assertEquals(Tier.EARLY, KpiEngine.tierFor(29.9))
        assertEquals(Tier.MODERATE, KpiEngine.tierFor(30.0))
        assertEquals(Tier.HIGH, KpiEngine.tierFor(84.9))
        assertEquals(Tier.MASTER, KpiEngine.tierFor(100.0))
    }

    @Test fun segmentConversion() {
        val list = listOf(v(Segment.EMBASSY_NGO, Stage.CLOSED_WON, 10, 100.0), v(Segment.EMBASSY_NGO, Stage.INITIAL_CONTACT, 5))
        val s = KpiEngine.segments(list).first { it.segment == Segment.EMBASSY_NGO }
        assertEquals(0.5, s.conversionRate, 0.0)
        assertEquals(15, s.coveredLives)
    }

    @Test fun xlsxHasBothSheets() {
        val visits = SeedData.visits()
        val out = ByteArrayOutputStream()
        XlsxExporter.write(out, visits, KpiEngine.snapshot(visits, 5))
        val names = mutableListOf<String>()
        ZipInputStream(out.toByteArray().inputStream()).use { z -> generateSequence { z.nextEntry }.forEach { names += it.name } }
        assert("xl/worksheets/sheet1.xml" in names && "xl/worksheets/sheet2.xml" in names)
    }
}
