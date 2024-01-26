package com.example.pdfexport

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random


class MainActivity : AppCompatActivity() {
    private lateinit var pdfDocument: PdfDocument
    private val temperatures = ArrayList<Float>()
    private val dates = ArrayList<String>()
    private val cellHeight = 40f
    private val maxTemp = 4200
    private val minTemp = 3200
    private val stepTemp = 50
    private val paddingOutside = 20f
    private val pageWidth = 2300
    private var dayCount = 30
    private val highlightStep = 100
    private val titleWidth = 72f
    private val cellWidth = (pageWidth - paddingOutside * 2 - titleWidth * 2) / dayCount
    private val temperatureCount = ((maxTemp - minTemp) / stepTemp + 1)
    private val rowCount = temperatureCount + 11
    private var pageHeight = (paddingOutside * 2 + rowCount * cellHeight).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        generateDummyData()
        val generatePDFBtn: Button = findViewById(R.id.idBtnGeneratePDF)
        generatePDFBtn.setOnClickListener {
            createPdf()
        }
    }

    private fun generateDummyData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (i in 0 until 30) {
            val date = dateFormat.format(calendar.time)
            val temperature = 35.2f + Random().nextFloat() * (37.5f - 35.2f)

            dates.add(date)
            temperatures.add(temperature)

            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun createAppDirectoryInDownloads(): File? {
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDirectory = File(downloadsDirectory, "PDF")

        if (!appDirectory.exists()) {
            val directoryCreated = appDirectory.mkdir()
            if (!directoryCreated) {
                // Failed to create the directory
                return null
            }
        }

        return appDirectory
    }

    private fun createPdf() {
        val downloadsDirectory = createAppDirectoryInDownloads()
        val fileName = "test.pdf"
        pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()  // A4 size
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas

        drawGraph(canvas)

        pdfDocument.finishPage(page)

        try {
            val fileOutputStream = FileOutputStream(File(downloadsDirectory, fileName))
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()

            // Run on the UI thread to show toast
            runOnUiThread {
                Toast.makeText(this@MainActivity, "PDF generated successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawYAxis(canvas: Canvas) {
        var temp = maxTemp
        val startXOffset = paddingOutside
        val endXOffset = pageWidth - paddingOutside - titleWidth
        var yOffset = (paddingOutside + cellHeight * 0.5).toFloat()
        val middleXOffset = pageWidth / 2 + 6f

        val boldPaint = Paint()
        boldPaint.textSize = 16f
        boldPaint.typeface = Typeface.DEFAULT_BOLD

        val normalPaint = Paint()
        normalPaint.textSize = 14f

        val borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = Color.parseColor("#C2C2C2")

        val linePaint = Paint()
        linePaint.color = Color.parseColor("#EFEFEF")
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 2f

        while (temp > minTemp - 0.1) {
            val decimalNumber = temp % highlightStep
            if (decimalNumber % highlightStep == 0) {
                canvas.drawLine(startXOffset + titleWidth, yOffset, endXOffset, yOffset, linePaint)
            }
            // Draw Left Title
            canvas.drawLine(startXOffset, yOffset, startXOffset + 8, yOffset, borderPaint)
            if (decimalNumber % highlightStep == 0) {
                canvas.drawText(
                    String.format("%.2f", temp / 100f),
                    startXOffset + 14,
                    yOffset + 6,
                    boldPaint
                )
            } else {
                canvas.drawText(".${decimalNumber}", startXOffset + 38, yOffset + 4, normalPaint)
            }
            canvas.drawLine(
                startXOffset + titleWidth - 8,
                yOffset,
                startXOffset + titleWidth,
                yOffset,
                borderPaint
            )

            // Draw Right Title
            canvas.drawLine(endXOffset, yOffset, endXOffset + 8, yOffset, borderPaint)
            if (decimalNumber % highlightStep == 0) {
                canvas.drawText(
                    String.format("%.2f", temp / 100f),
                    endXOffset + 14,
                    yOffset + 6,
                    boldPaint
                )
            } else {
                canvas.drawText(".${decimalNumber}", endXOffset + 38, yOffset + 4, normalPaint)
            }
            canvas.drawLine(
                endXOffset + titleWidth - 8,
                yOffset,
                endXOffset + titleWidth,
                yOffset,
                borderPaint
            )

            // Draw Middle Title
            if (decimalNumber % highlightStep == 0) {
                canvas.drawText(
                    String.format("%.2f", temp / 100f),
                    middleXOffset,
                    yOffset + 6,
                    boldPaint
                )
            } else {
                canvas.drawText(".${decimalNumber}", middleXOffset + 20, yOffset + 4, normalPaint)
            }

            yOffset += cellHeight
            temp = (temp - stepTemp.toDouble()).toInt()
        }
        yOffset -= cellHeight * 0.5f
        linePaint.color = Color.parseColor("#D4D4D4")
        canvas.drawLine(startXOffset, yOffset, endXOffset + titleWidth, yOffset, linePaint)

        yOffset += cellHeight * 3
        canvas.drawLine(startXOffset, yOffset, endXOffset + titleWidth, yOffset, linePaint)
    }

    private fun drawBorder(canvas: Canvas) {
        val left = 20f
        val top = 20f
        val bottom = pageHeight - 20f
        val right = pageWidth - 20f
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#C2C2C2")
        paint.strokeWidth = 1.5f
        canvas.drawRect(left, top, right, bottom, paint)
    }

    private fun drawBackground(canvas: Canvas) {
        val startXOffset = paddingOutside + titleWidth
        val endXOffset = pageWidth - paddingOutside - titleWidth
        val startYOffset = paddingOutside
        val endYOffset = pageHeight - paddingOutside

        val greyPaint = Paint()
        greyPaint.color = Color.parseColor("#0D000000")
        greyPaint.style = Paint.Style.FILL
        val whitePaint = Paint()
        whitePaint.color = Color.parseColor("#FFFFFF")
        whitePaint.style = Paint.Style.FILL
        val borderPaint = Paint()
        borderPaint.color = Color.parseColor("#D4D4D4")
        borderPaint.style = Paint.Style.STROKE

        for (i in temperatureCount + 2 until rowCount - 1) {
            if (i % 2 == 0) {
                canvas.drawRect(
                    startXOffset - titleWidth,
                    startYOffset + (i + 1f) * cellHeight,
                    endXOffset + titleWidth,
                    startYOffset + (i + 2f) * cellHeight,
                    greyPaint
                )
            }
        }

        for (i in 0 until  dayCount) {
            canvas.drawRect(
                startXOffset + i * cellWidth,
                startYOffset,
                startXOffset + (i + 1) * cellWidth,
                endYOffset,
                if (i % 2 == 0) greyPaint else whitePaint
            )
        }

        for (i in 0 .. dayCount) {
            canvas.drawRect(
                startXOffset + i * cellWidth,
                startYOffset,
                startXOffset + (i + 1) * cellWidth,
                endYOffset,
                borderPaint
            )
        }

        for (i in 0 until rowCount) {
            canvas.drawRect(
                startXOffset - if (i >= temperatureCount) titleWidth else 0f,
                startYOffset + (i + if (i >= temperatureCount) 1f else 0.5f) * cellHeight,
                endXOffset + if (i >= temperatureCount) titleWidth else 0f,
                startYOffset + (i + if (i >= temperatureCount) 1f else 0.5f) * cellHeight,
                borderPaint
            )
        }

        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = Color.parseColor("#C2C2C2")
        borderPaint.strokeWidth = 1.5f
        canvas.drawRect(paddingOutside + titleWidth, paddingOutside, pageWidth - paddingOutside - titleWidth, pageHeight - paddingOutside, borderPaint)
    }

    private fun drawDate(canvas: Canvas){
        val textPaint = Paint()
        textPaint.textSize = 14f
        val xOffset = paddingOutside + titleWidth
        val yOffset = paddingOutside + temperatureCount * cellHeight + cellHeight * 1.5f + 5f
        for (i in 0 until dayCount){
            canvas.drawText((i + 1).toString(), xOffset + (i + 0.5f) * cellWidth - if(i > 9) 8f else 4f, yOffset, textPaint,)
        }
    }

    private fun drawTemperature(canvas: Canvas){
        var previousXOffset:Float? = null
        var previousYOffset: Float? = null
        val xOffset = paddingOutside + titleWidth
        val minYOffset = paddingOutside + cellHeight * 0.5f
        val maxYOffset = minYOffset + (temperatureCount - 1) * cellHeight
        val paint = Paint()
        paint.color = Color.parseColor("#23D0B9")
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 1.5f

        for (i in 0 until dayCount){
            val temperature = temperatures[i]
            val yOffset = (maxTemp - temperature * 100f) * (maxYOffset - minYOffset) / (maxTemp - minTemp) + minYOffset
            canvas.drawCircle(xOffset + (i + 0.5f) * cellWidth, yOffset, 5f, paint)
            if(previousXOffset!=null && previousYOffset!=null){
                canvas.drawLine(previousXOffset, previousYOffset, xOffset + (i + 0.5f) * cellWidth, yOffset, paint)
            }
            previousXOffset = xOffset + (i + 0.5f)*cellWidth
            previousYOffset = yOffset
        }
    }

    private fun drawGraph(canvas: Canvas) {

        // Draw background
        drawBackground(canvas)

        // Draw border
        drawBorder(canvas)

        // Draw y-axis
        drawYAxis(canvas)

        // Draw Date
        drawDate(canvas)

        // Draw temperature points
        drawTemperature(canvas)
    }
}