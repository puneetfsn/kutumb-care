package com.puneetfsn.kutumbcare.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.ChecklistItem
import com.puneetfsn.kutumbcare.data.DashboardSnapshot
import com.puneetfsn.kutumbcare.data.NomineeStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DossierPdfGenerator(private val context: Context) {

    fun generate(snapshot: DashboardSnapshot): File {
        val dir = File(context.cacheDir, "dossier").apply { mkdirs() }
        val file = File(dir, "KutumbCare_Family_Dossier.pdf")
        val document = PdfDocument()
        try {
            val pages = mutableListOf<PagePainter>()
            pages += PagePainter { canvas, pageNo, pageCount -> drawCover(canvas, snapshot, pageNo, pageCount) }
            AssetCategory.entries.forEach { category ->
                val group = snapshot.items.filter { it.categoryEnum == category }
                if (group.isNotEmpty()) {
                    pages += PagePainter { canvas, pageNo, pageCount ->
                        drawCategoryPage(canvas, snapshot, category, group, pageNo, pageCount)
                    }
                }
            }
            pages.forEachIndexed { index, painter ->
                val pageNumber = index + 1
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = document.startPage(info)
                painter.draw(page.canvas, pageNumber, pages.size)
                document.finishPage(page)
            }
            file.outputStream().use { document.writeTo(it) }
        } finally {
            document.close()
        }
        return file
    }

    private fun drawCover(canvas: Canvas, snapshot: DashboardSnapshot, pageNo: Int, pageCount: Int) {
        drawPageChrome(canvas, pageNo, pageCount)
        val prefs = snapshot.preferences
        val date = SimpleDateFormat("d MMMM yyyy", Locale("en", "IN")).format(Date())

        headerBand(canvas, "FAMILY EMERGENCY DOSSIER")

        var y = 150f
        y = drawText(
            canvas,
            prefs.displayFamilyName,
            MARGIN,
            y,
            titlePaint(28f, Color.parseColor("#134E4A")),
            CONTENT_WIDTH
        )
        y = drawText(
            canvas,
            "Prepared for peaceful handover · $date",
            MARGIN,
            y + 6f,
            bodyPaint(11f, MUTED),
            CONTENT_WIDTH
        )

        y += 18f
        val scoreBox = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 78f)
        fillRound(canvas, scoreBox, Color.parseColor("#F3EDE3"), 10f)
        canvas.drawText("${snapshot.readinessPercent}% ready", scoreBox.left + 18f, scoreBox.top + 36f, titlePaint(22f, FOREST))
        canvas.drawText(
            "${snapshot.completedCount} of ${snapshot.totalCount} records captured  ·  ${snapshot.missingNomineeItems.size} nominee alerts",
            scoreBox.left + 18f,
            scoreBox.top + 58f,
            bodyPaint(11f, MUTED)
        )
        y = scoreBox.bottom + 22f

        y = sectionTitle(canvas, "Emergency contact", y)
        val contactName = prefs.emergencyContactName.ifBlank { "Not recorded yet" }
        val contactPhone = prefs.emergencyContactPhone.ifBlank { "—" }
        y = drawText(canvas, "$contactName  ·  $contactPhone", MARGIN, y, bodyPaint(13f, INK), CONTENT_WIDTH)
        y += 16f

        y = sectionTitle(canvas, "How to use this binder", y)
        val howTo = listOf(
            "Print this dossier on A4 paper. Keep one sealed copy with a trusted family member and one in a bank locker.",
            "Each table lists where originals live, the institution, optional account or policy numbers, nominee status, and the person who can help.",
            "This file never contains net-banking passwords, UPI PINs, ATM PINs, OTPs or CVVs. Those must stay out of this dossier.",
            "After a death or emergency, start with identity papers, then bank nominees, then insurance claims (LIC / health), then EPF / UAN."
        )
        howTo.forEach { line ->
            y = drawText(canvas, "•  $line", MARGIN, y, bodyPaint(11f, INK), CONTENT_WIDTH)
            y += 8f
        }

        y += 10f
        y = sectionTitle(canvas, "Readiness by category", y)
        snapshot.categoryProgress.forEach { progress ->
            val label = "${progress.category.displayName}   ${progress.completed}/${progress.total}"
            canvas.drawText(label, MARGIN, y + 12f, bodyPaint(11f, INK))
            val barLeft = MARGIN + 220f
            val bar = RectF(barLeft, y + 2f, PAGE_WIDTH - MARGIN, y + 14f)
            fillRound(canvas, bar, Color.parseColor("#E7E0D4"), 6f)
            val fill = RectF(bar.left, bar.top, bar.left + bar.width() * (progress.percent / 100f), bar.bottom)
            if (fill.width() > 4f) fillRound(canvas, fill, SAFFRON, 6f)
            y += 20f
        }

        y += 16f
        val notice = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 56f)
        fillRound(canvas, notice, Color.parseColor("#F8EFE6"), 8f)
        drawText(
            canvas,
            "Confidential family copy. Generated on this phone by Kutumb Care. Nothing in this PDF was uploaded anywhere — the app has no internet permission.",
            notice.left + 14f,
            notice.top + 12f,
            bodyPaint(10f, Color.parseColor("#7A4A28")),
            CONTENT_WIDTH - 28f
        )
    }

    private fun drawCategoryPage(
        canvas: Canvas,
        snapshot: DashboardSnapshot,
        category: AssetCategory,
        items: List<ChecklistItem>,
        pageNo: Int,
        pageCount: Int
    ) {
        drawPageChrome(canvas, pageNo, pageCount)
        headerBand(canvas, category.displayName.uppercase(Locale.US))
        var y = 148f
        y = drawText(canvas, category.subtitle, MARGIN, y, bodyPaint(11f, MUTED), CONTENT_WIDTH)
        y += 14f

        items.forEach { item ->
            val blockHeight = estimateItemHeight(item)
            if (y + blockHeight > PAGE_HEIGHT - 70f) {
                return@forEach
            }
            val box = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + blockHeight)
            fillRound(canvas, box, Color.parseColor("#FBF8F2"), 8f)
            val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E4DDD1")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(box, 8f, 8f, stroke)

            var inner = y + 12f
            inner = drawText(canvas, item.title, MARGIN + 12f, inner, titlePaint(13f, FOREST), CONTENT_WIDTH - 24f)
            val nominee = when (item.nomineeEnum) {
                NomineeStatus.NOMINEE_VERIFIED -> "Nominee verified"
                NomineeStatus.NEEDS_NOMINEE -> "Needs nominee"
                NomineeStatus.NOT_APPLICABLE -> "Nominee n/a"
            }
            inner = drawText(
                canvas,
                nominee + if (item.isCompleted) "  ·  Recorded" else "  ·  Not yet recorded",
                MARGIN + 12f,
                inner + 2f,
                bodyPaint(10f, if (item.nomineeEnum == NomineeStatus.NEEDS_NOMINEE) SAFFRON else MUTED),
                CONTENT_WIDTH - 24f
            )
            val lines = buildList {
                if (item.institutionName.isNotBlank()) add("Institution: ${item.institutionName}")
                if (item.accountRef.isNotBlank()) add("Reference: ${item.accountRef}")
                add("Location: ${item.physicalLocation.ifBlank { "Not recorded" }}")
                if (item.contactName.isNotBlank() || item.contactPhone.isNotBlank()) {
                    add("Contact: ${listOf(item.contactName, item.contactPhone).filter { it.isNotBlank() }.joinToString(" · ")}")
                }
                if (item.additionalNotes.isNotBlank()) add("Notes: ${item.additionalNotes}")
            }
            lines.forEach { line ->
                inner = drawText(canvas, line, MARGIN + 12f, inner + 3f, bodyPaint(10.5f, INK), CONTENT_WIDTH - 24f)
            }
            y += blockHeight + 10f
        }

        if (snapshot.preferences.primaryUserName.isNotBlank()) {
            canvas.drawText(
                "Owner: ${snapshot.preferences.primaryUserName}",
                MARGIN,
                PAGE_HEIGHT - 48f,
                bodyPaint(9f, MUTED)
            )
        }
    }

    private fun estimateItemHeight(item: ChecklistItem): Float {
        val extras = listOfNotNull(
            item.institutionName.takeIf { it.isNotBlank() },
            item.accountRef.takeIf { it.isNotBlank() },
            item.physicalLocation,
            item.contactName.takeIf { it.isNotBlank() },
            item.additionalNotes.takeIf { it.isNotBlank() }
        ).size
        return (86 + extras * 14).toFloat().coerceAtLeast(92f)
    }

    private fun headerBand(canvas: Canvas, title: String) {
        val band = RectF(0f, 0f, PAGE_WIDTH.toFloat(), 92f)
        canvas.drawRect(band, paint(FOREST))
        canvas.drawRect(RectF(0f, 92f, PAGE_WIDTH.toFloat(), 96f), paint(SAFFRON))
        canvas.drawText("KUTUMB CARE", MARGIN, 36f, titlePaint(11f, Color.parseColor("#E8D5B5")))
        canvas.drawText(title, MARGIN, 68f, titlePaint(18f, Color.WHITE))
        canvas.drawText("PRIVATE  ·  NO PASSWORDS", PAGE_WIDTH - MARGIN - 168f, 36f, bodyPaint(9f, Color.parseColor("#E8D5B5")))
    }

    private fun drawPageChrome(canvas: Canvas, pageNo: Int, pageCount: Int) {
        canvas.drawColor(Color.WHITE)
        canvas.drawText(
            "Kutumb Care  ·  Family copy  ·  Page $pageNo of $pageCount",
            MARGIN,
            PAGE_HEIGHT - 28f,
            bodyPaint(9f, MUTED)
        )
    }

    private fun sectionTitle(canvas: Canvas, text: String, y: Float): Float {
        canvas.drawText(text.uppercase(Locale.US), MARGIN, y + 12f, titlePaint(11f, SAFFRON))
        return y + 22f
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, paint: TextPaint, width: Float): Float {
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }

    private fun fillRound(canvas: Canvas, rect: RectF, color: Int, radius: Float) {
        canvas.drawRoundRect(rect, radius, radius, paint(color))
    }

    private fun paint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun titlePaint(size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    private fun bodyPaint(size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    private fun interface PagePainter {
        fun draw(canvas: Canvas, pageNo: Int, pageCount: Int)
    }

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 42f
        private const val CONTENT_WIDTH = 595f - 84f
        private val FOREST = Color.parseColor("#134E4A")
        private val SAFFRON = Color.parseColor("#C45C26")
        private val INK = Color.parseColor("#1C2321")
        private val MUTED = Color.parseColor("#5C6561")
    }
}
