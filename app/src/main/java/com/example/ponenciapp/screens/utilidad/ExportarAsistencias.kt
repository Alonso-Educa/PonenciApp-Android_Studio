package com.example.ponenciapp.screens.utilidad

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Modelo interno para las filas de exportación
data class AsistenciaRow(
    val fechaHora: String,
    val nombreEvento: String,
    val nombrePonencia: String,
    val nombreParticipante: String,
    val tipo: String
)

// Función auxiliar para cargar datos desde Firestore
suspend fun cargarDatosAsistencias(idEvento: String): List<AsistenciaRow> {
    val firestore = FirebaseFirestore.getInstance()

    val asistenciasSnapshot = firestore.collection("asistencias")
        .whereEqualTo("idEvento", idEvento)
        .get().await()

    val participantesSnapshot = firestore.collection("usuarios").get().await()

    val ponenciasSnapshot = firestore.collection("ponencias")
        .whereEqualTo("idEvento", idEvento)
        .get().await()

    val eventoSnapshot = firestore.collection("eventos")
        .document(idEvento)
        .get().await()

    val nombreEvento = eventoSnapshot.getString("nombre") ?: ""

    // Mapas para acceso rápido por id
    val participantesMap = participantesSnapshot.documents.associate { doc ->
        doc.id to "${doc.getString("nombre") ?: ""} ${doc.getString("apellidos") ?: ""}"
    }
    val ponenciasMap = ponenciasSnapshot.documents.associate { doc ->
        doc.id to (doc.getString("titulo") ?: "")
    }

    return asistenciasSnapshot.documents.map { doc ->
        val idParticipante = doc.getString("idParticipante") ?: ""
        val idPonencia = doc.getString("idPonencia") ?: ""
        val fechaHora = doc.getString("fechaHora") ?: ""

        // Formatear fecha desde milisegundos
        val fechaFormateada = try {
            val millis = fechaHora.toLong()
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))
        } catch (e: Exception) {
            fechaHora
        }

        AsistenciaRow(
            fechaHora = fechaFormateada,
            nombreEvento = nombreEvento,
            nombrePonencia = if (idPonencia.isNotEmpty()) ponenciasMap[idPonencia] ?: "" else "",
            nombreParticipante = participantesMap[idParticipante] ?: "",
            tipo = doc.getString("tipo") ?: ""
        )
    }.sortedWith(compareBy({ it.tipo }, { it.fechaHora }))
}

// ─── EXCEL ───────────────────────────────────────────────────────────────────

@Composable
fun exportarAsistenciasExcelUI(context: Context, idEvento: String) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            exportarAsistenciasExcel(context, idEvento)
        }
    }
}

