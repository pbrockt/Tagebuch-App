package com.pbrockt.tagebuch.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import java.io.File

object PdfExporter {

    fun export(
        context: Context,
        days: List<DiaryDay>,
        pagesMap: Map<String, List<DiaryPage>>,
        fileName: String = "Tagebuch-Export.pdf"
    ): File {
        val document = PdfDocument()
        val titlePaint = Paint().apply { textSize = 22f; color = Color.BLACK; isFakeBoldText = true }
        val datePaint = Paint().apply { textSize = 16f; color = Color.DKGRAY; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 13f; color = Color.BLACK }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        val pageWidth = 595   // A4 points
        val pageHeight = 842
        val margin = 48f
        val lineHeight = 18f

        var pageIndex = 1

        fun newPdfPage(): Pair<PdfDocument.Page, Canvas> {
            val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex++).create()
            val page = document.startPage(info)
            return page to page.canvas
        }

        fun drawWrappedText(canvas: Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint): Float {
            var y = startY
            val words = text.split(" ")
            var line = ""
            for (word in words) {
                val test = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(test) > maxWidth && line.isNotEmpty()) {
                    canvas.drawText(line, x, y, paint)
                    y += lineHeight
                    line = word
                } else {
                    line = test
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, x, y, paint)
                y += lineHeight
            }
            return y
        }

        // Title page
        val (titlePage, titleCanvas) = newPdfPage()
        titleCanvas.drawText("Mein Tagebuch", margin, 200f, titlePaint.apply { textSize = 32f })
        titleCanvas.drawText("Exportiert am ${java.time.LocalDate.now()}", margin, 240f, bodyPaint)
        document.finishPage(titlePage)

        // Entry pages
        for (day in days.sortedBy { it.date }) {
            val pages = pagesMap[day.date] ?: continue
            if (pages.isEmpty()) continue

            val (pdfPage, canvas) = newPdfPage()
            var y = margin + 20f

            // Date header
            canvas.drawText(day.date, margin, y, datePaint)
            y += 8f
            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
            y += 20f

            // Mood + Weather
            val meta = buildString {
                day.mood?.let { append("Stimmung: ${moodLabel(it)}  ") }
                day.weather?.let { append("Wetter: ${weatherLabel(it)}") }
            }
            if (meta.isNotEmpty()) {
                canvas.drawText(meta, margin, y, bodyPaint.apply { color = Color.GRAY })
                y += 24f
                bodyPaint.color = Color.BLACK
            }

            // Page content
            for ((idx, page) in pages.withIndex()) {
                if (pages.size > 1) {
                    canvas.drawText("Seite ${idx + 1}", margin, y, datePaint.apply { textSize = 13f })
                    y += 18f
                    datePaint.textSize = 16f
                }
                y = drawWrappedText(canvas, page.content.ifEmpty { "(Kein Text)" },
                    margin, y, (pageWidth - margin * 2), bodyPaint)
                y += 12f
                if (y > pageHeight - margin) break
            }

            document.finishPage(pdfPage)
        }

        val file = File(context.getExternalFilesDir(null), fileName)
        document.writeTo(file.outputStream())
        document.close()
        return file
    }

    private fun moodLabel(mood: String) = when (mood) {
        "great" -> "😁 Super"; "good" -> "😊 Gut"; "okay" -> "😐 Ok"
        "bad" -> "😔 Schlecht"; "awful" -> "😢 Schrecklich"; else -> mood
    }

    private fun weatherLabel(weather: String) = when (weather) {
        "sunny" -> "☀️ Sonnig"; "cloudy" -> "☁️ Bewölkt"; "rainy" -> "🌧️ Regnerisch"
        "snowy" -> "❄️ Schnee"; "stormy" -> "⛈️ Gewitter"; else -> weather
    }
}
