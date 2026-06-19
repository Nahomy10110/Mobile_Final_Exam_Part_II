package com.example.cattlerotation.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.cattlerotation.R
import com.example.cattlerotation.data.AppDatabase
import com.example.cattlerotation.data.Paddock
import com.example.cattlerotation.util.applyStatusBarTopPadding
import com.example.cattlerotation.util.toDateString
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class PaddockFormActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getInstance(this) }
    private var paddockId: Long = 0L
    private var creationDate: Long = System.currentTimeMillis()
    private var photoPath: String? = null
    private var videoPath: String? = null
    private var pendingPhotoFile: File? = null
    private var pendingVideoFile: File? = null

    private lateinit var inputName: TextInputEditText
    private lateinit var inputArea: TextInputEditText
    private lateinit var buttonDate: MaterialButton
    private lateinit var imagePhoto: ImageView
    private lateinit var imageVideo: ImageView
    private lateinit var textNoPhoto: TextView
    private lateinit var textNoVideo: TextView

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok && pendingPhotoFile != null) {
                photoPath = pendingPhotoFile!!.absolutePath
                imagePhoto.setImageURI(Uri.fromFile(pendingPhotoFile))
                textNoPhoto.visibility = View.GONE
            }
        }

    private val takeVideo =
        registerForActivityResult(ActivityResultContracts.CaptureVideo()) { ok ->
            if (ok && pendingVideoFile != null) {
                videoPath = pendingVideoFile!!.absolutePath
                showVideoThumb()
                textNoVideo.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paddock_form)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.applyStatusBarTopPadding()
        toolbar.setNavigationOnClickListener { finish() }

        inputName = findViewById(R.id.inputName)
        inputArea = findViewById(R.id.inputArea)
        buttonDate = findViewById(R.id.buttonDate)
        imagePhoto = findViewById(R.id.imagePhoto)
        imageVideo = findViewById(R.id.imageVideo)
        textNoPhoto = findViewById(R.id.textNoPhoto)
        textNoVideo = findViewById(R.id.textNoVideo)

        buttonDate.setOnClickListener { pickDate() }
        findViewById<MaterialButton>(R.id.buttonPhoto).setOnClickListener { launchPhoto() }
        findViewById<MaterialButton>(R.id.buttonVideo).setOnClickListener { launchVideo() }
        findViewById<MaterialButton>(R.id.buttonSave).setOnClickListener { save() }

        paddockId = intent.getLongExtra(EXTRA_ID, 0L)
        if (paddockId != 0L) {
            toolbar.setTitle(R.string.title_edit_paddock)
            loadPaddock()
        } else {
            toolbar.setTitle(R.string.title_new_paddock)
            updateDateButton()
        }
    }

    private fun loadPaddock() = lifecycleScope.launch {
        val p = withContext(Dispatchers.IO) { db.paddockDao().getById(paddockId) } ?: return@launch
        inputName.setText(p.name)
        inputArea.setText(p.areaM2.toString())
        creationDate = p.creationDate
        photoPath = p.photoPath
        videoPath = p.videoPath
        updateDateButton()
        photoPath?.let {
            imagePhoto.setImageURI(Uri.fromFile(File(it)))
            textNoPhoto.visibility = View.GONE
        }
        if (videoPath != null) {
            showVideoThumb()
            textNoVideo.visibility = View.GONE
        }
    }

    private fun pickDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = creationDate }
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d)
            creationDate = cal.timeInMillis
            updateDateButton()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateButton() {
        buttonDate.text = "${creationDate.toDateString()}"
    }

    private fun newMediaFile(ext: String): File {
        val dir = getExternalFilesDir("media") ?: filesDir
        return File(dir, "paddock_${System.currentTimeMillis()}.$ext")
    }

    private fun uriFor(file: File): Uri =
        FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

    private fun launchPhoto() {
        val file = newMediaFile("jpg")
        pendingPhotoFile = file
        takePhoto.launch(uriFor(file))
    }

    private fun launchVideo() {
        val file = newMediaFile("mp4")
        pendingVideoFile = file
        takeVideo.launch(uriFor(file))
    }

    private fun showVideoThumb() {
        val path = videoPath ?: return
        val bmp = try {
            val r = MediaMetadataRetriever()
            r.setDataSource(path)
            val frame = r.frameAtTime
            r.release()
            frame
        } catch (e: Exception) { null }
        bmp?.let { imageVideo.setImageBitmap(it) }
        imageVideo.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW)
                .setDataAndType(uriFor(File(path)), "video/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        }
    }

    private fun save() {
        val name = inputName.text?.toString()?.trim().orEmpty()
        val areaText = inputArea.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) { inputName.error = getString(R.string.name_required); return }
        if (areaText.isEmpty()) { inputArea.error = getString(R.string.area_required); return }

        lifecycleScope.launch {
            val paddock = Paddock(
                id = paddockId,
                name = name,
                areaM2 = areaText.toDoubleOrNull() ?: 0.0,
                creationDate = creationDate,
                photoPath = photoPath,
                videoPath = videoPath
            )
            withContext(Dispatchers.IO) {
                if (paddockId == 0L) db.paddockDao().insert(paddock)
                else db.paddockDao().update(paddock)
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_ID = "extra_paddock_id"
    }
}