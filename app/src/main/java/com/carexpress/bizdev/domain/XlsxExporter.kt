package com.carexpress.bizdev.domain

import com.carexpress.bizdev.data.local.VisitEntity
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Dependency-free, standards-compliant Office Open XML (.xlsx) writer.
 * Produces a two-sheet workbook mirroring the source tracker: "Visit Report" and "KPI Tracker".
 * (Avoids Apache POI, which is ~10 MB and needs desugared javax.xml on Android.)
 */
object XlsxExporter {

    private sealed interface Cell
    private data class Txt(val v: String, val style: Int = 0) : Cell
    private data class Num(val v: Double, val style: Int = 0) : Cell

    // Style indices defined in styles.xml below
    private const val S_HEADER = 1
    private const val S_CURRENCY = 2
    private const val S_PERCENT = 3
    private const val S_TITLE = 4

    private val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun write(out: OutputStream, visits: List<VisitEntity>, snapshot: KpiSnapshot) {
        val visitRows = buildList {
            add(listOf("Visit Date & Time", "Target Organisation", "Segment", "Key Person Met", "Role", "Visit Type",
                "Outcome / Stage", "Est. Covered Lives", "Est. Monthly Value ($)", "Lead Score", "Notes",
                "Next Action", "Follow-Up Date").map { Txt(it, S_HEADER) })
            visits.forEach { v ->
                add(listOf(
                    Txt(v.visitTimestamp.format(dtf)), Txt(v.organisation), Txt(v.segment.label), Txt(v.keyPerson),
                    Txt(v.role), Txt(v.visitType.label), Txt(v.stage.label), Num(v.coveredLives.toDouble()),
                    Num(v.monthlyValue, S_CURRENCY), Num(v.leadScore.toDouble()), Txt(v.notes), Txt(v.nextAction),
                    Txt(v.followUpDate?.format(df) ?: ""),
                ))
            }
        }

        val kpiRows = buildList<List<Cell>> {
            add(listOf(Txt("Total Weighted Execution Score", S_TITLE), Num(snapshot.totalScore / 100, S_PERCENT), Txt(snapshot.tier.label)))
            add(emptyList())
            add(listOf("KPI", "Target", "Actual", "Progress %", "Weight", "Weighted Score", "Status").map { Txt(it, S_HEADER) })
            snapshot.kpis.forEach { k ->
                val money = if (k.isCurrency) S_CURRENCY else 0
                add(listOf(Txt(k.title), Num(k.target, money), Num(k.actual, money), Num(k.progress, S_PERCENT),
                    Num(k.weight, S_PERCENT), Num(k.weightedScore / 100, S_PERCENT), Txt(k.status.label)))
            }
            add(emptyList())
            add(listOf("Segment", "Visits Logged", "Proposals Sent", "Accounts Won", "Conversion Rate", "Total Covered Lives", "Pipeline Value ($)")
                .map { Txt(it, S_HEADER) })
            snapshot.segments.forEach { s ->
                add(listOf(Txt(s.segment.label), Num(s.visits.toDouble()), Num(s.proposals.toDouble()), Num(s.won.toDouble()),
                    Num(s.conversionRate, S_PERCENT), Num(s.coveredLives.toDouble()), Num(s.pipeline, S_CURRENCY)))
            }
        }

        ZipOutputStream(out).use { zip ->
            fun put(name: String, body: String) {
                zip.putNextEntry(ZipEntry(name)); zip.write(body.toByteArray(Charsets.UTF_8)); zip.closeEntry()
            }
            put("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>""")
            put("_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""")
            put("xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Visit Report" sheetId="1" r:id="rId1"/><sheet name="KPI Tracker" sheetId="2" r:id="rId2"/></sheets>
</workbook>""")
            put("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""")
            put("xl/styles.xml", STYLES)
            put("xl/worksheets/sheet1.xml", sheet(visitRows, listOf(18, 34, 22, 22, 22, 15, 24, 14, 16, 10, 50, 36, 14)))
            put("xl/worksheets/sheet2.xml", sheet(kpiRows, listOf(46, 14, 14, 14, 12, 18, 30)))
        }
    }

    private fun sheet(rows: List<List<Cell>>, widths: List<Int>): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>")
        append("<cols>")
        widths.forEachIndexed { i, w -> append("<col min=\"${i + 1}\" max=\"${i + 1}\" width=\"$w\" customWidth=\"1\"/>") }
        append("</cols><sheetData>")
        rows.forEachIndexed { r, cells ->
            append("<row r=\"${r + 1}\">")
            cells.forEachIndexed { c, cell ->
                val ref = colName(c) + (r + 1)
                when (cell) {
                    is Txt -> append("<c r=\"$ref\" t=\"inlineStr\" s=\"${cell.style}\"><is><t xml:space=\"preserve\">${esc(cell.v)}</t></is></c>")
                    is Num -> append("<c r=\"$ref\" s=\"${cell.style}\"><v>${cell.v}</v></c>")
                }
            }
            append("</row>")
        }
        append("</sheetData></worksheet>")
    }

    private fun colName(i: Int): String {
        var n = i + 1; val sb = StringBuilder()
        while (n > 0) { val m = (n - 1) % 26; sb.insert(0, ('A' + m)); n = (n - 1) / 26 }
        return sb.toString()
    }

    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
        .filter { it == '\t' || it == '\n' || it == '\r' || it >= ' ' }

    private const val STYLES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="1"><numFmt numFmtId="164" formatCode="&quot;$&quot;#,##0.00"/></numFmts>
<fonts count="3"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font><font><b/><sz val="12"/><color rgb="FF005046"/><name val="Calibri"/></font></fonts>
<fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FF005046"/><bgColor indexed="64"/></patternFill></fill></fills>
<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="5">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
<xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
<xf numFmtId="10" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
<xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
</cellXfs>
</styleSheet>"""
}
