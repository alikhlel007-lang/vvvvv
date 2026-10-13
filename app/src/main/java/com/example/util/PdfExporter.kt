package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.CafeTable
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    fun generateAndShareTablesPdf(
        context: Context,
        cafeName: String,
        tables: List<CafeTable>
    ): File? {
        try {
            // Standard A4 dimensions at 72 dpi: 595 x 842 points
            val pageWidth = 595
            val pageHeight = 842

            val document = PdfDocument()

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(20, 30, 35)
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
            }

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0, 150, 136)
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }

            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(80, 100, 110)
                textSize = 12f
                textAlign = Paint.Align.CENTER
            }

            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(245, 250, 250)
                style = Paint.Style.FILL
            }

            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0, 180, 160)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

            val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0, 120, 110)
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }

            val instructionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(90, 110, 115)
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }

            // We can layout 4 tables per A4 page (2x2 grid)
            val tablesPerPage = 4
            val pagesCount = maxOf(1, (tables.size + tablesPerPage - 1) / tablesPerPage)

            val margin = 30f
            val cardSpacing = 20f
            val cardWidth = (pageWidth - 2 * margin - cardSpacing) / 2
            val cardHeight = 320f
            val qrSize = 160

            for (pageIndex in 0 until pagesCount) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Background
                canvas.drawColor(Color.WHITE)

                // Header
                canvas.drawText(cafeName.ifEmpty { "CAFE MENU" }, pageWidth / 2f, 50f, titlePaint)
                canvas.drawText("باركودات الطاولات للطلب الإلكتروني المباشر", pageWidth / 2f, 75f, subtitlePaint)
                canvas.drawText("الصفحة ${pageIndex + 1} من $pagesCount", pageWidth / 2f, 92f, subtitlePaint)

                val startIdx = pageIndex * tablesPerPage
                val endIdx = minOf(startIdx + tablesPerPage, tables.size)

                for (i in startIdx until endIdx) {
                    val localIndex = i - startIdx
                    val col = localIndex % 2
                    val row = localIndex / 2

                    val left = margin + col * (cardWidth + cardSpacing)
                    val top = 110f + row * (cardHeight + cardSpacing)
                    val right = left + cardWidth
                    val bottom = top + cardHeight

                    val cardRect = RectF(left, top, right, bottom)
                    // Draw card background & border
                    canvas.drawRoundRect(cardRect, 16f, 16f, cardPaint)
                    canvas.drawRoundRect(cardRect, 16f, 16f, cardBorderPaint)

                    val table = tables[i]
                    val cardCenterX = left + cardWidth / 2f

                    // Table Label
                    canvas.drawText(table.label.ifEmpty { "طاولة ${table.tableNumber}" }, cardCenterX, top + 35f, tableHeaderPaint)

                    // Generate QR Bitmap
                    val qrBitmap = QrCodeGenerator.generateQrBitmap(
                        content = table.qrPayload,
                        size = qrSize,
                        darkColor = Color.rgb(10, 40, 45),
                        lightColor = Color.TRANSPARENT
                    )

                    if (qrBitmap != null) {
                        val qrLeft = cardCenterX - (qrSize / 2f)
                        val qrTop = top + 55f
                        canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)
                    }

                    // Scan instruction
                    canvas.drawText("امسح الكود بكاميرا الهاتف", cardCenterX, bottom - 40f, textPaint.apply { textAlign = Paint.Align.CENTER; textSize = 11f })
                    canvas.drawText("لتصفح المنيو وطلب الوجبات مباشرة", cardCenterX, bottom - 22f, instructionPaint)
                }

                document.finishPage(page)
            }

            // Save PDF file in cache
            val pdfDir = File(context.cacheDir, "pdf_exports")
            if (!pdfDir.exists()) pdfDir.mkdirs()
            val pdfFile = File(pdfDir, "cafe_tables_qr.pdf")

            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            // Share intent
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "باركودات طاولات الكافيه - $cafeName")
                putExtra(Intent.EXTRA_TEXT, "ملف باركودات طاولات الكافيه جاهز للطباعة والتعليق.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "مشاركة أو طباعة باركودات الطاولات").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
