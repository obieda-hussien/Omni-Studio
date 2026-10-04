package com.novacut.editor.engine

import android.app.ActivityManager
import android.content.Context

/** Compare working allocations with live headroom before creating bitmaps or native activations. */
internal fun requireNeuralMemory(context: Context, heapBytes: Long, nativeBytes: Long) {
    val runtime = Runtime.getRuntime()
    val availableHeap = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())
    val info = ActivityManager.MemoryInfo()
    (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(info)
    NeuralMediaPolicy.requireMemory(heapBytes, nativeBytes, availableHeap, if (info.lowMemory) 0 else info.availMem)
}