suspend fun exportarAsistenciasExcel(context: Context, idEvento: String) {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Asistencias")

    // Cargar datos desde Firestore
    val filas = try {
        cargarDatosAsistencias(idEvento)
    } catch (e: Exception) {
        Toast.makeText(context, "Error cargando datos: ${e.message}", Toast.LENGTH_SHORT).show()
        return
    }

    // Estilos
    val styleTitulo: XSSFCellStyle = workbook.createCellStyle() as XSSFCellStyle
    styleTitulo.apply {
        alignment = HorizontalAlignment.CENTER
        val font: XSSFFont = workbook.createFont() as XSSFFont
        font.bold = true
        font.fontHeightInPoints = 16
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setFont(font)
    }

    val styleHeader: XSSFCellStyle = workbook.createCellStyle() as XSSFCellStyle
    styleHeader.apply {
        alignment = HorizontalAlignment.CENTER
        val font: XSSFFont = workbook.createFont() as XSSFFont
        font.bold = true
        setFont(font)
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
    }

    // Estilo para celdas normales
    val styleData: XSSFCellStyle = workbook.createCellStyle() as XSSFCellStyle
    styleData.apply {
        alignment = HorizontalAlignment.CENTER
        verticalAlignment = VerticalAlignment.CENTER
        wrapText = true
    }

    // Fila 0: Título
    val rowTitulo = sheet.createRow(0)
    val cellTitulo = rowTitulo.createCell(0)
    cellTitulo.setCellValue("PONENCIAPP")
    cellTitulo.cellStyle = styleTitulo
    sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 4))

    // Fila 1: Cabeceras
    val rowHeader = sheet.createRow(1)
    val headers = listOf("FECHA / HORA", "EVENTO", "PONENCIA", "PARTICIPANTE", "TIPO")
    headers.forEachIndexed { index, header ->
        val cell = rowHeader.createCell(index)
        cell.setCellValue(header)
        cell.cellStyle = styleHeader
    }

    // Filas de datos
    filas.forEachIndexed { index, fila ->
        val row = sheet.createRow(index + 2)

        val cell0 = row.createCell(0)
        cell0.setCellValue(fila.fechaHora)
        cell0.cellStyle = styleData

        val cell1 = row.createCell(1)
        cell1.setCellValue(fila.nombreEvento)
        cell1.cellStyle = styleData

        val cell2 = row.createCell(2)
        cell2.setCellValue(fila.nombrePonencia)
        cell2.cellStyle = styleData

        val cell3 = row.createCell(3)
        cell3.setCellValue(fila.nombreParticipante)
        cell3.cellStyle = styleData

        val cell4 = row.createCell(4)
        cell4.setCellValue(fila.tipo)
        cell4.cellStyle = styleData
    }

    // Anchos de columna fijos
    val columnWidths = listOf(6000, 8000, 5000, 7000, 4000)
    for (i in columnWidths.indices) {
        sheet.setColumnWidth(i, columnWidths[i])
    }

    // Guardar en Descargas
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "asistencias_${System.currentTimeMillis()}.xlsx")
        put(
            MediaStore.MediaColumns.MIME_TYPE,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        resolver.openOutputStream(it)?.use { stream ->
            workbook.write(stream)
        }
        workbook.close()
        Toast.makeText(
            context, "Informe de asistencias guardado en Descargas", Toast.LENGTH_LONG
        ).show()
    }
}


// ─── PDF ─────────────────────────────────────────────────────────────────────

@Composable
fun exportarAsistenciasPdfUI(context: Context, idEvento: String) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            exportarAsistenciasPdf(context, idEvento)
        }
    }
}

