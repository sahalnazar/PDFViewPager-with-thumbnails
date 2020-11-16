package io.github.sahalnazar.pdfviewpagerthumbnail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter
import io.github.sahalnazar.pdfviewpagerthumbnail.Constants.DOWNLOADED_FILE_NAME
import io.github.sahalnazar.pdfviewpagerthumbnail.Constants.PDF_URL
import io.github.sahalnazar.pdfviewpagerthumbnail.Constants.selectedItem
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var pdfViewPagerAdapter: PDFPagerAdapter? = null
    lateinit var targetFile: File
    lateinit var thumbnailAdaptor: ThumbnailAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PRDownloader.initialize(applicationContext)

        setupRootPath()

        setupThumbnailRecyclerView()

        downloadPDF()

        pdfViewPagerChangeListener()

        thumbnailAdaptor.setOnItemClickListener { position ->
            pdfViewPagerView.setCurrentItem(position, true)
            selectedItem = position
        }

    }

    private fun pdfViewPagerChangeListener() {
        pdfViewPagerView.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                rvThumbnail.scrollToPosition(position)
                selectedItem = position
                rvThumbnail.adapter?.notifyDataSetChanged()
            }

            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
        })
    }

    private fun downloadPDF() {
        PRDownloader.download(PDF_URL, getRootDirPath(this), DOWNLOADED_FILE_NAME).build()
            .setOnProgressListener {
                pbDownloading.isVisible = true
                tvDownloading.isVisible = true
                pbDownloading.max = it.totalBytes.toInt()
                pbDownloading.progress = it.currentBytes.toInt()
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    pbDownloading.isVisible = false
                    tvDownloading.isVisible = false
                    Toast.makeText(
                        this@MainActivity,
                        "Download Completed",
                        Toast.LENGTH_SHORT
                    ).show()


                    pdfViewPagerAdapter = PDFPagerAdapter(
                        this@MainActivity,
                        targetFile.absolutePath
                    )

                    pdfViewPagerView.adapter = pdfViewPagerAdapter

                    thumbnailAdaptor.differ.submitList(pdfToBitmap(targetFile))

                }

                @SuppressLint("SetTextI18n")
                override fun onError(error: Error?) {
                    pbDownloading.isVisible = false
                    tvDownloading.text = "Error: $error"
                    rvThumbnail.isVisible = false

                    Toast.makeText(this@MainActivity, "Error: $error", Toast.LENGTH_LONG)
                        .show()
                    println("$error")
                }
            })
    }

    private fun setupThumbnailRecyclerView() {
        thumbnailAdaptor = ThumbnailAdaptor()
        rvThumbnail.apply {
            adapter = thumbnailAdaptor
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }


    private fun setupRootPath() {
        val rootPath = getRootDirPath(this)
        targetFile = File(rootPath, DOWNLOADED_FILE_NAME)
    }

    private fun getRootDirPath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file: File = ContextCompat.getExternalFilesDirs(
                context.applicationContext,
                null
            )[0]
            file.absolutePath
        } else {
            context.applicationContext.filesDir.absolutePath
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (pdfViewPagerView.adapter as PDFPagerAdapter).close()
    }

    private fun pdfToBitmap(pdfFile: File): List<Bitmap>? {
        val bitmaps: MutableList<Bitmap> = ArrayList()
        try {
            val renderer =
                PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
            var bitmap: Bitmap
            val pageCount = renderer.pageCount
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val width = resources.displayMetrics.densityDpi / 200 * page.width
                val height = resources.displayMetrics.densityDpi / 200 * page.height
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                // close the page
                page.close()
            }
            // close the renderer
            renderer.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return bitmaps
    }
}