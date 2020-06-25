package org.succlz123.shrink.plugin

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.succlz123.shrink.plugin.JarKtx.toOutputAndGetDest

class RGradlePlugin : Transform(), Plugin<Project> {

    companion object {
        const val TAG = "Android-R-Shrink-Plugin"

        val SUPPORTED_TYPES = setOf(
            "anim", "array", "attr", "bool", "color", "dimen",
            "drawable", "id", "integer", "layout", "menu", "plurals", "string", "style", "styleable"
        )

        var supportRClass = ArrayList<String>()

        init {
            supportRClass.add("R.class")
            for (supportedType in SUPPORTED_TYPES) {
                supportRClass.add("R\$${supportedType}.class")
            }
        }
    }

    private var extension: KeepExtension? = null

    override fun apply(project: Project) {
        val android = project.extensions.getByType(AppExtension::class.java)
        project.extensions.create("androidShrink", KeepExtension::class.java, project)
        extension = project.extensions.getByName("androidShrink") as? KeepExtension
        android.registerTransform(this)
    }

    override fun getName(): String {
        return TAG
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

        override fun transform(transformInvocation: TransformInvocation?) {
        println("--- $name transform start ---\n")
        val outputProvider =
            transformInvocation?.outputProvider ?: return super.transform(transformInvocation)
        val inputs = transformInvocation.inputs
        val startTime = System.currentTimeMillis()
        outputProvider.deleteAll()

        val rJar = ArrayList<JarInput>()
        val libJar = ArrayList<JarInput>()
        inputs?.forEach { input ->
            input.directoryInputs.forEach { directoryInput ->
                directoryInput.toOutputAndGetDest(
                    outputProvider
                )
            }
            input.jarInputs.forEach { jarInput ->
                val src = jarInput.file
                if (src.path.contains("compile_and_runtime_not_namespaced_r_class_jar")) {
                    rJar.add(jarInput)
                } else {
                    libJar.add(jarInput)
                }
            }
        }

        val processor = RClassProcessor(false)

        println("------ Collect&Remove start ------\n")
        for (jarInput in rJar) {
            val dest = jarInput.toOutputAndGetDest(outputProvider)
            processor.debugLog("--- Collect&Remove - 1 - from ${jarInput.file.path} --- ")
            processor.debugLog("--- Collect&Remove - 1 - goto ${dest.absolutePath} --- ")
            processor.collectAndRemove(dest, extension)
        }
        val totalSize = processor.rFieldSize
        val collectedSize = processor.rInfoMap.size
        val keepSize = totalSize - collectedSize
        println("--- Collect&Remove - collected, rJarSize= ${rJar.size} totalSize = $totalSize collectedSize = $collectedSize keepSize = $keepSize ---")
        println("\n------ Collect&Remove end ------\n")

        println("------ Replace start ------\n")
        for (jarInput in libJar) {
            val dest = jarInput.toOutputAndGetDest(outputProvider)
            processor.debugLog("--- Replace - 1 - from ${jarInput.file.path} --- ")
            processor.debugLog("--- Replace - 1 - goto ${dest.absolutePath} --- ")
            processor.replaceRInfoFromJar(dest, extension)
        }
        println("--- Replace - libJarSize = ${libJar.size} ---")
        println("\n------ Replace end ------\n")

        rJar.clear()
        libJar.clear()
        val cost = (System.currentTimeMillis() - startTime) / 1000
        println("--- $name transform end - cost " + cost + "s ---")
    }
}