suspend fun exportarAsistenciasPdf(context: Context, idEvento: String) {
    val pdf = PdfDocument()

    // Cargar datos desde Firestore
    val filas = try {
        cargarDatosAsistencias(idEvento)
    } catch (e: Exception) {
        Toast.makeText(context, "Error cargando datos: ${e.message}", Toast.LENGTH_SHORT).show()
        return
    }

    // ── Configuración de página ──────────────────────────────────────
    val pageWidth = 595f
    val pageHeight = 842f
    val margin = 32f

    // ── Colores ──────────────────────────────────────────────────────
    val colorHeader = Color.parseColor("#2C3E50")
    val colorRowEven = Color.parseColor("#F2F6FA")
    val colorRowOdd = Color.WHITE
    val colorBorder = Color.parseColor("#BDC3C7")
    val colorHeaderTxt = Color.WHITE
    val colorBodyTxt = Color.parseColor("#2C3E50")

    // ── Pinceles ─────────────────────────────────────────────────────
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = colorBorder
        strokeWidth = 0.8f
    }
    val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 18f
        isFakeBoldText = true
        color = colorBodyTxt
        textAlign = Paint.Align.CENTER
    }
    val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
    }
    val headerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f
        isFakeBoldText = true
        color = colorHeaderTxt
        textAlign = Paint.Align.CENTER
    }
    val cellTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f
        color = colorBodyTxt
    }

    // ── Columnas ─────────────────────────────────────────────────────
    data class ColDef(val label: String, val weight: Float)

    val columns = listOf(
        ColDef("Fecha/Hora", 0.17f),
        ColDef("Evento", 0.32f),
        ColDef("Ponencia", 0.10f),
        ColDef("Participante", 0.18f),
        ColDef("Tipo", 0.24f)
    )
    val tableWidth = pageWidth - margin * 2
    val colWidths = columns.map { it.weight * tableWidth }
    val colX = colWidths.runningFold(margin) { acc, w -> acc + w }.dropLast(1)

    val cellPadH = 6f
    val cellPadV = 5f
    val headerH = 28f
    val minRowH = 22f

    // ── Estado de paginación ─────────────────────────────────────────
    var pageNum = 1
    var pageInfo =
        PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNum).create()
    var page = pdf.startPage(pageInfo)
    var canvas = page.canvas
    var y = margin

    // ── Helpers ──────────────────────────────────────────────────────
    fun buildLayout(text: String, width: Float, paint: TextPaint): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(2f, 1f)
            .setIncludePad(false)
            .build()

    fun drawHeader(startY: Float): Float {
        fillPaint.color = colorHeader
        canvas.drawRect(margin, startY, margin + tableWidth, startY + headerH, fillPaint)
        columns.forEachIndexed { i, col ->
            val cx = colX[i] + colWidths[i] / 2f
            val fm = headerTextPaint.fontMetrics
            val textH = fm.descent - fm.ascent
            val ty = startY + (headerH - textH) / 2f - fm.ascent
            canvas.drawText(col.label, cx, ty, headerTextPaint)
        }
        canvas.drawLine(
            margin, startY + headerH, margin + tableWidth, startY + headerH, borderPaint
        )
        return startY + headerH
    }

    fun drawPageHeader(isFirst: Boolean) {
        if (isFirst) {
            canvas.drawText("Asistencias al evento", pageWidth / 2f, y + 18f, titlePaint)
            y += 22f
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Generado el $fecha", pageWidth / 2f, y + 10f, subtitlePaint)
            y += 18f
        } else {
            canvas.drawText(
                "Asistencias al evento (continuación)", pageWidth / 2f, y + 18f, titlePaint
            )
            y += 28f
        }
        y = drawHeader(y) + 1f
    }

    fun newPage() {
        pdf.finishPage(page)
        pageNum++
        pageInfo =
            PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNum).create()
        page = pdf.startPage(pageInfo)
        canvas = page.canvas
        y = margin
        drawPageHeader(false)
    }

    // ── Dibujar encabezado primera página ────────────────────────────
    drawPageHeader(true)

    // ── Filas — usando datos de Firestore ────────────────────────────
    filas.forEachIndexed { rowIndex, fila ->
        val texts = listOf(
            fila.fechaHora,
            fila.nombreEvento,
            fila.nombrePonencia,
            fila.nombreParticipante,
            fila.tipo
        )

        val layouts = texts.mapIndexed { i, t ->
            buildLayout(t, colWidths[i] - cellPadH * 2, cellTextPaint)
        }
        val rowH = maxOf(minRowH, layouts.maxOf { it.height.toFloat() } + cellPadV * 2)

        if (y + rowH > pageHeight - margin) newPage()

        fillPaint.color = if (rowIndex % 2 == 0) colorRowEven else colorRowOdd
        canvas.drawRect(margin, y, margin + tableWidth, y + rowH, fillPaint)

        layouts.forEachIndexed { i, layout ->
            val textH = layout.height.toFloat()
            val textTop = y + (rowH - textH) / 2f
            canvas.save()
            canvas.clipRect(colX[i], y, colX[i] + colWidths[i], y + rowH)
            canvas.translate(colX[i] + cellPadH, textTop)
            layout.draw(canvas)
            canvas.restore()
        }

        canvas.drawLine(margin, y + rowH, margin + tableWidth, y + rowH, borderPaint)
        colX.forEach { cx ->
            canvas.drawLine(cx, y, cx, y + rowH, borderPaint)
        }
        canvas.drawLine(margin + tableWidth, y, margin + tableWidth, y + rowH, borderPaint)

        y += rowH
    }

    pdf.finishPage(page)

    // ── Guardar en Descargas ─────────────────────────────────────────
    val values = ContentValues().apply {
        put(
            MediaStore.MediaColumns.DISPLAY_NAME,
            "asistencias_${System.currentTimeMillis()}.pdf"
        )
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }
    try {
        val uri =
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                pdf.writeTo(stream)
                Toast.makeText(
                    context,
                    "Informe de asistencias guardado en Descargas",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        pdf.close()
    }
}