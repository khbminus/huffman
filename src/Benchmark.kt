import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.system.measureTimeMillis

fun genRandomFile(size: Int, outputFileName: String) {
    val writer = BufferedOutputStream(FileOutputStream(outputFileName))
    for (i in 1..size)
        writer.write((4..255).random())
    writer.close()
}

fun diffFiles(file1Name: String, file2Name: String): Boolean {
    val reader1 = BufferedInputStream(FileInputStream(file1Name))
    val reader2 = BufferedInputStream(FileInputStream(file2Name))
    var x = 0
    while (x != -1) {
        x = reader1.read()
        if (x != reader2.read()) {
            reader1.close()
            reader2.close()
            return false
        }
    }

    reader1.close()
    reader2.close()
    return true
}

fun main() {
    val haffman = Haffman()
    for (i in 1..1000) { // Warm Up
        genRandomFile(1000, "input.txt")
        haffman.archive("input.txt", "output.txt")
        haffman.unarchive("output.txt", "output1.txt")
        if (!diffFiles("input.txt", "output1.txt")) {
            println("Error!")
            return
        }
    }
    println("Ok")
    val step = 10 * 1024 * 1024 // 10MiB
    val end = 100 * 1024 * 1024 // 100MiB
    val iters = 1
    for (size in step..end step step) {
        genRandomFile(size, "input.txt")
        var szCompressed = 0
        var szUncompressed = 0
        var arcTimeComp = 0L
        var unArcTimeComp = 0L
        var coef = 0.0
        for (i in 1..iters) {
            val archiveTime = measureTimeMillis { szCompressed = haffman.archive("input.txt", "output.txt") }
            val unarchiveTime = measureTimeMillis { szUncompressed = haffman.unarchive("output.txt", "output1.txt") }
            arcTimeComp += archiveTime
            unArcTimeComp += unarchiveTime
            coef += szUncompressed.toDouble() / szCompressed
        }
        println("${size / 1024.0 / 1024},${arcTimeComp / 1000.0 / iters},${unArcTimeComp / 1000.0 / iters},${coef / iters}")
    }
}