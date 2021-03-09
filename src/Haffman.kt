// Kharitontsev-Beglov Sergey 2021V
// 01.03.2021
// Haffman code

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.system.measureTimeMillis

class Haffman {
    data class Vertex(
        var value: Int = 0,
        var left: Vertex? = null,
        var right: Vertex? = null,
        var isUsed: Boolean = false,
        var symbol: Int = -1,
        var code: String = ""
    ) {
        constructor(v: Int, s: Int) : this() {
            value = v
            symbol = s
        }

        operator fun plus(oth: Vertex) = Vertex(
            value = value + oth.value,
            left = this,
            right = oth
        )
    }

    fun makeTree(cnt: Array<Int>): Array<Vertex> {
        val N = max(cnt.filterNot { it == 0 }.size, 2)
        val tree = Array(2 * N - 1) { Vertex() }
        var m = 0
        for (i in 0..255)
            if (cnt[i] != 0)
                tree[m++] = Vertex(cnt[i], i)

        m = N
        val getMin = {
            var index = -1
            for (i in 0 until m)
                if (!tree[i].isUsed && (index == -1 || tree[index].value > tree[i].value))
                    index = i
            tree[index].isUsed = true
            tree[index]
        }
        for (i in 0 until N - 1) {
            tree[m] = getMin() + getMin()
            m++
        }
        return tree
    }

    fun getCodes(cnt: Array<Int>): Array<String> {

        val N = max(cnt.filterNot { it == 0 }.size, 2)
        val tree = makeTree(cnt)

        val codesTable = Array(256) { "" }
        for (i in 2 * N - 2 downTo N) {
            tree[i].left!!.code = tree[i].code + "0"
            tree[i].right!!.code = tree[i].code + "1"
        }
        for (i in 0 until N) {
            if (tree[i].symbol >= 0)
                codesTable[tree[i].symbol] = tree[i].code
        }
        return codesTable
    }


    public fun archive(inputFileName: String, outputFileName: String): Int {
        var reader = BufferedInputStream(FileInputStream(inputFileName))

        val cnt = Array(256) { 0 }
        var x = reader.read()
        while (x != -1) {
            cnt[x]++
            x = reader.read()
        }
        reader.close()
        reader = BufferedInputStream(FileInputStream(inputFileName))
        val codesTable = getCodes(cnt)

        val writer = BufferedOutputStream(FileOutputStream(outputFileName))
        val N = cnt.filterNot { it == 0 }.size
        writer.write(N)
        var sum = 0
        for (i in 0..255)
            if (cnt[i] != 0) {
                writer.write(i)
                val t = cnt[i]
                writer.write(t shr 24)
                writer.write(t shl 8 shr 24)
                writer.write(t shl 16 shr 24)
                writer.write(t shl 24 shr 24)
                sum += codesTable[i].length * cnt[i]
            }
        x = reader.read()
        val buffer = StringBuilder()
        while (x != -1) {
            buffer.append(codesTable[x])
            while (buffer.length >= 8) {
                writer.write(buffer.substring(0, 8).toInt(2))
                buffer.delete(0, 8)
            }
            x = reader.read()
        }
        if (buffer.isNotEmpty()) {
            writer.write(buffer.toString().toInt(2) shl (8 - buffer.length))
        }
        writer.close()
        return sum
    }

    public fun unarchive(inputFileName: String, outputFileName: String): Int {
        val reader = BufferedInputStream(FileInputStream(inputFileName))
        val cnt = Array(256) { 0 }
        val N = reader.read()
        var symbols = 0
        for (i in 0 until N) {
            val c = reader.read()
            var x = 0
            for (j in 0..3)
                x = (x shl 8) or reader.read()
            cnt[c] = x
            symbols += x
        }

        val tree = makeTree(cnt)
        val N1 = max(2, N)


        val writer = BufferedOutputStream(FileOutputStream(outputFileName))

        var data = reader.read()
        var read = 0
        var now = tree[2 * N1 - 2]
        var indexInBuffer = 0
        while (read < symbols) {
            if (((data shr (7 - indexInBuffer)) and 1) == 0)
                now = now.left!!
            else
                now = now.right!!
            indexInBuffer++
            if (now.symbol != -1) {
                writer.write(now.symbol)
                now = tree[2 * N1 - 2]
                read++
            }
            if (indexInBuffer == 8) {
                data = reader.read()
                indexInBuffer = 0
            }
        }
        writer.close()
        reader.close()
        return symbols * 8
    }

}


