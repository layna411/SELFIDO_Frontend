package com.simats.selfora.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.api.NetworkErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfReportDownloader {

    /**
     * Downloads an authentic clinical PDF report from the backend, validates the %PDF header signature,
     * persists it securely to external files storage, and launches an Android viewer/share intent.
     */
    suspend fun downloadAndOpenReport(
        context: Context,
        childId: Long,
        childName: String = "Child",
        onProgress: (Boolean) -> Unit,
        onError: (String) -> Unit,
        onSuccess: (File) -> Unit = {}
    ) {
        withContext(Dispatchers.Main) {
            onProgress(true)
        }

        try {
            val response = ApiClient.apiService.downloadChildReportPdf(childId)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val bytes = withContext(Dispatchers.IO) {
                    body.bytes()
                }

                // Verify genuine %PDF- binary signature (ASCII %PDF = 0x25 0x50 0x44 0x46)
                if (bytes.size < 4 || bytes[0] != 0x25.toByte() || bytes[1] != 0x50.toByte() ||
                    bytes[2] != 0x44.toByte() || bytes[3] != 0x46.toByte()
                ) {
                    withContext(Dispatchers.Main) {
                        onError("Validation error: Server response is not a valid binary PDF document.")
                        onProgress(false)
                    }
                    return
                }

                // Persist to downloads directory with clean filename
                val cleanChildName = childName.replace("[^a-zA-Z0-9]".toRegex(), "_")
                val filename = "Selfora_Clinical_Report_${cleanChildName}_Child_${childId}.pdf"
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val targetFile = File(dir, filename)

                withContext(Dispatchers.IO) {
                    FileOutputStream(targetFile).use { fos ->
                        fos.write(bytes)
                        fos.flush()
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Clinical PDF downloaded ($filename)", Toast.LENGTH_SHORT).show()
                    onProgress(false)
                    onSuccess(targetFile)
                    openPdf(context, targetFile)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError(NetworkErrorHandler.getHttpErrorMessage(response.code()))
                    onProgress(false)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(NetworkErrorHandler.getErrorMessage(e))
                onProgress(false)
            }
        }
    }

    /**
     * Launches Android's supported document viewer or chooser intent using FileProvider.
     */
    fun openPdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open or Share Clinical PDF Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Report saved to ${file.name}. No PDF viewer app found on device.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

