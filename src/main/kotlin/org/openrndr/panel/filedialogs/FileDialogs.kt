package org.openrndr.panel.filedialogs

import java.io.File
import org.lwjgl.util.nfd.NativeFileDialog
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.util.nfd.NativeFileDialog.NFD_OKAY


fun openFile(function: (File) -> Unit) {
    val filterList: CharSequence? = null
    val defaultPath: CharSequence? = null
    val out = memAllocPointer(1)
    val r = NativeFileDialog.NFD_OpenDialog(filterList, defaultPath, out)
    if (r == NFD_OKAY) {
        val ptr = out.get(0)
        val str = memUTF8(ptr)
        val f = File(str)
        function(f)
    }
    memFree(out)
}

fun saveFile(function: (File) -> Unit) {
    val filterList: CharSequence? = null
    val defaultPath: CharSequence? = null
    val out = memAllocPointer(1)
    val r = NativeFileDialog.NFD_SaveDialog(filterList, defaultPath, out)
    if (r == NFD_OKAY) {
        val ptr = out.get(0)
        val str = memUTF8(ptr)
        val f = File(str)
        function(f)
    }
    memFree(out)

}