package com.example.n3tapvocab

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader

data class Vocab(val reading: String, val word: String, val meaning: String)

class MainActivity : Activity() {
    private lateinit var words: List<Vocab>
    private var index = 0

    private lateinit var readingText: TextView
    private lateinit var wordText: TextView
    private lateinit var meaningText: TextView
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readingText = findViewById(R.id.readingText)
        wordText = findViewById(R.id.wordText)
        meaningText = findViewById(R.id.meaningText)
        progressText = findViewById(R.id.progressText)

        words = loadWords()
        index = getPreferences(MODE_PRIVATE).getInt("index", 0).coerceIn(0, maxOf(words.size - 1, 0))
        showWord()

        findViewById<View>(R.id.root).setOnClickListener {
            if (words.isNotEmpty()) {
                index = (index + 1) % words.size
                getPreferences(MODE_PRIVATE).edit().putInt("index", index).apply()
                showWord()
            }
        }
    }

    private fun showWord() {
        if (words.isEmpty()) {
            readingText.text = ""
            wordText.text = "단어 없음"
            meaningText.text = "assets/words.csv를 확인하세요."
            progressText.text = ""
            return
        }
        val v = words[index]
        readingText.text = v.reading
        wordText.text = v.word
        meaningText.text = v.meaning
        progressText.text = "${index + 1} / ${words.size}   ·   화면을 터치하면 다음 단어"
    }

    private fun loadWords(): List<Vocab> {
        val result = mutableListOf<Vocab>()
        assets.open("words.csv").use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).useLines { lines ->
                lines.drop(1).forEach { line ->
                    val cols = parseCsvLine(line)
                    if (cols.size >= 3) result.add(Vocab(cols[0], cols[1], cols[2]))
                }
            }
        }
        return result
    }

    private fun parseCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val cur = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (quoted && i + 1 < line.length && line[i + 1] == '"') {
                    cur.append('"'); i++
                } else quoted = !quoted
            } else if (c == ',' && !quoted) {
                out.add(cur.toString()); cur.clear()
            } else cur.append(c)
            i++
        }
        out.add(cur.toString())
        return out
    }
}
