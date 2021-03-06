package com.github.hiroshi_cl.sa.withSentinel.algorithm

import java.util.Arrays

import com.github.hiroshi_cl.sa.withSentinel.SuffixArray

class SAIS_KT : SuffixArray() {

    override fun saInternal(cs: CharArray, sa: IntArray): IntArray {
        return rec(cs.map { it.toInt() }.toList(), sa, cs.size, 1 shl Character.SIZE)
    }

    //isSの初期化とSのカウント
    private fun judgeS(input: List<Int>): Pair<Int, List<Boolean>> {
        val temp = generateSequence(Triple(true, 1, input.size - 2)) { (before, nS, i) ->
            val flag = input[i] < input[i + 1] || input[i] == input[i + 1] && before
            Triple(flag, if (flag) nS + 1 else nS, i - 1)
        }.take(input.size)

        return Pair(temp.last().second, temp.map { it.first }.toList().asReversed())
    }

    //isLMSの初期化とLMSのカウント
    private fun judgeLMS(isS: List<Boolean>): Pair<Int, List<Boolean>> {
        val temp = generateSequence(Triple(isS[0], if (isS[0]) 1 else 0, 1)) { (_, count, i) ->
            val flag = isS[i] && !isS[i - 1]
            Triple(flag, if (flag) count + 1 else count, i + 1)
        }.take(isS.size)

        return Pair(temp.last().second, temp.map { it.first }.toList())
    }

    // renumber処理部
    private fun renumber(sa: IntArray, N: Int, n: Int, isLMS: List<Boolean>): List<Int> {
        val lms = IntArray(n + 1)
        val rev = IntArray(N)

        var i = 0
        var j = 0
        var k = 0
        while (i < N) {
            if (isLMS[i]) {
                rev[i] = j
                lms[j] = i
                j++
            }
            if (isLMS[sa[i]]) sa[k++] = sa[i]
            i++
        }

        for (l in 0 until n) sa[l] = rev[sa[l]]

        lms[n] = N - 1

        return lms.toList()
    }

    // rename処理部
    private fun rename(input: List<Int>, sa: IntArray, n: Int, LMS: List<Int>): Pair<Int, List<Int>> {
        val s = IntArray(n)
        var kNew = -1
        var i = 0
        var j = -1
        var l = -1
        while (i < n) {
            val c = sa[i]
            var f = LMS[c + 1] - LMS[c] == l
            if (f) {
                val p = LMS[sa[i]]
                val q = LMS[sa[j]]
                var k = 0
                while (f && k <= l) {
                    f = input[p + k] == input[q + k]
                    k++
                }
            }
            if (f) s[c] = kNew
            else {
                l = LMS[c + 1] - LMS[c]
                j = i
                s[c] = ++kNew
            }
            i++
        }
        kNew++

        return Pair(kNew, s.toList())
    }

    private fun rec(input: List<Int>, sa: IntArray, N: Int, K: Int): IntArray {
        // determine L or S and count
        val (nS, isS) = judgeS(input)

        val n = if (nS > 1) {
            // step 1
            sort(input, sa, K, N, isS)

            // step 2
            val (n, isLMS) = judgeLMS(isS)

            // renumber
            val lms = renumber(sa, N, n, isLMS)

            // rename
            val (kNew, s) = rename(input, sa, n, lms)

            Arrays.fill(sa, 0, N, -1)
            // unique
            if (n == kNew) for (i in 0 until n) sa[s[i]] = lms[i]
            else { // not unique
                rec(s, sa, n, kNew)
                for (i in 0 until n) sa[i] = lms[sa[i]]
            }
            n
        } else {
            sa[0] = N - 1
            1
        }

        // step 3
        sort(input, sa, K, n, isS)

        return sa
    }

    private fun sort(input: List<Int>, sa: IntArray, K: Int, n: Int, isS: List<Boolean>) {
        val N = input.size
        // make buckets
        val bkt = IntArray(K)
        input.forEach { bkt[it]++ }
        for (i in 1 until K) bkt[i] += bkt[i - 1]
        val idx = IntArray(K)

        System.arraycopy(bkt, 0, idx, 0, K)
        // arrange S suffixes
        if (n < N)
            for (i in n - 1 downTo 0) {
                val c = sa[i]
                sa[i] = -1
                sa[--idx[input[c]]] = c
            }
        else for (i in 0 until N) if (isS[i]) sa[--idx[input[i]]] = i// sort by first character

        // copy S -> L
        System.arraycopy(bkt, 0, idx, 1, K - 1)
        idx[0] = 0
        sa.forEach {
            val k = it - 1
            if (k >= 0 && !isS[k]) sa[idx[input[k]]++] = k
        }

        // copy L -> S
        System.arraycopy(bkt, 0, idx, 0, K)
        for (i in N - 1 downTo 0) {
            val k = sa[i] - 1
            if (k >= 0 && isS[k]) sa[--idx[input[k]]] = k
        }
    }
}
