package com.example.sharity.data.local

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class FileTransferService {

    suspend fun sendFile(hostAddress: String, port: Int, file: File) = withContext(Dispatchers.IO) {
        runCatching {
            Socket(hostAddress, port).use { socket ->
                DataOutputStream(socket.getOutputStream()).use { dos ->
                    FileInputStream(file).use { fis ->
                        // Send filename
                        dos.writeUTF(file.name)
                        // Send file size
                        dos.writeLong(file.length())

                        // Send file data
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            dos.write(buffer, 0, bytesRead)
                        }
                        dos.flush()
                        Log.d("FILE_TRANSFER", "File sent: ${file.name}")
                    }
                }
            }
        }
    }

    suspend fun receiveFile(port: Int, outputDir: File?) = withContext(Dispatchers.IO) {
        runCatching {
            ServerSocket(port).use { serverSocket ->
                Log.d("FILE_TRANSFER", "Waiting for connection on port $port")

                serverSocket.accept().use { socket ->
                    DataInputStream(socket.getInputStream()).use { dis ->
                        // Read filename
                        val fileName = dis.readUTF()
                        // Read file size
                        val fileSize = dis.readLong()

                        val outputFile = File(outputDir, fileName)
                        FileOutputStream(outputFile).use { fos ->
                            val buffer = ByteArray(4096)
                            var remaining = fileSize

                            while (remaining > 0) {
                                val bytesRead = dis.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                                if (bytesRead == -1) break
                                fos.write(buffer, 0, bytesRead)
                                remaining -= bytesRead
                            }
                            fos.flush()
                            Log.d("FILE_TRANSFER", "File received: ${outputFile.absolutePath}")
                        }
                        outputFile
                    }
                }
            }
        }
    }
}