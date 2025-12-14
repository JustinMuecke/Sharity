package com.example.sharity.data.local

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

class FileTransferService {
    suspend fun sendFiles(
        hostAddress: String,
        port: Int,
        files: List<File>
    ) = withContext(Dispatchers.IO) {
        runCatching {
            Socket(hostAddress, port).use { socket ->
                DataOutputStream(socket.getOutputStream()).use { dos ->
                    dos.writeInt(files.size)

                    files.forEach { file ->
                        FileInputStream(file).use { fis ->
                            dos.writeUTF(file.name)
                            dos.writeLong(file.length())

                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while (fis.read(buffer).also { bytesRead = it } != -1) {
                                dos.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    dos.flush()
                    Log.d("FILE_TRANSFER", "Sent ${files.size} files")
                }
            }
        }
    }

    suspend fun receiveFiles(
        port: Int,
        outputDir: File
    ) = withContext(Dispatchers.IO) {
        runCatching {
            ServerSocket(port).use { serverSocket ->
                serverSocket.soTimeout = 10_000
                Log.d("FILE_TRANSFER", "Waiting for files on port $port")
                try {
                    serverSocket.accept().use { socket ->
                        DataInputStream(socket.getInputStream()).use { dis ->

                            val fileCount = dis.readInt()
                            val receivedFiles = mutableListOf<File>()

                            repeat(fileCount) {
                                val fileName = dis.readUTF()
                                val fileSize = dis.readLong()

                                val outputFile = File(outputDir, fileName)
                                FileOutputStream(outputFile).use { fos ->
                                    val buffer = ByteArray(4096)
                                    var remaining = fileSize

                                    while (remaining > 0) {
                                        val bytesRead = dis.read(
                                            buffer,
                                            0,
                                            minOf(buffer.size.toLong(), remaining).toInt()
                                        )
                                        if (bytesRead == -1) break
                                        fos.write(buffer, 0, bytesRead)
                                        remaining -= bytesRead
                                    }
                                }

                                receivedFiles.add(outputFile)
                                Log.d("FILE_TRANSFER", "Received: ${outputFile.name}")
                            }

                            receivedFiles
                            }
                    }
                } catch (e: SocketTimeoutException) {
                    Log.d("FILE_TRANSFER", "ServerSocket timed out")
                    return@use null
                }
            }
        }
    }



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
                ServerSocket(port).use { serverSocket ->
                    serverSocket.soTimeout = 10_000
                    Log.d("FILE_TRANSFER", "Waiting for connection on port $port")

                    try {
                        serverSocket.accept().use { socket ->
                            DataInputStream(socket.getInputStream()).use { dis ->
                                val fileName = dis.readUTF()
                                val fileSize = dis.readLong()

                                val outputFile = File(outputDir, fileName)
                                FileOutputStream(outputFile).use { fos ->
                                    val buffer = ByteArray(4096)
                                    var remaining = fileSize

                                    while (remaining > 0) {
                                        val bytesRead = dis.read(
                                            buffer,
                                            0,
                                            minOf(buffer.size.toLong(), remaining).toInt()
                                        )
                                        if (bytesRead == -1) break
                                        fos.write(buffer, 0, bytesRead)
                                        remaining -= bytesRead
                                    }
                                    fos.flush()
                                    Log.d(
                                        "FILE_TRANSFER",
                                        "File received: ${outputFile.absolutePath}"
                                    )
                                }
                                outputFile
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                            Log.d("FILE_TRANSFER", "ServerSocket timed out")
                            return@use null
                    }
                }
            }
        }
    }
}