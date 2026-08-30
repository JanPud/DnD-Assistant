package com.dndassistant.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.RandomAccessFile

class BinaryReader private constructor(
    private val index: Map<String, Entry>,
    private val file: RandomAccessFile
){
    data class Entry(
        val offset: Long,
        val size: Long,
        val width: Int,
        val height: Int
    )

    private val defaultBinary = "binaries/Images_.fook"
    private val defaultImage = "main_icon-playstore"

    companion object {
        private val MAGIC = "VOID".toByteArray()
        private var appContext: Context? = null
        fun loadFromAssets(context: Context, assetName: String): BinaryReader {
            appContext = context
            val externalDir = context.getExternalFilesDir(null)
            val outFile = File(externalDir, assetName)

            if (!outFile.exists()){
                outFile.parentFile?.mkdirs()
                context.assets.open(assetName).use { inputStream ->
                    outFile.outputStream().use { outputStream ->  inputStream.copyTo(outputStream) }
                }
            }

            return openFile(outFile.absolutePath)
        }


        fun openFile(path: String): BinaryReader {
            val raf = RandomAccessFile(path, "r")
            val magic = ByteArray(4)

            raf.readFully(magic)
            require(magic.contentEquals(MAGIC)) { "Not a valid binary file." }

//            val version = ByteArray(4)
//            raf.read(version)
            val version = raf.readInt()
//            val count = ByteArray(4)
//            raf.read(count)
            val count = raf.readInt()
            val index = mutableMapOf<String, Entry>()

            repeat(count){
//                val nameLength = ByteArray(4)
//                raf.readFully(nameLength)
                val nameLength = raf.readInt()
                val nameBytes = ByteArray(nameLength)
                raf.readFully(nameBytes)
                val name = String(nameBytes, Charsets.UTF_8)
                val offset = raf.readLong()
                val size = raf.readLong()
                val width = raf.readInt()
                val height = raf.readInt()
                index[name] = Entry(offset, size, width, height)
            }

            return BinaryReader(index,raf)
        }
    }

    val imageNames: List<String> = index.keys.toList()
    fun loadBitmap(name: String, maxWidth: Int, maxHeight: Int): Bitmap{
//        val metaData = index[name] ?: return createBitmap(0,0)
        val metaData = index[name] ?: index[name.lowercase()] ?: return BinaryReader.loadFromAssets(appContext!!, defaultBinary).loadBitmap(defaultImage, 64, 64)

        val bytes = ByteArray(metaData.size.toInt())
        synchronized(file){
            file.seek(metaData.offset)
            file.readFully(bytes)
        }

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeByteArray(bytes,0,bytes.size, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, width: Int, height: Int): Int{
        var sampleSize = 1
        if (options.outHeight > height || options.outWidth > width) {
            val halfH = options.outHeight / 2
            val halfW = options.outWidth / 2
            while (halfH / sampleSize >= height && halfW / sampleSize >= width) sampleSize *= 2
        }
        return sampleSize
    }

    fun getMetaData(name: String): Entry? = index[name]

    fun close() = file.close()
}