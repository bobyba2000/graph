package com.example.pdfexport

import android.graphics.Canvas
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
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random
import kotlin.math.floor
import kotlin.math.round


class MainActivity : AppCompatActivity() {
    private lateinit var pdfDocument: PdfDocument
    private val temperatures = ArrayList<Float>()
    private val dates = ArrayList<String>()
    private val cellHeight = 40f
    private val cellWidth = 70f
    private val maxTemp = 37.5
    private val minTemp = 35.2
    private val stepTemp = 0.1
    private val pageHeight = 1050
    private val padding = 10f
    private val pageWidth = 1200
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
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
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

    private fun createPdf(){
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
                Toast.makeText(this@MainActivity, "PDF generated successfully!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawYAxis(canvas: Canvas){
        var temp = minTemp
        var cellCount = ((maxTemp - minTemp) / stepTemp + 1) + //Total Cell for Temperature
                1 //Date Label
        var xOffset = 20f
        var yOffset = (padding * 2 + cellCount * cellHeight).toFloat()
        val boldPaint = Paint()
        boldPaint.textSize = 16f
        boldPaint.typeface = Typeface.DEFAULT_BOLD

        val normalPaint = Paint()
        normalPaint.textSize = 14f

        canvas.drawText("Date", xOffset + 5, yOffset + cellHeight, normalPaint)

        while(temp < maxTemp + 0.1){
            temp = temp.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toDouble()
            val decimalNumber = ((temp * 100).toInt() - (floor(temp) * 100)).toInt()
            if(decimalNumber % 50 == 0){
                canvas.drawText(String.format("%.2f", temp), xOffset, yOffset, boldPaint)
            }else{
                canvas.drawText(".${decimalNumber}", xOffset + 20, yOffset, normalPaint)
            }
            yOffset-=cellHeight
            temp +=stepTemp
        }
        canvas.drawLine(xOffset + 50, 40F, xOffset + 50, (padding * 2 + cellCount * cellHeight + cellHeight).toFloat(), normalPaint)
    }

    private fun drawGraph(canvas: Canvas) {
        val width = canvas.width
        val height = canvas.height

        val xScale = width.toFloat() / temperatures.size
        val yScale = height / (37.5f - 35.2f)

        val xOffset = 20f
        val yOffset = height - 20f

        // Draw x-axis
//        canvas.drawLine(xOffset, yOffset, width.toFloat(), yOffset, Paint())

        // Draw y-axis
        drawYAxis(canvas)

        // Draw temperature points
//        for (i in temperatures.indices) {
//            val x = xOffset + i * xScale
//            val y = yOffset - (temperatures[i] - 35.2f) * yScale
//
//            canvas.drawCircle(x, y, 5f, Paint())
//        }

        // Draw dates on x-axis
//        for (i in dates.indices) {
//            val x = xOffset + i * xScale
//            val y = yOffset + 15f
//
//            canvas.drawText(dates[i], x, y, Paint())
//        }
    }
}