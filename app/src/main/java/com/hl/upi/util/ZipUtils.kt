package com.hl.upi.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipUtils {
    // Basic function to ZIP a single file
    // We are using standard java.util.zip logic here
    fun zipFile(sourceFile: File, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            FileInputStream(sourceFile).use { fis ->
                // Adding the entry in ZIP
                val entry = ZipEntry(sourceFile.name)
                zos.putNextEntry(entry)
                val buffer = ByteArray(1024)
                var len: Int
                // Reading from source and writing to ZIP output stream
                while (fis.read(buffer).also { len = it } > 0) {
                    zos.write(buffer, 0, len)
                }
                zos.closeEntry()
            }
        }
    }
}
