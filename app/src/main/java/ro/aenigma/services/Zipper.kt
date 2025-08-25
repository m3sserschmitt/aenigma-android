package ro.aenigma.services

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import ro.aenigma.models.AttachmentsMetadata
import ro.aenigma.util.SerializerExtensions.toCanonicalJson
import java.io.ByteArrayInputStream
import java.io.File
import javax.inject.Inject
import kotlin.text.toByteArray
import androidx.core.net.toUri
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_METADATA_FILE
import ro.aenigma.util.ContextExtensions.getConversationFilesDir
import java.util.UUID

class Zipper @Inject constructor(@param:ApplicationContext val context: Context) {
    fun extractZipToFilesDir(
        context: Context,
        zipFile: File,
        destinationDir: String
    ): List<File> {
        return try {
            val tempDir = File(context.cacheDir, "${UUID.randomUUID()}")
            if (!tempDir.exists()) tempDir.mkdirs()

            val zip = ZipFile(zipFile)
            zip.extractAll(tempDir.absolutePath)

            val finalFiles = mutableListOf<File>()
            val destDir = context.getConversationFilesDir(destinationDir)
            destDir.mkdirs()

            tempDir.walkTopDown()
                .filter { it.isFile }
                .forEach { extractedFile ->
                    val destFile = File(destDir, extractedFile.name)
                    extractedFile.copyTo(destFile, overwrite = true)
                    extractedFile.delete()
                    finalFiles.add(destFile)
                }

            tempDir.deleteRecursively()

            finalFiles
        } catch (_: Exception) {
            listOf()
        }
    }

    fun createZip(
        files: List<String>,
        metadata: AttachmentsMetadata? = null
    ): File? {
        val tempFiles = files.mapNotNull { uri -> copyUriToCache(uri) }
        try {
            val zipParameters = ZipParameters().apply {
                compressionLevel = CompressionLevel.ULTRA
            }
            if (tempFiles.isEmpty()) return null
            val zipFile = File.createTempFile("archive_", ".zip", context.cacheDir)
            val archive = ZipFile(zipFile)
            tempFiles.forEach { file ->
                archive.addFile(file, zipParameters)
            }
            if (metadata != null) {
                val jsonMetadata = metadata.toCanonicalJson()
                if (jsonMetadata != null) {
                    val metadataStream = ByteArrayInputStream(jsonMetadata.toByteArray())
                    archive.addStream(
                        metadataStream,
                        zipParameters.apply { fileNameInZip = ATTACHMENTS_METADATA_FILE })
                }
            }
            return zipFile
        } catch (_: Exception) {
            return null
        } finally {
            tempFiles.forEach { file ->
                try {
                    file.delete()
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun copyUriToCache(uri: String): File? {
        return try {
            val parsedUri = uri.toUri()
            val inputStream = context.contentResolver.openInputStream(parsedUri) ?: return null
            val file =
                File.createTempFile("file_", ".${getFileExtension(parsedUri)}", context.cacheDir)
            file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
            return file
        } catch (_: Exception) {
            null
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).toString()
    }
}
