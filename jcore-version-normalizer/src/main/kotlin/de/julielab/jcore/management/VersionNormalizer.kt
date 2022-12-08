package de.julielab.jcore.management;

import com.ximpleware.AutoPilot
import com.ximpleware.VTDGen
import com.ximpleware.XMLModifier
import de.julielab.utilities.aether.AetherUtilities
import de.julielab.utilities.aether.MavenArtifact
import de.julielab.utilities.aether.MavenProjectUtilities
import de.julielab.xml.JulieXMLConstants
import de.julielab.xml.JulieXMLTools
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    if (args.size != 3) {
        println("Usage: VersionNormalizer <pom file> <new version> <allow snapshots for external julielab dependencies>")
        exitProcess(1)
    }

    val file = File(args[0])
    val version: String = args[1]
    val allowSnapshotForExternalDeps = args[2].toBoolean()
    val mavenProperties = MavenProjectUtilities.getEffectivePomModel(file).properties
    val artifactIdProperties2Version =
        mavenProperties.propertyNames().asSequence().map { o -> Pair((o as String).replace("-version", ""), "\${$o}") }
            .toMap()
    val projectModules = MavenProjectUtilities.getProjectModules(file, true)
    // Also add the root, i.e. the project's parent POM
    projectModules.add(".")
    // Collect the modules of this parent POM
    val artifactIds: Set<String> = projectModules.map { m -> File(Paths.get(m, "pom.xml").toString()) }
        .map { f -> MavenProjectUtilities.getRawPomModel(f) }.map { m -> m.artifactId }.toSet()
    // iterate over the modules of this parent pom, obtain their pom.xml files, set their parent versions and
    // remove their own, specific version, if it is lower or equal to the new version.

    // for comparison with existing versions, convert it to a number
    var newversionNumber = version.replace("-SNAPSHOT", "").replace(".", "").toInt()
    if (!version.contains("-SNAPSHOT"))
        ++newversionNumber
    for (module in projectModules) {
        val vg = VTDGen()
        val pomPath = Paths.get(module, "pom.xml").toString()
        vg.parseFile(pomPath, false)
        var nav = vg.nav

        // Do not delete the custom version for the root POM or change its parent -  this is the one location
        // that determines the actual
        // version for all the modules.
        val xm = XMLModifier(nav)
        var ap = AutoPilot(nav)
        if (!module.equals(".")) {
            ap.selectXPath("/project/parent/version")
            var i = ap.evalXPath()
            if (i != -1) {
                var currentIndex = nav.text
                xm.updateToken(currentIndex, version)
            }
            ap.resetXPath()
            ap.selectXPath("/project/version")
            i = ap.evalXPath()
            var oldversion = if(i != -1) JulieXMLTools.getElementText(nav) else null
            println("Old version of module $module is $oldversion")
            if (oldversion != null && !oldversion.isBlank()) {
                // Check if the module version is higher than the the version to set. We don't want to lower version
                // numbers.
                var oldversionNumber =
                    if (!oldversion.isBlank()) oldversion.replace("-SNAPSHOT", "").replace(".", "").toInt() else 0
                // add one if this is not a snapshot version to make a difference to snapshot versions which are "lower"
                if (!oldversion.contains("-SNAPSHOT"))
                    ++oldversionNumber
                if (oldversionNumber <= newversionNumber) {
                    println("Removing version $oldversion from module $module")
                    xm.remove()
                }
                else
                println("Retaining version $oldversion from module $module")
            }
        } else {
            // This is the root POM, set it to the specified version
            ap.resetXPath()
            ap.selectXPath("/project/version")
            val i = ap.evalXPath()
            if (i != -1) {
                val textIndex = nav.text
                xm.updateToken(textIndex, version)
            }
        }


        val field1 =
            JulieXMLTools.createField(JulieXMLConstants.NAME, "artifactId", JulieXMLConstants.XPATH, "artifactId")
        val field2 = JulieXMLTools.createField(JulieXMLConstants.NAME, "version", JulieXMLConstants.XPATH, "version")
        val field3 = JulieXMLTools.createField(JulieXMLConstants.NAME, "groupId", JulieXMLConstants.XPATH, "groupId")
        val fields = listOf(field1, field2, field3)
        val rowIt = JulieXMLTools.constructRowIterator(
            nav,
            "/project/dependencies/dependency[groupId='de.julielab']",
            fields,
            pomPath
        )
        for (row in rowIt) {
            if (row["version"] == null)
                continue
            if (row["groupId"]?.equals("de.julielab") != false) {
                val vtdIndex: Int = row[JulieXMLConstants.VTD_INDEX] as Int
                nav.push()
                nav.recoverNode(vtdIndex)
                var versionExpression = ""
                val artifactId = row["artifactId"]
                // Check if this dependency is also a module of the same Maven project
                if (artifactIds.contains(artifactId) || artifactIdProperties2Version.containsKey(artifactId)) {
                    var depVersionIsOlder = true
                    val depVersion = row["version"].toString()
                    // Do not lower versions of dependencies
                    if (!depVersion.isBlank() && !artifactIdProperties2Version.containsKey(artifactId)) {
                        var depVersionNumber = depVersion.replace("-SNAPSHOT", "").replace(".", "").toInt()
                        if (!depVersion.contains("-SNAPSHOT"))
                            ++depVersionNumber
                        if (depVersionNumber >= newversionNumber)
                            depVersionIsOlder = false
                    }
                    if (depVersionIsOlder) {
                        versionExpression =
                            if (!artifactIdProperties2Version.containsKey(artifactId)) version else artifactIdProperties2Version[artifactId].orEmpty()
                    }
                } else {
                    // this is a julielab dependency but not a project-internal one
                    val ma = MavenArtifact()
                    ma.artifactId = artifactId as String
                    ma.groupId = row["groupId"] as String
                    AetherUtilities.getVersions(ma)
                        .filter { v -> !v.contains("SNAPSHOT") || allowSnapshotForExternalDeps }
                        .forEach { v -> versionExpression = v }
                }
                if (!versionExpression.isBlank())
                    JulieXMLTools.setElementText(nav, AutoPilot(nav), xm, "version", versionExpression)
                nav.pop()
            }
        }


        val outputFile = Paths.get(module, "pom.xml")
        xm.output(outputFile.toString())
    }
}

