package org.succlz123.shrink.plugin

import org.objectweb.asm.*
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class RClassProcessor(private var debug: Boolean = false) {
    val rInfoMap = HashMap<String, Int>()
    var rFieldSize = 0

    /**
     * 1. remove the static final int field in the R.class except R$styleable.class
     * 2. keep the static final int field if it configured in build.gradle
     **/
    fun collectAndRemove(srcFile: File, extension: KeepExtension?) {
        val newJar = File(srcFile.parentFile, srcFile.name + ".tmp")
        newJar.createNewFile()
        val jos = JarOutputStream(FileOutputStream(newJar))

        val jarFile = JarFile(srcFile)
        val iterator = jarFile.entries().iterator()
        for (jarEntry in iterator) {
            val entryName = jarEntry.name
            debugLog("--- Collect&Remove - 2 - find class - $entryName ---")
            var bytes = jarFile.getInputStream(jarEntry).readBytes()
            val list = (entryName.split("/") as MutableList)
            val removeAt = list.removeAt(list.size - 1)
            val rInnerClassName = removeAt.removePrefix("R$").removeSuffix(".class")
            val sb = StringBuilder()
            list.forEachIndexed { index, s ->
                sb.append(s)
                if (index != list.size - 1) {
                    sb.append(".")
                }
            }
            val pn = sb.toString()
            val keeps = extension?.shouldKeepRFile(pn)
            val hasStyleable = entryName.contains("styleable")
            var hasKeep = false
            val classReader = ClassReader(bytes)
            val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            val classVisitor = object : ClassVisitor(Opcodes.ASM5, classWriter) {
                override fun visitField(
                        access: Int,
                        name: String?,
                        descriptor: String?,
                        signature: String?,
                        value: Any?
                ): FieldVisitor? {
                    if (value is Int) {
                        val key = "$entryName/$name"
                        if (keeps?.find { it.name == rInnerClassName }
                                        ?.shouldKeep(name) == true) {
                            debugLog("--- Collect&Remove - 3 - keep int - $key --- ")
                            rFieldSize++
                            hasKeep = true
                        } else {
                            debugLog("--- Collect&Remove - 3 - remove int - $key --- ")
                            rInfoMap[key] = value
                            rFieldSize++
                            return null
                        }
                    }
                    return super.visitField(access, name, descriptor, signature, value)
                }
            }
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            bytes = classWriter.toByteArray()
            if (bytes.isNotEmpty() && (hasKeep || hasStyleable)) {
                val zipEntry = ZipEntry(entryName)
                jos.putNextEntry(zipEntry)
                jos.write(bytes)
                jos.closeEntry()
                debugLog("--- Collect&Remove - 3 - write class - $entryName --- ")
            } else {
                debugLog("--- Collect&Remove - 3 - delete class - $entryName --- ")
            }
        }

        jarFile.close()
        srcFile.delete()
        jos.close()

        val renameTo = newJar.renameTo(srcFile)
        debugLog("--- Collect&Remove - 4 - rename result = $renameTo --- ")
    }

    /**
     * Traverse all classes in the JAR file, replacing all direct references to R.class
     */
    fun replaceRInfoFromJar(srcJar: File) {
        val newJar = File(srcJar.parentFile, srcJar.name + ".shrink.tmp")
        val jos = JarOutputStream(FileOutputStream(newJar))

        val srcJarFile = JarFile(srcJar)
        val iterator = srcJarFile.entries().iterator()
        for (jarEntry in iterator) {
            val entryName = jarEntry.name
            val inputStream = srcJarFile.getInputStream(jarEntry)
            var bytes = inputStream.readBytes()
            if (entryName.endsWith(".class")) {
                bytes = replaceRInfo(bytes)
            }
            if (bytes.isNotEmpty()) {
                val zipEntry = ZipEntry(entryName)
                jos.putNextEntry(zipEntry)
                jos.write(bytes)
                jos.closeEntry()
            }
        }
        srcJarFile.close()
        srcJar.delete()
        jos.flush()
        jos.close()
        val renameTo = newJar.renameTo(srcJar)
        debugLog("--- Replace - 3 - rename result = $renameTo --- ")
    }

    fun replaceRInfoFromDirectory(dirInputFile: File) {
        if (dirInputFile.isDirectory) {
            for (file in dirInputFile.walk()) {
                val name = file.name
                if (name.endsWith(".class")) {
                    val newFile = File(file.parentFile, "$name.shrink.tmp")
                    val jos = FileOutputStream(newFile)
                    var bytes = file.readBytes()
                    bytes = replaceRInfo(bytes)
                    jos.write(bytes)
                    jos.close()
                    file.delete()
                    val renameTo = newFile.renameTo(file)
                    debugLog("--- Replace - 1 - $name ---")
                }
            }
        }
    }

    private fun replaceRInfo(bytes: ByteArray): ByteArray {
        val classReader = ClassReader(bytes)
        val classWriter = ClassWriter(0)
        val classVisitor = object : ClassVisitor(Opcodes.ASM5, classWriter) {

            override fun visitMethod(
                    access: Int,
                    name: String?,
                    descriptor: String?,
                    signature: String?,
                    exceptions: Array<out String>?
            ): MethodVisitor? {
                var methodVisitor =
                        super.visitMethod(access, name, descriptor, signature, exceptions)
                methodVisitor = object : MethodVisitor(Opcodes.ASM5, methodVisitor) {

                    override fun visitFieldInsn(
                            opcode: Int,
                            owner: String?,
                            nameInner: String?,
                            descriptorInner: String?
                    ) {
                        val key = "$owner.class/$nameInner"
                        val value = rInfoMap[key]
                        if (value != null) {
                            debugLog("--- Replace - 2 - $key ---")
                            super.visitLdcInsn(value)
                        } else {
                            super.visitFieldInsn(opcode, owner, nameInner, descriptorInner)
                        }
                    }
                }
                return methodVisitor
            }
        }
        classReader.accept(classVisitor, 0)
        return classWriter.toByteArray()
    }

    fun debugLog(str: String) {
        if (debug) {
            println(str)
        }
    }
}