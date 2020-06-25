package org.succlz123.shrink.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformOutputProvider
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import java.io.File

object JarKtx {

    fun DirectoryInput.toOutputAndGetDest(outputProvider: TransformOutputProvider): File {
        val dest = outputProvider.getContentLocation(
            name,
            contentTypes,
            scopes,
            Format.DIRECTORY
        )
        FileUtils.copyDirectory(file, dest)
        return dest
    }

    fun JarInput.toOutputAndGetDest(outputProvider: TransformOutputProvider): File {
        var jarName = name
        val md5Name = DigestUtils.md5Hex(file.absolutePath)
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length - 4)
        }
        val dest = outputProvider.getContentLocation(
            jarName + md5Name,
            contentTypes,
            scopes,
            Format.JAR
        )
        FileUtils.copyFile(file, dest)
        return dest
    }
}